package com.tongji.helloworld.ui.notifications;

public class FlightOnScreen {
    MyFlightInfo myFlightInfo;
    double mPosX;
    double mPosY;
    double mWidth;
    double mHeight;
    FlightOnScreen(MyFlightInfo _myFlightInfo, double _mPosX, double _mPosY, double _mWidth, double _mHeight){
        myFlightInfo = _myFlightInfo;
        mPosX = _mPosX;
        mPosY = _mPosY;
        mWidth = _mWidth;
        mHeight = _mHeight;
    }
    public boolean IsClick(double x, double y) {
        boolean isClick = false;
        if (x >= mPosX && x <= mPosX + mWidth && y >= mPosY
                && y <= mPosY + mHeight) {
            isClick = true;
        }
        return isClick;
    }
}
