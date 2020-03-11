package com.tongji.helloworld.ui.notifications;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tongji.helloworld.R;
import com.tongji.helloworld.engine.FlightInfo;
import com.tongji.helloworld.util.FlightInfoReceiver;

import java.util.List;

public class MySurfaceView2 extends SurfaceView implements SurfaceHolder.Callback, Runnable, SensorEventListener {
    private SurfaceHolder mHolder;//SurfaceHolder
    private Canvas mCanvas;//用于绘制的Canvas
    private boolean mIsDrawing;//子线程标志位

    List<FlightInfo> flightInfoList;//周围飞机信息列表，只在界面初始化时更新一次
    private final double radius = 1;//寻找以手机位置为圆心，以该值为半径的圆以内的飞机

    private SensorManager sensorManager;
    private float pitch_angle = 0;//屏幕俯仰角（随时更新）
    public static double compass_angle = 0;
    private double longitude;//经度（随时更新）
    private double latitude;//纬度（随时更新）
    int screenWidth;//手机屏幕宽度
    int screenHeight;//手机屏幕高度


    public MySurfaceView2(Context context) {
        super(context);
        init();
    }

    public MySurfaceView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySurfaceView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        setZOrderOnTop(true);//使surfaceview放到最顶层
        mHolder.setFormat(PixelFormat.TRANSLUCENT);//使窗口支持透明度
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);

        //设置传感器
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);//用于获取俯仰角
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_GAME);

        //获取定位
        getLocation();

        //获取手机屏幕长宽度
        Display display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        //加载周围飞机
        flightInfoList = FlightInfoReceiver.getCurrentFlightInfo(
                longitude-radius,longitude+radius,latitude-radius,latitude+radius);
