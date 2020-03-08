package com.tongji.helloworld.ui.notifications;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.tongji.helloworld.R;

public class MySurfaceView2 extends SurfaceView implements SurfaceHolder.Callback,Runnable{
    private SurfaceHolder mHolder;//SurfaceHolder
    private Canvas mCanvas;//用于绘制的Canvas
    private boolean mIsDrawing;//子线程标志位

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

    private void init(){
        mHolder=getHolder();
        mHolder.addCallback(this);
        setZOrderOnTop(true);//使surfaceview放到最顶层
        mHolder.setFormat(PixelFormat.TRANSLUCENT);//使窗口支持透明度
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);

    }


    //创建
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing=true;
        new Thread(this).start();

    }

    //改变
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    //销毁
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing=false;
    }

    @Override
    public void run() {//此函数会被循环调用
        while(mIsDrawing){
            //draw();
            drawPlane();
            try {
                Thread.sleep(50); // 让线程休息50毫秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void draw(){//画圆
        try{
            mCanvas=mHolder.lockCanvas();
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色
            //这里写要绘制的内容
            Paint p = new Paint();//新建画笔
            p.setColor(Color.WHITE); // 设置画笔的颜色为白色
            mCanvas.drawCircle(50, 50, 30, p); // 画一个圆
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(mCanvas!=null){
                mHolder.unlockCanvasAndPost(mCanvas);//提交画布内容
            }
        }
    }

    private int test = 0;
    private void drawPlane(){//画飞机
        try{
            mCanvas=mHolder.lockCanvas();
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色（画布大小为整个屏幕）

            //这里写要绘制的内容

            //画笔操作
            Paint p = new Paint();//新建画笔
            p.setColor(Color.RED); // 设置画笔的颜色为红色
            p.setTextSize(80);//字体大小

            //加载飞机图标1号
            Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.plane);
            Matrix matrix = new Matrix();
            matrix.postScale((float)0.5,(float)0.5);//设置缩放比例
            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,false);
            //int left = mCanvas.getWidth()/2 - bitmap.getWidth()/2;
            int left = ((test++)%20)*50;
            int top = 80;
            mCanvas.drawBitmap(bitmap, left, top, new Paint());//居中画一个飞机

            //渲染航班号1号
            mCanvas.drawText("MH370", left, top + bitmap.getWidth(), p);

            //加载飞机图标2号
            matrix.postScale((float)0.9,(float)0.9);//设置缩放比例
            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,false);
            left = ((test++)%40)*25;
            top+=100;
            mCanvas.drawBitmap(bitmap, left, top, new Paint());//居中画一个飞机

            //渲染航班号2号
            p.setTextSize(40);
            mCanvas.drawText("CN1949", left, top + bitmap.getWidth(), p);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(mCanvas!=null){
                mHolder.unlockCanvasAndPost(mCanvas);//提交画布内容
            }
        }
    }
}
