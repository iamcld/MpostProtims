package com.mpos.db;

import android.provider.BaseColumns;

/**
 * MPos数据信息:不想被别人继承，故用final，数据库原数据的定义
 * Created by chenld on 2016/12/19.
 */

public final class MPosMetaData {

    //MPos表
    public static abstract class MPosTable implements BaseColumns{
        public static final String TABLE_NAME = "mpos";//表名

        public static final String _MAC = "_mac";//列,mac地址当主键
        public static final String NAME = "name";//列
        public static final String SN = "sn";//列
        public static final String PN = "pn";//列
        public static final String OS_VERSION = "os_version";//列
        public static final String BOOT_VERSION = "boot_version";//列
        public static final String BATTERY = "battery";//列
        public static final String ISUPDATE = "isupdate";

    }
}
