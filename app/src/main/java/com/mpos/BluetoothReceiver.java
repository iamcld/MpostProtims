package com.mpos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.mpos.adapter.BtPairAdapter;
import com.mpos.adapter.BtRepairAdapter;
import com.mpos.communication.CommBluetooth;
import com.mpos.communication.CommTcpip;
import com.mpos.communication.ICommunicator;
import com.mpos.db.DatabaseAdapter;
import com.mpos.db.MPos;
import com.mpos.sdk.MposSDK;;
import com.pax.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class BluetoothReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothReceiver";

    private Context context;
    private BtPairAdapter btPairAdapter;
    private BtRepairAdapter btRepairAdapter;
    private BluetoothAdapter btAdapter;//蓝牙适配器
//    private MyHandler mHandler;


    public BluetoothReceiver() {
    }

    public BluetoothReceiver(Context context, BtPairAdapter btPairAdapter, BtRepairAdapter btRepairAdapter,
                             BluetoothAdapter btAdapter) {
        this.context = context;
        this.btPairAdapter = btPairAdapter;
        this.btRepairAdapter = btRepairAdapter;
        this.btAdapter = btAdapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //得到intent里面的设备对象
        BluetoothDevice devices = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            //Toast.makeText(context,"发现蓝牙设备", Toast.LENGTH_SHORT).show();
            if (devices.getBondState() != BluetoothDevice.BOND_BONDED) {
                //未匹配设备
                HashMap<String, String> items = new HashMap<String, String>();
                //如果不是重复的mac地址，则可判断为新的蓝牙设备
                items.put(MposApplication.DEVICE_NAME, devices.getName());
                items.put(MposApplication.DEVICE_MAC, devices.getAddress());

                btPairAdapter.addDevice(items);
                // 若蓝牙列表数据更新后，可以用以下方法通知ListView更新显示
                btPairAdapter.notifyDataSetChanged();
            } else if (devices.getBondState() == BluetoothDevice.BOND_BONDED) {
                Set<BluetoothDevice> set = btAdapter.getBondedDevices();//获得已配对蓝牙设备集合
                //显示已经配对过的设备
                btRepairAdapter.addDevice(set);
                //通知listView数据改变，更新显示列表
                btRepairAdapter.notifyDataSetChanged();
            }
        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            LogUtils.d("onReceive 中线程id:" + Thread.currentThread());
            //如果蓝牙设备状态改变，且是匹配状态，则添加
            if (devices.getBondState() == BluetoothDevice.BOND_BONDED) {
                Set<BluetoothDevice> set = btAdapter.getBondedDevices();//获得已配对蓝牙设备集合
                //ArrayList<String> deleteList = new ArrayList<>();
                ArrayList<HashMap<String, String>> deleteList = new ArrayList<HashMap<String, String>>();

                //显示已经配对过的设备,
                deleteList = btRepairAdapter.addDevice(set);
                btPairAdapter.deleteDevice(deleteList);

                //通知listView数据改变，更新显示列表
                btRepairAdapter.notifyDataSetChanged();
                btPairAdapter.notifyDataSetChanged();

//                for (int i = 0; i < deleteList.size(); i++) {
//                    LogUtils.d("i: " + i);
//                    this.mHandler = new MyHandler(context, deleteList.get(i).get(MposApplication.DEVICE_MAC), deleteList.get(i).get(MposApplication.DEVICE_NAME));
//                    MyThread myThread = new MyThread(context, deleteList.get(i).get(MposApplication.DEVICE_MAC),
//                            deleteList.get(i).get(MposApplication.DEVICE_NAME), mHandler);
//                    myThread.start();
//
//                    //Intent intent1 = new Intent(context, MyService.class);
//                    Intent intent1 = new Intent(context, MyIntentService.class);
//                    intent1.putExtra(MposApplication.DEVICE_MAC, deleteList.get(i).get(MposApplication.DEVICE_MAC));
//                    intent1.putExtra(MposApplication.DEVICE_NAME, deleteList.get(i).get(MposApplication.DEVICE_NAME));
//
//                    context.startService(intent1);
//                }

//                for (int i = 0; i < deleteList.size(); i++) {
//                    LogUtils.d("i: " + i);
//                    this.mHandler = new MyHandler(context, deleteList.get(i).get(MposApplication.DEVICE_MAC), deleteList.get(i).get(MposApplication.DEVICE_NAME));
//                    MyThread myThread = new MyThread(context, deleteList.get(i).get(MposApplication.DEVICE_MAC),
//                            deleteList.get(i).get(MposApplication.DEVICE_NAME), mHandler);
//                    myThread.start();
//                }
            }
        } else if (action.equals(MposApplication.RECEIVER_ACTION)) {
            //自定义广播:蓝牙自动匹配
            String mac = intent.getStringExtra(MposApplication.DEVICE_MAC);
            String name = intent.getStringExtra(MposApplication.DEVICE_NAME);
//            this.mHandler = new MyHandler(context, mac, name);
            Set<BluetoothDevice> set = btAdapter.getBondedDevices();//获得已配对蓝牙设备集合

            //显示已经配对过的设备
            btRepairAdapter.addDevice(set);
            btPairAdapter.deleteDevice(mac);

            //通知listView数据改变，更新显示列表
            btRepairAdapter.notifyDataSetChanged();
            btPairAdapter.notifyDataSetChanged();
            LogUtils.i( "匹配的地址" + mac);
        }
    }

