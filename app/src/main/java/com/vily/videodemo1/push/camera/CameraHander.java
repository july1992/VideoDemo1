package com.vily.videodemo1.push.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;

import com.vily.videodemo1.Camer1.utils.DeviceUtils;
import com.vily.videodemo1.Camer1.utils.StringUtils;
import com.vily.videodemo1.push.util.DisplayUtil;

import java.io.IOException;
import java.util.List;

public class CameraHander {

    private android.hardware.Camera camera;



    private SurfaceTexture surfaceTexture;

    private int width;
    private int height;
    private android.hardware.Camera.Parameters mParameters;
    private int cameraId=0;

    public CameraHander(Context context){

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
            cameraId=cameraId;
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



            android.hardware.Camera.Size size = getFitSize(mParameters.getSupportedPictureSizes());
            mParameters.setPictureSize(320,240);

            size = getFitSize(mParameters.getSupportedPreviewSizes());
            mParameters.setPreviewSize(320, 240);

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

    public void stopPreview()
    {
        if(camera != null)
        {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void changeCamera(int cameraId)
    {
        if(camera != null)
        {
            stopPreview();
        }
        setCameraParm(cameraId);
    }

    private android.hardware.Camera.Size getFitSize(List<android.hardware.Camera.Size> sizes)
    {
        if(width < height)
        {
            int t = height;
            height = width;
            width = t;
        }

        for(android.hardware.Camera.Size size : sizes)
        {
            if(1.0f * size.width / size.height == 1.0f * width / height)
            {
                return size;
            }
        }
        return sizes.get(0);
    }

    public void startPreview() {
        if(camera != null)
        {
            stopPreview();
        }
        setCameraParm(cameraId);
    }
}
