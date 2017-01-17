package com.mpos.sdk;

/**
 * Created by chenld on 2016/12/29.
 */

import com.apkfuns.logutils.LogUtils;
import com.mpos.communication.ICommunicator;


import java.io.IOException;
import java.io.Serializable;

/**
 * MposSdk，调用jni接口，封装成jar包
 */

public class MposSDK implements Serializable, IBConnector{
    private static final String TAG = "MposSDK";

    private ICommunicator tmsCommunicator;
    private ICommunicator mposCommunicator;
    private boolean bConnectedTms;
    private boolean bConnectedMpos;
    private IProgressListener listener;
    private String termId;
    private String path;
    private int callMode;
    private boolean callModeFlag = false;
    private boolean interrupt = false;

    static {
        System.loadLibrary("protocol");
        initMethodID();
    }

    public MposSDK() {
    }

    public MposSDK(IProgressListener listener){
        this.listener = listener;
    }

    public native static int initMethodID();
    public native  int initEnv();
    public native  int download();
    public native  void release();

    public native  int getTermSn(byte[] sn);
    public native  byte[] getTermSN();//获取SN
    //public native  byte[] getTermExtSN();

    public native  int getTermVerInfo(byte[] verInfo);
    public native  byte[] getTermVerInfo();//获取版本信息

    public native  int getTerminalInfo(byte[] terInfo);
    public native  byte[] getTerminalInfo();//获取终端信息
    public native  int isUpdate();//是否需要更新

    public String getTermId(){
        return this.termId;
    }

    public void setTermId(String termId){
        this.termId = termId;
    }

    public String getPath(){
        return this.path;
    }

    public void setPath(String path){
        this.path = path;
    }

    public int getCallMode(){
        if(!callModeFlag){
            return 2; // COMM_TCPIP
        }
        return this.callMode;
    }

    public void setCallMode(int callMode){
        this.callMode = callMode;
        this.callModeFlag = true;
    }

    public void setInterrupt(boolean interrupt){
        this.interrupt = interrupt;
    }

    public void setProgress(int type, int step, int doneFlag, int fileCount, int curFile, int total, int cur, int curFileSize, int status){
        if(interrupt){
            return;
        }
        if(null != listener){
            listener.onDownloadSize(type, step, doneFlag, fileCount, curFile, total, cur, curFileSize, status);
        }
    }


    @Override
    public boolean connectTms() {
        LogUtils.d("connectTms, run in java");
        if(interrupt){
            return false;
        }
        if(null == tmsCommunicator){
            return false;
        }
        if(bConnectedTms){
            LogUtils.d( "connectTms, connected firstly");
            return bConnectedTms;
        }
        try {
            bConnectedTms = tmsCommunicator.connect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(!bConnectedTms){
            LogUtils.d("connectTms, failed to connect");
            return false;
        }

        LogUtils.d("connectTms, connnected successfullly");
        return bConnectedTms;
    }

    @Override
    public void closeTms() {
        if(interrupt){
            return;
        }
        if(null == tmsCommunicator){
            return;
        }
        if(!bConnectedTms){
            return;
        }
        tmsCommunicator.close();
        bConnectedTms = false;
        LogUtils.d("closeTms, run in java");
    }

    @Override
    public void resetTms() {
        if(interrupt){
            return;
        }
        if(null == tmsCommunicator){
            return;
        }
        if(!bConnectedTms){
            return;
        }
        LogUtils.d("resetTms, run in java");
        try {
            tmsCommunicator.reset();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public int recvTms(byte[] recvBuff, int offset, int maxLen) {
        if(interrupt){
            return -3;
        }
        if(null == tmsCommunicator){
            return -1;
        }
        if(!bConnectedTms){
            return -1;
        }
        int recvLen = -3;
        LogUtils.d("recvTms, run in java");
        try {
            recvLen = tmsCommunicator.recv(recvBuff, offset, maxLen);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LogUtils.d("recvTms, recvLen="+recvLen);
        return recvLen;
    }

    @Override
    public int sendTms(byte[] sendBuff, int offset, int sendLen) {
        if(interrupt){
            return -3;
        }
        if(null == tmsCommunicator){
            return -1;
        }
        if(!bConnectedTms){
            return -1;
        }
        int len = 0;
        LogUtils.d("sendTms, run in java");
        try {
            len = tmsCommunicator.send(sendBuff, offset, sendLen);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return len;
    }

    @Override
    public boolean isConnectedTms() {
        return bConnectedTms;
    }

    @Override
    public boolean connectMpos() {
        LogUtils.d("connectMpos, run in java");
        if(interrupt){
            return false;
        }
        if(null == mposCommunicator){
            return false;
        }
        if(bConnectedMpos){
            LogUtils.d("connectMpos, connected firstly");
            return bConnectedMpos;
        }
        try {
            bConnectedMpos = mposCommunicator.connect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(!bConnectedMpos){
            bConnectedMpos = false;
            LogUtils.d("connectMpos, failed to connect");
            return bConnectedMpos;
        }
        LogUtils.d("connectMpos, connected successfully");
        return bConnectedMpos;
    }

    @Override
    public void closeMpos() {
        if(interrupt){
            return;
        }
        if(null == mposCommunicator){
            return;
        }
        if(!bConnectedMpos){
            return;
        }
        mposCommunicator.close();
        bConnectedMpos = false;
        LogUtils.d("closeMpos, run in java");
    }

    @Override
    public void resetMpos() {
        LogUtils.d("resetMpos in");
        if(interrupt){
            return;
        }
        if(null == mposCommunicator){
            return;
        }
        if(!bConnectedMpos){
            return;
        }
        LogUtils.d("resetMpos, run in java");
        try {
            mposCommunicator.reset();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public int recvMpos(byte[] recvBuff, int offset, int expLen) {
        if(interrupt){
            return -3;
        }
        if(null == mposCommunicator){
            return -1;
        }
//        if(!bConnectedMpos){
//            return -1;
//        }
        int len = -3;
        try {
            LogUtils.d("start recv");
            len =  mposCommunicator.recv(recvBuff, offset, expLen);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return len;
    }

    @Override
    public int sendMpos(byte[] sendBuff, int offset, int sendLen) {
        if(interrupt){
            return -3;
        }
        if(null == mposCommunicator){
            LogUtils.d("mposCommunicator is null");
            return -1;
        }
//        if(!bConnectedMpos){
//           LogUtils.d("bConnectedMpos is null");
//            return -1;
//        }
        int len = 0;
        try {
            LogUtils.d("statr send");
            len = mposCommunicator.send(sendBuff, offset, sendLen);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return len;
    }

    @Override
    public boolean isConnectedMpos() {
        return bConnectedMpos;
    }

    @Override
    public void setTmsCommunicator(ICommunicator tmsCommunicator) {
        this.tmsCommunicator = tmsCommunicator;
    }

    @Override
    public void setMposCommunicator(ICommunicator mposCommunicator) {
        this.mposCommunicator = mposCommunicator;
    }


}
