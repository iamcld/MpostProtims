package com.mpos.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chenld.mpostprotimstest.R;
import com.mpos.UpdateModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenld on 2017/1/3.
 */

public class DownloadAdapter extends BaseAdapter {
    private static final String TAG = "DownloadAdapter";

    private List<UpdateModel> datas;
    private Context context;

    public DownloadAdapter(Context context) {
        datas = new ArrayList<>();
        this.context = context;
    }

    public void addData(UpdateModel model) {
        datas.add(model);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int pos) {
        return datas.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int pos, View convertView, ViewGroup viewGroup) {
        UpdateModel model = datas.get(pos);
        ViewCacher cacher = null;

        // the first time create the View for each item
        if (null == convertView) {
            cacher = new ViewCacher();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, null);
            cacher.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            cacher.textView    = (TextView) convertView.findViewById(R.id.textView);
            cacher.value    = (TextView) convertView.findViewById(R.id.value);
            cacher.type_doneFlag_step    = (TextView) convertView.findViewById(R.id.type_doneFlag_step);
            cacher.fileCount_curFile    = (TextView) convertView.findViewById(R.id.fileCount_curFile);
            cacher.status    = (TextView) convertView.findViewById(R.id.status);
            // cache the view
            convertView.setTag(cacher);
        }
        else {
            // when not the first time, get the view from cache
            cacher = (ViewCacher) convertView.getTag();
            convertView.setTag(cacher);
        }
        cacher.textView.setText(model.getBtDevName() + "  " + model.getBtDevMac());
        cacher.progressBar.setProgress(0);
        // return the same view for each item
        return convertView;
    }

    public class ViewCacher {
        public TextView textView;
        public ProgressBar progressBar;
        public TextView value;
        public TextView type_doneFlag_step;
        public TextView fileCount_curFile;
        public TextView status;
    }

}
