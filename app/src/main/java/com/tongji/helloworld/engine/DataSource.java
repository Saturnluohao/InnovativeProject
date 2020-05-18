package com.tongji.helloworld.engine;

import android.annotation.SuppressLint;
import android.util.Log;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.tongji.helloworld.R;
import com.tongji.helloworld.util.MarkerUpdater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DataSource {
    private BaiduMap mBaiduMap;
    private boolean shouldExit;
    private String token = "";
    private String tokenUrl = "https://zh.flightaware.com/live/";
    private String queryurl = "https://zh.flightaware.com/ajax/ignoreall/vicinity_aircraft.rvt?";
    public MarkerUpdater markerUpdater;

    public DataSource(BaiduMap map){
        mBaiduMap = map;
        markerUpdater = new MarkerUpdater();
        shouldExit = false;
        initToken();
    }

    private void initToken(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(tokenUrl);
                    String content = "";
                    String line;
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    if(conn.getResponseCode() != 200){
                        return;
                    }
                    BufferedReader isr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while((line = isr.readLine()) != null){
                        content += line;
                    }
                    int start = content.indexOf("VICINITY_TOKEN");
                    int end = content.indexOf(',', start);
                    token = content.substring(start + 17, end - 1);

                    isr.close();
                    conn.disconnect();
                }catch (MalformedURLException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startUpdateAirplanePos(){
        shouldExit = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!shouldExit){
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


    private String getTrack(){
        StringBuilder json = new StringBuilder();
        String line;
        try {
            URL url = new URL("http://192.168.2.103:5000/track");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK){
                return "";
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while((line = br.readLine()) != null){
                json.append(line);
            }
            br.close();
            conn.disconnect();

        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return json.toString();
    }


    public void stopUpdate(){
        shouldExit = true;
    }

    public void removeAircraft(){
        markerUpdater.removeAllCraft();
    }

    private void setAirplanePos() {
        if(mBaiduMap == null || mBaiduMap.getMapStatus() == null)
            return;

        List<OverlayOptions> options = new ArrayList<OverlayOptions>();
        BitmapDescriptor bitmap = new BitmapDescriptorFactory().fromResource(R.drawable.airplane);
        LatLng neBound = mBaiduMap.getMapStatus().bound.northeast;
        LatLng swBound = mBaiduMap.getMapStatus().bound.southwest;
        String flightInfoData = getFlightInfoDataFromRadar(swBound.longitude, neBound.longitude, swBound.latitude, neBound.latitude);
        List<FlightInfo> flightInfoList = parseDataFramRadar(flightInfoData);
        markerUpdater.updateOverlayMap(flightInfoList, mBaiduMap);
    }

    private String getFlightInfoData(double minLon, double maxLon, double minLat, double maxLat){
        StringBuilder json = new StringBuilder();
        try {
//            String queryString = "?minLon=" + minLon + "&maxLon=" + maxLon + "&minLat=" + minLat + "&maxLat=" + maxLat;
//            URL url = new URL(host + "/flightInfo" + queryString);
            String queryString = "minLon=" + minLon + "&maxLon=" + maxLon + "&minLat=" + minLat + "&maxLat=" + maxLat + "&token=" + token;
            URL url = new URL(queryurl + queryString);
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

    private String getFlightInfoDataFromRadar(double minLon, double maxLon, double minLat, double maxLat){
        StringBuilder json = new StringBuilder();
        try {
            String queryString = String.format("https://data-live.flightradar24.com/zones/fcgi/feed.js?bounds=%f,%f,%f,%f&faa=1&satellite=1&mlat=1&flarm=1&adsb=1&gnd=1&air=1&vehicles=1&estimated=1&maxage=14400&gliders=1&stats=1",
                    maxLat, minLat, minLon, maxLon);
            URL url = new URL(queryString);
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

    private ArrayList<FlightInfo> parseData(String data){
        ArrayList<FlightInfo> FlightInfoList = new ArrayList<FlightInfo>();
        try {
            JSONArray flightInfo = new JSONObject(data).getJSONArray("features");
            for(int i = 0; i < flightInfo.length(); i++){
                JSONObject item = flightInfo.getJSONObject(i);
                JSONArray point = item.getJSONObject("geometry").getJSONArray("coordinates");
                Double lat = point.getDouble(1);
                Double lng = point.getDouble(0);
                JSONObject prop = item.getJSONObject("properties");
                int altitude = prop.optInt("altitude", 0);
                int direction = prop.getInt("direction");
                String icao = prop.getString("flight_id");
                FlightInfoList.add(new FlightInfo(icao, lat, lng, altitude, direction, "hhh"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return FlightInfoList;
    }

    private ArrayList<FlightInfo> parseDataFramRadar(String data){
        ArrayList<FlightInfo> FlightInfoList = new ArrayList<FlightInfo>();
        try {
            JSONObject flightInfo = new JSONObject(data);
            flightInfo.remove("full_count");
            flightInfo.remove("version");
            flightInfo.remove("stats");
            Iterator<String> keys = flightInfo.keys();

            while(keys.hasNext()){
                String key = keys.next();
                JSONArray item = flightInfo.getJSONArray(key);
                String icao = item.getString(0);
                Double lat = item.getDouble(1);
                Double lng = item.getDouble(2);
                int direction = item.getInt(3);
                int altitude = item.getInt(4);
                FlightInfoList.add(new FlightInfo(icao, lat, lng, altitude, direction, "hhh"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return FlightInfoList;
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
