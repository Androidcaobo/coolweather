package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolwather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.TransparentStateBar;
import com.coolweather.app.util.Utility;

/**
 * Created by 曹博 on 2016/9/11.
 * 天气显示界面的活动
 */
public class weatherActivity extends Activity implements View.OnClickListener{
    private LinearLayout weatherInfoLayout;

    //用于显示城市名
    private TextView mCityNameText;

    //用于显示发布时间
    private TextView mPublishText;

    //用于显示天气描述信息
    private TextView mWeatherDespText;

    //用于显示气温1
    private TextView mTemp1;

    //用于显示气温2
    private TextView mTemp2;

    //用于显示当前日期
    private TextView mCurrentDateText;

    //切换城市按钮
    private Button mSwitchCity;

    //刷新按钮
    private Button mRefreshWeather;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);

        //封装好的状态栏
        TransparentStateBar.Transparent(getWindow());


        //初始化各种控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        mCityNameText = (TextView) findViewById(R.id.city_name);
        mPublishText = (TextView) findViewById(R.id.publish_text);
        mCurrentDateText = (TextView) findViewById(R.id.current_date);
        mTemp1 = (TextView) findViewById(R.id.temp1);
        mTemp2 = (TextView) findViewById(R.id.temp2);
        mWeatherDespText = (TextView) findViewById(R.id.weather_desp);
        mSwitchCity = (Button) findViewById(R.id.switch_city);
        mRefreshWeather = (Button) findViewById(R.id.refresh);

        //设置点击事件
        mSwitchCity.setOnClickListener(this);
        mRefreshWeather.setOnClickListener(this);

        //尝试获取intent中的county数据，然后判断一下，当chooseActivity活动转过带的时候携带coutyCode数据
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            //有县级代码就去查询天气，查询前设置描述信息和城市名不可见
            mPublishText.setText("同步中...");
            mWeatherDespText.setVisibility(View.INVISIBLE);
            mCityNameText.setVisibility(View.INVISIBLE);
            //去查询
            queryWeatherCode(countyCode);
        } else {
            //如果为空，就显示当前天气
            showWeather();
        } 
    }


    /*
    * 查询县级代码对应的天气代码
    * */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }


    /*
    * 根据传入的地址和类型去查询天气代号和天气信息
    * */
    private void queryFromServer(final String address, final String type) {
        Log.d("weatherActivity", "address is" + address);
        Log.d("weatherActivity", "type is " + type);

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                //判断是县级代号还是天气代号
                Log.d("weatherActivity", "response is " + response);

                if ("countyCode".equals(type)) {
                    //从服务器返回的数据中解析出天气代号，如:县级代号|天气代号 13131|154778
                    String[] array = response.split("\\|");
                    if (array != null && array.length == 2) {
                        String weatherCode = array[1];
                        queryWeatherInfo(weatherCode);
                    }

                } else if ("weatherCode".equals(type)) {
                    //如果是天气代号，则处理返回的天气信息
                    Utility.handleWeatherResponse(weatherActivity.this,response);

                    //处理天气信息，回到主线程显示天气
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });

                }
            }



            @Override
            public void onError(Exception e) {
                Log.d("weatherActivity", "Exception is " + e);

                //如果出现异常，就回到主线程，提示同步失败
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPublishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /*
    * 根据查到的天气代号，去查询天气信息
    * */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address,"weatherCode");
    }

    /*
    * 从sharePreferences文件中读取存储的天气信息，并显示到界面上
    * */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCityNameText.setText(prefs.getString("city_name", ""));
        mTemp1.setText(prefs.getString("temp1", ""));
        mTemp2.setText(prefs.getString("temp2", ""));
        mWeatherDespText.setText(prefs.getString("weather_desp", ""));
        mPublishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
        mCurrentDateText.setText(prefs.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        mCityNameText.setVisibility(View.VISIBLE);

        //显示天气信息时顺便启动后台服务，八小时自动更新天气
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh:
                mPublishText.setText("同步中....");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherCode(weatherCode);
                }
                break;
            default:
                break;
        }
    }
}
