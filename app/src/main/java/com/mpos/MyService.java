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
import com.paxsz.easylink.api.EasyLinkSdkManager;
import com.paxsz.easylink.cmd.DeviceInfo;
import com.paxsz.easylink.listener.CloseDeviceListener;
import com.paxsz.easylink.listener.OpenDeviceListener;
import com.paxsz.easylink.listener.SwitchCommModeListener;

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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i("service onStartCommand: ");
        String mac = intent.getStringExtra(MposApplication.DEVICE_MAC);
        String name = intent.getStringExtra(MposApplication.DEVICE_NAME);
        EasyLinkSdkManager mEasyLinkSdkManager = EasyLinkSdkManager.getInstance(this);

        MyThread myThread = new MyThread(mEasyLinkSdkManager, mac, name);
        myThread.start();

        return super.onStartCommand(intent, flags, startId);

    }

    private class MyThread extends Thread {
        private EasyLinkSdkManager mEasyLinkSdkManager;
        private String mac;
        private String name;

        public MyThread(EasyLinkSdkManager easyLinkSdkManager, String mac, String name) {
            this.mEasyLinkSdkManager = easyLinkSdkManager;
            this.mac = mac;
            this.name = name;
        }
        @Override
        public void run() {
            DeviceInfo deviceInfo = new DeviceInfo(name, mac);
            mEasyLinkSdkManager.connect(deviceInfo, new OpenDeviceListener() {
                @Override
                public void openSucc() {
                    LogUtils.d("connect success");
                    //连接成功
                    //切换协议,0x1支持新旧协议切换，支持tms；0x0只支持新协议
                    mEasyLinkSdkManager.switchCommMode(0x1, new SwitchCommModeListener() {
                        @Override
                        public void onSucc() {
                            LogUtils.e("switchCommMode successful");
                        }

                        @Override
                        public void onError(int code, String errDesc) {
                            LogUtils.e("switchCommMode error, code=" + code);
                        }
                    });

                    mEasyLinkSdkManager.disconnect(new CloseDeviceListener() {
                        @Override
                        public void closeSucc() {
                            LogUtils.e("disconnect success");
                        }

                        @Override
                        public void onError(int code, String errDesc) {
                            LogUtils.e("disconnect error, code=" + code);
                        }
                    });
                }

                @Override
                public void onError(int code, String errDesc) {
                    LogUtils.e("connect error,code=" + code);

                    //TODO
                    //pos端写死已经连接上，故再次连接会报1001,故在此函数中打开旧协议以及关掉连接
                    //以后不会需要
//                    mEasyLinkSdkManager.switchCommMode(0x1, new SwitchCommModeListener() {
//                        @Override
//                        public void onSucc() {
//                            LogUtils.e("switchCommMode successful");
//                        }
//                        @Override
//                        public void onError(int code, String errDesc) {
//                            LogUtils.e("switchCommMode error, code=" + code);
//                        }
//                    });
//
//                    mEasyLinkSdkManager.disconnect(new CloseDeviceListener() {
//                        @Override
//                        public void closeSucc() {
//                            LogUtils.e("disconnect success");
//                        }
//
//                        @Override
//                        public void onError(int code, String errDesc) {
//                            LogUtils.e("disconnect error, code=" + code);
//                        }
//                    });
                }
            });

            MyService.this.stopSelf();
        }//end run
    }//end MyThread
}//end MyService
