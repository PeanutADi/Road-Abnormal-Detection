package com.example.peanut.gpstest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    WifiManager wifiManager;
    List<ScanResult> wifiinfo;

    LocationManager locationManager;
    Location location;

    TextView textView;
    TextView textView2;
    Button button;
    Button button2;
    Button button3;

    FileUtils fileUtils;

    private static boolean flag = true;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();

        fileUtils = new FileUtils();

        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);

        textView2.setMovementMethod(new ScrollingMovementMethod() {
        });

        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // 获取wifi对象
        wifiinfo=getWifiInfo(wifiManager);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 获取location对象
        location = getBestLocation(locationManager);

        updateView(location,wifiinfo);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                3000, 8, new LocationListener() {

                    @Override
                    public void onStatusChanged(String provider, int status,
                                                Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        updateView(locationManager
                                .getLastKnownLocation(provider),wifiinfo);
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        updateView(null,wifiinfo);
                    }

                    @Override
                    public void onLocationChanged(Location location) {
                        location = getBestLocation(locationManager);// 每次都去获取GPS_PROVIDER优先的location对象
                        updateView(location,wifiinfo);
                    }
                });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileUtils.appendDataToFile(getApplicationContext(), String.valueOf(textView.getText()), "test.txt");
                String test = fileUtils.loadDataFromFile(getApplicationContext(), "test.txt");
                if(test.length()>10)
                    test=test.substring(0,4)+test.substring(4).replace("当","\n\n当").replace("wifi","\nwifi").replace("WiFi","\nWiFi")+"\n";
                textView2.setText(test);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileUtils.saveDataToFile(getApplicationContext(), "", "test.txt");
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String test = fileUtils.loadDataFromFile(getApplicationContext(), "test.txt");
                if(test.length()>10)
                    test=test.substring(0,4)+test.substring(4).replace("当","\n\n当").replace("wifi","\nwifi").replace("WiFi","\nWiFi")+"\n";
                textView2.setText(test);
            }
        });
        setTimer();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    private StringBuffer updateView(Location location,List<ScanResult> wifiinfo) {
        StringBuffer sb = new StringBuffer();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        sb.append(" 当前时间：\n" + str + "\n");
        if (location != null) {
            sb.append(" 位置信息：\n");
            sb.append("经度：" + location.getLongitude() + ", 纬度："
                    + location.getLatitude()+"  \n");
            sb.append("wifi信息： \n");
            for(int i=0;i<wifiinfo.size();i++) {
                //String WiFiID=wifiList.get(i).SSID;
                String WiFiID = wifiinfo.get(i).BSSID;//get the AP's IP
                double Level = wifiinfo.get(i).level;
                String strcontent = "WiFi MAC: "+WiFiID + " ,强度： " + String.valueOf(Level) + "  \n";
                sb.append(strcontent);
            }
            textView.setText(sb.toString());
        } else {
            textView.setText("");
        }
        return sb;
    }

    @SuppressLint("MissingPermission")
    private Location getBestLocation(LocationManager locationManager) {
        Location result = null;
        if (locationManager != null) {
            result = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (result != null) {
                return result;
            } else {
                result = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                return result;
            }
        }
        return result;
    }

    private List<ScanResult> getWifiInfo(WifiManager wifimanager){
        List<ScanResult> wifiList=null;
        wifimanager.startScan();
        wifiList=wifimanager.getScanResults();
        Collections.sort(wifiList, new Comparator<ScanResult>() {
            public int compare(ScanResult arg0, ScanResult arg1) {
                return arg1.level-arg0.level;
            }
        });
        if (wifiList.size()>5)
            return wifiList.subList(0,5);
        else
            return wifiList;
    }
    private void setTimer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag){
                    try {
                        Thread.sleep(1000); //休眠一秒
                        mHanler.sendEmptyMessage(123);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private Handler mHanler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 123:
                    wifiinfo=getWifiInfo(wifiManager);
                    updateView(location,wifiinfo);
                    break;
                default:
                    break;
            }
        }
    };
    private void stopTimer(){
        flag = false;
    }


    void getPermission(){
        int permission4 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission4 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_WIFI_STATE},1);
        }
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CHANGE_WIFI_STATE},1);
        }
        int permission3 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission3 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }
}
