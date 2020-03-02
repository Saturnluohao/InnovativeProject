package com.tongji.helloworld.engine;

import android.app.Application;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Gradient;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.tongji.helloworld.R;
import com.tongji.helloworld.util.MarkerUpdater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class DataSource {
    private BaiduMap mBaiduMap;
    private boolean shouldExit;
    public MarkerUpdater markerUpdater;

    public DataSource(BaiduMap map){
        mBaiduMap = map;
        markerUpdater = new MarkerUpdater();
        shouldExit = false;
    }

    public void startUpdateAirplanePos(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true && !shouldExit){
                    try {
                        Log.d("saturn", "updating maker");
                        setAirplanePos();
                        Thread.sleep(2000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void showHeatMap(){
        int[] DEFAULT_GRADIENT_COLORS = {Color.rgb(102, 225, 0), Color.rgb(255, 0, 0)};
//设置渐变颜色起始值
        float[] DEFAULT_GRADIENT_START_POINTS = {0.2f, 1f};
//构造颜色渐变对象
        Gradient gradient = new Gradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_START_POINTS);
        List<LatLng> randomList = new ArrayList<LatLng>();

        Random r = new Random();
        for (int i = 0; i < 500; i++) {
            // 116.220000,39.780000 116.570000,40.150000
            int rlat = r.nextInt(370000);
            int rlng = r.nextInt(370000);
            int lat = 39780000 + rlat;
            int lng = 116220000 + rlng;
            LatLng ll = new LatLng(lat / 1E6, lng / 1E6);
            randomList.add(ll);
        }
        HeatMap mCustomHeatMap = new HeatMap.Builder()
                .data(randomList)
                .gradient(gradient)
                .build();

        mBaiduMap.addHeatMap(mCustomHeatMap);
    }

    public void stopUpdate(){
        shouldExit = true;
    }

    private void setAirplanePos() {
        if(mBaiduMap == null || mBaiduMap.getMapStatus() == null)
            return;

        List<OverlayOptions> options = new ArrayList<OverlayOptions>();
        BitmapDescriptor bitmap = new BitmapDescriptorFactory().fromResource(R.drawable.airplane);
        LatLng neBound = mBaiduMap.getMapStatus().bound.northeast;
        LatLng swBound = mBaiduMap.getMapStatus().bound.southwest;
        String flightInfoData = getFlightInfoData(swBound.longitude, neBound.longitude, swBound.latitude, neBound.latitude, "http://www.saturnluo.cn:5000");
        List<FlightInfo> flightInfoList = getFlightInfoList(flightInfoData);
        markerUpdater.updateOverlayMap(flightInfoList, mBaiduMap);
    }

    private String getFlightInfoData(double minLon, double maxLon, double minLat, double maxLat, String host){
        StringBuilder json = new StringBuilder();
        try {
            String queryString = "?minLon=" + minLon + "&maxLon=" + maxLon + "&minLat=" + minLat + "&maxLat=" + maxLat;
            URL url = new URL(host + "/flightInfo" + queryString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            if (urlConnection.getResponseCode() != 200) {
                return "";
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection
                    .getInputStream(), "utf-8"));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
            urlConnection.disconnect();
        }catch (java.io.IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private ArrayList<FlightInfo> getFlightInfoList(String data){
        ArrayList<FlightInfo> FlightInfoList = new ArrayList<FlightInfo>();
        try {
            JSONArray items = new JSONObject(data).getJSONArray("data");
            for (int i = 0; i < items.length(); ++i) {
                FlightInfo newData = new FlightInfo(items.getJSONArray(i));
                FlightInfoList.add(newData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return FlightInfoList;
    }
}
