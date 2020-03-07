package com.tongji.helloworld.ui.dashboard;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.tongji.helloworld.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;

    private final int REQUEST_PERMISSION_CODE = 1;
    private ArrayList<String> REQUIRED_PERMISSIONS = new ArrayList<String>(
            Arrays.asList(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
    private CameraManager cameraManager;
    private ArrayList<String> cameraIdList;//可用摄像头Id列表
    private CameraDevice mCameraDevice;
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() //监听相机状态
    {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            //创建CameraPreviewSession
           // createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };
    /**
     * 为相机预览创建新的CameraCaptureSession
     */
//    private void createCameraPreviewSession() {
//        try {
//            //设置了一个具有输出Surface的CaptureRequest.Builder。
//            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            mPreviewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
//            //创建一个CameraCaptureSession来进行相机预览。
//            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface()),
//                    new CameraCaptureSession.StateCallback() {
//
//                        @Override
//                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                            // 相机已经关闭
//                            if (null == mCameraDevice) {
//                                return;
//                            }
//                            // 会话准备好后，我们开始显示预览
//                            mCaptureSession = cameraCaptureSession;
//                            try {
//                                // 自动对焦应
//                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
//                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                                // 闪光灯
//                                setAutoFlash(mPreviewRequestBuilder);
//                                // 开启相机预览并添加事件
//                                mPreviewRequest = mPreviewRequestBuilder.build();
//                                //发送请求
//                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
//                                        null, mBackgroundHandler);
//                                Log.e(TAG," 开启相机预览并添加事件");
//                            } catch (CameraAccessException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onConfigureFailed(
//                                @NonNull CameraCaptureSession cameraCaptureSession) {
//                            Log.e(TAG," onConfigureFailed 开启预览失败");
//                        }
//                    }, null);
//        } catch (CameraAccessException e) {
//            Log.e(TAG," CameraAccessException 开启预览失败");
//            e.printStackTrace();
//        }
//    }

    /**
     * 判断我们需要的权限是否被授予，只要有一个没有授权，我们都会返回 false，并且进行权限申请操作。
     *
     * @return true 权限都被授权
     */
    private Boolean checkRequiredPermissions() {
        ArrayList<String> deniedPermissions = new ArrayList<String>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this.getActivity(), permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        if (!deniedPermissions.isEmpty()) {
            requestPermissions((String[]) deniedPermissions.toArray(new String[0]), REQUEST_PERMISSION_CODE);
        }
        return deniedPermissions.isEmpty();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("hidayat", "Dash Fragment's view created");

        //询问权限
        checkRequiredPermissions();

        //获取CameraManager实例
        cameraManager = (CameraManager) Objects.requireNonNull(this.getActivity()).getSystemService(Context.CAMERA_SERVICE);

        //获取所有摄像头ID列表
        String[] tmp_cameraIdList = new String[0];
        try {
            assert cameraManager != null;
            tmp_cameraIdList = cameraManager.getCameraIdList();
            Log.i("所有摄像头id列表", Arrays.toString(tmp_cameraIdList));
        } catch (CameraAccessException e) {
            Log.e("hidayat","can't get cameraIdList");
            e.printStackTrace();
        }

        //获取可用摄像头ID列表
        cameraIdList = new ArrayList<>();
        CameraCharacteristics cameraCharacteristics;
        for(String id: tmp_cameraIdList){
            try {
                cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = Objects.requireNonNull(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING));
                if(facing == CameraCharacteristics.LENS_FACING_FRONT){
                    cameraIdList.add(id);
                }
//                Log.i("摄像头前后置信息", "第" + id + "号摄像头：" +
//                        Objects.requireNonNull(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)).toString());
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        Log.i("可用摄像头id列表", cameraIdList.toString());

        //打开第一个可用的摄像头


        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("saturn", "Dash Fragment paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("saturn", "Dash Fragment resumed");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("saturn", "Dash Fragment destroyed");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("saturn", "Dash Fragment created");
    }
}