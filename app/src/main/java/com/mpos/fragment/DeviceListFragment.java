package com.mpos.fragment;


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.example.chenld.mpostprotimstest.R;
import com.mpos.MposApplication;
import com.mpos.MyIntentService;
import com.mpos.MyService;
import com.mpos.activity.DownloadActivity;
import com.mpos.activity.ServerSetActivity;
import com.mpos.activity.ShowDeviceInfoActivity;
import com.mpos.adapter.NotUpdateDeviceAdapter;
import com.mpos.adapter.UpdateDeviceAdapter;
import com.mpos.communication.CommBluetooth;
import com.mpos.communication.CommTcpip;
import com.mpos.db.DatabaseAdapter;
import com.mpos.db.MPos;
import com.mpos.sdk.MposSDK;
import com.pax.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceListFragment extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "DeviceListFragment";
    private final static int EDIT = 0x1;
    private final static int FINISH = 0x2;
    private final static int UPDATE = 0x3;
    private final static int DELETE = 0x4;
    private final static int BT_SEND_RECEIVE = 0x5;
    private final static int PROTIMS_SEND_RECEIVE = 0x6;

    private ListView updateList;//可更新pos列表
    private ListView notUpdateList;//不可更新pos列表
    private UpdateDeviceAdapter updateDeviceAdapter;//可更新pos列表自定义适配器
    private NotUpdateDeviceAdapter notUpdateDeviceAdapter;//不可更新pos列表自定义适配器
    private Button edit_btn;//编辑按钮
    private Button finish_btn;//完成按钮
    private ImageButton add_ImgBtn;//添加按钮
    private Button update_pos_btn;//更新按钮
    private Button delete_pos_btn;//删除按钮
    private LinearLayout layout_update_delete;
    private RadioGroup radiogroup;
    private RadioButton radio_all_select;//全选
    private RadioButton radio_inverst_select;//反选
    //更新列表数据
    private ArrayList<HashMap<String, String>> datasUpdate;
    //无更新列表数据
    private ArrayList<HashMap<String, String>> datasNotUpdate;
    private DatabaseAdapter databaseAdapter;
    //获取数据库中数据
    private ArrayList<MPos> mPoslist;
    //单任务线程池
    private ExecutorService esBt = Executors.newSingleThreadExecutor();
    private ExecutorService esTcp = Executors.newSingleThreadExecutor();


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EDIT:
                    finish_btn.setVisibility(View.VISIBLE);//完成按钮可见
                    edit_btn.setVisibility(View.GONE);//编辑按钮不可见
                    layout_update_delete.setVisibility(View.VISIBLE);//水平布局
                    radiogroup.clearCheck();//初始都不选中状态
                    NotUpdateDeviceAdapter(true);
                    UpdateDeviceAdapter(true);
                    break;
                case FINISH:
                    //checkBox不可见
                    NotUpdateDeviceAdapter(false);
                    UpdateDeviceAdapter(false);
                    finish_btn.setVisibility(View.GONE);//完成按钮不可见
                    edit_btn.setVisibility(View.VISIBLE);//编辑按钮可见
                    layout_update_delete.setVisibility(View.GONE);//水平布局
                    break;
                case UPDATE:
                    ArrayList<String> checkUpdateDevice = new ArrayList<>();
                    //Toast.makeText(DeviceListActivity.this, "update pos",Toast.LENGTH_SHORT).show();
                    //NotUpdateDeviceAdapter(false);
                    //UpdateDeviceAdapter(false);
                    finish_btn.setVisibility(View.GONE);//完成按钮不可见
                    edit_btn.setVisibility(View.VISIBLE);//编辑按钮可见
                    layout_update_delete.setVisibility(View.GONE);//水平布局
                    for (int i = 0; i < datasUpdate.size(); i++) {
                        if (UpdateDeviceAdapter.getIsSelected().get(i)) {
                            String name = datasUpdate.get(i).get(MposApplication.DEVICE_NAME);
                            String mac = datasUpdate.get(i).get(MposApplication.DEVICE_MAC);
                            checkUpdateDevice.add(name+ "\n"+ mac);
                        }
                    }
                    NotUpdateDeviceAdapter(false);
                    UpdateDeviceAdapter(false);
                    if (checkUpdateDevice.size() > 0){
                        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                        if(btAdapter != null) {
                            if (!btAdapter.isEnabled()) {
                                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                getContext().startActivity(intent);
                            }else {
                                Intent intent = new Intent();
                                intent.setClass(getActivity(), DownloadActivity.class);
                                intent.putStringArrayListExtra(MposApplication.CHECK_UPDATE_DEVICE, checkUpdateDevice);
                                startActivity(intent);
                            }
                        }
                    }
                    break;
                case DELETE:
                    int i;
                    //Toast.makeText(DeviceListActivity.this, "delete pos",Toast.LENGTH_SHORT).show();
                    //NotUpdateDeviceAdapter(false);
                    //UpdateDeviceAdapter(false);
                    finish_btn.setVisibility(View.GONE);//完成按钮不可见
                    edit_btn.setVisibility(View.VISIBLE);//编辑按钮可见
                    layout_update_delete.setVisibility(View.GONE);//水平布局

                    for (i = 0; i < datasUpdate.size(); i++) {
                        LogUtils.d("UpdateDeviceAdapter.getIsSelected().get(i)):"+UpdateDeviceAdapter.getIsSelected().get(i));

                        if (UpdateDeviceAdapter.getIsSelected().get(i)) {
                            String mac = datasUpdate.get(i).get(MposApplication.DEVICE_MAC);
                            LogUtils.d("更新列表要删除的mac:"+mac);
                            databaseAdapter.rawDelete(mac);
                            datasUpdate.remove(i);
                            i--;

                        }
                    }
                    for (i = 0; i < datasNotUpdate.size(); i++) {
                        LogUtils.d("NotUpdateDeviceAdapter.getIsSelected().get(i)):"+NotUpdateDeviceAdapter.getIsSelected().get(i));
                        if (NotUpdateDeviceAdapter.getIsSelected().get(i)) {
                            String mac = (datasNotUpdate.get(i).get(MposApplication.DEVICE_MAC));
                            LogUtils.d("不更新列表要删除的mac:"+mac);
                            databaseAdapter.rawDelete(mac);
                            datasNotUpdate.remove(i);
                            i--;
                        }
                    }

                    NotUpdateDeviceAdapter(false);
                    UpdateDeviceAdapter(false);
                    updateDeviceAdapter.notifyDataSetChanged();
                    notUpdateDeviceAdapter.notifyDataSetChanged();

                    break;
                case BT_SEND_RECEIVE:
                    //获取数据库中最新数据
                    getDataFromDB();
                    initDataList();
                    break;
                case PROTIMS_SEND_RECEIVE:
                    getDataFromDB();
                    initDataList();
                    break;
                default:
                    break;
            }
        }
    };




    public DeviceListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_device_list, container, false);
        initView(view);

        return view;
    }


    private void initView(View view) {
        updateList = (ListView) view.findViewById(R.id.update_list);
        notUpdateList = (ListView) view.findViewById(R.id.not_update_list);
        edit_btn = (Button) view.findViewById(R.id.edit_btn);
        add_ImgBtn = (ImageButton) view.findViewById(R.id.add_ImgBtn);
        finish_btn = (Button) view.findViewById(R.id.finish_btn);
        update_pos_btn = (Button) view.findViewById(R.id.update_pos_btn);
        delete_pos_btn = (Button) view.findViewById(R.id.delete_pos_btn);
        layout_update_delete = (LinearLayout) view.findViewById(R.id.layout_update_delete);
        radiogroup = (RadioGroup) view.findViewById(R.id.radiogroup);
        radio_all_select = (RadioButton) view.findViewById(R.id.radio_all_select);
        radio_inverst_select = (RadioButton) view.findViewById(R.id.radio_inverst_select);

        radiogroup.setOnCheckedChangeListener(this);
        edit_btn.setOnClickListener(this);
        add_ImgBtn.setOnClickListener(this);
        finish_btn.setOnClickListener(this);
        update_pos_btn.setOnClickListener(this);
        delete_pos_btn.setOnClickListener(this);

        finish_btn.setVisibility(View.GONE);//默认不显示
        layout_update_delete.setVisibility(View.GONE);//默认不显示
    }

    public void NotUpdateDeviceAdapter(Boolean isShow) {
        notUpdateDeviceAdapter = new NotUpdateDeviceAdapter(getContext(), datasNotUpdate, isShow);
        notUpdateList.setAdapter(notUpdateDeviceAdapter);

    }

    public void UpdateDeviceAdapter(Boolean isShow) {
        updateDeviceAdapter = new UpdateDeviceAdapter(getContext(), datasUpdate, isShow);
        updateList.setAdapter(updateDeviceAdapter);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        Message msg = Message.obtain();
        switch (v.getId()) {
            case R.id.add_ImgBtn:
                layout_update_delete.setVisibility(View.GONE);//水平布局
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();//开启一个事物
                AddDeviceFragment af = new AddDeviceFragment();

                //把af放到这个id中R.id.fragmenttabhostcontent
                ft.replace(R.id.fragmenttabhostcontent, af);
                //把当前Fragment添加到Activity栈中
                ft.addToBackStack(null);
                ft.commit();//提交事务

//                intent.setClass(getContext(), AddDeviceActivity.class);
//                startActivity(intent);
                break;

            case R.id.edit_btn:
                msg.what = EDIT;
                handler.sendMessage(msg);
                break;

            case R.id.finish_btn:
                msg.what = FINISH;
                handler.sendMessage(msg);
                break;

            case R.id.update_pos_btn:
                msg.what = UPDATE;
                handler.sendMessage(msg);
                break;

            case R.id.delete_pos_btn:
                msg.what = DELETE;
                handler.sendMessage(msg);
                break;

            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int i;
        if (checkedId == radio_all_select.getId()) {
            //Toast.makeText(getContext(), "全选更新:" + datasUpdate.size() + ":" + datasNotUpdate.size(), Toast.LENGTH_SHORT).show();
            for (i = 0; i < datasNotUpdate.size(); i++) {
                NotUpdateDeviceAdapter.getIsSelected().put(i, true);
            }
            for (i = 0; i < datasUpdate.size(); i++) {
                UpdateDeviceAdapter.getIsSelected().put(i, true);
            }
            updateDeviceAdapter.notifyDataSetChanged();
            notUpdateDeviceAdapter.notifyDataSetChanged();
        } else if (checkedId == radio_inverst_select.getId()) {
            Toast.makeText(getContext(), "反选:" + datasUpdate.size() + ":" + datasNotUpdate.size(), Toast.LENGTH_SHORT).show();
            for (i = 0; i < datasUpdate.size(); i++) {
                if (UpdateDeviceAdapter.getIsSelected().get(i)) {
                    UpdateDeviceAdapter.getIsSelected().put(i, false);
                } else {
                    UpdateDeviceAdapter.getIsSelected().put(i, true);
                }
            }
            for (i = 0; i < datasNotUpdate.size(); i++) {
                if (NotUpdateDeviceAdapter.getIsSelected().get(i)) {
                    NotUpdateDeviceAdapter.getIsSelected().put(i, false);
                } else {
                    NotUpdateDeviceAdapter.getIsSelected().put(i, true);
                }
            }
            updateDeviceAdapter.notifyDataSetChanged();
            notUpdateDeviceAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        getDataFromDB();
        //网络和蓝牙均可用才访问后台和设备
        if (isNetworkAvailable() && isBluetoothAvailable()) {
            initThread();
        }
        initDataList();
    }

    public void getDataFromDB() {
        databaseAdapter = new DatabaseAdapter(getContext());
        mPoslist = new ArrayList<MPos>();
        mPoslist = databaseAdapter.rawFindAll();
        LogUtils.i("mPoslist" + mPoslist);
    }

    public boolean isBluetoothAvailable() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            return false;
        }
        if (!btAdapter.isEnabled()) {
//                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                getContext().startActivity(intent);
            LogUtils.d("蓝牙不可用");
            return false;
        } else {
            LogUtils.d("蓝牙可用");
            return true;
        }
    }

    public boolean isNetworkAvailable() {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    LogUtils.d(i + "===状态===" + networkInfo[i].getState());
                    LogUtils.d(i + "===类型===" + networkInfo[i].getTypeName());
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    //初始化列表数据
    private void initDataList() {

        initUpdataDataList();
        initNotUpdataDataList();
        //数据库数据可能在其他activity中发生改变，故在onStart阶段加载数据
        NotUpdateDeviceAdapter(false);
        UpdateDeviceAdapter(false);
    }


    private void initThread() {
//        DatabaseAdapter databaseAdapter = new DatabaseAdapter(getContext());
//        ArrayList<MPos> mPoslist = new ArrayList<MPos>();
//        mPoslist = databaseAdapter.rawFindAll();
        for (int i = 0; i < mPoslist.size(); i++) {
//            if (mPoslist.get(i).getSn() == null) {
//                Intent intent1 = new Intent(getContext(), MyIntentService.class);
//                //Intent intent1 = new Intent(getContext(), MyService.class);
//                intent1.putExtra(MposApplication.DEVICE_MAC, mPoslist.get(i).getMac());
//                intent1.putExtra(MposApplication.DEVICE_NAME, mPoslist.get(i).getName());
//                getContext().startService(intent1);
//            }
            //不更新的设备访问protims后台获取更新状态
            //sn为空的设备，连接蓝牙获取sn
//            if (!mPoslist.get(i).getIsupdate().equals("10") || mPoslist.get(i).getSn() == null){
//                //getContext().startService(intent1);
//                MyThread myThread = new MyThread(getContext(), mPoslist.get(i).getMac(),
//                        mPoslist.get(i).getName(), mPoslist.get(i).getSn());
//                myThread.start();
//            }

            //不更新的设备访问protims后台获取更新状态
            if (mPoslist.get(i).getIsupdate() == null || !mPoslist.get(i).getIsupdate().equals("10")) {
//                Thread protimeThread = new Thread(new ProtimsThread(getContext()));
//                protimeThread.start();
                LogUtils.i("ProtimsThread....");
                //new Thread(new ProtimsRunnable(getContext(), mPoslist.get(i), handler)).start();
                //ExecutorService es = Executors.newSingleThreadExecutor();
                //esTcp.execute(new ProtimsRunnable(getContext(), mPoslist.get(i).getMac(), handler));
                new Thread(new ProtimsRunnable(getContext(), mPoslist.get(i).getMac(), handler)).start();
            }

            String sn = mPoslist.get(i).getSn();
            LogUtils.d("sn is null"+sn);
            //sn为空的设备，连接蓝牙获取sn
            if ( sn == null) {
                LogUtils.d("sn is null two"+sn);
                Thread btThread = new Thread(new BtRunnable(getContext(), mPoslist.get(i), handler));
                btThread.start();

//                esBt.execute(new BtRunnable(getContext(), mPoslist.get(i), handler));
            }

        }
    }

    private static class ProtimsRunnable implements Runnable {
        private Context context;
        private String mac;
        Handler handler;

        public ProtimsRunnable(Context context, String mac, Handler handler) {
            this.context = context;
            this.mac = mac;
            this.handler = handler;
        }

        @Override
        public void run() {
            SharedPreferences sp;
            String server_ip;
            String server_port;
            String key_tid;
            CommTcpip commTcpip;
            MposSDK mposSDK;
            MPos mPos;
            int isupdate = 0;
            DatabaseAdapter databaseAdapter;
            LogUtils.d("当前线程:" + Thread.currentThread());
            sp = context.getSharedPreferences(ServerSetActivity.SHARED_MAIN, Context.MODE_PRIVATE);

            server_ip = sp.getString(ServerSetActivity.KEY_SERVER_IP, null);
            server_port = sp.getString(ServerSetActivity.KEY_SERVER_PORT, null);
            key_tid = sp.getString(ServerSetActivity.KEY_TID, null);
            if (server_ip == null) {
                server_ip = "192.168.0.136";
            }
            if (server_port == null) {
                server_port = "8580";
            }
            if (key_tid == null) {
                key_tid = "00000001";
            }

            commTcpip = new CommTcpip(server_ip, Integer.valueOf(server_port));

            mposSDK = new MposSDK();
            mposSDK.setTermId(key_tid);
            mposSDK.setTmsCommunicator(commTcpip);

            LogUtils.d("服务器地址:" + server_ip);
            LogUtils.d("服务器端口:" + server_port);
            LogUtils.d("TID:" + key_tid);

            mposSDK.initEnv();
            LogUtils.d("isUpdate befor...");

            //获取更新状态只需要连接TMS后台，不需要连接Mpos
            //只跟TID有关系
            isupdate = mposSDK.isUpdate();
            LogUtils.d("当前线程:" + Thread.currentThread());
            LogUtils.d("设备是否更新 isupdate:" + isupdate);
            //commTcpip.close();

            databaseAdapter = new DatabaseAdapter(context);
            databaseAdapter.rawUpdateIsupdata(mac, String.valueOf(isupdate));

            LogUtils.d("mposSDK.release befor");
            mposSDK.release();
            LogUtils.d("mposSDK.release end");
            //发送接收标志，更新list数据
            Message msg = handler.obtainMessage();
            msg.what = PROTIMS_SEND_RECEIVE;
            handler.sendMessage(msg);

            ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();
            LogUtils.i("数据库更新后:" + mPoslist);

        }
    }

    private static class BtRunnable implements Runnable {
        private Context context;
        private MPos oldMpos;
        Handler handler;

        public BtRunnable(Context context, MPos oldMpos, Handler handler) {
            this.context = context;
            this.oldMpos = oldMpos;
            this.handler = handler;

        }

        @Override
        public void run() {
            byte[] termVerInfo = new byte[8 + 1];
            byte[] termSN = new byte[8 + 1];
            byte[] terminalInfo = new byte[30 + 1];
            CommBluetooth commBluetooth;
            MposSDK mposSDK;
            MPos mPos;
            DatabaseAdapter databaseAdapter;

            LogUtils.d("当前线程:" + Thread.currentThread());
            commBluetooth = new CommBluetooth(oldMpos.getMac());
            databaseAdapter = new DatabaseAdapter(context);
            mposSDK = new MposSDK();
            mposSDK.setMposCommunicator(commBluetooth);
            mposSDK.initEnv();

            LogUtils.i("连接蓝牙设备中...");

            if (commBluetooth.connect()) {
                LogUtils.i("蓝牙连接成功...");
                LogUtils.d("当前线程:" + Thread.currentThread());

                //接收处已经开了一个线程
//                termSN = mposSDK.getTermSN();
//                LogUtils.i("终端sn为:" + new Utils().AsciiStringToString(new Utils().bcd2Str(termSN)));
                int iRet = mposSDK.getTermSn(termSN);
                LogUtils.i("getTermSn iRet:" + iRet);
                if (iRet == 0) {
                    LogUtils.i("终端sn为:" + new Utils().AsciiStringToString(new Utils().bcd2Str(termSN)));
                    termVerInfo = mposSDK.getTermVerInfo();
                    LogUtils.i("终端版本为:" + new Utils().bcd2Str(termVerInfo));
//                terminalInfo = mposSDK.getTerminalInfo();
//                LogUtils.i("终端信息为:" + new Utils().bcd2Str(termVerInfo));

                    if (termVerInfo[2] > 9) {
                        mPos = new MPos(oldMpos.getMac(), oldMpos.getName(), new Utils().AsciiStringToString(new Utils().bcd2Str(termSN)),
                                oldMpos.getPn(), String.valueOf(termVerInfo[1]) + "." + String.valueOf(termVerInfo[2]),
                                String.valueOf(termVerInfo[0]), oldMpos.getBattery());
                    } else {
                        mPos = new MPos(oldMpos.getMac(), oldMpos.getName(), new Utils().AsciiStringToString(new Utils().bcd2Str(termSN)),
                                null, String.valueOf(termVerInfo[1]) + ".0" + String.valueOf(termVerInfo[2]),
                                String.valueOf(termVerInfo[0]), oldMpos.getBattery());
                    }
                    databaseAdapter.rawUpdate(mPos);

                    //发送接收标志，更新list数据
                    Message msg = handler.obtainMessage();
                    msg.what = BT_SEND_RECEIVE;
                    handler.sendMessage(msg);
                }

            }

            commBluetooth.close();
            mposSDK.release();
            ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();
            LogUtils.i("数据库更新后:" + mPoslist);

        }
    }

//    private class MyThread extends Thread {
//        private Context context;
//        private String mac;
//        private String name;
//        private String sn;
//
//        public MyThread(Context context, String mac, String name, String sn) {
//            this.context = context;
//            this.mac = mac;
//            this.name = name;
//            this.sn = sn;
//
//        }
//
//        @Override
//        public void run() {
//            SharedPreferences sp;
//            String server_ip;
//            String server_port;
//            String key_tid;
//            byte[] termVerInfo = new byte[8+1];
//            byte[] termSN = new byte[8+1];
//            byte[] terminalInfo = new byte[30+1];
//            CommTcpip commTcpip;
//            CommBluetooth commBluetooth;
//            MposSDK mposSDK;
//            MPos mPos;
//            int isupdate = 0;
//
//            sp = context.getSharedPreferences(ServerSetActivity.SHARED_MAIN, Context.MODE_PRIVATE);
//
//            server_ip = sp.getString(ServerSetActivity.KEY_SERVER_IP, null);
//            server_port = sp.getString(ServerSetActivity.KEY_SERVER_PORT, null);
//            key_tid = sp.getString(ServerSetActivity.KEY_TID, null);
//            if (server_ip == null){
//                server_ip = "192.168.0.136";
//            }
//            if (server_port == null){
//                server_port = "8580";
//            }
//            if (key_tid == null){
//                key_tid = "00000001";
//            }
//
//            LogUtils.i("BtThread 中线程id:" + Thread.currentThread());
//            commTcpip = new CommTcpip(server_ip,
//                    Integer.valueOf(server_port));
//            commBluetooth = new CommBluetooth(mac);
//
//            mposSDK = new MposSDK();
//            mposSDK.setTermId(key_tid);
//            mposSDK.setMposCommunicator(commBluetooth);
//            mposSDK.setTmsCommunicator(commTcpip);
//
//            LogUtils.d("服务器地址:"+server_ip);
//            LogUtils.d("服务器端口:"+server_port);
//            LogUtils.d("TID:"+key_tid);
//
//            mposSDK.initEnv();
//            LogUtils.d("isUpdate befor...");
//
//            //获取更新状态只需要连接TMS后台，不需要连接Mpos
//            //只跟TID有关系
//            isupdate = mposSDK.isUpdate();
//            LogUtils.d("设备是否更新 isupdate:" + isupdate);
//            commTcpip.close();
//
//            DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
//            ArrayList<MPos> mPoslistbefor = databaseAdapter.rawFindAll();
//            LogUtils.i("数据库之前:" + mPoslistbefor);
//            //(String mac, String name, String sn, String pn, String os_version, String boot_version,battery)
//
//            if (sn == null){
//                LogUtils.i("连接蓝牙设备中...");
//                if (commBluetooth.connect()){
//                    termVerInfo = mposSDK.getTermVerInfo();
//                    LogUtils.i("终端版本为:" + new Utils().bcd2Str(termVerInfo));
//
//                    //接收处已经开了一个线程
//                    termSN = mposSDK.getTermSN();
//                    LogUtils.i("终端sn为:" + new Utils().AsciiStringToString(new Utils().bcd2Str(termSN)));
//
////                terminalInfo = mposSDK.getTerminalInfo();
////                LogUtils.i("终端信息为:" + new Utils().bcd2Str(termVerInfo));
//
//                    if (termVerInfo[2] > 9) {
//                        mPos = new MPos(mac, name, new Utils().AsciiStringToString(new Utils().bcd2Str(termSN)),
//                                null, String.valueOf(termVerInfo[1]) + "." + String.valueOf(termVerInfo[2]),
//                                String.valueOf(termVerInfo[0]), null, String.valueOf(isupdate));
//                    } else {
//                        mPos = new MPos(mac, name, new Utils().AsciiStringToString(new Utils().bcd2Str(termSN)),
//                                null, String.valueOf(termVerInfo[1]) + ".0" + String.valueOf(termVerInfo[2]),
//                                String.valueOf(termVerInfo[0]), null, String.valueOf(isupdate));
//                    }
//                    databaseAdapter.rawUpdate(mPos);
//                    commBluetooth.close();
//                }
//            }else {
//                mPos = new MPos(String.valueOf(isupdate));
//                databaseAdapter.rawUpdate(mPos);
//            }
//
//
//            ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();
//            LogUtils.i("数据库更新后:"+ mPoslist);
//
//        }
//    }

    //初始化更新列表数据
    private void initUpdataDataList() {
        datasUpdate = new ArrayList<HashMap<String, String>>();
//        DatabaseAdapter databaseAdapter = new DatabaseAdapter(getContext());
//        ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();

        for (int i = 0; i < mPoslist.size(); i++) {
            HashMap<String, String> items = new HashMap<>();
            //获取更新的pos列表
            if (mPoslist.get(i).getIsupdate() != null && mPoslist.get(i).getIsupdate().equals("10")) {
                items.put(MposApplication.DEVICE_NAME, mPoslist.get(i).getName());
                items.put(MposApplication.DEVICE_MAC, mPoslist.get(i).getMac());
                items.put("version", "1.0." + i);
                items.put("size", "25." + i + "M");
                items.put("hideArea_tv1", "1、微博评论全面升级，焕然一新" + i);
                items.put("hideArea_tv2", "2、全新发现，精彩内容，触手可得" + i);
                items.put("hideArea_tv3", "3、适配ios10，可直接分享适配到微博" + i);
                datasUpdate.add(items);
            }

        }

    }

    //初始化无更新列表数据
    private void initNotUpdataDataList() {
        datasNotUpdate = new ArrayList<HashMap<String, String>>();
//        DatabaseAdapter databaseAdapter = new DatabaseAdapter(getContext());
//        ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();

        for (int i = 0; i < mPoslist.size(); i++) {
            HashMap<String, String> items = new HashMap<>();
            //获取不可更新的pos列表
            if (mPoslist.get(i).getIsupdate() == null || !mPoslist.get(i).getIsupdate().equals("10")) {
                items.put(MposApplication.DEVICE_NAME, mPoslist.get(i).getName());
                items.put(MposApplication.DEVICE_MAC, mPoslist.get(i).getMac());
                items.put("version", "1.0." + i);
                items.put("size", "25." + i + "M");
                items.put("hideArea_tv1", "1、微博评论全面升级，焕然一新" + i);
                items.put("hideArea_tv2", "2、全新发现，精彩内容，触手可得" + i);
                items.put("hideArea_tv3", "3、适配ios10，可直接分享适配到微博" + i);
                datasNotUpdate.add(items);
            }
        }
    }
}
