package com.mpos;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;


import com.apkfuns.logutils.LogUtils;
import com.mpos.activity.ServerSetActivity;
import com.mpos.communication.CommBluetooth;
import com.mpos.communication.CommTcpip;
import com.mpos.db.DatabaseAdapter;
import com.mpos.db.MPos;
import com.mpos.sdk.MposSDK;
import com.pax.utils.Utils;

import java.util.ArrayList;

public class MyService extends Service {
    private static final String TAG = "MyService";
    MyService myService;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i("service onCreate: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i("service onStartCommand: ");
        String mac = intent.getStringExtra(MposApplication.DEVICE_MAC);
        String name = intent.getStringExtra(MposApplication.DEVICE_NAME);

        MyThread myThread = new MyThread(this, mac, name);
        myThread.start();

        return super.onStartCommand(intent, flags, startId);

    }

    private class MyThread extends Thread {
        private Context context;
        private String mac;
        private String name;

        public MyThread(Context context, String mac, String name) {
            this.context = context;
            this.mac = mac;
            this.name = name;

        }

        @Override
        public void run() {
            SharedPreferences sp;
            String server_ip;
            String server_port;
            String key_tid;
            sp = getSharedPreferences(ServerSetActivity.SHARED_MAIN, Context.MODE_PRIVATE);

            server_ip = sp.getString(ServerSetActivity.KEY_SERVER_IP, null);
            server_port = sp.getString(ServerSetActivity.KEY_SERVER_PORT, null);
            key_tid = sp.getString(ServerSetActivity.KEY_TID, null);
            if (server_ip == null){
                server_ip = "192.168.0.136";
            }
            if (server_port == null){
                server_port = "8580";
            }
            if (key_tid == null){
                key_tid = "00000001";
            }

            LogUtils.i("MyThread 中线程id:" + Thread.currentThread());
            CommTcpip commTcpip = new CommTcpip(server_ip,
                    Integer.valueOf(server_port));
            CommBluetooth commBluetooth = new CommBluetooth(mac);
            byte[] termVerInfo = new byte[8+1];
            byte[] termSN = new byte[8+1];
            byte[] terminalInfo = new byte[30+1];
            String strTmp = "";
            MposSDK mposSDK = new MposSDK();
            mposSDK.setTermId(key_tid);
            mposSDK.setMposCommunicator(commBluetooth);
            mposSDK.setTmsCommunicator(commTcpip);
            MPos mPos;
            LogUtils.d("服务器地址:"+server_ip);
            LogUtils.d("服务器端口:"+server_port);
            LogUtils.d("TID:"+key_tid);

            int iRet = 0;
            iRet = mposSDK.initEnv();
            LogUtils.d("isUpdate befor...");
            int isupdate = 0;

            //获取更新状态只需要连接TMS后台，不需要连接Mpos
            //只跟TID有关系
            isupdate = mposSDK.isUpdate();
            LogUtils.d("设备是否更新 isupdate:" + isupdate);

            DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
            ArrayList<MPos> mPoslistbefor = databaseAdapter.rawFindAll();
            LogUtils.i("数据库之前:" + mPoslistbefor);
            //(String mac, String name, String sn, String pn, String os_version, String boot_version,battery)

            MPos mPos1 = new MPos();
            mPos1 = databaseAdapter.rawFindById(mac);
            if (mPos1.getSn() == null){
                LogUtils.i("连接蓝牙设备中...");
                if (commBluetooth.connect()){
                    termVerInfo = mposSDK.getTermVerInfo();
                    LogUtils.i("终端版本为:" + new Utils().bcd2Str(termVerInfo));

                    //接收处已经开了一个线程
                    termSN = mposSDK.getTermSN();
                    strTmp = new Utils().bcd2Str(termSN);
                    LogUtils.i("终端sn为:" + new Utils().AsciiStringToString(strTmp));

//                terminalInfo = mposSDK.getTerminalInfo();
//                LogUtils.i("终端信息为:" + new Utils().bcd2Str(termVerInfo));
                }
            }


            if (termVerInfo[2] > 9) {
                mPos = new MPos(mac, name, new Utils().AsciiStringToString(strTmp), null, String.valueOf(termVerInfo[1]) + "."
                        + String.valueOf(termVerInfo[2]), String.valueOf(termVerInfo[0]), null, String.valueOf(isupdate));
            } else {
                mPos = new MPos(mac, name, new Utils().AsciiStringToString(strTmp), null, String.valueOf(termVerInfo[1]) + ".0"
                        + String.valueOf(termVerInfo[2]), String.valueOf(termVerInfo[0]), null, String.valueOf(isupdate));
            }
            databaseAdapter.rawUpdate(mPos);

            ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();
            LogUtils.i("数据库更新后:"+ mPoslist);

            commTcpip.close();
            commBluetooth.close();

            MyService.this.stopSelf();
        }
    }


}
