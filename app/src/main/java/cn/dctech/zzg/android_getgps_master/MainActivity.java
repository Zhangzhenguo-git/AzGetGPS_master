package cn.dctech.zzg.android_getgps_master;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity {
    @InjectView(R.id.tv_GetGPS)
    Button tvGetGPS;
    @InjectView(R.id.tv_ShowInfo)
    TextView tvShowInfo;

    private double latitude = 0.0;
    private double longitude = 0.0;
    private Button info;
    private LocationManager myLocationManager;
    private static final int REQUEST_PERMISSION = 0;

    private int GPS_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initView();
        myClick();
    }

    private void initView() {
        //实例化Location应用管理类
        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private void myClick() {
        tvGetGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager pkgManager = getPackageManager();
                // 读写 sd card 权限非常重要, android6.0默认禁止的, 建议初始化之前就弹窗让用户赋予该权限
                boolean gpsPermission = pkgManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName()) == PackageManager.PERMISSION_GRANTED;
                if (Build.VERSION.SDK_INT >= 23 && !gpsPermission) {
                    requestPermission();
                } else {
                    checkGPSIsStart();
                }
            }
        });
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //回调，判断用户到底点击是还是否。
        //如果同时申请多个权限，可以for循环遍历
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //写入你需要权限才能使用的方法
            checkGPSIsStart();
        } else {
            // 没有获取 到权限，从新请求，或者关闭app
            Toast.makeText(this, "需要获得XXX权限", Toast.LENGTH_SHORT).show();
            requestPermission();
        }
    }

    /**
     * 判断当前位置服务是否打开
     * 如果未打开进行跳转到设置服务界面，然后延迟进行获取经纬度
     *
     * @return
     */
    private void checkGPSIsStart() {
        boolean isOpen = false;
        isOpen = myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isOpen) {
            getLocation();
        } else {
            //跳转GPS设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, GPS_REQUEST_CODE);
            new Handler() {
            }.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getLocation();
                }
            }, 2000);
        }
    }

    /**
     * 获取经纬度进行显示
     */
    private String getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        //查找到位置服务
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);//高精度
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗

        String provider = myLocationManager.getBestProvider(criteria, true); // 获取GPS信息

        Location location = myLocationManager.getLastKnownLocation(provider);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        } else {
            myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, myLocationListener);
        }
        tvShowInfo.setText("纬度：" + latitude + "\n" + "经度：" + longitude);
        return "纬度：" + latitude + "\n" + "经度：" + longitude;
    }

    /**
     * 获取GPS位置监听器，包含四个不同触发方式
     */
    LocationListener myLocationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        // 当位置获取（GPS）打开时调用此方法
        @Override
        public void onProviderEnabled(String provider) {
            Log.d("执行，GPS的打开", provider);
        }

        // 当位置获取（GPS）关闭时调用此方法
        @Override
        public void onProviderDisabled(String provider) {
            Log.d("执行，GPS关闭", provider);
        }

        // 当坐标改变时触发此方法，如果获取到相同坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                latitude = location.getLatitude(); // 经度
                longitude = location.getLongitude(); // 纬度
                Log.d("执行，坐标发生改变", "Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());
            }
        }
    };
}
