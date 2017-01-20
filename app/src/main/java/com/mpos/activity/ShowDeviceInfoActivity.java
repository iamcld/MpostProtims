package com.mpos.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.mpos.MposApplication;
import com.mpos.db.DatabaseAdapter;
import com.example.chenld.mpostprotimstest.R;
import com.mpos.db.MPos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowDeviceInfoActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "ShowDeviceInfoActivity";
    private ImageView imageView;
    private Button button;
    private TextView tv_show_pos_sn;
    private TextView tv_show_pos_name;
    private TextView tv_show_pos_pn;
    private TextView tv_show_os_version;
    private TextView tv_show_boot_version;
    private TextView tv_show_battery_info;
    private CircleImageView iv_avatar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_device_info);
        initView();

    }


    public void initView(){
        String mac = getIntent().getStringExtra(MposApplication.DEVICE_MAC);
        String name = getIntent().getStringExtra(MposApplication.DEVICE_NAME);
        ArrayList<MPos> mposList = new ArrayList<>();

        LogUtils.d("mac:"+mac);
        LogUtils.d("name:"+name);
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(this);
        MPos mPos = new MPos();
        mPos = databaseAdapter.rawFindById(mac);
        LogUtils.d("mPos:"+mPos);
        mposList = databaseAdapter.rawFindAll();
        LogUtils.d("数据库中所有记录:"+mposList);

        imageView = (ImageView) findViewById(R.id.back_iv);
        button = (Button) findViewById(R.id.delete_bt);
        iv_avatar = (CircleImageView) findViewById(R.id.iv_avatar);
        tv_show_pos_sn = (TextView) findViewById(R.id.tv_show_pos_sn);
        tv_show_pos_name = (TextView)findViewById(R.id.tv_show_pos_name);
        tv_show_pos_pn = (TextView)findViewById(R.id.tv_show_pos_pn);
        tv_show_os_version = (TextView)findViewById(R.id.tv_show_os_version);
        tv_show_boot_version = (TextView)findViewById(R.id.tv_show_boot_version);
        tv_show_battery_info = (TextView)findViewById(R.id.tv_show_battery_info);
        //tv_show_pos_name.setText(mPos.getName());
        tv_show_pos_name.setText(name);
        if (mPos != null){
            tv_show_pos_sn.setText(mPos.getSn());
            tv_show_pos_pn.setText(mPos.getPn());
            tv_show_os_version.setText(mPos.getOs_version());
            tv_show_boot_version.setText(mPos.getBoot_version());
            tv_show_battery_info.setText(mPos.getBattery());
        }
        setImageResouse(name);

        imageView.setOnClickListener(this);
        button.setOnClickListener(this);
    }

    private void setImageResouse(String posName){
        boolean flag = false;
        for (int i=0; i<MposApplication.deviceName.length; i++){
            if (posName.indexOf(MposApplication.deviceName[i]) != -1 ){
                iv_avatar.setImageResource(MposApplication.img[i]);
                flag = true;
                break;
            }
        }

        //设置默认图片
        if (!flag){
            iv_avatar.setImageResource(MposApplication.img[4]);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_iv:
                finish();
                break;
            case R.id.delete_bt:
                tipDialog();

            default:
                    break;
        }

    }

    public void tipDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定删除？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ShowDeviceInfoActivity.this,"确定", Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                String mac = intent.getStringExtra(MposApplication.DEVICE_MAC);
                DatabaseAdapter databaseAdapter = new DatabaseAdapter(ShowDeviceInfoActivity.this);
                databaseAdapter.rawDelete(mac);

                //取消蓝牙匹配
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    try {
                        Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
                        removeBondMethod.invoke(bluetoothDevice);//取消匹配
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ShowDeviceInfoActivity.this,"取消", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();

    }

}
