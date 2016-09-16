package com.coolweather.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolwather.app.R;
import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.TransparentStateBar;
import com.coolweather.app.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 曹博 on 2016/9/8.
 */
public class ChooseAreaActivity extends Activity {

    //设置级别
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog mProgressDialog;

    private ListView mListView;

    private TextView mTextView;

    private ArrayAdapter<String> mAdapter;

    private CoolWeatherDB mCoolWeatherDB;

    private List<String> dataList = new ArrayList<String>();

    //省列表
    private List<Province> mProvinceList;

    //市列表
    private List<City> mCityList;

    //县列表
   private List<County> mCountyList;

    //选中的省份
    private Province selectedProvince;

    //选中的市
    private City selectedCity;

    //选中的县
    private County selectedCounty;

    //当前选中的级别
    private int currentLevel;

    //是否是从weatherActivity中跳转过来的
    private boolean isFromWeatherActivity;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_area);

        //判断是否是从weather跳转过来的
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);

        //封装好的状态栏
        TransparentStateBar.Transparent(getWindow());


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //city_selected是我们在存储sharepreference时存的一个标志位，判断是否选择过城市，如果选择过直接跳过去，
        //如果是从weather跳过来的，那么就不执行下面这个If里面的语句
        if (prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity) {
            Intent intent = new Intent(this, weatherActivity.class);
            startActivity(intent);
            finish();
            return;//不再向下执行oncreate代码，也就是不让他加载省级数据
        }


        mListView = (ListView) findViewById(R.id.list_view);
        mTextView = (TextView) findViewById(R.id.title_text);

        //listView适配器，用系统默认的设置
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mAdapter);

        //由于使用了单例设计模式，而且我们自己定义的一个方法getinstance，可以获取到对象
        mCoolWeatherDB  = CoolWeatherDB.getInstance(this);

        //listView点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                //判断当前等级
                if (currentLevel == LEVEL_PROVINCE) {
                    //被选中的省份的编号
                    selectedProvince = mProvinceList.get(index);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = mCityList.get(index);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String countyCode = mCountyList.get(index).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this, weatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        //我们进入活动，先加载省分
        queryProvinces();
    }

    /*
    * 查找全国所有的省，优先从数据库查询，如果没有查到，就去服务器查询
    * */
    private void queryProvinces() {
        //调用方法查询省份
        mProvinceList = mCoolWeatherDB.loadProvince();

        //判断是否从数据库中读到的数据，读到就进去,如果上句没有读到，那就去服务器查询
        if (mProvinceList.size() > 0) {
            //先确定我们定义的初始化的数组是空，先清空，否则又残留数据将会影响程序
            dataList.clear();
            for (Province province : mProvinceList
                    ) {
                //遍历的数据存到初始化数组中
                dataList.add(province.getProvinceName());
            }
            //当listView数据发生改变时，刷新
            mAdapter.notifyDataSetChanged();
            //listView中有100条记录，如果想定位到某一条上面去就可以直接调用listView.setSelection(position);
            mListView.setSelection(0);
            mTextView.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromService(null, "province");
        }

    }



    private void queryCounties() {
        mCountyList = mCoolWeatherDB.loadCounties(selectedCity.getId());

        if (mCountyList.size() > 0) {
            dataList.clear();
            for (County county : mCountyList
                    ) {
                //遍历的数据存到初始化数组中
                dataList.add(county.getCountyName());
            }
            //当listView数据发生改变时，刷新
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mTextView.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromService(selectedCity.getCityCode(), "county");
        }
    }

    private void queryCities() {
        //调用方法查询城市
        mCityList = mCoolWeatherDB.loadCities(selectedProvince.getId());

        if (mCityList.size() > 0) {
            dataList.clear();
            for (City city : mCityList
                    ) {
                //遍历的数据存到初始化数组中
                dataList.add(city.getCityName());
            }
            //当listView数据发生改变时，刷新
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mTextView.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromService(selectedProvince.getProvinceCode(), "city");
        }

    }

    /*
    * 根据传入的代码和类型查询省县市的数据
    * */
    private void queryFromService(final  String code, final String type) {
        String address ;
        //判断code是否为空，如果为空则说明要查询省的信息,不为空说明要查询县市
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();

        //回调机制
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

            //sendHttpRequest方法中的onfinish方法在调用前已经从address返回了response，然后回调了重写的onfinish
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(mCoolWeatherDB, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(mCoolWeatherDB, response,selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(mCoolWeatherDB, response, selectedCity.getId());
                }

                //如果数据处理成功,result为true
                if (result) {
                    //数据通过上面的代码已经存到数据库中了，我们从新调用queryprovince等方法，就可以直接从数据库得到数据，然后更新Ui
                    //由于现在在子线程当中，所以我们通过runonUiThread方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //关闭对话框
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }

                        }
                    });
                }


            }



            @Override
            public void onError(Exception e) {
            //由于现在在子线程当中，所以我们通过runonUiThread方法回到主线程处理UI,toast
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //关闭对话框
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }


    /*
     * 关闭进度对话框
     * */
    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

    }



    /*
   * 显示对话框
   * */
    private void showProgressDialog() {

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("正在加载....");
            //back键不能取消
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    /*
    * 捕获当前back按键，根据当前的级别来判断，此时应该返回市列表，还是省列表，还是直接退出
    * */

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        } else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        } else {
            if (isFromWeatherActivity) {
                Intent intent = new Intent(this, weatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }


}
