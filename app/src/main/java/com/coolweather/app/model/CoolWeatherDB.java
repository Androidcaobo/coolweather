package com.coolweather.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.coolweather.app.db.CoolWeatherOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 曹博 on 2016/9/7.
 * 数据库封装类，将常用的数据库操作封装起来
 */
public class CoolWeatherDB {

    //数据库名字
    public static final String DB_NAME = "cool_weather";

    //数据库版本
    public static final int VERSION = 1;

    private static CoolWeatherDB coolWeatherDB;

    private SQLiteDatabase db;


    //构造方法私有化，
    private CoolWeatherDB(Context context) {
        CoolWeatherOpenHelper dbhelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
        db = dbhelper.getWritableDatabase();
    }

    //获取CoolWeatherDB实例,sync保证这块代码只有一个线程进行访问，一个现成访问时，其他现成访问将被堵塞
    public synchronized static CoolWeatherDB getInstance(Context context) {
        //判断Coolweather对象是否已经生成
        if (coolWeatherDB == null) {
            coolWeatherDB = new CoolWeatherDB(context);
        }
        //如果已经存在，则直接返回
        return coolWeatherDB;
    }


    //将Province实例存储到数据库
    public void saveProvince(Province province) {
        if (province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", province.getProvinceName());
            values.put("province_code",province.getProvinceCode());
            db.insert("Province", null, values);
        }
    }

    //从数据库中读取全国所有省份信息
    public List<Province> loadProvince() {
        List<Province> list = new ArrayList<Province>();
        //查询数据
        Cursor cursor = db.query("Province", null, null, null, null, null, null);
        //先将数据库中的数据定位到第一行
        if (cursor.moveToFirst()) {
            do {
                //获取province实例，来设置省份信息
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            }while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }


    //将City实例存储到数据库
    public void saveCity(City city) {
        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            values.put("province_id", city.getProvinceId());
            db.insert("City", null, values);
        }
    }

    //从数据库读取所有城市信息
    public List<City> loadCities(int provinceId) {
        List<City> list = new ArrayList<City>();
        Cursor cursor = db.query("City", null, "province_id=?", new String[]{ String.valueOf(provinceId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                list.add(city);
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    //将county实例存储到数据库
    public void saveCounty(County county) {
        if (county != null) {
            ContentValues values = new ContentValues();
            values.put("county_name", county.getCountyName());
            values.put("county_code", county.getCountyCode());
            values.put("city_id", county.getCityId());
            db.insert("county", null, values);
        }
    }

    //从数据库中读取某城市下所有县的信息
    public List<County> loadCounties(int cityId) {
        List<County> list = new ArrayList<County>();
        Cursor cursor = db.query("County", null, "city_id = ?", new String[]{String.valueOf(cityId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                list.add(county);
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }
}
