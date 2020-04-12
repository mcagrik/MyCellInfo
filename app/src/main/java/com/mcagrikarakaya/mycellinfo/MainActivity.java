package com.mcagrikarakaya.mycellinfo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST = 1337;

    private static final String TAG = "My Cell Info";

    //用于获取GPS
    private LocationManager locationManager;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //若要实时获取GPS更新，可在此处理
            Log.i(TAG, "Location changed...");
            Log.i(TAG, "Latitude :        " + location.getLatitude());
            Log.i(TAG, "Longitude :       " + location.getLongitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    //用于获取基站信息
    private TelephonyManager telephonyManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!canAccessLocation()) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }
        locationManager.requestLocationUpdates(provider, 1000, 1, locationListener);//参数依次为provider（GPS，或者NETWORK_PROVIDER或PASSIVE_PROVIDER），执行更新的最小时间，执行更新的最小距离，更新后的listener
//        locationManager.requestSingleUpdate(provider, locationListener, null);//或者仅仅进行单词更新

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);

    }

    public void showGps(View view) {
        TextView tv = findViewById(R.id.cell_value);
        if (locationManager != null) {
            String provider = LocationManager.GPS_PROVIDER;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                tv.setText("无权限");
                return;
            }
            Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
            if (lastKnownLocation != null) {
                Double lat = lastKnownLocation.getLatitude();
                Double lon = lastKnownLocation.getLongitude();
                Double alt = lastKnownLocation.getAltitude();
                Float acc = lastKnownLocation.getAccuracy();
                Float spd = lastKnownLocation.getSpeed();
                tv.setText("Latitude: " + lat + ", Longitude: " + lon + ",\nAltitude: " + alt + ", Accuracy: " + acc + ", Speed: " + spd);
            } else {
                tv.setText("未获取到GPS");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showCellinfo(View view) {
        TextView tv = findViewById(R.id.cell_value);
        List<CellInfo> cellInfoList = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            tv.setText("无权限");
            //return;
            if (!canAccessLocation()) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }


        }
        cellInfoList = telephonyManager.getAllCellInfo();
        if (cellInfoList == null) {
            tv.setText("getAllCellInfo()返回null");
        } else if (cellInfoList.size() == 0) {
            tv.setText("基站列表为空");
        } else {
            int cellNumber = cellInfoList.size();
            BaseStation main_BS = bindData(cellInfoList.get(0));
            tv.setText("获取到" + cellNumber + "个基站, \n主基站信息：\n" + main_BS.toString());
            for (CellInfo cellInfo : cellInfoList) {
                BaseStation bs = bindData(cellInfo);
                Log.i(TAG, bs.toString());
            }
        }

    }

    private BaseStation bindData(CellInfo cellInfo) {
        BaseStation baseStation = null;
        //基站有不同信号类型：2G，3G，4G
        if (cellInfo instanceof CellInfoWcdma) {
            //联通3G
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
            CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
            baseStation = new BaseStation();
            baseStation.setType("WCDMA");
            baseStation.setCid(cellIdentityWcdma.getCid());
            baseStation.setLac(cellIdentityWcdma.getLac());
            baseStation.setMcc(cellIdentityWcdma.getMcc());
            baseStation.setMnc(cellIdentityWcdma.getMnc());
            baseStation.setBsic_psc_pci(cellIdentityWcdma.getPsc());
            if (cellInfoWcdma.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoWcdma.getCellSignalStrength().getAsuLevel()); //Get the signal level as an asu value between 0..31, 99 is unknown Asu is calculated based on 3GPP RSRP.
                baseStation.setSignalLevel(cellInfoWcdma.getCellSignalStrength().getLevel()); //Get signal level as an int from 0..4
                baseStation.setDbm(cellInfoWcdma.getCellSignalStrength().getDbm()); //Get the signal strength as dBm
            }
        } else if (cellInfo instanceof CellInfoLte) {
            //4G
            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
            CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
            baseStation = new BaseStation();
            baseStation.setType("LTE");
            baseStation.setCid(cellIdentityLte.getCi());
            baseStation.setMnc(cellIdentityLte.getMnc());
            baseStation.setMcc(cellIdentityLte.getMcc());
            baseStation.setLac(cellIdentityLte.getTac());
            baseStation.setBsic_psc_pci(cellIdentityLte.getPci());
            if (cellInfoLte.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoLte.getCellSignalStrength().getAsuLevel());
                baseStation.setSignalLevel(cellInfoLte.getCellSignalStrength().getLevel());
                baseStation.setDbm(cellInfoLte.getCellSignalStrength().getDbm());
            }
        } else if (cellInfo instanceof CellInfoGsm) {
            //2G
            CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
            CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
            baseStation = new BaseStation();
            baseStation.setType("GSM");
            baseStation.setCid(cellIdentityGsm.getCid());
            baseStation.setLac(cellIdentityGsm.getLac());
            baseStation.setMcc(cellIdentityGsm.getMcc());
            baseStation.setMnc(cellIdentityGsm.getMnc());
            baseStation.setBsic_psc_pci(cellIdentityGsm.getPsc());
            if (cellInfoGsm.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoGsm.getCellSignalStrength().getAsuLevel());
                baseStation.setSignalLevel(cellInfoGsm.getCellSignalStrength().getLevel());
                baseStation.setDbm(cellInfoGsm.getCellSignalStrength().getDbm());
            }
        } else {
            //电信2/3G
            Log.e(TAG, "CDMA CellInfo................................................");
        }
        return baseStation;
    }


}
