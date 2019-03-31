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
import com.bigkoo.pickerview.TimePickerView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;
    private TextView textView = null;
    private TextView colorBar = null;

    private TextView tv_start=null;
    private TextView tv_end=null;

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

    String uuid="";
    int count=0;
    TimePickerView pvTime1=null;
    TimePickerView pvTime2=null;
    //String porturl="http://192.168.199.230:8080";
    String porturl="http://203.195.152.23:8080";

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
                            t_list.add(gs.fromJson(contentdata, LatLng.class));
                        }
                        points=t_list;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    int ans=(int)msg.obj;
                    if(ans<0) {
                        Toast.makeText(getApplicationContext(), "upload wrong!!!!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "upload ok!!!!", Toast.LENGTH_SHORT).show();
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
                if((Math.abs(CurrentPostion.latitude-20)<10)&&(Math.abs(CurrentPostion.longitude-110)<20)) {
                    upload(CurrentPostion);
                }
            }
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Date nowtime=new Date();
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        textView = (TextView) findViewById(R.id.textView5);
        colorBar = (TextView) findViewById(R.id.colorBar);
        tv_start = (TextView) findViewById(R.id.tv_start);
        tv_end = (TextView) findViewById(R.id.tv_end);
        tv_start.setText(df.format(nowtime));
        tv_end.setText(df.format(nowtime));

        start = (Button) findViewById(R.id.start);
        delete = (Button) findViewById(R.id.delete);
        end = (Button) findViewById(R.id.end);
        read = (Button) findViewById(R.id.read);
        draw = (Button) findViewById(R.id.draw);
        btn_upload=(Button)findViewById(R.id.upload);

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
                uploadall();
            }
        });
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });


        pvTime1 = new TimePickerView.Builder(this, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date,View v) {//选中事件回调
                tv_start.setText(df.format(date));
            }
        }).build();
        pvTime2 = new TimePickerView.Builder(this, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date,View v) {//选中事件回调
                tv_end.setText(df.format(date));
            }
        }).build();

        tv_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pvTime1.setDate(Calendar.getInstance());//注：根据需求来决定是否使用该方法（一般是精确到秒的情况），此项可以在弹出选择器的时候重新设置当前时间，避免在初始化之后由于时间已经设定，导致选中时间与当前时间不匹配的问题。
                pvTime1.show();
            }
        });
        tv_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pvTime2.setDate(Calendar.getInstance());//注：根据需求来决定是否使用该方法（一般是精确到秒的情况），此项可以在弹出选择器的时候重新设置当前时间，避免在初始化之后由于时间已经设定，导致选中时间与当前时间不匹配的问题。
                pvTime2.show();
            }
        });


        SharedPreferences sharedPreferences = getSharedPreferences("MY_PREFERENCE", Context.MODE_PRIVATE);
        uuid= sharedPreferences.getString("uuid", "");
        count= sharedPreferences.getInt("count", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
        if(uuid.equals("")){
            uuid=UUID.randomUUID().toString();
            editor.putString("uuid",uuid);
        }
        count+=1;
        editor.putInt("count",count);
        editor.commit();//提交修改
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
                Date nowtime=new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp=df.format(nowtime);
                try {
                    jsonObject.put("uuid",uuid);
                    jsonObject.put("timestamp",timestamp );
                    jsonObject.put("content",objectstr);
                    jsonObject.put("count",count);
                    URL url = new URL(porturl+"/Tomcat_test/gps");
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
    void uploadall(){
        System.out.println("run");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Gson gs = new Gson();
                String objectstr=gs.toJson(points);
                JSONObject jsonObject=new JSONObject() ;
                int msg=-100;   //callback
                Date nowtime=new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp=df.format(nowtime);
                try {
                    jsonObject.put("uuid",uuid);
                    jsonObject.put("timestamp",timestamp );
                    jsonObject.put("content",objectstr);
                    URL url = new URL(porturl+"/Tomcat_test/gpsall");
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
                        Message message = handler.obtainMessage(3, 1, 2, msg);
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
                    URL url = new URL(porturl+"/Tomcat_test/gps?uuid="+uuid+"&from="+tv_start.getText().toString()+"&to="+tv_end.getText().toString());
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
