package com.example.peanut.baidumaprecord;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;
    private TextView textView = null;
    private TextView colorBar = null;

    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;

    MyLocationConfiguration.LocationMode mCurrentMode;

    LatLng CurrentPostion = new LatLng(23.062,113.386);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        textView = (TextView) findViewById(R.id.textView5);
        colorBar = (TextView) findViewById(R.id.colorBar);

        colorBar.bringToFront();
        textView.bringToFront();

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);

        mLocationClient = new LocationClient(this);

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        mLocationClient.setLocOption(option);

        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);

        mLocationClient.start();

        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;//定位跟随态
        MapStatusUpdate status1 = MapStatusUpdateFactory.newLatLng(CurrentPostion);
        mBaiduMap.setMapStatus(status1);
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            CurrentPostion = new LatLng(location.getLatitude(),location.getLongitude());
            textView.setText(CurrentPostion.toString());
        }
    }
    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}
