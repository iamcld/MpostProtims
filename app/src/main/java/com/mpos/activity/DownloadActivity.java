package com.mpos.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.example.chenld.mpostprotimstest.R;
import com.mpos.MposApplication;
import com.mpos.db.DatabaseAdapter;
import com.mpos.sdk.MposSDK;
import com.mpos.UpdateModel;
import com.pax.utils.MyLog;
import com.mpos.adapter.DownloadAdapter;
import com.mpos.communication.CommBluetooth;
import com.mpos.communication.CommTcpip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadActivity extends Activity {
    private static final String TAG = "DownloadActivity";

    private ListView listview;
    private DownloadAdapter adapter;
    private ArrayList<String> devices; // item  = name + "\n" + mac
    //private HashMap<String, String> devices;
    private Map<String, Integer> statuses;
    private ArrayList<MposSDK> loaders;
    private Map<String, Integer> progresses;
    private BluetoothAdapter btAdapter;
    private ArrayList<BluetoothDevice> btScannedDevs;
    private Thread[] downloadThreads;
   // private String name;
    //private String mac;
    private SharedPreferences sp;
    private String server_ip;
    private String server_port;
    private String key_id;
    private ExecutorService esDownload = Executors.newSingleThreadExecutor();

    /**
     * handler created in UI main thread, waiting to receive sub-thread download progress message
     */
    private Handler handler = new UIHander();


    private final class UIHander extends Handler {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String btMac = bundle.getString("mac");
            int type = bundle.getInt("type");
            int step = bundle.getInt("step");
            int doneFlag = bundle.getInt("doneFlag");
            int fileCount = bundle.getInt("fileCount");
            int curFile = bundle.getInt("curFile");
            int total = bundle.getInt("total");
            int cur = bundle.getInt("cur");
            int curFileSize = bundle.getInt("curFileSize");
            int status = bundle.getInt("status");
            int value = 0;
            LogUtils.d("type=" + type + ", doneFlag=" + doneFlag + ", fileCount=" + fileCount + ", curFile=" + curFile + ", step=" + step + ", total=" + total + ", cur=" + cur + ", curFileSize=" + curFileSize + ", status=" + status);
            statuses.put(btMac, status);
            if (0 != total) {
                LogUtils.d("UI UPDATE VALUE : cur=" + cur + " ,total=" + total + " ,rate=" + cur * 100 / total);
                progresses.put(btMac, cur * 100 / total);
                value = cur * 100 / total;
            } else {
                progresses.put(btMac, 0);
                value = 0;
            }
            switch (msg.what) {
                case 1:
                    for (int i = 0; i < btScannedDevs.size(); i++) {
                        View view = listview.getChildAt(i);
                        LogUtils.d("key:"+i+" view:"+view);


                        if (view != null && view.getTag() != null && view.getTag() instanceof DownloadAdapter.ViewCacher) {
                            DownloadAdapter.ViewCacher vh = (DownloadAdapter.ViewCacher) view.getTag();
                            String mac = vh.textView.getText().toString();

                            LogUtils.d("vh.textView:"+mac);
                            LogUtils.d("btMac:"+btMac);
                            int flag = mac.indexOf(btMac);
                            LogUtils.d("flag="+flag);

                            if (mac.indexOf(btMac) != -1){
                                //vh.progressBar.setProgress(progresses.get(btScannedDevs.get(i).getAddress()));
                                vh.progressBar.setProgress(value);
                                vh.value.setText("" + value + "%");
                                String typeDoneFlagStep = getTypeDoneFlagStepText(type, doneFlag, step, status, total, cur);
                                vh.type_doneFlag_step.setText(typeDoneFlagStep);
                                String fileCountCurFile = getFileCountCurFileText(fileCount, curFile);
                                vh.fileCount_curFile.setText(fileCountCurFile);
                                String txStatus = "";
                                if (status == 0) {
                                    // txStatus = getString(R.string.status_ok);
                                    txStatus = "";
                                    if (cur == total && total != 0 && type == 2) {
                                        txStatus = getString(R.string.download_success);
                                    }
                                } else
                                    txStatus = getString(R.string.status_fail);
                                vh.status.setText(txStatus);
                            }
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences(ServerSetActivity.SHARED_MAIN, Context.MODE_PRIVATE);

        server_ip = sp.getString(ServerSetActivity.KEY_SERVER_IP, null);
        server_port = sp.getString(ServerSetActivity.KEY_SERVER_PORT, null);
        key_id = sp.getString(ServerSetActivity.KEY_TID, null);
        if (server_ip == null){
            server_ip = "192.168.0.1";
        }
        if (server_port == null){
            server_port = "9999";
        }
        if (key_id == null){
            key_id = "00000000";
        }
        devices = getIntent().getStringArrayListExtra(MposApplication.CHECK_UPDATE_DEVICE);
        LogUtils.d("选中的更新列表:"+devices);

//        name = getIntent().getStringExtra(MposApplication.DEVICE_NAME);
//        mac = getIntent().getStringExtra(MposApplication.DEVICE_MAC);
        //devices = getIntent().getParcelableArrayListExtra("devices");
        if (devices == null || devices.size() <= 0) {
            Toast.makeText(getApplicationContext(), R.string.no_devices_error, Toast.LENGTH_SHORT).show();
            return;
        }
        statuses = new HashMap<String, Integer>();
        progresses = new HashMap<String, Integer>();
        listview = (ListView) findViewById(R.id.listview);
        adapter = new DownloadAdapter(this);

//        UpdateModel model = new UpdateModel();
//        model.setBtDevName(name);
//        model.setProgress(0);
//        model.setBtDevMac(mac);
//        adapter.addData(model);

        for(int i=0; i<devices.size(); i++){
            String[] info = devices.get(i).split("\n");
            if(info.length==2 && info[1]!=null && !info[1].trim().equals(""))
            {
                UpdateModel model = new UpdateModel();
                model.setBtDevName(info[0]);
                model.setProgress(0);
                model.setBtDevMac(info[1]);
                adapter.addData(model);
            }
        }

        listview.setAdapter(adapter);

        loaders = new ArrayList<MposSDK>();
        btScannedDevs = new ArrayList<BluetoothDevice>();
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        for (String s : devices) {
            String[] info = s.split("\n");
            if(info.length==2 && info[1]!=null && !info[1].trim().equals(""))
            {
                BluetoothDevice btDev = btAdapter.getRemoteDevice(info[1]);
                if(null != btDev && btDev.getBondState() == BluetoothDevice.BOND_BONDED)
                {
                    btScannedDevs.add(btDev);
                    statuses.put(info[1], 0); // 0-init
                    progresses.put(info[1], 0);
                }
            }
        }
    }

    public String getFileCountCurFileText(int fileCount, int curFile) {
        return getString(R.string.file) + fileCount + " files in total, current file : " + curFile;
    }

    public String getTypeDoneFlagStepText(int type, int doneFlag, int step, int status, int total, int cur) {
        String txMpos = getString(R.string.type_mpos);
        String txTms = getString(R.string.type_tms);
        String txUnknown = getString(R.string.type_unknown);
        String txStart = getString(R.string.done_start);
        String txDoing = getString(R.string.done_doing);
        String txFinish = getString(R.string.done_finish);
        String ret = getString(R.string.action);
        if (1 == type) {
            ret += txTms;
        } else if (2 == type) {
            ret += txMpos;
        } else {
            ret += txUnknown;
        }
        ret += " ";

        if (1 == doneFlag) {
            ret += txStart;
        } else if (2 == doneFlag) {
            ret += txDoing;
        } else if (3 == doneFlag) {
            ret += txFinish;
        }
        ret += " ";

        int stepId = 0;
        if (type == 1) {
            switch (step) {
                case 101: // STEP_TMS_CONNECT
                    stepId = R.string.step_tms_connect;
                    break;
                case 102: // STEP_TMS_HANDSHAKE
                    stepId = R.string.step_tms_handshake;
                    break;
                case 103: // STEP_TMS_TERM_AUTH
                    stepId = R.string.step_tms_term_auth;
                    break;
                case 104: // STEP_TMS_REQ_UPLOAD
                    stepId = R.string.step_tms_req_upload;
                    break;
                case 105: // STEP_TMS_UPLOAD
                    stepId = R.string.step_tms_upload;
                    break;
                case 106: // STEP_TMS_MULTI_TASK
                    stepId = R.string.step_tms_multi_task;
                    break;
                case 107: // STEP_TMS_PARSE_TASK_TABLE
                    stepId = R.string.step_tms_parse_task_table;
                    break;
                case 108: // STEP_TMS_PARSE_TASK_TABLE2
                    stepId = R.string.step_tms_parse_task_table2;
                    break;
                case 109: // STEP_TMS_COMPARE_TASK_LOCAL_TMS
                    stepId = R.string.step_tms_compare_task_local_tms;
                    break;
                case 110: // STEP_TMS_DELETE_TASK_LOCAL
                    stepId = R.string.step_tms_delete_task_local;
                    break;
                case 111: // STEP_TMS_SAVE_TASK_LOCAL
                    stepId = R.string.step_tms_save_task_local;
                    break;
                case 112: // STEP_TMS_LOAD_FILE
                    stepId = R.string.step_tms_load_file;
                    break;
                case 113: // STEP_TMS_TERM_ID
                    stepId = R.string.step_tms_term_id;
                    break;
                default:
                    break;
            }
        } else if (2 == type) {
            switch (step) {
                case 201: // STEP_MPOS_CONNECT
                    stepId = R.string.step_mpos_connect;
                    break;
                case 202: // STEP_MPOS_TERM_INFO
                    stepId = R.string.step_mpos_term_info;
                    break;
                case 203: // STEP_MPOS_SN
                    stepId = R.string.step_mpos_sn;
                    break;
                case 204: // STEP_MPOS_EXSN
                    stepId = R.string.step_mpos_exsn;
                    break;
                case 205: // STEP_MPOS_VERN_INFO
                    stepId = R.string.step_mpos_vern_info;
                    break;
                case 206: // STEP_MPOS_SET_TASK
                    stepId = R.string.step_mpos_set_task;
                    break;
                case 207: // STEP_MPOS_SAVE_FILE
                    stepId = R.string.step_mpos_save_file;
                    break;
                default:
                    break;
            }
        }

        String txStep = getString(stepId);
        ret += txStep;

        ret += ", " + cur + "/" + total;

        if (status != 0) {
            ret += getString(R.string.execute_fail) + status;
        }

        return ret;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), R.string.sdcard_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (btScannedDevs.size() == 0) {
            Toast.makeText(getApplicationContext(), R.string.none_selected, Toast.LENGTH_SHORT).show();
            return;
        } else{ // only one bluetooth device
            //downloadThreads = new Thread[btScannedDevs.size()];
            for (int i = 0; i < btScannedDevs.size(); i++){
//                downloadThreads[i] = new Thread(new DownloadRunnable(loaders, sp, handler, btScannedDevs,
//                        server_port, server_ip, i, this));
//                downloadThreads[i].start();
                //单线程池
                esDownload.execute(new DownloadRunnable(loaders, sp, handler, btScannedDevs,
                       server_port, server_ip, i, this));

//                downloadThreads[i] = new Thread(new Runnable() {
//
//                    @Override
//                    public void run() {
//
//                        DatabaseAdapter databaseAdapter;
//                        CommTcpip commTcpip = new CommTcpip(server_ip,
//                                Integer.valueOf(server_port));
//                        CommBluetooth commBluetooth = new CommBluetooth(btScannedDevs.get(i).getAddress());
//
//                        MposSDK loader = new MposSDK(new com.mpos.sdk.IProgressListener() {
//                            @Override
//                            public void onDownloadSize(int type, int step, int doneFlag, int fileCount, int curFile,
//                                                       int total, int cur, int curFileSize, int status) {
//                                Message msg = new Message();
//                                msg.what = 1;
//                                msg.getData().putString("mac", btScannedDevs.get(0).getAddress());
//                                msg.getData().putInt("type", type);
//                                msg.getData().putInt("step", step);
//                                msg.getData().putInt("doneFlag", doneFlag);
//                                msg.getData().putInt("fileCount", fileCount);
//                                msg.getData().putInt("curFile", curFile);
//                                msg.getData().putInt("total", total);
//                                msg.getData().putInt("cur", cur);
//                                msg.getData().putInt("curFileSize", curFileSize);
//                                msg.getData().putInt("status", status);
//                                handler.sendMessage(msg);
//                            }
//                        });
//
//                        loader.setTermId(sp.getString(ServerSetActivity.KEY_TID, null));
//                        loader.setMposCommunicator(commBluetooth);
////					boolean bConnMpos = false;
////					bConnMpos = loader.connectMpos();
////					Log.e(TAG, "Connect MPOS : " + bConnMpos);
////					if(!bConnMpos){
////						return;
////					}
//                        loader.setTmsCommunicator(commTcpip);
////					boolean bConnTms = loader.connectTms();
////					Log.e(TAG, "Connect TMS : " + bConnTms);
////					if(!bConnTms){
////						return;
////					}
//                        synchronized (loaders) {
//                            loaders.add(loader);
//                        }
//
//                        int iRet = 0;
//                        iRet = loader.initEnv();
//                        iRet = loader.download();
//                        LogUtils.d("loader.download return:"+iRet);
//                        if (10 != iRet) {
//                            return;
//                        }
//
//                        loader.release();
//                        LogUtils.d("更新成功，更新数据库标志");
//                        //更新成功，更新数据库标志
//                        databaseAdapter = new DatabaseAdapter(getApplicationContext());
//
//                        LogUtils.d("数据库原数据:"+databaseAdapter.rawFindAll());
//                        //标记为不更新
//                        databaseAdapter.rawUpdateIsupdata(mac, "0");
//                        LogUtils.d("数据库修改后数据:"+databaseAdapter.rawFindAll());
//
//                        try {
//                            Thread.sleep(1);
//                        } catch (InterruptedException e) {
//                            //e.printStackTrace();
//                            LogUtils.d( "++++++++++++++++++++++++++++++++++");
//                            LogUtils.d("++++++++++++++++++++++++++++++++++");
//                        }
//                    }
//                });
//                downloadThreads[i].start();
            }

        }
    }


    public void setProgress(UpdateModel model, int position) {
        int first = listview.getFirstVisiblePosition();
        int last = listview.getLastVisiblePosition();
        if (position >= first && position <= last) {
            View view = listview.getChildAt(position - first);
            if (view.getTag() instanceof DownloadAdapter.ViewCacher) {
                DownloadAdapter.ViewCacher vh = (DownloadAdapter.ViewCacher) view.getTag();
                // TODO : if mac equal
                vh.progressBar.setProgress(model.getProgress());
            }
        }
    }

    private static class DownloadRunnable implements Runnable{
        int i;
        String server_ip;
        String server_port;
        ArrayList<BluetoothDevice> btScannedDevs;
        Handler handler;
        SharedPreferences sp;
        ArrayList<MposSDK> loaders;
        Context context;

        public DownloadRunnable(ArrayList<MposSDK> loaders, SharedPreferences sp, Handler handler,
                                ArrayList<BluetoothDevice> btScannedDevs, String server_port,
                                String server_ip, int i, Context context) {
            this.loaders = loaders;
            this.sp = sp;
            this.handler = handler;
            this.btScannedDevs = btScannedDevs;
            this.server_port = server_port;
            this.server_ip = server_ip;
            this.i = i;
            this.context = context;
        }

        @Override
        public void run() {
            DatabaseAdapter databaseAdapter;
            CommTcpip commTcpip = new CommTcpip(server_ip,
                    Integer.valueOf(server_port));
            CommBluetooth commBluetooth = new CommBluetooth(btScannedDevs.get(i).getAddress());
            LogUtils.d("btScannedDevs.get(i).getAddress():"+btScannedDevs.get(i).getAddress());
            LogUtils.d("线程commTcpip对象:"+commTcpip);
            LogUtils.d("线程commBluetooth对象:"+commBluetooth);

            MposSDK loader = new MposSDK(new com.mpos.sdk.IProgressListener() {
                @Override
                public  void onDownloadSize(int type, int step, int doneFlag, int fileCount, int curFile,
                                           int total, int cur, int curFileSize, int status) {
                    Message msg = new Message();
                    msg.what = 1;
                    LogUtils.d("key:"+i);
                    msg.getData().putInt("key", i);
                    msg.getData().putString("mac", btScannedDevs.get(i).getAddress());
                    msg.getData().putInt("type", type);
                    msg.getData().putInt("step", step);
                    msg.getData().putInt("doneFlag", doneFlag);
                    msg.getData().putInt("fileCount", fileCount);
                    msg.getData().putInt("curFile", curFile);
                    msg.getData().putInt("total", total);
                    msg.getData().putInt("cur", cur);
                    msg.getData().putInt("curFileSize", curFileSize);
                    msg.getData().putInt("status", status);
                    handler.sendMessage(msg);
                }
            });

            loader.setTermId(sp.getString(ServerSetActivity.KEY_TID, null));
            loader.setMposCommunicator(commBluetooth);
//					boolean bConnMpos = false;
//					bConnMpos = loader.connectMpos();
//					Log.e(TAG, "Connect MPOS : " + bConnMpos);
//					if(!bConnMpos){
//						return;
//					}
            loader.setTmsCommunicator(commTcpip);
//					boolean bConnTms = loader.connectTms();
//					Log.e(TAG, "Connect TMS : " + bConnTms);
//					if(!bConnTms){
//						return;
//					}
            synchronized (loaders) {
                loaders.add(loader);
            }

            int iRet = 0;
            iRet = loader.initEnv();
            iRet = loader.download();
            LogUtils.d("loader.download return:"+iRet);
            commTcpip.close();
            commBluetooth.close();

            if (10 != iRet) {
                return;
            }


            loader.release();
            LogUtils.d("更新成功，更新数据库标志");
            //更新成功，更新数据库标志
            databaseAdapter = new DatabaseAdapter(context);

            LogUtils.d("数据库原数据:"+databaseAdapter.rawFindAll());
            //标记为不更新
            databaseAdapter.rawUpdateIsupdata(btScannedDevs.get(i).getAddress(), "0");
            LogUtils.d("数据库修改后数据:"+databaseAdapter.rawFindAll());

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                LogUtils.d( "++++++++++++++++++++++++++++++++++");
                LogUtils.d("++++++++++++++++++++++++++++++++++");
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            for (int i = 0; i < loaders.size(); i++){
                loaders.get(0).setInterrupt(true);
            }

            btAdapter.disable();
            Toast.makeText(getApplicationContext(), getString(R.string.return_tips), Toast.LENGTH_LONG).show();
            finish();
        }
        return false;
    }

}
