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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team1.easyhelp.R;
import com.team1.easyhelp.entity.User;
import com.team1.easyhelp.utils.RequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SOSMapActivity extends AppCompatActivity {

    private int user_id;

    private MapView mapView;
    private BaiduMap mMap;


//    private Marker[] markers;
    private LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    private LocationMode mCurrentMode;
    BitmapDescriptor bitmap1;
    BitmapDescriptor bitmap2;
    BitmapDescriptor bitmap0;

//    private LatLng[] neighborsLoc;
    private List<User> neighbors;

    boolean isFirstLoc = true; // 是否首次定位

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sosmap);

        initialLayout();

        // 在地图上标记出周围的用户
        new Thread(setMarkerRunnable).start();
    }

    @Override
    protected void onDestroy() {
        // activity 销毁时同时销毁地图控件
        mLocClient.stop();
        mMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // activity 暂停时同时暂停地图控件
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activity 恢复时同时恢复地图控件
        mapView.onResume();
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

    // 加载界面后的初始化操作
    private void initialLayout() {
        user_id = this.getSharedPreferences("user_info", Context.MODE_PRIVATE)
                .getInt("user_id", -1);
        initialToolBar();
        initialMap();
        initialLocation();
        initialIcon();
    }

    private void initialToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("发送求救");
        toolbar.setTitleTextColor(getResources().getColor(R.color.TitleTextColor));
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
    }

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

    private void initialLocation() {
        // 设置定位功能

        mCurrentMode = LocationMode.FOLLOWING; // 定位图层类型
        mMap.setMyLocationEnabled(true); // 开启定位图层
        /**
         * 配置定位图层显示方式；
         * 参数依次为定位图层显示方式， 是否允许显示方向信息，用户自定义定位图标
         */
        mMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, true, null));
        // 初始化定位
        mLocClient = new LocationClient(this);
        // 设置定位监听器
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 开启GPS
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1500); // 设置定位刷新时间为1.5秒
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    /**
     * 定位SDK监听类
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不再处理新接收的位置
            if (location == null || mapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mMap.animateMapStatus(u);
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    // 设定不同尺寸的图片资源
    private void initialIcon() {
        bitmap0 = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_place_red_600_24dp);
        bitmap1 = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_place_red_600_36dp);
        bitmap2 = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_place_red_600_48dp);
    }

    // 获取周围的用户
    private void getNeighbors() {
        String jsonString = "{" +
                "\"id\":" + user_id +
                ",\"type\":0" +
                "}";
        String url = "http://120.24.208.130:1501";
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
    }

    // 将获取到的信息显示在地图上
    public void showNeighborsOnMap() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (User user : neighbors) {
                    LatLng userLoc = new LatLng(user.getLatitude(), user.getLongitude());
                    mMap.addOverlay(new MarkerOptions().position(userLoc).icon(bitmap0)); // 使用中尺寸的图标标记用户位置
                }
            }
        });
    }


    // 设置Runnable在后台获取以及标记附近的人
    Runnable setMarkerRunnable = new Runnable() {
        @Override
        public void run() {
            getNeighbors();
            showNeighborsOnMap();
        }
    };
}

