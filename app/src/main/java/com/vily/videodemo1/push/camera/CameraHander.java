package com.vily.videodemo1.push.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import com.vily.videodemo1.Camer1.utils.DeviceUtils;
import com.vily.videodemo1.Camer1.utils.StringUtils;
import com.vily.videodemo1.MyApplication;
import com.vily.videodemo1.camera0.RecordActivity;
import com.vily.videodemo1.push.util.DisplayUtil;

import java.io.IOException;
import java.util.List;

public class CameraHander {

    private android.hardware.Camera camera;



    private SurfaceTexture surfaceTexture;

    private int width;
    private int height;
    private android.hardware.Camera.Parameters mParameters;
    private int cameraId=1;
    private Context mContext;

    public CameraHander(Context context){

        this.mContext=context;
        this.width = DisplayUtil.getScreenWidth(context);
        this.height = DisplayUtil.getScreenHeight(context);

    }

    public void initCamera(SurfaceTexture surfaceTexture, int cameraId)
    {
        this.surfaceTexture = surfaceTexture;
        setCameraParm(cameraId);

    }

    private void setCameraParm(int cameraId)
    {
        try {
            camera = android.hardware.Camera.open(cameraId);

            this.cameraId=cameraId;
            camera.setPreviewTexture(surfaceTexture);
            mParameters = camera.getParameters();

            mParameters.setFlashMode("off");
            mParameters.setPreviewFormat(ImageFormat.NV21);



            String mode = getAutoFocusMode();
            if (StringUtils.isNotEmpty(mode)) {
                mParameters.setFocusMode(mode);
            }
            if (isSupported(mParameters.getSupportedWhiteBalance(), "auto"))
                mParameters.setWhiteBalance("auto");
            //是否支持视频防抖
            if ("true".equals(mParameters.get("video-stabilization-supported")))
                mParameters.set("video-stabilization", "true");
            if (!DeviceUtils.isDevice("GT-N7100", "GT-I9308", "GT-I9300")) {
                mParameters.set("cam_mode", 1);
                mParameters.set("cam-mode", 1);
            }

            mParameters.setPictureSize(MyApplication.mWidth,MyApplication.mHeight);

            mParameters.setPreviewSize(MyApplication.mWidth, MyApplication.mHeight);

            camera.setParameters(mParameters);
            camera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  连续自动对焦 */
    private String getAutoFocusMode() {
        if (mParameters != null) {
            //持续对焦是指当场景发生变化时，相机会主动去调节焦距来达到被拍摄的物体始终是清晰的状态。
            List<String> focusModes = mParameters.getSupportedFocusModes();
            if ((Build.MODEL.startsWith("GT-I950") || Build.MODEL.endsWith("SCH-I959") || Build.MODEL.endsWith("MEIZU MX3")) && isSupported(focusModes, "continuous-picture")) {
                return "continuous-picture";
            } else if (isSupported(focusModes, "continuous-video")) {
                return "continuous-video";
            } else if (isSupported(focusModes, "auto")) {
                return "auto";
            }
        }
        return null;
    }

    //  检测是否支持指定特性 */
    private boolean isSupported(List<String> list, String key) {
        return list != null && list.contains(key);
    }


    public void switchCamera() {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {

            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            if(camera != null)
            {
                destroyPreview();
            }
            setCameraParm(cameraId);
        } else {

            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            if(camera != null)
            {
                destroyPreview();
            }
            setCameraParm(cameraId);
        }
    }

    //  闪光灯是否可用
    public boolean flashEnable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
                && cameraId == Camera.CameraInfo.CAMERA_FACING_BACK;

    }

    //  闪光灯
    public boolean changeFlash() {
        boolean flashOn = false;
        if (flashEnable(mContext)) {
            Camera.Parameters params = camera.getParameters();
            if (Camera.Parameters.FLASH_MODE_TORCH.equals(params.getFlashMode())) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                flashOn = false;
            } else {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                flashOn = true;
            }
            camera.setParameters(params);
        }
        return flashOn;
    }



    public void reStartPreview() {

        setCameraParm(cameraId);
    }
    public void stopPreview()
    {
        if(camera != null)
        {
            camera.stopPreview();

        }
    }
    public void startPreview() {

        camera.startPreview();
    }

    public void destroyPreview(){
        if(camera != null)
        {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
