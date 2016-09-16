package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.coolwather.app.R;
import com.coolweather.app.util.TransparentStateBar;

/**
 * Created by 曹博 on 2016/9/10.
 * app欢迎界面
 */
public class AppStartActivity extends Activity{
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //封装好的状态栏
        TransparentStateBar.Transparent(getWindow());

        setContentView(R.layout.app_startactivity);

        //这个方法延时2秒跳转
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(AppStartActivity.this, ChooseAreaActivity.class);
                startActivity(intent);
                finish();//可代替onpause
            }
        }, 2000);

    }

    //当活动准备跳转到另一个活动时，销毁当前活动
    /*@Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }*/
}
