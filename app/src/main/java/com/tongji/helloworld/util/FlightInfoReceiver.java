package com.tongji.helloworld.util;

import com.tongji.helloworld.engine.FlightInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlightInfoReceiver {
    public static List<FlightInfo> getCurrentFlightInfo(double minLon, double maxLon, double minLat, double maxLat){
        ReceiveThread thread = new ReceiveThread(minLon, maxLon, minLat, maxLat);
        try {
            thread.start();
            thread.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return thread.flightInfoList;
    }
}


class ReceiveThread extends Thread{
    List<FlightInfo> flightInfoList;

    public ReceiveThread(double minLon, double maxLon, double minLat, double maxLat) {
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.minLat = minLat;
        this.maxLat = maxLat;
    }

    public void run(){
        String data = getFlightInfoDataFromRadar(minLon, maxLon, minLat, maxLat);
        flightInfoList = parseDataFramRadar(data);
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
                FlightInfoList.add(new FlightInfo(icao, lat, lng, altitude, direction));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return FlightInfoList;
    }

    private double minLon, maxLon, minLat, maxLat;
}