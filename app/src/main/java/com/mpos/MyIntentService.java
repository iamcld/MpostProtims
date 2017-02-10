
package com.mpos;

import android.app.IntentService;

import android.content.Intent;

import com.apkfuns.logutils.LogUtils;
import com.mpos.communication.CommBluetooth;
import com.mpos.db.DatabaseAdapter;
import com.mpos.db.MPos;
import com.mpos.sdk.MposSDK;
import com.pax.utils.Utils;

import java.util.ArrayList;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 *
 */
public class MyIntentService extends IntentService {
    private static final String TAG = "MyIntentService";


    public MyIntentService() {
        super("MyIntentService");
        LogUtils.d("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        byte[] termVerInfo = new byte[8 + 1];
//        byte[] termSN = new byte[8 + 1];
//        byte[] terminalInfo = new byte[30 + 1];
//        CommBluetooth commBluetooth;
//        MposSDK mposSDK;
//        MPos mPos;
//        DatabaseAdapter databaseAdapter;
//
//        String mac = intent.getStringExtra(MposApplication.DEVICE_MAC);
//        String name = intent.getStringExtra(MposApplication.DEVICE_NAME);
//
//        LogUtils.d("当前线程:" + Thread.currentThread());
//        commBluetooth = new CommBluetooth(mac);
//        databaseAdapter = new DatabaseAdapter(this);
//        mposSDK = new MposSDK();
//        mposSDK.setMposCommunicator(commBluetooth);
//        mposSDK.initEnv();
//
//        LogUtils.i("连接蓝牙设备中...");
//
//        if (commBluetooth.connect()) {
//            LogUtils.i("蓝牙连接成功...");
//            LogUtils.d("当前线程:" + Thread.currentThread());
//            termVerInfo = mposSDK.getTermVerInfo();
//            LogUtils.i("终端版本为:" + new Utils().bcd2Str(termVerInfo));
//
//            termSN = mposSDK.getTermSN();
//            LogUtils.i("终端sn为:" + new Utils().AsciiStringToString(new Utils().bcd2Str(termSN)));
//
//                terminalInfo = mposSDK.getTerminalInfo();
//                LogUtils.i("终端信息为:" + new Utils().bcd2Str(termVerInfo));
//
////            if (termVerInfo[2] > 9) {
////                mPos = new MPos(oldMpos.getMac(), oldMpos.getName(), new Utils().AsciiStringToString(new Utils().bcd2Str(termSN)),
////                        oldMpos.getPn(), String.valueOf(termVerInfo[1]) + "." + String.valueOf(termVerInfo[2]),
////                        String.valueOf(termVerInfo[0]), oldMpos.getBattery(), oldMpos.getIsupdate());
////           } else {
////                mPos = new MPos(oldMpos.getMac(), oldMpos.getName(), new Utils().AsciiStringToString(new Utils().bcd2Str(termSN)),
////                        null, String.valueOf(termVerInfo[1]) + ".0" + String.valueOf(termVerInfo[2]),
////                        String.valueOf(termVerInfo[0]), oldMpos.getBattery(), oldMpos.getIsupdate());
////            }
////            databaseAdapter.rawUpdate(mPos);
//            commBluetooth.close();
//
//            //发送接收标志，更新list数据
////            Message msg = handler.obtainMessage();
////            msg.what = BT_SEND_RECEIVE;
////            handler.sendMessage(msg);
//        }
//
//        ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();
//        LogUtils.i("数据库更新后:" + mPoslist);

    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        String mac = intent.getStringExtra(MposApplication.DEVICE_MAC);
//        String name = intent.getStringExtra(MposApplication.DEVICE_NAME);
//        SharedPreferences sp;
//        String server_ip;
//        String server_port;
//        String key_tid;
//        sp = getSharedPreferences(ServerSetActivity.SHARED_MAIN, Context.MODE_PRIVATE);
//
//        server_ip = sp.getString(ServerSetActivity.KEY_SERVER_IP, null);
//        server_port = sp.getString(ServerSetActivity.KEY_SERVER_PORT, null);
//        key_tid = sp.getString(ServerSetActivity.KEY_TID, null);
//        if (server_ip == null){
//            server_ip = "192.168.0.136";
//        }
//        if (server_port == null){
//            server_port = "8580";
//        }
//        if (key_tid == null){
//            key_tid = "00000001";
//        }
//
//        LogUtils.i("MyThread 中线程id:" + Thread.currentThread());
//        CommTcpip commTcpip = new CommTcpip(server_ip,
//                Integer.valueOf(server_port));
//        CommBluetooth commBluetooth = new CommBluetooth(mac);
//        byte[] termVerInfo = new byte[8+1];
//        byte[] termSN = new byte[8+1];
//        byte[] terminalInfo = new byte[30+1];
//        String strTmp = "";
//        MposSDK mposSDK = new MposSDK();
//        mposSDK.setTermId(key_tid);
//        mposSDK.setMposCommunicator(commBluetooth);
//        mposSDK.setTmsCommunicator(commTcpip);
//        MPos mPos;
//        LogUtils.d("服务器地址:"+server_ip);
//        LogUtils.d("服务器端口:"+server_port);
//        LogUtils.d("TID:"+key_tid);
//
//        int iRet = 0;
//        iRet = mposSDK.initEnv();
//        LogUtils.d("isUpdate befor...");
//        int isupdate = 0;
//
//        //获取更新状态只需要连接TMS后台，不需要连接Mpos
//        //只跟TID有关系
//        isupdate = mposSDK.isUpdate();
//        LogUtils.d("设备是否更新 isupdate:" + isupdate);
//
//        DatabaseAdapter databaseAdapter = new DatabaseAdapter(this);
//        ArrayList<MPos> mPoslistbefor = databaseAdapter.rawFindAll();
//        LogUtils.i("数据库之前:" + mPoslistbefor);
//        //(String mac, String name, String sn, String pn, String os_version, String boot_version,battery)
//
//        MPos mPos1 = new MPos();
//        mPos1 = databaseAdapter.rawFindById(mac);
//        if (mPos1.getSn() == null){
//            LogUtils.i("连接蓝牙设备中...");
//            if (commBluetooth.connect()){
//                termVerInfo = mposSDK.getTermVerInfo();
//                LogUtils.i("终端版本为:" + new Utils().bcd2Str(termVerInfo));
//
//                //接收处已经开了一个线程
//                termSN = mposSDK.getTermSN();
//                strTmp = new Utils().bcd2Str(termSN);
//                LogUtils.i("终端sn为:" + new Utils().AsciiStringToString(strTmp));
//
////                terminalInfo = mposSDK.getTerminalInfo();
////                LogUtils.i("终端信息为:" + new Utils().bcd2Str(termVerInfo));
//            }
//        }
//
//
//        if (termVerInfo[2] > 9) {
//            mPos = new MPos(mac, name, new Utils().AsciiStringToString(strTmp), null, String.valueOf(termVerInfo[1]) + "."
//                    + String.valueOf(termVerInfo[2]), String.valueOf(termVerInfo[0]), null, String.valueOf(isupdate));
//        } else {
//            mPos = new MPos(mac, name, new Utils().AsciiStringToString(strTmp), null, String.valueOf(termVerInfo[1]) + ".0"
//                    + String.valueOf(termVerInfo[2]), String.valueOf(termVerInfo[0]), null, String.valueOf(isupdate));
//        }
//        databaseAdapter.rawUpdate(mPos);
//
//        ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();
//        LogUtils.i("数据库更新后:"+ mPoslist);
//
//        commTcpip.close();
//        commBluetooth.close();
//
//    }

}
