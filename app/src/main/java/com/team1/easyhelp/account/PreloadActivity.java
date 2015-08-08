package com.team1.easyhelp.account;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.team1.easyhelp.R;
import com.team1.easyhelp.testActivity;
import com.team1.easyhelp.utils.RequestHandler;

import cn.jpush.android.api.JPushInterface;

public class PreloadActivity extends AppCompatActivity {
    // 设置定位相关变量
    private LocationClient mLocationClient;
    private LocationMode tempMode = LocationMode.Hight_Accuracy; // 设置选项为高精度定位模式
    String tempcoor="bd09ll"; // 设置坐标类型为百度经纬度标准

    private double latitude;
    private double longitude;

    private int user_id;
    private String identity_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preload);

        initial();


    }

    @Override
    protected void onStop() {
        mLocationClient.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initial() {
        // 在开始应用时初始化Application级别的地图控件
        SDKInitializer.initialize(getApplicationContext());
        initialLocationClient(); // 开启定位功能
        // 初始化JPUSH推送相关功能
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        // 读取用户id
        user_id = this.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getInt("user_id", -1);
        // 获取极光推送的用户识别ID
        identity_id = JPushInterface.getRegistrationID(getApplicationContext());

        // 查询登陆状态， 延迟1.5s后执行
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        check_if_confirmed();
                    }
                });
            }
        }).start();
    }

    // 查询登录状态，如果已有登陆的user_id，则跳过登陆验证界面，去到主页
    public void check_if_confirmed() {
        if (user_id != -1) {
            Intent it = new Intent(this, testActivity.class);
            startActivity(it);
        } else {
            Intent it = new Intent(this, LoginActivity.class);
            startActivity(it);
        }
        PreloadActivity.this.finish();
    }

    // 初始化定位模块
    private void initialLocationClient() {
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(new MyLocationListener());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode); // 设置定位精度
        option.setScanSpan(0); // 定位刷新频率，如果为0则只定位一次
        option.setOpenGps(true); // 开启Gps
        option.setIgnoreKillProcess(false);
        option.setCoorType(tempcoor); // 设置坐标类型
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    // 每次登陆时上传当前状态的用户位置信息与极光推送ID
    public void uploadUserLocation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String jsonString = "{" +
                            "\"id\":" + user_id +
                            ",\"identity_id\":\"" + identity_id + "\"" +
                            ",\"latitude\":" + latitude +
                            ",\"longitude\":" + longitude +
                            "}";
                    String url = "http://120.24.208.130:1501/user/modify_information";
                    RequestHandler.sendPostRequest(url, jsonString);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 设置定位的监听器
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            // 更新定位信息
            uploadUserLocation();
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
}
