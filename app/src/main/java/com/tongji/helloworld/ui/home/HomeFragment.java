package com.tongji.helloworld.ui.home;
import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.tongji.helloworld.R;
import com.tongji.helloworld.engine.DataSource;
import com.tongji.helloworld.engine.FlightInfo;
import com.tongji.helloworld.service.UpdateAirplanePosService;
import com.tongji.helloworld.util.FlightInfoReceiver;

import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private MapView mMapView = null;
    private LocationClient mLocationClient = null;
    private BaiduMap mMap = null;
    public static boolean isLocated = false;
    private DataSource dataSource;
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

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mMapView = root.findViewById(R.id.bmapView);
        initMyMap();
        //RecyclerView city_list = getActivity().findViewById(R.id.city_list);
        //city_list.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

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
}