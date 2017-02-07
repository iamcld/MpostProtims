package com.mpos.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.chenld.mpostprotimstest.R;
import com.mpos.adapter.GalleryAdapter;
import com.mpos.activity.AddDeviceActivity;
import com.mpos.activity.DeviceListActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceFragment extends Fragment implements View.OnClickListener{

    private RecyclerView mRecyclerView;
    private GalleryAdapter mAdapter;
    private List<Integer> mDatas;


    public DeviceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_device, container, false);
        view.findViewById(R.id.addDevice_button).setOnClickListener(this);
        view.findViewById(R.id.deviceList_button).setOnClickListener(this);

        initData();
        //得到控件
        mRecyclerView = (RecyclerView) view.findViewById(R.id.id_recyclerview_horizontal);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        //设置适配器
        mAdapter = new GalleryAdapter(getActivity(), mDatas);
        mAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                Toast.makeText(getContext(), position+"", Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    private void initData(){
        mDatas = new ArrayList<>(Arrays.asList(
                R.drawable.d180, R.drawable.d180, R.drawable.d180, R.drawable.d180,
                R.drawable.d180, R.drawable.d180, R.drawable.d180, R.drawable.d180,
                R.drawable.d180
        ));
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.addDevice_button:
                intent = new Intent();
                intent.setClass(getActivity(), AddDeviceActivity.class);
                startActivity(intent);
                break;
            case R.id.deviceList_button:
                intent = new Intent(getActivity(), DeviceListActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

    }
}
