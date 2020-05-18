package com.tongji.helloworld.engine;

public class FlightDetail {
    public String icao, type, start, end, flight;
    public int height, direction;
    public double lat, lng;

    public FlightDetail(String _icao, double _lat, double _lng, int _height, int _direction, String _type, String _start, String _end, String _flight){
        icao = _icao;
        lat = _lat;
        lng = _lng;
        height = _height;
        direction = _direction;
        start = _start;
        end = _end;
        flight = _flight;
        this.type = _type;
    }
}
