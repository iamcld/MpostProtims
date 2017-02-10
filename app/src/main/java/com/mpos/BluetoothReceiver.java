package com.mpos;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import com.apkfuns.logutils.LogUtils;
import com.mpos.adapter.BtPairAdapter;
import com.mpos.adapter.BtRepairAdapter;

import com.paxsz.easylink.api.EasyLinkSdkManager;
import com.paxsz.easylink.cmd.DeviceInfo;
import com.paxsz.easylink.listener.CloseDeviceListener;
import com.paxsz.easylink.listener.OpenDeviceListener;
import com.paxsz.easylink.listener.SwitchCommModeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class BluetoothReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothReceiver";

    private BtPairAdapter btPairAdapter;
    private BtRepairAdapter btRepairAdapter;
    private BluetoothAdapter btAdapter;//蓝牙适配器

    public BluetoothReceiver() {
    }

    public BluetoothReceiver(Context context, BtPairAdapter btPairAdapter, BtRepairAdapter btRepairAdapter,
                             BluetoothAdapter btAdapter) {
        this.btPairAdapter = btPairAdapter;
        this.btRepairAdapter = btRepairAdapter;
        this.btAdapter = btAdapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //得到intent里面的设备对象
        BluetoothDevice devices = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            if (devices.getBondState() != BluetoothDevice.BOND_BONDED) {
                //未匹配设备
                HashMap<String, String> items = new HashMap<>();
                //如果不是重复的mac地址，则可判断为新的蓝牙设备
                items.put(MposApplication.DEVICE_NAME, devices.getName());
                items.put(MposApplication.DEVICE_MAC, devices.getAddress());
                if (!("1F:1F:1F:1F:1F:1F").equals( devices.getAddress())) {
                    btPairAdapter.addDevice(items);
                    // 若蓝牙列表数据更新后，可以用以下方法通知ListView更新显示
                    btPairAdapter.notifyDataSetChanged();
                }

            } else if (devices.getBondState() == BluetoothDevice.BOND_BONDED) {
                Set<BluetoothDevice> set = btAdapter.getBondedDevices();//获得已配对蓝牙设备集合
                //显示已经配对过的设备
                btRepairAdapter.addDevice(set);
                //通知listView数据改变，更新显示列表
                btRepairAdapter.notifyDataSetChanged();
            }
        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            LogUtils.d("onReceive 中线程id:" + Thread.currentThread());
            //如果蓝牙设备状态改变，且是匹配状态，则添加
            if (devices.getBondState() == BluetoothDevice.BOND_BONDED) {
                Set<BluetoothDevice> set = btAdapter.getBondedDevices();//获得已配对蓝牙设备集合
                //显示已经配对过的设备,
                ArrayList<HashMap<String, String>> deleteList = btRepairAdapter.addDevice(set);
                btPairAdapter.deleteDevice(deleteList);

                //通知listView数据改变，更新显示列表
                btRepairAdapter.notifyDataSetChanged();
                btPairAdapter.notifyDataSetChanged();


                //调用EasylinkSdk，打开旧协议
                for (int i = 0; i < deleteList.size(); i++) {
//                    Thread thread = new Thread(new SwitchCommModeRunnable(mEasyLinkSdkManager,
//                            deleteList.get(i).get(MposApplication.DEVICE_NAME),
//                            deleteList.get(i).get(MposApplication.DEVICE_MAC)));
//                    thread.start();
//

//
                    Intent intent1 = new Intent(context, MyService.class);
//                    Intent intent1 = new Intent(context, MyIntentService.class);
                    intent1.putExtra(MposApplication.DEVICE_MAC, deleteList.get(i).get(MposApplication.DEVICE_MAC));
                    intent1.putExtra(MposApplication.DEVICE_NAME, deleteList.get(i).get(MposApplication.DEVICE_NAME));
                    context.startService(intent1);

                }
            } else if (action.equals(MposApplication.RECEIVER_ACTION)) {
                //自定义广播:蓝牙自动匹配
                String mac = intent.getStringExtra(MposApplication.DEVICE_MAC);
                Set<BluetoothDevice> set = btAdapter.getBondedDevices();//获得已配对蓝牙设备集合

                //显示已经配对过的设备
                btRepairAdapter.addDevice(set);
                btPairAdapter.deleteDevice(mac);

                //通知listView数据改变，更新显示列表
                btRepairAdapter.notifyDataSetChanged();
                btPairAdapter.notifyDataSetChanged();
                LogUtils.i("匹配的地址" + mac);
            }
        }
    }

}
