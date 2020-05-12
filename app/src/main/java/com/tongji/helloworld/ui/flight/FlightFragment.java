package com.tongji.helloworld.ui.flight;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Gradient;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.tongji.helloworld.R;
import com.tongji.helloworld.engine.DataSource;
import com.tongji.helloworld.ui.flight.Dialog.CityPickerDialog;
import com.tongji.helloworld.ui.flight.Interface.HeatMapOperation;
import com.tongji.helloworld.util.FlightInfoReceiver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FlightFragment extends Fragment implements HeatMapOperation {

    int[] DEFAULT_GRADIENT_COLORS = {Color.rgb(102, 225, 0), Color.rgb(255, 0, 0)};
    //设置渐变颜色起始值
    float[] DEFAULT_GRADIENT_START_POINTS = {0.2f, 1f};
    //构造颜色渐变对象
    Gradient gradient = new Gradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_START_POINTS);

    private HeatMap mCustomHeatMap;
    private FlightViewModel flightViewModel;
    private MapView mMapView = null;
    private LocationClient mLocationClient = null;
    private BaiduMap mMap = null;
    public static boolean isLocated = false;
    private DataSource dataSource;

    private static final int OPEN_SET_REQUEST_CODE = 100;
    private String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS};


    /*private UpdateAirplanePosService.UpdateAirplanePosBinder mUAPBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUAPBinder = (UpdateAirplanePosService.UpdateAirplanePosBinder)service;
            mUAPBinder.startPostionUpdate();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };*/

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            if(location == null || mMapView == null){
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mMapView.getMap().setMyLocationData(locData);
            LatLng point = new LatLng(locData.latitude, locData.longitude);
            if(!isLocated) {
                mMapView.getMap().setMapStatus(MapStatusUpdateFactory.newLatLng(point));
                isLocated = true;
            }

        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("saturn", "Home Fragment's view created");

        flightViewModel = ViewModelProviders.of(this).get(FlightViewModel.class);
        View root = inflater.inflate(R.layout.fragment_flight, container, false);
        mMapView = root.findViewById(R.id.bmapView);
        if (missPermission(permissions)){
            ActivityCompat.requestPermissions(getActivity(), permissions, OPEN_SET_REQUEST_CODE);
        }

        initMyMap();

        setHasOptionsMenu(true);

        return root;
    }

    void initMyMap(){
        SDKInitializer.setCoordType(CoordType.BD09LL);

        mMap = mMapView.getMap();

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setOverlookingGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        mLocationClient = new LocationClient(getActivity().getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        mLocationClient.start();

        dataSource = new DataSource(mMap);
        dataSource.startUpdateAirplanePos();
        //dataSource.showHeatMap();
        mMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.HMAction:
                CityPickerDialog cityPicker = new CityPickerDialog();
                cityPicker.setHeatMapOperation(this);
                cityPicker.show(getFragmentManager(), "CITYPICKER");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        Log.d("saturn", "Home Fragment resumed");
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        Log.d("saturn", "Home Fragment paused");
        //dataSource.stopUpdate();
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d("saturn", "Home Fragment destroyed");
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void showHeatMap(final String city, final int timespan) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Resources res = getResources();
                    String[] cities = res.getStringArray(R.array.cities);
                    Field field = R.string.class.getField(city);

                    int id = field.getInt(field);


                    String latlng = res.getString(id);
                    String[] p = latlng.split(",");
                    double minLat = Double.parseDouble(p[0]);
                    double maxLat = Double.parseDouble(p[1]);
                    double minLng = Double.parseDouble(p[2]);
                    double maxLng = Double.parseDouble(p[3]);

                    List<LatLng> randomList = FlightInfoReceiver.getHistoryTrack(minLng, maxLng, minLat, maxLat, timespan);

                    if(randomList.isEmpty()){
                        Toast.makeText(getContext(), "Failed to load flight data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mCustomHeatMap = new HeatMap.Builder()
                            .data(randomList)
                            .gradient(gradient)
                            .build();

                    mMap.addHeatMap(mCustomHeatMap);

                    MapStatus status = new MapStatus.Builder().target(new LatLng((minLat + maxLat) / 2,
                            (minLng + maxLng) / 2))
                            .build();
                    MapStatusUpdate update = MapStatusUpdateFactory.newMapStatus(status);
                    mMap.setMapStatus(update);

                    dataSource.stopUpdate();
                    dataSource.removeAircraft();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void hideHeatMap() {
        dataSource.startUpdateAirplanePos();
        if(mCustomHeatMap != null){
            mCustomHeatMap.removeHeatMap();
        }
    }

    private boolean missPermission(String[] permissions){
        for (String permission : permissions){
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case OPEN_SET_REQUEST_CODE:
                if (grantResults.length > 0){
                    for (int i = 0; i < grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(getContext(), "未获取位置权限", Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                }
        }
    }
}