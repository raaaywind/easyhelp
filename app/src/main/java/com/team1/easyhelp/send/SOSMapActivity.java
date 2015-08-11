package com.team1.easyhelp.send;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team1.easyhelp.R;
import com.team1.easyhelp.entity.User;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class SOSMapActivity extends AppCompatActivity {

    private int user_id;
    private int event_id;

    private MapView mapView;
    private BaiduMap mMap;
    private LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    public GeoCoder geoCoder;
    public OnGetGeoCoderResultListener listener;
    private BitmapDescriptor bitmap1;
    private BitmapDescriptor bitmap2;
    private BitmapDescriptor bitmap0;
    private double latitude;
    private double longitude;
    private String locString = ""; // 包含用户地理位置的字符串

    boolean isFirstLoc = true; // 是否首次定位

    private List<User> neighbors; // 求救者周围的用户

    private Gson gson = new Gson();
    private String url = "http://120.24.208.130:1501";
    private String sosType; // 求救类型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sosmap);

        initialLayout();

        // 在地图上标记出周围的用户
        new Thread(new Runnable() {
            @Override
            public void run() {
                getNeighbors();
            }
        }).start();

        // 在后台服务器为此求救事件创建信息，并推送求救到附近的人
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 延迟2.5s后再执行此线程任务，上传事件详情，以确保坐标已定位准确
                    Thread.sleep(2500);
                    sendSOS();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 发送广播
        this.sendBroadcast(getIntent());

    }

    @Override
    protected void onDestroy() {
        // activity 销毁时同时销毁地图控件
        mLocClient.stop();
        mMap.setMyLocationEnabled(false);
        geoCoder.destroy();
        mapView.onDestroy();
        mapView = null;

        // 通知后台将该条求救信息状态设为已完成
        finishSOS();

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // activity 暂停时同时暂停地图控件与推送控件
        mapView.onPause();
        JPushInterface.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activity 恢复时同时恢复地图控件与推送控件
        mapView.onResume();
        JPushInterface.onResume(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sosmap, menu);
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

    // 加载基本界面后，进行的初始化操作
    private void initialLayout() {
        user_id = this.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getInt("user_id", -1);
        initialToolBar();
        initialMap();
        initialLocation();
        initialGeoCoder();
        initialIcon();
        // 初始的求救类型设为默认
        sosType = "SOS";
    }

    // 初始化ToolBar
    private void initialToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("发送求救");
        toolbar.setTitleTextColor(getResources().getColor(R.color.TitleTextColor));
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        // 设置toolbar的NavigationIcon的点击响应事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SOSMapActivity.this.finish();
            }
        });
    }

    // 初始化地图的基本显示
    private void initialMap() {
        mapView = (MapView) findViewById(R.id.mapView);
        mMap = mapView.getMap();
        // 设为普通地图界面
        mMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        // 去除无关图标
        for (int i = 0; i < mapView.getChildCount(); i++) {
            View child = mapView.getChildAt(i);
            if (child instanceof ZoomControls || child instanceof ImageView)
                child.setVisibility(View.INVISIBLE);
        }

        // 设置地图初始缩放级别
        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(16);
        mMap.animateMapStatus(u);
    }

    // 初始化定位功能
    private void initialLocation() {
        // 设置定位功能
        LocationMode mCurrentMode = LocationMode.FOLLOWING; // 定位图层类型
        mMap.setMyLocationEnabled(true); // 开启定位图层
        /**
         * 配置定位图层显示方式；
         * 参数依次为定位图层显示方式， 是否允许显示方向信息，用户自定义定位图标 */
        mMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, null));
        // 初始化定位控制器
        mLocClient = new LocationClient(this);
        // 设置定位监听器
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 开启GPS
        option.setCoorType("bd09ll"); // 设置坐标类型为百度经纬度标准
        option.setScanSpan(1000); // 设置定位刷新时间为1秒
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    // 设置定位的监听器
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不再处理新接收的位置
            if (location == null || mapView == null)
                return;
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(latitude)
                    .longitude(longitude).build();
            mMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                // 设置地图中心为当前定位点
                LatLng ll = new LatLng(latitude, longitude);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mMap.animateMapStatus(u);
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    // 设定不同尺寸的标记图标资源
    private void initialIcon() {
        bitmap0 = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_place_red_600_24dp);
        bitmap1 = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_place_red_600_36dp);
        bitmap2 = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_place_red_600_48dp);
    }

    public void initialGeoCoder() {
        // 创建地理编码检索实例
        geoCoder = GeoCoder.newInstance();
        listener = new OnGetGeoCoderResultListener() {
            // 地理编码查询结果回调函数（未使用该功能）
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}

            // 反地理编码查询结果回调函数
            @Override
            public void onGetReverseGeoCodeResult(
                    ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null ||
                        reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(SOSMapActivity.this, "获取地理位置编码失败",
                            Toast.LENGTH_LONG).show();
                } else {
                    locString = reverseGeoCodeResult.getAddress(); // 存储得到的地理位置信息
                }
            }
        };

        // 设置地理编码检索监听者
        geoCoder.setOnGetGeoCodeResultListener(listener);
    }

    // 根据获得的定位坐标反编码得到地理位置
    public void getReverseGeoCode() {
        // 执行反地理编码查询
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption()
                .location(new LatLng(latitude, longitude)));
    }

    // 获取周围的用户，初始化neighbors列表
    private void getNeighbors() {
        String jsonString = "{" +
                "\"id\":" + user_id +
                ",\"type\":0" +
                "}";
        String message = RequestHandler.sendPostRequest(url + "/user/neighbor", jsonString);
        if (message.equals("false")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "无法获取数据，请检查网络连接",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            try {
                JSONObject jO = new JSONObject(message);
                if (jO.getInt("status") == 500) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "获取周围用户信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                String jsonStringList = jO.getString("iid_list");
                neighbors = gson.fromJson(jsonStringList, new TypeToken<List<User>>(){}
                        .getType());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 根据所得到的信息调用函数绘制标记
        showNeighborsOnMap();
    }

    // 将获取到的neighbors信息显示在地图上
    public void showNeighborsOnMap() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (User user : neighbors) {
                    if (user.getId() != user_id) {
                        LatLng userLoc = new LatLng(user.getLatitude(), user.getLongitude());
                        mMap.addOverlay(new MarkerOptions()
                                .position(userLoc).icon(bitmap0)); // 使用中尺寸的图标标记用户位置
                    }
                }
            }
        });
    }

    // 将本条求救信息发送至后台服务器端存储
    private void sendSOS() {
        try {
            // 根据定位坐标初始化地理位置字串
            getReverseGeoCode();

            Thread.sleep(2000); // 等待地理位置字串查询成功
            String jsonString = "{" +
                    "\"id\":" + user_id +
                    ",\"type\":2" +
                    ",\"title\":\"" + sosType + "\"" +
                    ",\"longitude\":" + longitude +
                    ",\"latitude\":" + latitude +
                    ",\"location\":\"" + locString + "\"" +
                    "}";
            String message = RequestHandler.sendPostRequest(
                    url + "/event/add", jsonString);

            if (message.equals("false")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "上传求救信息失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                try {
                    JSONObject jO = new JSONObject(message);
                    if (jO.getInt("status") == 500) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "上传求救信息失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        event_id = jO.getJSONObject("value").getInt("event_id");

                        // 事件创建成功后通过极光推送推送给附近的人
                        pushToNeighbors();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "求救事件" + event_id + "已发送成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    // 退出求救状态以后更新服务器端事件状态
    private void finishSOS() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String jsonString = "{" +
                        "\"id\":" + user_id +
                        ",\"event_id\":" + event_id +
                        ",\"state\":1" +
                        "}";
                String msg = RequestHandler.sendPostRequest(
                        url + "/event/modify", jsonString);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "求救状态已撤销", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    // 通过极光推送发送求救到附近用户 （未来将同时向紧急联系人推送）
    private void pushToNeighbors() {
        String jsonString = "{" + "\"platform\":\"android\","
                + "\"audience\":{\"registration_id\":[";
        int i = 0;
        for (User user : neighbors) {
            if (user.getId() != user_id) {
                if (i == 0) {
                    jsonString = jsonString + "\"" + user.getIdentity_id() + "\"";
                } else {
                    jsonString = jsonString + ",\"" + user.getIdentity_id() + "\"";
                }
                i++;
            }
        }
        jsonString = jsonString +
                "]},\"notification\":{\"alert\":\"有人正在附近求救！点击查看事件："
                + event_id + "\"}}";
        String url = "https://api.jpush.cn/v3/push";
        String msg = RequestHandler.sendJPushPostRequest(url, jsonString);
        if (msg.equals("false")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SOSMapActivity.this, "推送失败",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // 设置求救类型为健康类求救
    public void setHealthSOSTag(View view) {}

    // 设置求救类型为安全类求救
    public void setSafeSOSTag(View view) {}

}

