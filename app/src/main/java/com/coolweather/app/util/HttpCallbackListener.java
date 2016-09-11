package com.coolweather.app.util;

/**
 * Created by 曹博 on 2016/9/8.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);

}
