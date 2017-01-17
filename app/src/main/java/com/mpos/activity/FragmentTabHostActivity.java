package com.mpos.activity;

import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;

import android.support.v4.app.FragmentTabHost;
//import com.fragmenthost.FragmentTabHost;//修改源码后的FragmentTabHost

import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.example.chenld.mpostprotimstest.R;

import com.mpos.fragment.DeviceListFragment;
import com.mpos.fragment.SetFragment;


//由于用到了getSupportFragmentManager(),所以主Activity的extends需为FragmentActivity
//通过代码加载fragment
public class FragmentTabHostActivity extends FragmentActivity {
    private static final String TAG = "FragmentTabHostActivity";

    private FragmentTabHost mFragmentTabHost;
    /**
     * Fragment数组界面
     *
     */
//    private Class mFragmentArray[] = {DeviceFragment.class, SetFragment.class};

    private Class mFragmentArray[] = {DeviceListFragment.class, SetFragment.class};
    /**
     * 存放图片数组
     *
     */
    private int mImageArray[] = {R.drawable.tab_device_btn, R.drawable.tab_set_btn};

    /**
     * 选修卡文字
     *
     */
    private String mTextArry [] = {"设备", "设置"};

    /**
     * 布局填充器
     *
     */
    private LayoutInflater mLayoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_tab_host);

        initView();
    }
    private void initView(){
        //得到当前activiy的布局填充器
        mLayoutInflater = LayoutInflater.from(this);

        // 找到TabHost
        mFragmentTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mFragmentTabHost.setup(this, getSupportFragmentManager(), R.id.fragmenttabhostcontent);

        //得到fragment个数
        int count = mFragmentArray.length;
        for(int i = 0; i < count; i++){
            //给每个tab设置按钮，图标和内容
            TabHost.TabSpec tabSpec = mFragmentTabHost.newTabSpec(mTextArry[i])
                    .setIndicator(getTabItemView(i));
            //将tab按钮加入到tab选项卡中
            mFragmentTabHost.addTab(tabSpec, mFragmentArray[i], null);
            //设备tab按钮的背景
            mFragmentTabHost.getTabWidget().getChildAt(i)
                    .setBackgroundResource(R.drawable.selector_tab_background);
        }

    }

    private View getTabItemView(int index){

        View view = mLayoutInflater.inflate(R.layout.tab_item_view, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView_tab_host);
        imageView.setImageResource(mImageArray[index]);
        TextView textView = (TextView) view.findViewById(R.id.tab_textview);
        textView.setText(mTextArry[index]);
        return view;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {
            int count = getFragmentManager().getBackStackEntryCount();
            LogUtils.d("当前栈中个数:"+count);
            //如果栈中的个数为0个，则直接返回，退出应用
            if ( count == 0){
                // 创建退出对话框
                AlertDialog isExit = new AlertDialog.Builder(this).create();
                // 设置对话框标题
                isExit.setTitle("系统提示");
                // 设置对话框消息
                isExit.setMessage("确定要退出吗");
                // 添加选择按钮并注册监听
                isExit.setButton(AlertDialog.BUTTON_POSITIVE,"确定", listener);
                isExit.setButton(AlertDialog.BUTTON_NEGATIVE,"取消", listener);
                // 显示对话框
                isExit.show();
            }else {
                //如果有东西，则进行出栈
                getFragmentManager().popBackStack();
            }
        }

        return false;

    }
    /**监听对话框里面的button点击事件*/
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };
}
