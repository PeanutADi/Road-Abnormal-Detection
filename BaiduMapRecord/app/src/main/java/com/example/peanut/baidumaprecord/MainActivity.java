package com.example.peanut.baidumaprecord;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;
    private TextView textView = null;
    private TextView colorBar = null;

    private Button read = null;
    private Button start = null;
    private Button end = null;
    private Button delete = null;
    private Button draw = null;
    private Button btn_upload = null;

    private LinearLayout linearLayout = null;

    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;

    MyLocationConfiguration.LocationMode mCurrentMode;

    List<LatLng> points = new ArrayList<LatLng>();
    LatLng CurrentPostion = new LatLng(23.062,113.386);

    private FileUtils fileUtils = null;

    static int cnt = 0;

    String data_id="";

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    int result=(int)msg.obj;
                    System.out.println("upload:"+result);
                    break;
                case 2:
                    List<LatLng> t_list = new ArrayList<LatLng>();
                    try {
                        String content = (String) msg.obj;
                        Gson gs = new Gson();
                        JSONArray ary = new JSONArray(content);
                        for (int i=0;i<ary.length();i++) {
                            String contentdata=ary.getString(i);
                            System.out.println(contentdata);
                            t_list.add(gs.fromJson(contentdata, LatLng.class));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
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
        btn_upload=(Button) findViewById(R.id.upload);

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
                fileUtils.saveDataToFile(getApplicationContext(),"","GPS.txt");
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

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(points.size()!=0)
                    for(int i=0;i < points.size();i++){
                        upload(points.get(i));
                    }
                else colorBar.setText("No point");
            }
        });

        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MY_PREFERENCE", Context.MODE_PRIVATE);
        String uuid= sharedPreferences.getString("uuid", "");
        int count=sharedPreferences.getInt("count", -1);
        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        if(uuid.equals("")){
            uuid=UUID.randomUUID().toString();
            editor.putString("uuid",uuid);
        }
        count+=1;
        editor.putInt("count",count);
        editor.commit();//提交修改
        data_id=count+"-"+uuid;
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

    void upload(LatLng cpos){
        System.out.println("run");
        final LatLng cpos1=cpos;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Gson gs = new Gson();
                String objectstr=gs.toJson(cpos1);
                JSONObject jsonObject=new JSONObject() ;
                int msg=-100;   //callback
                try {
                    jsonObject.put("timestamp",data_id );
                    jsonObject.put("content",objectstr);
                    URL url = new URL("http://203.195.152.23:8080/Tomcat_test/gps");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(5000);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(jsonObject.toString().getBytes());
                    if (conn.getResponseCode() == 200){
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                        String line = null;
                        StringBuilder sb = new StringBuilder();
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        JSONObject obj=new JSONObject(sb.toString());
                        msg=obj.getInt("msg");
                        Message message = handler.obtainMessage(1, 1, 2, msg);
                        handler.sendMessage(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    void download(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://203.195.152.23:8080/Tomcat_test/gps?timestamp="+data_id);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    if(conn.getResponseCode()==200){
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                        String line = null;
                        StringBuilder sb = new StringBuilder();
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        JSONObject obj=new JSONObject(sb.toString());
                        String msg=obj.getString("msg");
                        if(msg.equals("ok!")){
                            String content=obj.getString("result");
                            System.out.println(content);
                            Message message = handler.obtainMessage(2, 1, 2, content);
                            handler.sendMessage(message);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