//        FlightInfo flightInfo = new FlightInfo("MH370", 100, 30, 5000, 1);
//        flightInfoList = flightInfoList.subList(0,0);
//        flightInfoList.add(flightInfo);
        Log.d("列表中的飞机数：", String.valueOf(flightInfoList.size()));
    }


    //创建
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        new Thread(this).start();

    }

    //改变
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    //销毁
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {//此函数会被循环调用
        while (mIsDrawing) {
            //draw();
            drawPlane();
            try {
                Thread.sleep(200); // 让线程休息1000毫秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int test = 0;

    private void drawPlane() {//画飞机
        if (pitch_angle < -100) {//举起手机,出现手机
            try {
                mCanvas = mHolder.lockCanvas();
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色（画布大小为整个屏幕）

                //这里写要绘制的内容

                //画笔操作
                Paint p = new Paint();//新建画笔
                p.setColor(Color.RED); // 设置画笔的颜色为红色
                p.setTextSize(80);//字体大小


                //渲染飞机
                for(FlightInfo flightInfo : flightInfoList){
                    float results[] = new float[3];
                    computeDistanceAndBearing(latitude, longitude, flightInfo.position.latitude, flightInfo.position.longitude, results);
                    float distance = results[0];
                    float initialBearing = results[1];
                    float finalBearing = results[2];

                    //计算方向角之差
                    float bearingDiff = (float) (compass_angle - finalBearing);
                    float absBearingDiff = (float) Math.abs(compass_angle - finalBearing);
                    //计算飞机在屏幕左侧还是右侧
                    int leftRight = (bearingDiff > 0 && absBearingDiff < 180) || (bearingDiff < 0 && absBearingDiff > 180) ? -1 : 1;
                    //将方向角之差修正为小于180度
                    if (absBearingDiff > 180) {
                        absBearingDiff = 360 - absBearingDiff;
                    }
                    //如果方向角之差小于30度，则渲染
                    if(absBearingDiff < 30){
                        //绘制图标
                        //加载飞机图标
                        Bitmap bitmap;
                        String type = flightInfo.type;
                        if(type.charAt(0) == 'A'){//空客的飞机
                            switch (type){
                                case "A300":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a300);
                                    break;
                                case "A318":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a318);
                                    break;
                                case "A319":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a319);
                                    break;
                                case "A320":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a320);
                                    break;
                                case "A20N":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a320neo);
                                    break;
                                case "A21N"://将A321neo趋近于A320neo
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a320neo);
                                    break;
                                case "A321":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a321);
                                    break;
                                case "A330":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a330m200);
                                    break;
                                case "A333"://将a330-300趋近于a330-200
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a330m200);
                                    break;
                                case "A30N":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a330neo);
                                    break;
                                case "A332":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a330m200);
                                    break;
                                case "A340":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a340m300);
                                    break;
                                case "A350":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a350);
                                    break;
                                case "A380":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.a380);
                                    break;
                                default:
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.airbus);
                            }
                        }
                        else if(type.charAt(0) == 'B'){//波音的飞机
                            switch (type){
                                case "B707":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b707);
                                    break;
                                case "B717":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b717);
                                    break;
                                case "B727":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b727m100);
                                    break;
                                case "B736"://将B737-600趋近于B737-500
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b737m500);
                                    break;
                                case "B737":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b737m200);
                                    break;
                                case "B738":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b737m800);
                                    break;
                                case "B744":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b747m400);
                                    break;
                                case "B747":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b747m200);
                                    break;
                                case "B752":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b757m200);
                                    break;
                                case "B767":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b767m200);
                                    break;
                                case "B777":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b777m200);
                                    break;
                                case "B77W":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b777m300er);
                                    break;
                                case "B787":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.b787);
                                    break;
                                default:
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.boeing);
                            }
                        }
                        else{//其他机型
                            switch (type){
                                case "E190":
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.e190e2);
                                    break;
                                default:
                                    bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.plane);
                            }
                        }
                        Matrix matrix = new Matrix();//用于设置缩放比例
                        matrix.postScale((float) 0.4, (float) 0.4);//设置缩放比例
                        //压缩飞机图标
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        //计算屏幕中的横坐标
                        float left = screenWidth/2 - bitmap.getWidth()/2 + leftRight * absBearingDiff * screenWidth / 60;
                        //计算在屏幕中的纵坐标
                        float height = flightInfo.height > 40000? 40000 : flightInfo.height;//默认最高4w，超过四万按照四万算
                        float top = screenHeight * 3/4 * (1-height/40000);
                        mCanvas.drawBitmap(bitmap, left, top, new Paint());
                        //绘制航班号
                        String icao = flightInfo.type;
                        p.setTextSize(40);
                        mCanvas.drawText(icao, left+50, top + bitmap.getWidth()+20, p);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCanvas != null) {
                    mHolder.unlockCanvasAndPost(mCanvas);//提交画布内容
                }
            }
        } else {//摊平手机，手机消失
            mCanvas = mHolder.lockCanvas();
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色（画布大小为整个屏幕）
            mHolder.unlockCanvasAndPost(mCanvas);//提交画布内容
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        pitch_angle = (float) (event.values[1] * 100) / 100;
        //Log.d("方位角：", String.valueOf((float)(event.values[0]*100)/100));
        //更新方位角
        double temp_compass_angle = event.values[0] > 180? event.values[0] - 360 : event.values[0];//把 0~360 的角度转化为 -180~180
        //如果手机举起来朝天，则调转180
//        if(pitch_angle < -90){
//            if(temp_compass_angle > 0){
//                compass_angle = temp_compass_angle - 180;
//            }
//            else compass_angle = temp_compass_angle + 180;
//        }else {
//            compass_angle = temp_compass_angle;
//        }
        compass_angle = temp_compass_angle;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 以下内容为获取定位的代码
     */

    public static final int LOCATION_CODE = 301;
    private LocationManager locationManager;
    private String locationProvider;

    private void getLocation() {
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //获取权限（如果没有开启权限，会弹出对话框，询问是否开启权限）
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
            } else {
                //监视地理位置变化
                locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);
                Location location = locationManager.getLastKnownLocation(locationProvider);
                if (location != null) {
                    //输入经纬度
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    Log.d("Location:",location.getLongitude() + " " + location.getLatitude());
                    Toast.makeText(getContext(), location.getLongitude() + " " + location.getLatitude() + "", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            //监视地理位置变化
            locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null) {
                //不为空,显示地理位置经纬度
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.d("Location:",location.getLongitude() + " " + location.getLatitude());
                Toast.makeText(getContext(), location.getLongitude() + " " + location.getLatitude() + "", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public LocationListener locationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {

        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {

        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                //不为空,显示地理位置经纬度
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.d("Location:",location.getLongitude() + " " + location.getLatitude());
                Toast.makeText(getContext(), location.getLongitude() + " " + location.getLatitude() + "", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == getContext().getPackageManager().PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "申请权限", Toast.LENGTH_LONG).show();
                    try {
                        List<String> providers = locationManager.getProviders(true);
                        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                            //如果是Network
                            locationProvider = LocationManager.NETWORK_PROVIDER;
                        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
                            //如果是GPS
                            locationProvider = LocationManager.GPS_PROVIDER;
                        }
                        //监视地理位置变化
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);
                        Location location = locationManager.getLastKnownLocation(locationProvider);
                        if (location != null) {
                            //不为空,显示地理位置经纬度
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                             Toast.makeText(getContext(), location.getLongitude() + " " + location.getLatitude() + "", Toast.LENGTH_SHORT).show();
                        }
                    }catch (SecurityException e){
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), "缺少权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * 以下内容为获取两点方位角和距离的代码
     */
    private static void computeDistanceAndBearing(double lat1, double lon1, double lat2, double lon2, float[] results) {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)
        int MAXITERS = 20;
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;
        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 :
                    cosU1cosU2 * sinLambda / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 :
                    cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)
            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
            A = 1 + (uSquared / 16384.0) * // (3)
            (4096.0 + uSquared * (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) * // (4)
            (256.0 + uSquared * (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma * // (6)
            (cos2SM + (B / 4.0) * (cosSigma * (-1.0 + 2.0 * cos2SMSq) - (B / 6.0) * cos2SM *
                    (-3.0 + 4.0 * sinSigma * sinSigma) * (-3.0 + 4.0 * cos2SMSq)));
            lambda = L + (1.0 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SM + C * cosSigma *
                    (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)
            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }
        float distance = (float) (b * A * (sigma - deltaSigma));
        results[0] = distance;
        if (results.length > 1) {
            float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
                    cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
            initialBearing *= 180.0 / Math.PI;
            results[1] = initialBearing;
            if (results.length > 2) {
                float finalBearing = (float) Math.atan2(cosU1 * sinLambda, -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
                finalBearing *= 180.0 / Math.PI;
                results[2] = finalBearing;
            }
        }
    }

}
