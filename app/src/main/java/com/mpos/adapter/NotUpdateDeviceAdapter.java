package com.mpos.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chenld.mpostprotimstest.R;
import com.mpos.MposApplication;
import com.mpos.activity.ShowDeviceInfoActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chenld on 2016/12/7.
 */
public class NotUpdateDeviceAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, String>> list;
    private int currentItem = -1; //用于记录点击的 Item 的 position，是控制 item 展开的核心
    private boolean isShow = false;
    private static HashMap<Integer, Boolean> isSelected;//用于存储checkBox的选中状态

    public NotUpdateDeviceAdapter(Context context, ArrayList<HashMap<String,String>> list, Boolean isShow){
        super();
        this.context = context;
        this.list = list;
        this.isShow = isShow;
        isSelected = new HashMap<Integer, Boolean>();//isSelected对象创建
        // 初始化数据
        initDate();
    }

    // 初始化isSelected的数据
    private void initDate() {
        for (int i = 0; i < list.size(); i++) {
            getIsSelected().put(i, false);
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
        ViewHolder vh;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.not_updata_device_item, null);
            vh = new ViewHolder();
            vh.not_showArea = (RelativeLayout) convertView.findViewById(R.id.not_layout_showArea);
            vh.not_checkBox = (CheckBox) convertView.findViewById(R.id.not_checkbox);
            vh.not_img_devece = (ImageView) convertView.findViewById(R.id.not_img_devece);
            vh.not_deviceName = (TextView) convertView.findViewById(R.id.not_device_name_tv);
            vh.not_version= (TextView) convertView.findViewById(R.id.not_version_tv);
            vh.not_size = (TextView) convertView.findViewById(R.id.not_size_tv);
            vh.newfun_btn = (Button) convertView.findViewById(R.id.not_newfun_btn);
            vh.not_hideArea_tv1 = (TextView) convertView.findViewById(R.id.not_hideArea_tv1);
            vh.not_hideArea_tv2 = (TextView) convertView.findViewById(R.id.not_hideArea_tv2);
            vh.not_hideArea_tv3 = (TextView) convertView.findViewById(R.id.not_hideArea_tv3);
            vh.not_hideArea = (LinearLayout) convertView.findViewById(R.id.not_layout_hideArea);
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> item = list.get(position);
        //vh.not_showArea.setTag(position);
        vh.newfun_btn.setTag(position);

        vh.not_deviceName.setText(item.get(MposApplication.DEVICE_NAME));
        vh.not_version.setText(item.get("version"));
        vh.not_size.setText(item.get("size"));
        vh.not_hideArea_tv1.setText(item.get("hideArea_tv1"));
        vh.not_hideArea_tv2.setText(item.get("hideArea_tv2"));
        vh.not_hideArea_tv3.setText(item.get("hideArea_tv3"));

        //是否显示checkBox
        if(isShow){
            vh.not_checkBox.setVisibility(View.VISIBLE);
        }else{
            vh.not_checkBox.setVisibility(View.GONE);
        }

        //是否显示item隐藏内容
        if(currentItem == position){
            vh.not_hideArea.setVisibility(View.VISIBLE);
        }else{
            vh.not_hideArea.setVisibility(View.GONE);
        }

        // 监听checkBox并根据原来的状态来设置新的状态
        vh.not_checkBox.setOnClickListener(new CheckBoxClickListener(isSelected, position));
        vh.not_img_devece.setOnClickListener(new ImageClickListener(context, list, position));

        vh.newfun_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //vh.newfun_btn.setVisibility(View.GONE);
                int tag = (Integer) v.getTag();
                if(tag == currentItem){
                    currentItem = -1;
                }else{
                    currentItem = tag;
                }
                //通知适配器数据发生改变
                notifyDataSetChanged();
            }
        });

        // 根据isSelected来设置checkbox的选中状况
        vh.not_checkBox.setChecked(getIsSelected().get(position));
        return convertView;
    }

    public static HashMap<Integer, Boolean> getIsSelected(){
        return isSelected;
    }

    public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
        NotUpdateDeviceAdapter.isSelected = isSelected;
    }


    private static class CheckBoxClickListener implements View.OnClickListener{
        HashMap<Integer, Boolean> isSelected;
        private int position;

        public CheckBoxClickListener(HashMap<Integer, Boolean> isSelected, int position) {
            this.isSelected = isSelected;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if(isSelected.get(position)){
                isSelected.put(position,false);
                setIsSelected(isSelected);
            }else {
                isSelected.put(position,true);
                setIsSelected(isSelected);
            }
        }
    }


    private static class ImageClickListener implements View.OnClickListener{
        private Context context;
        private int position;
        private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        public ImageClickListener(Context context, ArrayList<HashMap<String, String>> list, int position) {
            this.context = context;
            this.list = list;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            Toast.makeText(context, "imageView ", Toast.LENGTH_SHORT).show();
            intent.setClass(context, ShowDeviceInfoActivity.class);
            intent.putExtra(MposApplication.DEVICE_MAC, list.get(position).get(MposApplication.DEVICE_MAC));
            intent.putExtra(MposApplication.DEVICE_NAME, list.get(position).get(MposApplication.DEVICE_NAME));
            context.startActivity(intent);
        }
    }

    private static class ViewHolder{
        private RelativeLayout not_showArea;

        private CheckBox not_checkBox;
        private ImageView not_img_devece;
        private TextView not_deviceName;
        private TextView not_version;
        private TextView not_size;
        private Button newfun_btn;

        private TextView not_hideArea_tv1;
        private TextView not_hideArea_tv2;
        private TextView not_hideArea_tv3;

        private LinearLayout not_hideArea;
    }
}
