package com.tongji.helloworld.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class UpdateAirplanePosService extends Service {
    private UpdateAirplanePosBinder mBinder = new UpdateAirplanePosBinder();

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand invoke");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        System.out.println("onDestroy invoke");
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class UpdateAirplanePosBinder extends Binder{
        public void startPostionUpdate(){
            new Thread(new Runnable() {
                @Override
                public void run() {

                }
            }).start();
        }
    }
}


