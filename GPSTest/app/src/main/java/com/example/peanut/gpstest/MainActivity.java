package com.example.peanut.gpstest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
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
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    Location location;

    TextView textView;
    TextView textView2;
    Button button;
    Button button2;
    Button button3;

    FileUtils fileUtils;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileUtils = new FileUtils();

        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);

        textView2.setMovementMethod(new ScrollingMovementMethod() {
        });

        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 获取location对象
        location = getBestLocation(locationManager);

        updateView(location);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                3000, 8, new LocationListener() {

                    @Override
                    public void onStatusChanged(String provider, int status,
                                                Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        updateView(locationManager
                                .getLastKnownLocation(provider));
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        updateView(null);
                    }

                    @Override
                    public void onLocationChanged(Location location) {
                        location = getBestLocation(locationManager);// 每次都去获取GPS_PROVIDER优先的location对象
                        updateView(location);
                    }
                });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileUtils.appendDataToFile(getApplicationContext(), String.valueOf(textView.getText()), "test.txt");
                String test = fileUtils.loadDataFromFile(getApplicationContext(), "test.txt");
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
                textView2.setText(test);
            }
        });
    }

    private StringBuffer updateView(Location location) {
        StringBuffer sb = new StringBuffer();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        sb.append(" 当前时间：\n" + str + "\n");
        if (location != null) {
            sb.append(" 位置信息：\n");
            sb.append("经度：" + location.getLongitude() + ", 纬度："
                    + location.getLatitude());
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
}
