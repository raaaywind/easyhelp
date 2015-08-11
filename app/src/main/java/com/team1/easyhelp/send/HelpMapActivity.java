package com.team1.easyhelp.send;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.team1.easyhelp.R;
import com.team1.easyhelp.utils.ActivityCollector;


/**
 * 利用地图SDK的Poi检索功能，实现地点检索功能
 */

public class HelpMapActivity extends AppCompatActivity {

    private EditText locationEdit;

    private MapView mapView;
    private BaiduMap mMap;
    private LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    public GeoCoder geoCoder;
    private PoiSearch mPoiSearch = null;

    private double latitude;
    private double longitude;
    private String locString = ""; // 包含用户地理位置的字符串


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_map);
        // 收集该Activity，便于销毁
        ActivityCollector.getInstance().addActivity(this);
        // 初始化UI以及服务组件
        initialLayout();
    }

    @Override
    protected void onDestroy() {
        // activity 销毁时同时销毁地图上相关的控件
        mLocClient.stop();
        mMap.setMyLocationEnabled(false);
        geoCoder.destroy();
        mPoiSearch.destroy();
        mapView.onDestroy();
        mapView = null;

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // activity 暂停时同时暂停地图控件与推送控件
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activity 恢复时同时恢复地图控件与推送控件
        mapView.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            mPoiSearch.searchInCity((new PoiCitySearchOption())
                    .city("")
                    .keyword(locationEdit.getText().toString())
                    .pageNum(0));
        } else if (id == R.id.confirm) {
            // 将得到的数据信息，传至下一页面
            Intent intent = new Intent(this, HelpSubmitActivity.class);
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", latitude);
            bundle.putDouble("longitude", longitude);
            bundle.putString("location", locString);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    // 绑定UI控件
    private void initialLayout() {
        initialToolbar();
        initialMap();
        initialGeoCoder();
        initialLocation();
        initialPoiSearch();
    }

    // 设置该页面的TooBar
    private void initialToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        toolbar.setTitle("输入求助地点");
        toolbar.setTitleTextColor(getResources().getColor(R.color.TitleTextColor));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);

        // 设置toolbar的NavigationIcon的点击响应事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpMapActivity.this.finish();
            }
        });
    }

    // 初始化地图显示
    private void initialMap() {
        mapView = (MapView) findViewById(R.id.mapview);
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
        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(15);
        mMap.animateMapStatus(u);
    }

    // 初始化定位控件
    private void initialLocation() {
        MyLocationConfiguration.LocationMode mCurrentMode =
                MyLocationConfiguration.LocationMode.NORMAL; //设置定位图层类型
        mMap.setMyLocationEnabled(true); // 开启定位图层
        /**
         * 配置定位图层显示方式；
         * 参数依次为定位图层显示方式， 是否允许显示方向信息，用户自定义定位图标
         */
        mMap.setMyLocationConfigeration(new MyLocationConfiguration(
                mCurrentMode, false, null));
        // 初始化定位控制器
        mLocClient = new LocationClient(this);
        // 设置定位监听器
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 开启GPS
        option.setCoorType("bd09ll"); // 设置坐标类型为百度经纬度标准
        option.setScanSpan(0); // 设置定位只进行一次
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
            // 设置地图中心为当前定位点
            mMap.setMyLocationData(locData);
            LatLng ll = new LatLng(latitude, longitude);
            mMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(ll));

            // 根据定位得到的结果初始化locString
            getReverseGeoCode();
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    // 初始化地图搜索模块
    private void initialPoiSearch() {
        // 绑定搜索输入框
        locationEdit = (EditText) findViewById(R.id.edit_location);
        // 初始化Poi检索
        mPoiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){
            //获取POI检索结果
            public void onGetPoiResult(PoiResult result){
                if (result == null
                        || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                    Toast.makeText(HelpMapActivity.this, "未找到结果", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    mMap.clear();
                    PoiOverlay overlay = new MyPoiOverlay(mMap);
                    // 设置overLay点击事件
                    mMap.setOnMarkerClickListener(overlay);
                    // 设置PoiOverlay数据
                    overlay.setData(result);
                    // 添加PoiOverlay到地图中
                    overlay.addToMap();
                    overlay.zoomToSpan();
                    return;
                }
                // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
                    String strInfo = "在";
                    for (CityInfo cityInfo : result.getSuggestCityList()) {
                        strInfo += cityInfo.city;
                        strInfo += ",";
                    }
                    strInfo += "找到结果";
                    Toast.makeText(HelpMapActivity.this, strInfo, Toast.LENGTH_LONG)
                            .show();
                }
            }
            //获取Place详情页检索结果
            public void onGetPoiDetailResult(PoiDetailResult result){
                if (result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(HelpMapActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // 获取到该位置的文字描述以及坐标信息
                    locString = result.getName() + ": " + result.getAddress();
                    LatLng ll = result.getLocation();
                    latitude = ll.latitude;
                    longitude = ll.longitude;
                    Toast.makeText(HelpMapActivity.this, "您已选择: " + locString,
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
    }

    public void initialGeoCoder() {
        // 创建地理编码检索实例
        geoCoder = GeoCoder.newInstance();
        OnGetGeoCoderResultListener geoListener = new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null ||
                        reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(HelpMapActivity.this, "获取地理位置编码失败",
                            Toast.LENGTH_LONG).show();
                } else {
                    locString = reverseGeoCodeResult.getAddress(); // 存储得到的地理位置信息
                }
            }
        };

        // 设置地理编码检索监听器
        geoCoder.setOnGetGeoCodeResultListener(geoListener);
    }

    // 根据定位坐标反编码得到地理位置
    public void getReverseGeoCode() {
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(
                new LatLng(latitude, longitude)));
    }

    // 构造Poi检索覆盖类
    private class MyPoiOverlay extends PoiOverlay {
        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
                    .poiUid(poi.uid));
            return true;
        }
    }
}
