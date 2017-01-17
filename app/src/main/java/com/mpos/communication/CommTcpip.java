package com.mpos.communication;

import android.util.Log;

import com.apkfuns.logutils.LogUtils;
import com.pax.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by chenld on 2016/12/28.
 */

public class CommTcpip implements ICommunicator {

    private static final String TAG = "CommTcpip";
    private static final int CONN_TIMEOUT_DEFAULT = 10000;    // ms
    private static final int READ_TIMEOUT_DEFAULT = 3000;    // ms
    private static final int RECV_TIMEOUT_DEFAULT = 7000;    // ms

    private String serverAddr = null;
    public int serverPort = 0;
    private boolean isIpConected = false;

    private Socket client = null;
    //tcp输入输出流
    private OutputStream ipOutputStream = null;
    private InputStream ipInputStream = null;

    public CommTcpip(String serverAddr, int serverPort) {
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        isIpConected = false;
    }

    //与TMS相连接
    public  boolean connect() {
        try {

            LogUtils.d("当前线程:" + Thread.currentThread());
            client = new Socket();
            client.connect(new InetSocketAddress(serverAddr, serverPort), CONN_TIMEOUT_DEFAULT);

            LogUtils.d("TCP套接字client:"+client);
            client.setSoTimeout(RECV_TIMEOUT_DEFAULT);
            ipOutputStream = client.getOutputStream();
            ipInputStream = client.getInputStream();


        } catch (IOException e) {
            LogUtils.d(e.toString());
            e.printStackTrace();
            isIpConected = false;
            return false;
        }

        LogUtils.d("client.isConnected()");
        if (null == ipOutputStream || null == ipInputStream) {
            return false;
        }

        isIpConected = true;
        LogUtils.d("IP connected, isIpConected:" + isIpConected);
        return true;
    }

    public  int send(byte[] buf, int offset, int sendLen) {
        if (null == client || null == ipOutputStream || !isIpConected) {
            LogUtils.d("client:" + client);
            LogUtils.d("ipOutputStream:" + ipOutputStream);
            LogUtils.d("isIpConected:" + isIpConected);
            return -1;
        }
        if (sendLen <= 0 || offset < 0 || null == buf) {
            return -2;
        }

        byte[] sendBuff = new byte[sendLen];
        System.arraycopy(buf, offset, sendBuff, 0, sendLen);
        LogUtils.d("tcp send ********sendLen=%d", sendLen);
        Utils.logHexData(sendBuff, 0, sendLen);
        try {
            ipOutputStream.write(sendBuff);
        } catch (IOException e) {
            LogUtils.e(e.toString());
        }
        LogUtils.d("sendLen" + sendLen);
        return sendLen;

    }

    public  int recv(byte[] buf, int offset, int maxLen) {
        if (null == client || null == ipInputStream || !isIpConected) {
            return -1;
        }
        if (null == buf || maxLen <= 0 || offset < 0) {
            return -2;
        }

        int cLen = 0;
        byte[] recvBuff = new byte[maxLen];
        try {
            cLen = ipInputStream.read(recvBuff, 0, maxLen);
        } catch (IOException e) {
            LogUtils.d("receive data timeout");
            LogUtils.d("recv len=" + cLen);
        }

        LogUtils.d("tcp recv **************cLen=" + cLen);
        if (cLen > 0) {
            Utils.logHexData(recvBuff, 0, cLen);
            System.arraycopy(recvBuff, 0, buf, offset, cLen);
        }
        return cLen;

    }

    public void reset() {
        if (null == client || null == ipOutputStream || null == ipInputStream || !isIpConected) {
            return;
        }
    }

    public void close() {
        if (null == client || null == ipOutputStream || null == ipInputStream || !isIpConected) {
            return;
        }
        try {
            LogUtils.d("ip client closing...");
            if (client != null) {
                LogUtils.d("关闭TCP套接字:"+client);
                client.shutdownInput();
                client.shutdownOutput();
                client.close();
                client = null;
                LogUtils.d("ip client closed");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            ipInputStream = null;
            ipOutputStream = null;
            isIpConected = false;
            LogUtils.d("close finally");
        }
    }

    public boolean isConnected() {
        if (null == client || null == ipInputStream || null == ipOutputStream || !isIpConected) {
            return false;
        }
        return isIpConected;
    }

}
