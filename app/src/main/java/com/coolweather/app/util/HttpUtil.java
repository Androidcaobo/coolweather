package com.coolweather.app.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 曹博 on 2016/9/8.
 * 发送网络请求
 */
public class HttpUtil {
    public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                //发送网络请求
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }

                    //回调方法或是操作在子线程中进行
                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }
                } catch (IOException e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                }finally {
                    if (connection != null) {
                        //关闭连接
                        connection.disconnect();

                    }
                }
            }
        }).start();
    }

}
