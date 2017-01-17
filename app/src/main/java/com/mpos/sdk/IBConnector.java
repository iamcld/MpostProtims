package com.mpos.sdk;

import com.mpos.communication.ICommunicator;

/**
 * Created by chenld on 2016/12/29.
 */

public interface IBConnector {
    public void setTmsCommunicator(ICommunicator tmsCommunicator);
    public void setMposCommunicator(ICommunicator mposCommunicator);

    public boolean connectTms();
    public void closeTms();
    public void resetTms();
    public int recvTms(byte[] recvBuff, int offset, int expLen);
    public int sendTms(byte[] sendBuff, int offset, int sendLen);
    public boolean isConnectedTms();

    public boolean connectMpos();
    public void closeMpos();
    public void resetMpos();
    public int recvMpos(byte[] recvBuff, int offset, int expLen);
    public int sendMpos(byte[] sendBuff, int offset, int sendLen);
    public boolean isConnectedMpos();
}
