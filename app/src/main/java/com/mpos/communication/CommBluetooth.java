package com.mpos.communication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Looper;

import com.apkfuns.logutils.LogUtils;
import com.pax.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by chenld on 2016/12/28.
 */

public class CommBluetooth implements ICommunicator{

    private static final String TAG = "mytag";
    // fa87c0d0-afac-11de-8a39-0800200c9a66  00001101-0000-1000-8000-00805F9B34FB
    private final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int CONN_SUCCESS = 0x1;
    private static final int CONN_FAIL = 0x2;
    private static final int RECEIVE_INFO = 0x3;
    private static final int SET_EDITTEX_NULL = 0x4;
    private static final int RECV_TIMEOUT_DEFAULT = 60000; // ms

    private String pairBtMac;//已经匹配的蓝牙地址
    private BluetoothAdapter bluetoothAdapter;//本地蓝牙设备
    private BluetoothDevice device;//远程蓝牙设备
    private BluetoothSocket btsocket;//蓝牙Socket客户端

    private IOException  btIoException = null;
    private BTReadThread btReadThread  = null;
    private Utils.RingBuffer btRingBuffer;

    private boolean isBtConnected = false;

    //蓝牙输入输出流
    private OutputStream btOut;
    private InputStream btIn;

    public CommBluetooth(String pairBtMac){
        this.pairBtMac = pairBtMac;
        this.isBtConnected = false;
    }

    //与mpos连接
    public  boolean connect(){
        isBtConnected = false;
        //1、得到本地蓝牙设备的默认适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //2、通过本地蓝牙设备得到远程蓝牙设备,连接的设备
        device = bluetoothAdapter.getRemoteDevice(pairBtMac);

        if (device == null || device.getBondState() != BluetoothDevice.BOND_BONDED){
            LogUtils.d("蓝牙设备错误");
            return false;
        }

        //启动线程，连接蓝牙设备
        //new Thread(new Runnable() {
        // @Override
        // public void run() {
        try {
            //3、根据UUID 创建并返回一个BluetoothSocket,UUID需要和服务器端的一样
            btsocket = device.createInsecureRfcommSocketToServiceRecord(BT_UUID);
            LogUtils.d("蓝牙套接字btsocket:"+btsocket);
            if (btsocket != null){
                LogUtils.d("当前线程:"+Thread.currentThread());
                btsocket.connect();
                LogUtils.d("连接成功");
                //4、处理客户端输入输出流
                btOut = btsocket.getOutputStream();
                btIn = btsocket.getInputStream();
                if (btOut == null || btIn == null){
                    isBtConnected = false;

                    //cld test
                    //isBtConnected = true;
                    LogUtils.i("btout或btIn 为null");
                    return false;
                    //handler.sendEmptyMessage(CONN_FAIL);//发送一个空消息，表示连接成功
                }else {
                    LogUtils.i("-------");
                    isBtConnected = true;
                    //handler.sendEmptyMessage(CONN_SUCCESS);//发送一个空消息，表示连接成功
                }
            }else {
                LogUtils.d(TAG,"套接字创建失败1");
                return false;
            }

        } catch (IOException e) {
            LogUtils.d("当前线程:"+Thread.currentThread());
            LogUtils.d("套接字创建失败2");
            e.printStackTrace();
            LogUtils.d("套接字创建失败2,return false:");
            //handler.sendEmptyMessage(CONN_FAIL);//发送一个空消息，表示连接成功
            return false;
        }
        //}
        // }).start();
        LogUtils.d("当前线程:"+Thread.currentThread());
        LogUtils.d("连接成功");
        return true;
    }

    public  int send(final byte[] buf, int offset, int sendLen){
        LogUtils.d("start bt send...");
        if (isBtConnected && btOut != null){
            LogUtils.d( "bt send data, offset="+offset + ", sendLen="+sendLen);
            try {
                //Utils.logHexData(buf, 0, sendLen);
                btOut.write(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            LogUtils.d( "连接状态:isBtConnected"+isBtConnected + "内容:"+null);
            sendLen = -1;
        }
        LogUtils.d("endt bt send...");
        return sendLen;
    }

    @Override
    public boolean isConnected() {
        return isBtConnected;
    }

    public  int recv(byte[] buf, int offset, int exp){
        LogUtils.d("start bt recv...");
        if (exp == 0){
            return 0;
        }

        if (btIoException != null) {
            btReadThread = null;
            btIoException = null;
        }

        if (btReadThread == null && isBtConnected) {
            btReadThread = new BTReadThread();
            btReadThread.start();
        }
        int totalLen = 0;
        int cLen;
        //long end = System.currentTimeMillis() + RECV_TIMEOUT_DEFAULT;

        //cld test
        long end = System.currentTimeMillis() + 10000;
        LogUtils.d("System.currentTimeMillis() + RECV_TIMEOUT_DEFAULT"+end);
        while (totalLen < exp && (System.currentTimeMillis() < end)){
            cLen = btRingBuffer.read(buf, offset + totalLen, exp - totalLen);
            totalLen += cLen;

            //cld test
            //Thread.yield();
            if (btIoException != null){
                LogUtils.d("btIoException.....");
                return -1;
            }
        }
        if (totalLen == 0){
            LogUtils.d( "recv nothing");
        }
        LogUtils.d("end bt recv.....:%d", totalLen);
        //Utils.logHexData(buf, 0, totalLen);
        return totalLen;
    }

    final class BTReadThread extends Thread{
        private byte[] mBuff;
        public BTReadThread() {
            mBuff = new byte[10240];
            btRingBuffer = new Utils.RingBuffer(10240);
        }
        @Override
        public void run() {
            Looper.prepare();
            try {
                while(true) {
                    if(!isBtConnected){
                        break;
                    }
                    int len = btIn.read(mBuff);
                    if (len < 0) {
                        throw new IOException("input stream read error: " + len);
                    } else {
                        btRingBuffer.write(mBuff, len);
                    }
                }
            } catch (IOException e) {
                btIoException = e;
            }
        }
    }

    public void reset(){
        if (btRingBuffer != null){
            btRingBuffer.reset();
        }
    }

    public void close(){
        try {
            LogUtils.d( "bt closing...");
            if (btsocket != null) {
                LogUtils.d("关闭蓝牙套接字:"+btsocket);
                Thread.sleep(500);
                btsocket.close();
                btsocket = null;
                LogUtils.d("bt closed");
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            btOut = null;
            btIn = null;
            isBtConnected = false;
            //cld test
            //isBtConnected = true;
            LogUtils.d("bt close finally");
        }

    }

//
//    private static Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what){
//                case CONN_SUCCESS:
//                    Log.i(TAG, "connect mpos success");
//                    break;
//                case CONN_FAIL:
//                    Log.i(TAG, "connect mpos fail");
//            }
//        }
//    };


}
