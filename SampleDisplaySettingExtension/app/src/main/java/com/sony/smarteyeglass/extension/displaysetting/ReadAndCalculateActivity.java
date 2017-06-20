package com.sony.smarteyeglass.extension.displaysetting;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sonyericsson.extras.liveware.aef.registration.Registration;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by daiki on 2017/06/07.
 */

public class ReadAndCalculateActivity extends Activity implements LocationListener {

    private LocationManager locationManager;
    private long startTime;
    private double beforeLon = 0;
    private double beforeLat = 0;
    private double totalDisRound1 = 0;
    private double totalDisRound2 = 0;
    ListViewActivity listview;
    Round round;
    final double[] dummyLongitude = new double[]{135.01, 135.02, 135.03, 135.04, 135.05, 135.06, 135.07, 135.08, 135.09, 135.10};
    final double[] dummyLatitude = new double[]{35.01, 35.02, 35.03, 35.04, 35.05, 35.06, 35.07, 35.08, 35.09, 35.10};
    int clickGpsButtonCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measure_layout);
        startTime = System.currentTimeMillis();
        String fileName = getIntent().getStringExtra("getFileName");

        listview = new ListViewActivity();
        try {
            RoundReader roundReader;
            Log.d("file", "File:"+fileName);
            roundReader = new RoundReader(fileName);

            round = roundReader.read();
        } catch (IOException e) {
            Log.e("roundReader", "not open file", e);
        }
        Button startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                System.out.println("Push Start");
                if (ContextCompat.checkSelfPermission(ReadAndCalculateActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ReadAndCalculateActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 2000);
                    Log.d("debug", "checkSelfPermission false");
                    return;
                }

                // スタートボタン押下時を0秒とする
                startTime = System.currentTimeMillis();
                locationStart();
            }
        });

        Button stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                System.out.println("Push Stop");
                ReadAndCalculateActivity.this.finish();
            }
        });

        Button getGPSButton = (Button) findViewById(R.id.gps_button);
        getGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (clickGpsButtonCount >= 10) {
                    ReadAndCalculateActivity.this.finish();
                } else {
                    Location location = new Location("Dummy");
                    location.setLatitude(dummyLongitude[clickGpsButtonCount]);
                    location.setLongitude(dummyLatitude[clickGpsButtonCount]);
                    onLocationChanged(location);
                    clickGpsButtonCount += 1;
                    TextView measuring = (TextView) findViewById(R.id.measuring);
                    measuring.setText("Get GPS");
                }

            }
        });

        // SmartEyeglassで表示する準備:Activity内でExtensionServiceを持ってくる
        if (SampleExtensionService.Object == null) {
            Intent intent = new Intent(Registration.Intents
                    .EXTENSION_REGISTER_REQUEST_INTENT);
            Context context = getApplicationContext();
            intent.setClass(context, SampleExtensionService.class);
            context.startService(intent);
        }
    }

    private void locationStart() {
        Log.d("debug", "locationStart()");

        // LocationManager インスタンス生成
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 部屋の中でGPSを取得する場合はProviderを切り替える
//        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            // GPSを設定するように促す
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            Log.d("debug", "not gpsEnable, startActivity");
        } else {
            Log.d("debug", "gpsEnabled");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);

            Log.d("debug", "checkSelfPermission false");
            return;
        }

        // 部屋の中でGPSを取得する場合はProviderを切り替える
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            // GPSの使用
            case 1000:
                // 使用が許可された
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("debug", "checkSelfPermission true");


                    return;

                } else {
                    // それでも拒否された時の対応
                    Toast toast = Toast.makeText(this, "これ以上なにもできません", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            // ストレージの使用
            case 2000:
                // 使用が許可された
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("debug", "checkSelfPermission true");
                    return;

                } else {
                    // それでも拒否された時の対応
                    Toast toast = Toast.makeText(this, "これ以上なにもできません", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d("debug", "LocationProvider.AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        long LapTime = System.currentTimeMillis() - startTime;

        // 初回更新時はデータが１つしかないので緯度と経度を各変数に入れておく
        if (beforeLat == 0 && beforeLon == 0) {
            beforeLon = location.getLongitude();
            beforeLat = location.getLatitude();
        } else { // 2回目以降の更新では距離を算出する
            // 2点間の距離
            double disRound2 = calcDistance(beforeLon, beforeLat, location.getLongitude(), location.getLatitude());
            totalDisRound2 += disRound2;
            // 1巡目に保存した距離のデータと今更新した座標から求めた2点間の距離を比較して
            for (int j = 0; j < round.points.length - 1; j++) {
                if (totalDisRound2 < totalDisRound1) {
                    double speedRound1 = calcSpeed(round.points[j].distance, round.points[j].time, round.points[j + 1].time);
                    double estimatedTime = calcEstimatedTime(speedRound1, round.points[j].distance, disRound2, round.points[j].time);
                    System.out.println("Speed:" + speedRound1);
                    System.out.println("EstimatedTime:" + estimatedTime);
                    double timeDifference = LapTime - estimatedTime;
                    BigDecimal bigDecimal = new BigDecimal(timeDifference/1000);
                    bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
                    System.out.println(bigDecimal + "s");
                    // SmartEyeglassに送信
                    startExtension((bigDecimal).toString()+"s");
                    break;
                } else {
                    totalDisRound1 += round.points[j].distance;
                }
            }
            // 座標更新
            beforeLon = location.getLongitude();
            beforeLat = location.getLatitude();
            TextView lon = (TextView) findViewById(R.id.lon);
            TextView lat = (TextView) findViewById(R.id.lat);
            TextView measuring = (TextView) findViewById(R.id.measuring);
            lon.setText(Double.toString(location.getLongitude()));
            lat.setText(Double.toString(location.getLatitude()));
            measuring.setText("Get GPS");
            System.out.println(location.getLongitude());
            System.out.println(location.getLatitude());
        }
    }

    /**
     *  Start the app with the message "Hello SmartEyeglass"
     */
    public void startExtension(String displayData) {
        Log.d("startExtension", displayData);
        // Check ExtensionService is ready and referenced
        if (SampleExtensionService.Object != null) {
            Log.d("startExtension", "SampleExtensionService.Object != null");
            if (SampleExtensionService.SmartEyeglassControl != null) {
                Log.d("startExtension", "SampleExtensionService.SmartEyeglassControl != null");
                SampleExtensionService.SmartEyeglassControl.message = displayData;
                SampleExtensionService.SmartEyeglassControl.updateDisplay();
            }
        }
    }

    // 二点間の距離計算
    public static double calcDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double distance = Math.sqrt(Math.pow((longitude2 - longitude1) * 40075 * 1000 * Math.cos(Math.toRadians(35)) / 360, 2) + Math.pow((latitude2 - latitude1) * 40009 * 1000 / 360, 2));
        return distance;
    }

    // 一巡目の二点間のspeed計算
    public static double calcSpeed(double disRound1, long timeRound1_1, long timeRound1_2) {
        double speed = disRound1 / (timeRound1_2 - timeRound1_1);
        return speed;
    }

    // 推定時間計算
    public static double calcEstimatedTime(double speed, double disRound1, double disRound2, long time) {
        double estimatedTime = (disRound2 - disRound1) / speed + time;
        return estimatedTime;
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}


