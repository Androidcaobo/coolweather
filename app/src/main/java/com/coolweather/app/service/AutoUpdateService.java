package com.coolweather.app.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

/**
 * Created by 曹博 on 2016/9/16.
 */
public class AutoUpdateService extends Service {

    /*
    * 这个方法是用来跟活动绑定通信用的
    * */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //耗时操作在这里进行
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();

        //定时服务
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //需要定时的时间
        int anHour = 8 * 60 * 60 * 1000;
        //触发时间，SystemClock.elapsedRealtime()获取到开机到现在的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        //指定处理定时任务的的广播接收器
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        //调用set方法设置
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /*
    * 更新天气信息
    * */
    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = prefs.getString("weatherCode", "");
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleWeatherResponse(AutoUpdateService.this, response);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
}
