package com.tongji.helloworld.util;

import com.baidu.mapapi.model.LatLng;
import com.tongji.helloworld.engine.FlightInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FlightInfoReceiver {
    public static List<FlightInfo> getCurrentFlightInfo(double minLon, double maxLon, double minLat, double maxLat){
        ReceiveThread thread = new ReceiveThread(minLon, maxLon, minLat, maxLat, DATATYPE.FLIGHTINFO);
        try {
            thread.start();
            thread.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return thread.flightInfoList;
    }

    public static List<LatLng> getHistoryTrack(double minLon, double maxLon, double minLat, double maxLat, int timespan){
        ReceiveThread thread = new ReceiveThread(minLon, maxLon, minLat, maxLat, DATATYPE.HISTORYTRACK, timespan);
        try{
            thread.start();
            thread.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return thread.historyTrack;
    }
}


class ReceiveThread extends Thread{
    List<FlightInfo> flightInfoList;

    List<LatLng> historyTrack;

    public ReceiveThread(double minLon, double maxLon, double minLat, double maxLat, DATATYPE datatype) {
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.datatype = datatype;
    }
    public ReceiveThread(double minLon, double maxLon, double minLat, double maxLat, DATATYPE datatype, int timespan) {
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.datatype = datatype;
        this.timespan = timespan;
    }

    public void run(){
        switch (datatype){
            case FLIGHTINFO:
                String data = getFlightInfoDataFromRadar(minLon, maxLon, minLat, maxLat);
                flightInfoList = parseDataFramRadar(data);
                break;
            case HISTORYTRACK:
                String trackData = getHistoryTrack(minLon, maxLon, minLat, maxLat, timespan);
                historyTrack = parseHistoryTrack(trackData);
                break;
        }
    }

    private String getHistoryTrack(double minLon, double maxLon, double minLat, double maxLat, int timespan){
        StringBuilder builder = new StringBuilder();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Calendar calendar = Calendar.getInstance();
            String toDate = sdf.format(calendar.getTime());

            calendar.add(Calendar.HOUR, -timespan);
            String fromDate = sdf.format(calendar.getTime());


            String queryUrl = String.format("http://49.235.244.95:12450/getFlightInfo" +
                    "?minLat=%f&maxLat=%f&minLng=%f&maxLng=%f" +
                    "&start=%s&end=%s", minLat, maxLat, minLon, maxLon, fromDate, toDate);
            URL url = new URL(queryUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            if(urlConnection.getResponseCode() != 200){
                return "";
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
            String line;
            while((line = reader.readLine()) != null){
                builder.append(line);
            }
            urlConnection.disconnect();
            reader.close();
        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return builder.toString();
    }

    private List<LatLng> parseHistoryTrack(String data){
        List<LatLng> history = new ArrayList<>();
        try {
            JSONObject tracks = new JSONObject(data);
            if(tracks.getInt("status") != 200){
                return history;
            }
            JSONArray points = tracks.getJSONArray("data");
            for(int i = 0; i < points.length(); i++){
                JSONArray point = points.getJSONArray(i);
                LatLng pos = new LatLng(point.getDouble(0), point.getDouble(1));
                history.add(pos);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return history;
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
                String type = item.getString(8);
                FlightInfoList.add(new FlightInfo(icao, lat, lng, altitude, direction, type));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return FlightInfoList;
    }

    private double minLon, maxLon, minLat, maxLat;
    private DATATYPE datatype;
    private int timespan;
}

enum DATATYPE{
    FLIGHTINFO,
    HISTORYTRACK
}