package com.mpos.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chenld.mpostprotimstest.R;
import com.mpos.BluetoothReceiver;
import com.mpos.MposApplication;
import com.mpos.adapter.BtPairAdapter;
import com.mpos.adapter.BtRepairAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */

//android系统会在12秒之内搜索蓝牙设备，并且发送广播(BluetoothDevice.ACTION_FOUND,
// BluetoothAdapter.ACTION_DISCOVERY_FINISHED,)
public class BTFragment extends Fragment implements View.OnClickListener {
    private final int REQUEST_BT_ENABLE   = 1;

    private ListView pairBtList;
    private ListView repairBtList;
    private BtPairAdapter btPairAdapter;
    private BtRepairAdapter btRepairAdapter;
    private BluetoothAdapter btAdapter;//蓝牙适配器
    ArrayList<HashMap<String,String>> datas = new ArrayList<>();
    private BluetoothReceiver mBluetoothReceiver;

    public BTFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_bt, container, false);
        pairBtList = (ListView) view.findViewById(R.id.bt_pair_list);
        repairBtList = (ListView) view.findViewById(R.id.bt_repair_list);
        view.findViewById(R.id.search_btn).setOnClickListener(this);
        view.findViewById(R.id.stop_btn).setOnClickListener(this);

        //获取本地蓝牙适配器
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null){
            btPairList();
            btRepairList();

            //自动搜索蓝牙设备,开始广播
            btAdapter.startDiscovery();

            mBluetoothReceiver = new BluetoothReceiver(getContext(), btPairAdapter, btRepairAdapter, btAdapter);
            IntentFilter intentFilter1 =  new IntentFilter();
            //将其action指定为BluetoothDevice.ACTION_FOUND,查找蓝牙
            intentFilter1.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter1.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
            intentFilter1.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            intentFilter1.addAction(MposApplication.RECEIVER_ACTION);//自动匹配
            getActivity().registerReceiver(mBluetoothReceiver, intentFilter1);
        }
        return view;
    }

    private void btPairList(){
        btPairAdapter = new BtPairAdapter(getActivity());
        pairBtList.setAdapter(btPairAdapter);
    }
    private void btRepairList(){
        Set<BluetoothDevice> devices = btAdapter.getBondedDevices();//获得已配对蓝牙设备集合

        btRepairAdapter = new BtRepairAdapter(getActivity());
        repairBtList.setAdapter(btRepairAdapter);
        //显示已经配对过的设备
        btRepairAdapter.addDevice(devices);
        //通知listView数据改变，更新显示列表
        btRepairAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.search_btn:
                if(btAdapter != null) {
                    if (!btAdapter.isEnabled()) {
                        intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, REQUEST_BT_ENABLE);
                    } else {
                        //寻找蓝牙设备，android会将查找到的设备以广播形式发出去
                        btAdapter.startDiscovery();
                        Toast.makeText(getActivity(), "正在搜索蓝牙设备", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(),"设备不支持蓝牙", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.stop_btn:
                Toast.makeText(getActivity(),"停止",Toast.LENGTH_SHORT).show();
                if(btAdapter != null && btAdapter.isDiscovering()){
                    btAdapter.cancelDiscovery();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //fragment销毁，注销掉广播等
        if(btAdapter != null){
            if(btAdapter.isDiscovering()){
                btAdapter.cancelDiscovery();
            }
        }
        //解除注册
        getActivity().unregisterReceiver(mBluetoothReceiver);
    }


}
