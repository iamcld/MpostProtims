package com.mpos.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
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

import com.apkfuns.logutils.LogUtils;
import com.example.chenld.mpostprotimstest.R;
import com.mpos.MposApplication;
import com.mpos.activity.DownloadActivity;
import com.mpos.activity.ShowDeviceInfoActivity;
import com.pax.utils.Utils;


import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chenld on 2016/12/7.
 */
public class UpdateDeviceAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<HashMap<String, String>> list;
    private int currentItem = -1; //用于记录点击的 Item 的 position，是控制 item 展开的核心
    private boolean isShow = false;
    private static HashMap<Integer, Boolean> isSelected;//用于存储checkBox的选中状态
    private boolean mSwitch = false;


    public UpdateDeviceAdapter(Context context, ArrayList<HashMap<String,String>> list,Boolean isShow,
                               boolean mSwitch){
        super();
        this.context = context;
        this.list = list;
        this.isShow = isShow;
        if (isSelected == null){
            isSelected = new HashMap<>();
        }

        this.mSwitch = mSwitch;
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

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.updata_device_item, null);
            vh = new ViewHolder();
            //vh.showArea = (RelativeLayout) convertView.findViewById(R.id.layout_showArea);
            vh.update_checkBox = (CheckBox) convertView.findViewById(R.id.update_checkbox);
            vh.img_devece = (ImageView) convertView.findViewById(R.id.img_devece);
            vh.deviceName = (TextView) convertView.findViewById(R.id.device_name_tv);
            vh.version= (TextView) convertView.findViewById(R.id.version_tv);
            vh.size = (TextView) convertView.findViewById(R.id.size_tv);
            vh.newfun_btn = (Button) convertView.findViewById(R.id.newfun_btn);
            vh.update_btn = (Button) convertView.findViewById(R.id.update_btn);
            vh.hideArea_tv1 = (TextView) convertView.findViewById(R.id.hideArea_tv1);
            vh.hideArea_tv2 = (TextView) convertView.findViewById(R.id.hideArea_tv2);
            vh.hideArea_tv3 = (TextView) convertView.findViewById(R.id.hideArea_tv3);
            vh.hideArea = (LinearLayout) convertView.findViewById(R.id.layout_hideArea);

            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> items = list.get(position);
        //vh.showArea.setTag(position);
        vh.newfun_btn.setTag(position);

        vh.deviceName.setText(items.get(MposApplication.DEVICE_NAME));
        vh.version.setText(items.get("version"));
        vh.size.setText(items.get("size"));
        vh.hideArea_tv1.setText(items.get("hideArea_tv1"));
        vh.hideArea_tv2.setText(items.get("hideArea_tv2"));
        vh.hideArea_tv3.setText(items.get("hideArea_tv3"));
        setImageResouse(items, vh);
        //是否显示checkBox
        if(isShow){
            vh.update_checkBox.setVisibility(View.VISIBLE);
        }else{
            vh.update_checkBox.setVisibility(View.GONE);
        }

        if(currentItem == position){
            vh.hideArea.setVisibility(View.VISIBLE);
        }else{
            vh.hideArea.setVisibility(View.GONE);
        }
        vh.update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                if (Utils.isNetworkAvailable(context, mSwitch)){
                    if(btAdapter != null) {
                        if (!btAdapter.isEnabled()) {
                            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            context.startActivity(intent);
                        }else {
                            Toast.makeText(context,"Toast...",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(context, DownloadActivity.class);
                            ArrayList<String> updateDeviceList = new ArrayList<>();
                            //intent.putStringArrayListExtra("devices", list);
                            //intent.putParcelableArrayListExtra("devices", (ArrayList<? extends Parcelable>) list);
//                        intent.putExtra(MposApplication.DEVICE_MAC, list.get(position).get(MposApplication.DEVICE_MAC));
//                        intent.putExtra(MposApplication.DEVICE_NAME, list.get(position).get(MposApplication.DEVICE_NAME));

                            updateDeviceList.add(list.get(position).get(MposApplication.DEVICE_NAME)+"\n"+
                                    list.get(position).get(MposApplication.DEVICE_MAC));
                            intent.putStringArrayListExtra(MposApplication.CHECK_UPDATE_DEVICE, updateDeviceList);
                            context.startActivity(intent);
                        }
                    }
                }
            }
        });

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

                notifyDataSetChanged();
            }
        });

        vh.update_checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"checkBox",Toast.LENGTH_SHORT).show();
                if(UpdateDeviceAdapter.isSelected.get(position)){
                    LogUtils.d("checkBox set false");
                    UpdateDeviceAdapter.isSelected.put(position,false);
                    setIsSelected(isSelected);
                }else {
                    UpdateDeviceAdapter.isSelected.put(position,true);
                    LogUtils.d("checkBox set true");
                    setIsSelected(isSelected);
                }

            }
        });
        vh.img_devece.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(MposApplication.DEVICE_NAME, list.get(position).get(MposApplication.DEVICE_NAME));
                intent.putExtra(MposApplication.DEVICE_MAC, list.get(position).get(MposApplication.DEVICE_MAC));
                Toast.makeText(context, "imageView ", Toast.LENGTH_SHORT).show();
                intent.setClass(context, ShowDeviceInfoActivity.class);
                context.startActivity(intent);
            }
        });


//        vh.showArea.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int tag = (Integer) v.getTag();
//                if(tag == currentItem){
//                    currentItem = -1;
//                }else{
//                    currentItem = tag;
//                }
//
//                notifyDataSetChanged();
//            }
//        });

        // 根据isSelected来设置checkbox的选中状况
        vh.update_checkBox.setChecked(getIsSelected().get(position));
        return convertView;
    }

    private void setImageResouse(HashMap<String,String> items, ViewHolder vh){
        boolean flag = false;
        for (int i=0; i<MposApplication.deviceName.length; i++){
            if (items.get(MposApplication.DEVICE_NAME).contains(MposApplication.deviceName[i])){
                vh.img_devece.setImageResource(MposApplication.img[i]);
                flag = true;
                break;
            }
        }

        //设置默认图片
        if (!flag){
            vh.img_devece.setImageResource(MposApplication.img[4]);
        }
    }

    public static HashMap<Integer, Boolean> getIsSelected(){
        return isSelected;
    }
    public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
        UpdateDeviceAdapter.isSelected = isSelected;
    }


    private static class ViewHolder{
        //private RelativeLayout showArea;

        private CheckBox update_checkBox;
        private ImageView img_devece;
        private TextView deviceName;
        private TextView version;
        private TextView size;
        private Button newfun_btn;
        private Button update_btn;
        private TextView hideArea_tv1;
        private TextView hideArea_tv2;
        private TextView hideArea_tv3;

        private LinearLayout hideArea;

//        private int position;
//        private ArrayList<HashMap<String, String>> list;
//
//        private ViewHolder vh;
//        private Context context;
//
//        public ViewHolder(ViewHolder vh) {
//            this.vh = vh;
//        }
//
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()){
//                case R.id.img_devece:
//                    Intent intent = new Intent();
//                    intent.putExtra(MposApplication.DEVICE_NAME, list.get(vh.position).get(MposApplication.DEVICE_NAME));
//                    intent.putExtra(MposApplication.DEVICE_MAC, list.get(position).get(MposApplication.DEVICE_MAC));
//                    Toast.makeText(context, "imageView ", Toast.LENGTH_SHORT).show();
//                    intent.setClass(context, ShowDeviceInfoActivity.class);
//                    context.startActivity(intent);
//                    break;
//            }
//
//        }
    }
}
