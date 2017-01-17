package com.mpos.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chenld.mpostprotimstest.R;
import com.mpos.activity.AboutActivity;
import com.mpos.activity.ServerSetActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetFragment extends Fragment implements View.OnClickListener{

    public SetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set, container, false);
        //frament中按钮通过此方法注册按钮监听器，不在要xml中通过android:onClick注册，否则程序编译不过或运行错误
        view.findViewById(R.id.serverSetButton).setOnClickListener(this);
        view.findViewById(R.id.aboutButton).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.serverSetButton:
                intent = new Intent(getActivity(), ServerSetActivity.class);
                //intent.setClass(getActivity(), ServerSetActivity.class);
                startActivity(intent);
                break;
            case R.id.aboutButton:
                intent = new Intent();
                //通过调用getActivity()来获取当前fragment的activity
                intent.setClass(getActivity(), AboutActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
