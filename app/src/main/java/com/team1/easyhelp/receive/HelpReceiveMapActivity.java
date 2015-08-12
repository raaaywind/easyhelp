package com.team1.easyhelp.receive;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ZoomControls;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.team1.easyhelp.R;
import com.team1.easyhelp.entity.Event;
import com.team1.easyhelp.send.HelpMapActivity;

public class HelpReceiveMapActivity extends AppCompatActivity {

    private Event help;

    private MapView mapView;
    private BaiduMap mMap;
    private BitmapDescriptor bitmap0;
    private Marker eventMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_receive_map);

        initial();
    }

    @Override
    protected void onDestroy() {
        // activity 销毁时同时销毁地图上相关的控件
        mMap.setMyLocationEnabled(false);
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
        getMenuInflater().inflate(R.menu.menu_help_receive_map, menu);
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
        // 获取该页面所需的event信息
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            help = (Event) bundle.getSerializable("event");
        }
        initialToolbar();
        initialIcon();
        initialMap();
    }

    // 设置该页面的TooBar
    private void initialToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("求助事件");
        toolbar.setTitleTextColor(getResources().getColor(R.color.TitleTextColor));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        // 设置toolbar的NavigationIcon的点击响应事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpReceiveMapActivity.this.finish();
            }
        });
    }

    // 初始化地图
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

        // 设置地图显示求助事件要求的地点
        LatLng ll = new LatLng(help.getLatitude(), help.getLongitude());
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        mMap.animateMapStatus(u);
        eventMarker = (Marker) mMap.addOverlay(new MarkerOptions()
                .position(ll).icon(bitmap0));
        // 设置地图初始缩放级别与地点
        u = MapStatusUpdateFactory.zoomTo(16);
        mMap.animateMapStatus(u);
    }

    private void initialIcon() {
        bitmap0 = BitmapDescriptorFactory
                .fromResource(R.drawable.ic_place_red_600_24dp);
    }

}
