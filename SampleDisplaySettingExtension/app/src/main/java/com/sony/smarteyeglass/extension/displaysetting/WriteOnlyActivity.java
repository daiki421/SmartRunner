package com.sony.smarteyeglass.extension.displaysetting;

import android.Manifest;
import android.app.Activity;
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

import java.io.File;
import java.io.IOException;

public class WriteOnlyActivity extends Activity implements LocationListener {

    private LocationManager locationManager;
    private long startTime;
    private RoundWriter roundWriter;
    private double beforeLon = 0;
    private double beforeLat = 0;
    private double beforeDis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measure_layout);
        startTime = System.currentTimeMillis();

        Button startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                System.out.println("Push Start");

                startExtension("Get GPS Data...");
                // スタートボタン押下時を0秒とする
                startTime = System.currentTimeMillis();
                // ディレクトリのパスを指定し、ファイルを作成
                try {
                    final String directoryPath = android.os.Environment.getExternalStorageDirectory().getPath() + "/DataList/";
                    final String filePath = directoryPath + System.currentTimeMillis() + ".csv";
                    Log.d("displaysetting", filePath);
                    new File(directoryPath).mkdir();
                    roundWriter = new RoundWriter(filePath);
                    locationStart();
                } catch (IOException e) {
                    Toast.makeText(WriteOnlyActivity.this, "ファイルが作成できません", Toast.LENGTH_SHORT).show();
                    Log.e("displaysetting", "", e);
                }
            }
        });

        Button stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                System.out.println("Push Stop");
                try {
                    // writeしたデータを保存
                    roundWriter.flush();
                    WriteOnlyActivity.this.finish();
                    Intent intent = new Intent(WriteOnlyActivity.this, ListViewActivity.class);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void locationStart() {
        Log.d("debug", "locationStart()");

        // LocationManager インスタンス生成
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 部屋の中でGPSを取得する時はProviderを切り替える
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
        // 部屋の中でGPSを取得する時はProviderを切り替える
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

                    locationStart();
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
        if (beforeLat == 0 && beforeLon == 0) { // 初回の座標更新
            // writeで格納
            try {
                roundWriter.write(0, LapTime, location.getLongitude(), location.getLatitude());
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 初回更新時はデータが１つしかないのでDistanceに0を格納した後,次の更新時に使うためにbeforeLon,beforeLatにデータを入れておく
            beforeLon = location.getLongitude();
            beforeLat = location.getLatitude();
        } else { // 2回目以降の座標更新
            double disRound2 = calcDistance(beforeLon, beforeLat, location.getLongitude(), location.getLatitude());

            try {
                roundWriter.write(disRound2, LapTime, location.getLongitude(), location.getLatitude());
            } catch (IOException e) {
                e.printStackTrace();
            }
            beforeLon = location.getLongitude();
            beforeLat = location.getLatitude();
        }
        Log.d("location", String.valueOf(location.getLatitude()));
        Log.d("location", String.valueOf(location.getLongitude()));
        TextView lon = (TextView) findViewById(R.id.lon);
        TextView lat = (TextView) findViewById(R.id.lat);
        lon.setText(Double.toString(location.getLongitude()));
        lat.setText(Double.toString(location.getLatitude()));
        System.out.println(location.getLongitude());
        System.out.println(location.getLatitude());
    }

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
    public static double calcSpeed(double disRound1, int timeRound1_1, int timeRound1_2) {
        double speed = disRound1 / (timeRound1_2 - timeRound1_1);
        return speed;
    }

    // 推定時間計算
    public static double calcEstimatedTime(double speed, double disRound1, double disRound2, int time) {
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


