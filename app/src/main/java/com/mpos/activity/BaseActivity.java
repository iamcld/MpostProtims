package com.mpos.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有Activity的父类
 * Created by chenld on 2017/1/1.
 * 1、当启动一个子类activity时，就会调用父类中onCreate的 ActivityCollector.addActivity(this);
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    //管理activity
    public static class ActivityCollector{

        public static List<Activity> activityList = new ArrayList<>();

        //添加activity
        public static void addActivity(Activity activity){
            activityList.add(activity);
        }

        //移除activity
        public static void removeActivity(Activity activity){
            activityList.remove(activity);
        }

        public static void finishAll(){
            for (Activity activity : activityList){
                if (!activity.isFinishing()){
                    activity.finish();
                }
            }
        }
    }
}
