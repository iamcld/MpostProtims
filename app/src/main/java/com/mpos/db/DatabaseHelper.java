
package com.mpos.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * Created by chenld on 2016/12/19.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "mpos.db";
    private static final int VERSION = 1;

//    private String mac;
//    private String name;//列
//    private String sn;//列
//    private String pn;//列
//    private String os_version;//列
//    private String boot_version;//列
//    private String battery;//列
//    private String isupdate;

    private static final String CREATE_TABLE_MPOS = "CREATE TABLE mpos(_mac TEXT PRIMARY KEY," +
            "name TEXT,sn TEXT,pn TEXT,os_version TEXT,boot_version TEXT,battery TEXT,isupdate TEXT)";


    private static final String DROP_TABLE_MPOS = "DROP TABLE IF EXISTS mpos";
    public DatabaseHelper(Context context) {
        //factory:游标，为null时表示使用系统自己定义的游标
        super(context, DB_NAME, null, VERSION);
    }

    //如果数据库表不存在，那么会调用该方法
    @Override
    public void onCreate(SQLiteDatabase db) {
        //SQLiteDatabase用于操作数据的工具类
        db.execSQL(CREATE_TABLE_MPOS);//创建数据库
    }

    //升级，更新
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_MPOS);//删除数据库
        db.execSQL(CREATE_TABLE_MPOS);//创建数据库
    }


}
