package com.tongji.helloworld.engine;

import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

public class FlightInfo {
    public String icao, registerNo, callSiginal;
    public LatLng position;
    public double horizontalVelocity, veticalVelocity;
    public int height, direction;

    FlightInfo(JSONArray data){
        try {
            /*icao = data.getString(0);
            registerNo = data.getString(1);
            callSiginal = data.getString(2);
            latitude = data.getDouble(3);
            longitude = data.getDouble(4);
            height = data.getInt(5);
            horizontalVelocity = data.getDouble(6);
            direction = data.getDouble(7);
            veticalVelocity = data.getDouble(8);*/
            icao = data.getString(0);
            position = new LatLng(data.getDouble(1), data.getDouble(2));
            height = data.getInt(3);
            direction = data.getInt(4);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    FlightInfo(String _icao, double _lat, double _lng, int _height, int _direction){
        icao = _icao;
        position = new LatLng(_lat, _lng);
        height = _height;
        direction = _direction;
    }
}
