package com.mpos.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mpos.MposApplication;
import com.mpos.db.DatabaseAdapter;
import com.mpos.db.MPos;
import com.example.chenld.mpostprotimstest.R;
import com.mpos.adapter.NotUpdateDeviceAdapter;
import com.mpos.adapter.UpdateDeviceAdapter;

import java.util.ArrayList;
import java.util.HashMap;




public class DeviceListActivity extends Activity implements View.OnClickListener,RadioGroup.OnCheckedChangeListener{

    private final static int EDIT   = 0x1;
    private final static int FINISH = 0x2;
    private final static int UPDATE = 0x3;
    private final static int DELETE = 0x4;

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

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
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
                    Toast.makeText(DeviceListActivity.this, "update pos",Toast.LENGTH_SHORT).show();
                    NotUpdateDeviceAdapter(false);
                    UpdateDeviceAdapter(false);
                    finish_btn.setVisibility(View.GONE);//完成按钮不可见
                    edit_btn.setVisibility(View.VISIBLE);//编辑按钮可见
                    layout_update_delete.setVisibility(View.GONE);//水平布局
                    break;
                case DELETE:
                    Toast.makeText(DeviceListActivity.this, "delete pos",Toast.LENGTH_SHORT).show();
                    NotUpdateDeviceAdapter(false);
                    UpdateDeviceAdapter(false);
                    finish_btn.setVisibility(View.GONE);//完成按钮不可见
                    edit_btn.setVisibility(View.VISIBLE);//编辑按钮可见
                    layout_update_delete.setVisibility(View.GONE);//水平布局
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        initView();

    }

    private void initView(){
        updateList = (ListView) findViewById(R.id.update_list);
        notUpdateList = (ListView)findViewById(R.id.not_update_list);
        edit_btn = (Button) findViewById(R.id.edit_btn);
        add_ImgBtn = (ImageButton) findViewById(R.id.add_ImgBtn);
        finish_btn = (Button) findViewById(R.id.finish_btn);
        update_pos_btn = (Button) findViewById(R.id.update_pos_btn);
        delete_pos_btn = (Button) findViewById(R.id.delete_pos_btn);
        layout_update_delete = (LinearLayout) findViewById(R.id.layout_update_delete);
        radiogroup = (RadioGroup) findViewById(R.id.radiogroup);
        radio_all_select = (RadioButton) findViewById(R.id.radio_all_select);
        radio_inverst_select = (RadioButton) findViewById(R.id.radio_inverst_select);

        radiogroup.setOnCheckedChangeListener(this);
        edit_btn.setOnClickListener(this);
        add_ImgBtn.setOnClickListener(this);
        finish_btn.setOnClickListener(this);
        update_pos_btn.setOnClickListener(this);
        delete_pos_btn.setOnClickListener(this);

        finish_btn.setVisibility(View.GONE);//默认不显示
        layout_update_delete.setVisibility(View.GONE);//默认不显示
    }

    public void NotUpdateDeviceAdapter(Boolean isShow){

        notUpdateDeviceAdapter = new NotUpdateDeviceAdapter(this, datasNotUpdate, isShow);
        notUpdateList.setAdapter(notUpdateDeviceAdapter);

    }

    public void UpdateDeviceAdapter(Boolean isShow){
        updateDeviceAdapter = new UpdateDeviceAdapter(this, datasUpdate, isShow, true);
        updateList.setAdapter(updateDeviceAdapter);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        Message msg = Message.obtain();
        Toast.makeText(this, "...",Toast.LENGTH_SHORT).show();
        switch (v.getId()){
            case R.id.add_ImgBtn:
                layout_update_delete.setVisibility(View.GONE);//水平布局
                Toast.makeText(this, "add",Toast.LENGTH_SHORT).show();
                intent.setClass(this, AddDeviceActivity.class);
                startActivity(intent);
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
    protected void onStart() {
        super.onStart();
        initDataList();
        //数据库数据可能在其他activity中发生改变，故在onStart阶段加载数据
        NotUpdateDeviceAdapter(false);
        UpdateDeviceAdapter(false);
    }

    //初始化列表数据
    private void initDataList(){
        initUpdataDataList();
        initNotUpdataDataList();
    }
    //初始化更新列表数据
    private void initUpdataDataList(){
        datasUpdate = new ArrayList<>();
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(this);
        ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();

        for (int i = 0; i < mPoslist.size(); i++){
            HashMap<String, String> items = new HashMap<>();
            items.put(MposApplication.DEVICE_NAME,mPoslist.get(i).getName() );
            items.put(MposApplication.DEVICE_MAC, mPoslist.get(i).getMac());
            items.put("version","1.0."+i);
            items.put("size","25."+i+"M");
            items.put("hideArea_tv1","1、微博评论全面升级，焕然一新"+i);
            items.put("hideArea_tv2","2、全新发现，精彩内容，触手可得"+i);
            items.put("hideArea_tv3","3、适配ios10，可直接分享适配到微博"+i);
            datasUpdate.add(items);
        }
//        datasUpdate = new ArrayList<HashMap<String, String>>();
//        for(int i=0; i<10; i++){
//            HashMap<String, String> items = new HashMap<String, String>();
//            items.put(MposApplication.DEVICE_NAME,"D180-1314000"+i );
//            items.put(MposApplication.DEVICE_MAC, "00:00:00:00:00:"+1);
//            items.put("version","1.0."+i);
//            items.put("size","25."+i+"M");
//            items.put("hideArea_tv1","1、微博评论全面升级，焕然一新"+i);
//            items.put("hideArea_tv2","2、全新发现，精彩内容，触手可得"+i);
//            items.put("hideArea_tv3","3、适配ios10，可直接分享适配到微博"+i);
//            datasUpdate.add(items);
//        }

    }

    //初始化无更新列表数据
    private void initNotUpdataDataList(){
        datasNotUpdate = new ArrayList<>();
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(this);
        ArrayList<MPos> mPoslist = databaseAdapter.rawFindAll();

        for (int i = 0; i < mPoslist.size(); i++){
            HashMap<String, String> items = new HashMap<>();
            items.put(MposApplication.DEVICE_NAME,mPoslist.get(i).getName() );
            items.put(MposApplication.DEVICE_MAC, mPoslist.get(i).getMac());
            items.put("version","1.0."+i);
            items.put("size","25."+i+"M");
            items.put("hideArea_tv1","1、微博评论全面升级，焕然一新"+i);
            items.put("hideArea_tv2","2、全新发现，精彩内容，触手可得"+i);
            items.put("hideArea_tv3","3、适配ios10，可直接分享适配到微博"+i);
            datasNotUpdate.add(items);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int i;
        if (checkedId == radio_all_select.getId()) {
            Toast.makeText(this, "全选更新:" + datasUpdate.size() + ":" + datasNotUpdate.size(), Toast.LENGTH_SHORT).show();
            for (i = 0; i < datasNotUpdate.size(); i++) {
                NotUpdateDeviceAdapter.getIsSelected().put(i, true);
            }
            for (i = 0; i < datasUpdate.size(); i++) {
                UpdateDeviceAdapter.getIsSelected().put(i, true);
            }
            updateDeviceAdapter.notifyDataSetChanged();
            notUpdateDeviceAdapter.notifyDataSetChanged();
        } else if (checkedId == radio_inverst_select.getId()){
            Toast.makeText(this, "反选:" + datasUpdate.size() + ":" + datasNotUpdate.size(), Toast.LENGTH_SHORT).show();
            for (i = 0; i < datasUpdate.size(); i++) {
                if(UpdateDeviceAdapter.getIsSelected().get(i)){
                    UpdateDeviceAdapter.getIsSelected().put(i, false);
                }else {
                    UpdateDeviceAdapter.getIsSelected().put(i, true);
                }
            }
            for (i = 0; i < datasNotUpdate.size(); i++) {
                if(NotUpdateDeviceAdapter.getIsSelected().get(i)){
                    NotUpdateDeviceAdapter.getIsSelected().put(i, false);
                }else {
                    NotUpdateDeviceAdapter.getIsSelected().put(i, true);
                }
            }
            updateDeviceAdapter.notifyDataSetChanged();
            notUpdateDeviceAdapter.notifyDataSetChanged();
        }

    }
}
