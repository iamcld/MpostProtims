package com.mpos.activity;


import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentTabHost;
import com.example.chenld.mpostprotimstest.R;
import com.fragmenthost.FragmentTabHost;//修改源码后的FragmentTabHost
import com.mpos.fragment.BTFragment;
import com.mpos.fragment.USBFragment;
import com.mpos.fragment.WIFIFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;


public class AddDeviceActivity extends FragmentActivity implements View.OnClickListener{

    //使用自定义后修改后的FragmentTabHost.java源码,使得切换fragment后，被切换的fragment数据保存起来
    //源码中：FragmentTabHost不保存状态是因为切换fragment的时候是使用detach和attach来Fragment的隐藏和显示的，
    // 这样的话每次切换肯定要重新加载布局，处理使用detach和attach，我们还可以使用show和hide来实现显示和隐藏，这样可以保存状态
    private FragmentTabHost mFragmentTabHost;
    //Fragment数组界面
    private Class mFragmentArray[] = {BTFragment.class, USBFragment.class, WIFIFragment.class};
    //选秀卡文字
    private String mTextArry [] = {"BT", "USB", "WIFI"};
    //布局填充器
    private LayoutInflater mLayoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        findViewById(R.id.back_iv).setOnClickListener(this);

        initView();
    }


    private void initView(){
        mLayoutInflater = LayoutInflater.from(this);
        //找到TabHost
        mFragmentTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mFragmentTabHost.setup(this, getSupportFragmentManager(), R.id.fragmentCommMode);

        //得到fragment个数
        int count = mFragmentArray.length;
        for(int i = 0; i < count; i++){
            TabHost.TabSpec tabSpec = mFragmentTabHost.newTabSpec(mTextArry[i])
                    .setIndicator(getTabItemView(i));
            mFragmentTabHost.addTab(tabSpec, mFragmentArray[i], null);

            //设备tab按钮的背景
           // mFragmentTabHost.getTabWidget().getChildAt(i)
            //        .setBackgroundResource(R.drawable.selector_tab_background);

        }

    }

    private View getTabItemView(int index){

        View view = mLayoutInflater.inflate(R.layout.tab_item_comm_view, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView_tab_host);
        TextView textView = (TextView) view.findViewById(R.id.tab_host_tv);
        textView.setText(mTextArry[index]);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_iv:
                finish();
                break;
            default:
                break;
        }
    }
}
