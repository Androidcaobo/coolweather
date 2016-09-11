package com.coolweather.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

/**
 * Created by 曹博 on 2016/9/8.
 * 工具类，用来解析和处理这种数据
 */
public class Utility {

    /*
    * 解析和处理服务器返回的省级数据
    * */
    public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB, String response) {
        //如果response不为空，则开始解析数据，省份数据为如:01|北京，02|上海，
        if (!TextUtils.isEmpty(response)) {
            //先以逗号为分隔符，将数据放到数组中
            String[] allProvinces = response.split(",");


            //判断数组是否为空
            if (allProvinces != null && allProvinces.length > 0) {
                //遍历上边以，分割好的数据
                for (String p : allProvinces) {
                    //以|为分隔符，将数据分割成两个，结果为，1，北京，2，上海
                    //|符号，要这样分割 加//
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析出来的数据存储到province表中
                    coolWeatherDB.saveProvince(province);
                }
                //如果数据中的数据不为空，那么走到这返回true，整个方法结束，不会走到下面了
                return true;
            }


        }
        return false;
    }


    /*
    * 解析和处理市级数据
    * */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }

        }
        return false;
    }

    /*
    * 解析县级的数据
    * */

    public static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB, String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    /*
    * 解析服务器返回的JSON数据，并将解析出来的数据储存到本地
    * */
    public static void handleWeatherResponse(Context context, String response) {
        try {
            //先讲response放到jsonObject对象中，然后用这个对象解析出键为weatherinfo的数据对象
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");//weatherinfo是服务器返回的键值
            String cityName = weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            //将数据保存到本地
            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /*
    * 将服务器返回的所有天气信息储存到SharedPreferences文件中
    * */
    private static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        //获取到SharedPreferences.Editor对象
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        //开始房数据
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();

    }
    }



