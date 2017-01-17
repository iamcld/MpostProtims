package com.mpos.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.example.chenld.mpostprotimstest.R;
import com.mpos.MposApplication;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chenld on 2016/12/12.
 */
public class BtPairAdapter extends BaseAdapter{
    private static final String TAG = "BtPairAdapter";
    private Context context;
    private ArrayList<HashMap<String, String>> list;

    public BtPairAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<HashMap<String, String>>();

    }

    //动态添加新扫描到的蓝牙设备
    public void addDevice(HashMap<String, String> device){
        if(device == null){
            return;
        }
        boolean flag = false;
        String btAddress =  device.get(MposApplication.DEVICE_MAC);
        //比较当前扫描到的蓝牙设备mac是否与列表中已有设备的mac一致
        for (int i = 0; i < list.size(); i++){
            if(list.get(i).get(MposApplication.DEVICE_MAC).equals(btAddress)){
                //若有，则标记为true，后续不添加到列表上
               flag = true;
            }
        }
        if (!flag ){

            list.add(device);
        }

    }

    //自动匹配时调用
    public void deleteDevice(String mac){
        for (int i = 0; i < list.size(); i++){
            if (list.get(i).get(MposApplication.DEVICE_MAC).equals(mac)){
                list.remove(i);
                break;
            }
        }
    }

    //手动匹配时调用
    public void deleteDevice(ArrayList<HashMap<String, String>> deleteList){

        //System.out.println("删除列表:"+deleteList);
        LogUtils.i("删除列表:"+deleteList);
        for (int j = 0; j < deleteList.size(); j++) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).get(MposApplication.DEVICE_MAC).equals(deleteList.get(j).get(MposApplication.DEVICE_MAC))) {
                    list.remove(i);
                    break;
                }
            }
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler vh;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.bt_pair_item, null);

            vh = new ViewHodler();
            vh.deviceImage = (ImageView) convertView.findViewById(R.id.bt_pair_device_image);
            vh.device_name_tv = (TextView) convertView.findViewById(R.id.bt_pair_device_name);
            vh.device_mac_tv = (TextView) convertView.findViewById(R.id.bt_pair_device_mac);
            vh.pair_button = (Button) convertView.findViewById(R.id.bt_pair_btn);
            convertView.setTag(vh);
        }else{
            vh = (ViewHodler) convertView.getTag();
        }

        final HashMap<String,String> items = list.get(position);
        //vh.imageView.setImageResource(items.get("deviceImage"));
        vh.device_name_tv.setText(items.get(MposApplication.DEVICE_NAME));
        vh.device_mac_tv.setText(items.get(MposApplication.DEVICE_MAC));
//        //imageView监听器
//        vh.deviceImage.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                Toast.makeText(context, "imageView ", Toast.LENGTH_SHORT).show();
//                intent.setClass(context, ShowDeviceInfoActivity.class);
//
//                context.startActivity(intent);
//            }
//        });
        vh.pair_button.setOnClickListener(new PairButtonListener(context, position, list));

        return convertView;
    }

    private static class PairButtonListener implements View.OnClickListener {
        private Context context;
        private int position;
        private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        public PairButtonListener(Context context, int position, ArrayList<HashMap<String, String>> list) {
            this.context = context;
            this.position = position;
            this.list = list;
        }

        @Override
        public void onClick(View v) {

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(list.get(position)
                    .get(MposApplication.DEVICE_MAC));

            Boolean returnValue = false;
            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                try {
                    //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    Toast.makeText(context, "开始配对", Toast.LENGTH_SHORT).show();
                    createBondMethod.invoke(bluetoothDevice);//开始配对
                    long start = System.currentTimeMillis();
                    long end = System.currentTimeMillis() + 5000;
                    while (end - start > 0) {
                        start = System.currentTimeMillis();
                        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
//
                            Intent intent = new Intent();
                            intent.setAction(MposApplication.RECEIVER_ACTION);//自动匹配
                            intent.putExtra(MposApplication.DEVICE_MAC, bluetoothDevice.getAddress());
                            intent.putExtra(MposApplication.DEVICE_NAME, bluetoothDevice.getName());
                            context.sendBroadcast(intent);//发送自定义广播
                            break;
                        }
                        //Log.i("mytag", new String().valueOf(System.currentTimeMillis()));
                    }

                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                Toast.makeText(context, "开始连接", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private static class ViewHodler{
        private ImageView deviceImage;
        private TextView device_name_tv;
        private TextView device_mac_tv;
        private Button pair_button;
    }

}
