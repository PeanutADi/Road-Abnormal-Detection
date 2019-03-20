package com.example.peanut.baidumaprecord;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;
    private TextView textView = null;
    private TextView colorBar = null;

    private Button read = null;
    private Button start = null;
    private Button end = null;
    private Button delete = null;
    private Button draw = null;

    private LinearLayout linearLayout = null;

    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;

    MyLocationConfiguration.LocationMode mCurrentMode;

    List<LatLng> points = new ArrayList<LatLng>();
    LatLng CurrentPostion = new LatLng(23.062,113.386);

    private FileUtils fileUtils = null;

    static int cnt = 0;

    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            if(fileUtils != null && colorBar != null){
                colorBar.setText("Recording: "+String.valueOf(cnt++)+". "+CurrentPostion.toString()+"\n");
                fileUtils.appendDataToFile(getApplicationContext(),CurrentPostion.toString()+"\n","GPS.txt");

                points.add(CurrentPostion);
            }
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        textView = (TextView) findViewById(R.id.textView5);
        colorBar = (TextView) findViewById(R.id.colorBar);

        start = (Button) findViewById(R.id.start);
        delete = (Button) findViewById(R.id.delete);
        end = (Button) findViewById(R.id.end);
        read = (Button) findViewById(R.id.read);
        draw = (Button) findViewById(R.id.draw);

        linearLayout = (LinearLayout)findViewById(R.id.ll);

        colorBar.bringToFront();
        textView.bringToFront();
        linearLayout.bringToFront();

        fileUtils = new FileUtils();

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

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cnt = 0;
                handler.postDelayed(runnable, 100);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileUtils.appendDataToFile(getApplicationContext(),"","GPS.txt");
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                if(colorBar!=null){
                    colorBar.setText("End recording");
                }
            }
        });

        draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OverlayOptions mOverlayOptions = new PolylineOptions()
                        .width(10)
                        .color(0xAAFF0000)
                        .points(points);
                Overlay mPolyline = mBaiduMap.addOverlay(mOverlayOptions);
            }
        });


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
            textView.setText(CurrentPostion.toString()+"\n");
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
        handler.removeCallbacks(runnable);
    }
}
