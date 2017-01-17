package com.mpos.communication;

import java.io.IOException;

/**
 * Created by chenld on 2016/12/28.
 */

public interface ICommunicator {
    public boolean connect() throws IOException;
    public void close();
    public void reset() throws IOException;
    public int recv(byte[] recvBuff, int offset, int maxLen) throws IOException;
    public int send(byte[] sendBuff, int offset, int sendLen) throws IOException;
    public boolean isConnected();

}
