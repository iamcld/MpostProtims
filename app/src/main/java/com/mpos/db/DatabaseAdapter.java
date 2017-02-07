package com.mpos.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by chenld on 2016/12/19.
 */
public class DatabaseAdapter {
    private DatabaseHelper dbHelper;

    public DatabaseAdapter(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    //使用原生sql语句
    public void rawAdd(MPos mpos){
        String sql = "insert into mpos(_mac, name, sn, pn, os_version, boot_version, battery, isupdate) " +
                "values(?,?,?,?,?,?,?,?)";
        Object[] args = {mpos.getMac(), mpos.getName(), mpos.getSn(), mpos.getPn(), mpos.getOs_version(),
                        mpos.getBoot_version(), mpos.getBattery(), mpos.getIsupdate()};
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(sql,args);
        db.close();
    }

    public void rawDelete(String mac){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "delete from mpos where _mac=?";
        Object[] args = {mac};
        db.execSQL(sql,args);
        db.close();
    }


    public void rawUpdate(MPos mpos){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "update mpos set name=?,sn=?,pn=?,os_version=?,boot_version=?,battery=? where _mac=?";
        Object[] args = {mpos.getName(), mpos.getSn(), mpos.getPn(), mpos.getOs_version(),
                mpos.getBoot_version(), mpos.getBattery(), mpos.getMac()};
        db.execSQL(sql,args);
        db.close();
    }

    //更新更新标志
    public void rawUpdateIsupdata(String mac, String isupdata){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "update mpos set isupdate=? where _mac=?";
        Object[] args = {isupdata, mac};
        db.execSQL(sql, args);
        db.close();
    }


    public MPos rawFindById(String mac){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "select _mac,name,sn,pn,os_version,boot_version,battery,isupdate from mpos where _mac=?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(mac)});
        MPos mpos = null;
        if (c.moveToNext()){
            mpos = new MPos();
            mpos.setMac(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable._MAC)));
            mpos.setName(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.NAME)));
            mpos.setSn(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.SN)));
            mpos.setPn(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.PN)));
            mpos.setOs_version(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.OS_VERSION)));
            mpos.setBoot_version(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.BOOT_VERSION)));
            mpos.setBattery(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.BATTERY)));
            mpos.setIsupdate(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.ISUPDATE)));
        }
        c.close();
        db.close();
        return mpos;
    }

    public ArrayList<MPos> rawFindAll(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "select _mac,name,sn,pn,os_version,boot_version,battery, isupdate from mpos";
        Cursor c = db.rawQuery(sql,null);

        ArrayList<MPos> mposList = new ArrayList<>();
        MPos mpos;
        while (c.moveToNext()){
            mpos = new MPos();
            mpos.setMac(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable._MAC)));
            mpos.setName(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.NAME)));
            mpos.setSn(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.SN)));
            mpos.setPn(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.PN)));
            mpos.setOs_version(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.OS_VERSION)));
            mpos.setBoot_version(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.BOOT_VERSION)));
            mpos.setBattery(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.BATTERY)));
            mpos.setIsupdate(c.getString(c.getColumnIndexOrThrow(MPosMetaData.MPosTable.ISUPDATE)));
            mposList.add(mpos);
        }
        c.close();
        db.close();
        return mposList;
    }


}
