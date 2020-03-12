package com.tongji.helloworld.ui.notifications;

import com.tongji.helloworld.engine.FlightInfo;

public class MyFlightInfo {
    FlightInfo flightInfo;
    double distance;
    double bearing;
    public MyFlightInfo(FlightInfo _flightInfo, double _distance, double _bearing){
        flightInfo = _flightInfo;
        distance = _distance;
        bearing = _bearing;
    };
}
