package com.mpos.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chenld.mpostprotimstest.R;
import com.mpos.activity.ShowDeviceInfoActivity;

import java.util.List;

/**
 * Created by chenld on 2016/12/6.
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder>{

    private Context context;
    private LayoutInflater mInflater;
    private List<Integer> mDatas;

    /**
     * 由于RecyclerView 没有提供setOnItemClickListener这个回调，故需要自己实现
     * ItemClick的回调接口
     */
    public interface OnItemClickListener{
        void OnItemClick(View view, int position);
    }
    private OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }


    public GalleryAdapter(Context context, List<Integer> datas){
        this.context = context;
        //得到当前activity的布局填充器
        mInflater = LayoutInflater.from(context);
        mDatas = datas;
    }

    //创建静态ViewHodler类，用于存储getView方法中每次返回的View
    public static class ViewHolder  extends RecyclerView.ViewHolder{
        public ViewHolder (View itemView) {
            super(itemView);
        }
        ImageView mImg;
        TextView mTxt;
    }

    @Override
    public int getItemCount() {
        //返回 list的总item个数
        return mDatas.size();
    }

    /**
     * 创建ViewHolder
     */
    @Override
    public ViewHolder  onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.mImg = (ImageView) view.findViewById(R.id.recycleview_item_image);
        viewHolder.mImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Toast.makeText(context, "imageView ", Toast.LENGTH_SHORT).show();
                intent.setClass(context, ShowDeviceInfoActivity.class);
                context.startActivity(intent);
            }
        });

        return viewHolder;
    }

    /**
     * 设置值,绑定数据到RecyclerView
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mImg.setImageResource(mDatas.get(position));

        //如果设置了回调，则设置点击事件
        if (mOnItemClickListener != null)
        {
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                   mOnItemClickListener.OnItemClick(holder.itemView, position);
                }
            });

        }
    }


}
