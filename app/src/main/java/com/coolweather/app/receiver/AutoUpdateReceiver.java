package com.coolweather.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coolweather.app.service.AutoUpdateService;

/**
 * Created by 曹博 on 2016/9/16.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {

    //处理定时任务的逻辑在这里写
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}