//    private static class MyThread extends Thread {
//        private Context context;
//        private String mac;
//        private String name;
//        private MyHandler mHandler;
//
//        public MyThread(Context context, String mac, String name, MyHandler mHandler) {
//            this.context = context;
//            this.mac = mac;
//            this.name = name;
//            this.mHandler = mHandler;
//        }
//
//        @Override
//        public void run() {
//            LogUtils.d("MyThread 中线程id:" + Thread.currentThread());
//            CommTcpip commTcpip = new CommTcpip("192.168.0.136",
//                    8580);
//            CommBluetooth commBluetooth = new CommBluetooth(mac);
//            byte[] termVerInfo = new byte[8+1];
//            byte[] termSN = new byte[8+1];
//            byte[] terminalInfo = new byte[30+1];
//            String strTmp = "";
//            MposSDK mposSDK = new MposSDK();
//            mposSDK.setTermId("18666666");
//            mposSDK.setMposCommunicator(commBluetooth);
//            mposSDK.setTmsCommunicator(commTcpip);
//            MPos mPos;
//
//            int iRet = 0;
//            iRet = mposSDK.initEnv();
//            LogUtils.d("isUpdate befor...");
//            int isupdate = 0;
//            isupdate = mposSDK.isUpdate();
//            LogUtils.d("设备是否更新 isupdate:" + isupdate);
//
//            if (isupdate == 10){
//                termVerInfo = mposSDK.getTermVerInfo();
//                //System.out.println("终端版本为:" + new Utils().bcd2Str(termVerInfo));
//                LogUtils.d("终端版本为:" + new Utils().bcd2Str(termVerInfo));
//
//                //接收处已经开了一个线程
//                termSN = mposSDK.getTermSN();
//                strTmp = new Utils().bcd2Str(termSN);
//                //System.out.println("终端sn为:" + new Utils().AsciiStringToString(strTmp));
//                LogUtils.d("终端sn为:" + new Utils().AsciiStringToString(strTmp));
//
//                terminalInfo = mposSDK.getTerminalInfo();
//                //System.out.println("终端信息为:" + new Utils().bcd2Str(termVerInfo));
//                LogUtils.d("终端信息为:" + new Utils().bcd2Str(termVerInfo));
//            }
//
//
//            DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
//            ArrayList<MPos> mPoslistbefor = databaseAdapter.rawFindAll();
//            LogUtils.d("数据库之前:" + mPoslistbefor);
//            //(String mac, String name, String sn, String pn, String os_version, String boot_version,battery)
//
//            if (termVerInfo[2] > 9) {
//                mPos = new MPos(mac, name, new Utils().AsciiStringToString(strTmp), null, String.valueOf(termVerInfo[1]) + "."
//                        + String.valueOf(termVerInfo[2]), String.valueOf(termVerInfo[0]), null, String.valueOf(isupdate));
//            } else {
//                mPos = new MPos(mac, name, new Utils().AsciiStringToString(strTmp), null, String.valueOf(termVerInfo[1]) + ".0"
//                        + String.valueOf(termVerInfo[2]), String.valueOf(termVerInfo[0]), null, String.valueOf(isupdate));
//            }
//            databaseAdapter.rawUpdate(mPos);
//
//            ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();
//            LogUtils.d("数据库更新后:"+ mPoslist);
//
//            //new MyHandler.TestThread(loader).start();
//
////            CommBluetooth commBluetooth = new CommBluetooth(mac);
////            CommTcpip commTcpip = new CommTcpip("192.168.0.136", 8586);
////            LogUtils.d("befor connect------------");
//            //boolean flag = commBluetooth.connect();
////            boolean flag = true;
////
////
//////            MposSDK mposSDK = new MposSDK();
//////            //mposSDK.setMposCommunicator((ICommunicator)msg.obj);
//////            mposSDK.setMposCommunicator(commBluetooth);
//////            mposSDK.initEnv();
//////            boolean flag = mposSDK.connectMpos();
////
////            LogUtils.d("连接状态:"+flag);
////            LogUtils.d("当前线程id:"+currentThread());
////            if(flag){
////                Message msg = Message.obtain();
////                msg.what = 1;
////                msg.obj = commBluetooth;
////                //handler.sendEmptyMessage(1);
////                mHandler.sendMessage(msg);
////            }else {
////                mHandler.sendEmptyMessage(2);
////            }
//
//        }
//    }
//
//
//    private static class MyHandler extends Handler {
//        private Context context;
//        private String mac;
//        private String name;
//
//        public MyHandler(Context context, String mac, String name) {
//            this.context = context;
//            this.mac = mac;
//            this.name = name;
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case 1:
//
//                    MposSDK mposSDK = new MposSDK();
//                    MPos mPos;
//                    mposSDK.setMposCommunicator((ICommunicator) msg.obj);
//                    mposSDK.initEnv();
////                    byte[] termVerInfo = mposSDK.getTermVerInfo();
////                    System.out.println("终端版本为:"+new Utils().bcd2Str(termVerInfo));
////
////                    //接收处已经开了一个线程
////                    byte[] termSN = mposSDK.getTermSN();
////                    String strTmp = new Utils().bcd2Str(termSN);
////                    System.out.println("终端sn为:"+new Utils().AsciiStringToString(strTmp));
////
////                    byte[] terminalInfo = mposSDK.getTerminalInfo();
////                    System.out.println("终端信息为:"+new Utils().bcd2Str(termVerInfo));
////
////
////                    DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
////                    ArrayList<MPos> mPoslistbefor = databaseAdapter.rawFindAll();
////                    System.out.println("数据库之前:"+mPoslistbefor);
////                    //(String mac, String name, String sn, String pn, String os_version, String boot_version,battery)
////
////                    if (termVerInfo[2] > 9){
////                        mPos = new MPos(mac, name, new Utils().AsciiStringToString(strTmp), null, String.valueOf(termVerInfo[1])+"."
////                                + String.valueOf(termVerInfo[2]), String.valueOf(termVerInfo[0]), null);
////                    }else {
////                        mPos = new MPos(mac, name, new Utils().AsciiStringToString(strTmp), null, String.valueOf(termVerInfo[1])+".0"
////                                + String.valueOf(termVerInfo[2]), String.valueOf(termVerInfo[0]), null);
////                    }
////                    databaseAdapter.rawUpdate(mPos);
////
////                    ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();
////                    System.out.println("数据库更新后:"+mPoslist);
//
//                    LogUtils.d( "isUpdate befor21211...");
//                    //new TestThread(mposSDK).start();
//
//                    break;
//                case 2:
//                    LogUtils.d("未连接");
//            }
//        }
//
//        public static class TestThread extends Thread {
//            MposSDK mposSDK;
//
//            public TestThread(MposSDK mposSDK) {
//                this.mposSDK = mposSDK;
//            }
//
//            @Override
//            public void run() {
//                //super.run();
//                //int isupdate = mposSDK.isUpdate();
//            }
//        }
//    }

}
