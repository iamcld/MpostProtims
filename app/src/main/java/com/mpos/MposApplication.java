
package com.mpos;


import android.app.Application;

import com.apkfuns.logutils.LogLevel;
import com.apkfuns.logutils.LogUtils;
import com.example.chenld.mpostprotimstest.R;

/**
 * Created by chenld on 2016/12/31.
 */

public class MposApplication extends Application {

    public static final String DEVICE_MAC = "deviceMac";
    public static final String DEVICE_NAME = "deviceName";
    public static final String RECEIVER_ACTION = "android.bluetooth.device.action.MY_BROADCAST";
    public static final String CHECK_UPDATE_DEVICE = "checkUpdataDevice";
    public static final int[] img = {R.drawable.d180, R.drawable.d180, R.drawable.d210,
            R.drawable.d210, R.drawable.u19};
    public static final String[] deviceName = {"d180", "D180", "d210", "D210"};


    @Override
    public void onCreate() {
        super.onCreate();

        /**
         *configAllowLog:是否允许日志输出
         * configTagPrefix:日志log的前缀
         * configShowBorders:是否显示边界
         * configLevel:日志显示等级
         */
        LogUtils.getLogConfig().configAllowLog(true).configTagPrefix("LogUtils")
                .configShowBorders(false).configLevel(LogLevel.TYPE_DEBUG);
    }
}
