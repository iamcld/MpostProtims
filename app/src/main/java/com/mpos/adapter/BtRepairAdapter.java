package com.mpos.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.mpos.MposApplication;
import com.mpos.MyIntentService;
import com.mpos.db.DatabaseAdapter;
import com.mpos.db.MPos;
import com.example.chenld.mpostprotimstest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by chenld on 2016/12/12.
 */
public class BtRepairAdapter extends BaseAdapter{
    private static final String TAG = "BtRepairAdapter";
    private Context context;
    private ArrayList<HashMap<String, String>> list;


    public BtRepairAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<HashMap<String, String>>();
    }

    //动态添加已经匹配到的蓝牙设备
    public ArrayList<HashMap<String, String>> addDevice(Set<BluetoothDevice> devices){
        boolean flag;
        //ArrayList<String> resultList = new ArrayList<String>();
        ArrayList<HashMap<String, String>> deleteResult = new ArrayList<HashMap<String, String>>();
        HashMap<String,String> hm;
        if(devices == null){
            return null;
        }

        for (BluetoothDevice bd :devices){
            flag = false;
            for (int i = 0; i < list.size(); i++){
                if (list.get(i).get(MposApplication.DEVICE_MAC).equals(bd.getAddress())){
                    flag = true;
                }
            }
            if (!flag){
                hm = new HashMap<String, String>();
                hm.put(MposApplication.DEVICE_NAME, bd.getName());
                hm.put(MposApplication.DEVICE_MAC, bd.getAddress());
                insertDataToDatabase( bd.getAddress(), bd.getName());//插入到数据库中
                list.add(hm);
                deleteResult.add(hm);
            }
        }
        return deleteResult;

    }



    //将内容插入到数据库中
    public void insertDataToDatabase(String mac, String name){
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
        MPos mPosOld = new MPos();
        mPosOld = databaseAdapter.rawFindById(mac);
        //如果数据库中没有，则插入
        if (mPosOld == null){
            LogUtils.i("数据库中没有相同的记录，插入新数据");
            MPos mPosNew = new MPos(mac, name);
            databaseAdapter.rawAdd(mPosNew);
        }

    }
    //数据库中
    public void findDatafromDatabase(){
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);;
        ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();
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
            convertView = inflater.inflate(R.layout.bt_repair_item, null);

            vh = new ViewHodler();
            vh.device_image = (ImageView) convertView.findViewById(R.id.bt_repair_device_image);
            vh.device_name_tv = (TextView) convertView.findViewById(R.id.bt_repair_device_name);
            vh.device_mac_tv = (TextView) convertView.findViewById(R.id.bt_repair_device_mac);
            vh.repair_button = (Button) convertView.findViewById(R.id.bt_repair_btn);
            convertView.setTag(vh);
        }else{
            vh = (ViewHodler) convertView.getTag();
        }

//        vh.device_image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                Toast.makeText(context, "imageView ", Toast.LENGTH_SHORT).show();
//                intent.setClass(context, ShowDeviceInfoActivity.class);
//                context.startActivity(intent);
//            }
//        });

        vh.repair_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "重新匹配", Toast.LENGTH_SHORT).show();
            }
        });
        //convertView.setOnClickListener(new ConvertViewListener(context, position, list));


        HashMap<String,String> items = list.get(position);
        //vh.device_image.setImageResource(items.get("deviceImage"));
        vh.device_name_tv.setText(items.get(MposApplication.DEVICE_NAME));
        vh.device_mac_tv.setText(items.get(MposApplication.DEVICE_MAC));

        vh.repair_button.setOnClickListener(new RePairButtonListener(context, position, list));

        return convertView;
    }

//    private static class ConvertViewListener implements View.OnClickListener{
//        private Context context;
//        private int position;
//        private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
//
//        public ConvertViewListener(Context context, int position, ArrayList<HashMap<String, String>> list) {
//            this.context = context;
//            this.position = position;
//            this.list = list;
//        }
//
//        @Override
//        public void onClick(View v) {
//            Toast.makeText(context,"item", Toast.LENGTH_SHORT).show();
////            CommBluetooth commBluetooth = new CommBluetooth(list.get(position).get("deviceMac"));
////            commBluetooth.connect();
//        }
//    }

    private static class RePairButtonListener implements View.OnClickListener {
        private Context context;
        private int position;
        private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        public RePairButtonListener(Context context, int position, ArrayList<HashMap<String, String>> list) {
            this.context = context;
            this.position = position;
            this.list = list;
        }

        @Override
        public void onClick(View v) {

            Intent intent1 = new Intent(context, MyIntentService.class);
            intent1.putExtra(MposApplication.DEVICE_MAC, list.get(position).get(MposApplication.DEVICE_MAC));
            intent1.putExtra(MposApplication.DEVICE_NAME, list.get(position).get(MposApplication.DEVICE_NAME));

            context.startService(intent1);

//            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(list.get(position)
//                    .get(MposApplication.DEVICE_MAC));
//
////            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
//                try {
//                    //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
//                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
////                    Toast.makeText(context, "开始配对", Toast.LENGTH_SHORT).show();
//                    createBondMethod.invoke(bluetoothDevice);//开始配对
//                    long start = System.currentTimeMillis();
//                    long end = System.currentTimeMillis() + 5000;
//                    while (end - start > 0) {
//                        start = System.currentTimeMillis();
//                        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
////
//                            Intent intent = new Intent();
//                            intent.setAction(MposApplication.RECEIVER_ACTION);//自动匹配
//                            intent.putExtra(MposApplication.DEVICE_MAC, bluetoothDevice.getAddress());
//                            intent.putExtra(MposApplication.DEVICE_NAME, bluetoothDevice.getName());
//                            context.sendBroadcast(intent);
//                            Log.i("mytag", "-----------");
//                            break;
//                        }
//                        //Log.i("mytag", new String().valueOf(System.currentTimeMillis()));
//                    }
//
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
////            } else if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
////                Toast.makeText(context, "开始连接", Toast.LENGTH_SHORT).show();
////            }

        }
    }

    private static class ViewHodler{
        private ImageView device_image;
        private TextView device_name_tv;
        private TextView device_mac_tv;
        private Button repair_button;
    }
}
