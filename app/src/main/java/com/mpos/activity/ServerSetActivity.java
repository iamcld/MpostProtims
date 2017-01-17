package com.mpos.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.chenld.mpostprotimstest.R;
import com.pax.utils.Utils;

public class ServerSetActivity extends Activity implements View.OnClickListener{
    private ImageView imageView;
    private Button button;
    private EditText serverip_edit;
    private EditText serverport_edit;
    private EditText tid_edit;

    private SharedPreferences sp = null;
    //保存服务器设置的SharedPreferences文件:main.xml
    public final static String SHARED_MAIN = "main";

    public final static String KEY_SERVER_IP = "server_ip";
    public final static String KEY_SERVER_PORT = "server_port";
    public final static String KEY_TID = "tid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_server_set);
        imageView = (ImageView) findViewById(R.id.back_iv);
        button = (Button) findViewById(R.id.save_btn);
        serverip_edit = (EditText) findViewById(R.id.serverip_edit);
        serverport_edit = (EditText) findViewById(R.id.serverport_edit);
        tid_edit = (EditText) findViewById(R.id.tid_edit);

        serverip_edit.setText("192.168.1.1");
        serverport_edit.setText("9999");
        tid_edit.setText("00000000");

        imageView.setOnClickListener(this);
        button.setOnClickListener(this);
        //拿到名称是SHARED_MAIN的对象.保存的文件在/data/data/当前包名/shared_prefs/main.xml
        sp = getSharedPreferences(SHARED_MAIN, Context.MODE_PRIVATE);
        //读取保存在SharedPreferences中的数据
        recoverData();
    }

    void recoverData(){
        String server_ip = sp.getString(KEY_SERVER_IP, null);
        String server_port = sp.getString(KEY_SERVER_PORT, null);
        String tid = sp.getString(KEY_TID, null);

        if (server_ip != null && server_port != null && tid != null){
            serverip_edit.setText(server_ip);
            serverport_edit.setText(server_port);
            tid_edit.setText(tid);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_iv:
                finish();
                break;
            case R.id.save_btn:
                Toast.makeText(this,"save", Toast.LENGTH_SHORT).show();
                String server_ip = serverip_edit.getText().toString();
                String server_port = serverport_edit.getText().toString();
                String tid = tid_edit.getText().toString();
                if(!Utils.isValidIp(server_ip)){
                    Toast.makeText(this, "请输入正确ip", Toast.LENGTH_SHORT).show();
                    serverip_edit.setText("");
                    return;
                }
                if (!Utils.isVliadPort(server_port)){
                    Toast.makeText(this, "请输入正确端口", Toast.LENGTH_SHORT).show();
                    serverport_edit.setText("");
                    return;
                }
                if (tid == null){
                    Toast.makeText(this, "TID不允许为空", Toast.LENGTH_SHORT).show();
                    return;
                }


                //获得SharedPreferences的编辑器
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(KEY_SERVER_IP, server_ip);
                editor.putString(KEY_SERVER_PORT, server_port);
                editor.putString(KEY_TID, tid);
                editor.commit();

                break;
            default:
                break;
        }

    }
}
