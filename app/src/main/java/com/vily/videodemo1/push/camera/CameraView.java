package com.vily.videodemo1.push.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.vily.videodemo1.push.egl.EGLSurfaceView;


public class CameraView extends EGLSurfaceView {

    private static final String TAG = "WlCameraView";
    private CameraRender wlCameraRender;
    private CameraHander camera;

    private int cameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;

    private int textureId = -1;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        wlCameraRender = new CameraRender(context);
        camera = new CameraHander(context);
        setRender(wlCameraRender);
        previewAngle(context);
        wlCameraRender.setOnSurfaceCreateListener(new CameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture, int tid) {
                Log.i(TAG, "onSurfaceCreate: ----------------tid:"+tid);
                camera.initCamera(surfaceTexture, cameraId);
                textureId = tid;
                if(mOnSurfaceRenderListener!=null){
                    mOnSurfaceRenderListener.onSurfaceRender(surfaceTexture,tid);
                }

            }
        });
    }

    public void onDestory()
    {
        if(camera != null)
        {
            camera.stopPreview();
        }
    }

    public void onResume(){
        if(camera != null)
        {
            camera.startPreview();
        }
    }

    public void previewAngle(Context context)
    {
        int angle = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        wlCameraRender.resetMatrix();
        switch (angle)
        {
            case Surface.ROTATION_0:
                Log.d("ywl5320", "0");
                if(cameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    wlCameraRender.setAngle(90, 0, 0, 1);
                    wlCameraRender.setAngle(180, 1, 0, 0);
                }
                else
                {
                    wlCameraRender.setAngle(90f, 0f, 0f, 1f);
                }

                break;
            case Surface.ROTATION_90:
                Log.d("ywl5320", "90");
                if(cameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    wlCameraRender.setAngle(180, 0, 0, 1);
                    wlCameraRender.setAngle(180, 0, 1, 0);
                }
                else
                {
                    wlCameraRender.setAngle(90f, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_180:
                Log.d("ywl5320", "180");
                if(cameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    wlCameraRender.setAngle(90f, 0.0f, 0f, 1f);
                    wlCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                }
                else
                {
                    wlCameraRender.setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                Log.d("ywl5320", "270");
                if(cameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    wlCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                }
                else
                {
                    wlCameraRender.setAngle(0f, 0f, 0f, 1f);
                }
                break;
        }
    }

    public int getTextureId()
    {
        return textureId;
    }

    private OnSurfaceRenderListener mOnSurfaceRenderListener;
    public interface OnSurfaceRenderListener{
       void onSurfaceRender(SurfaceTexture surfaceTexture, int tid);
    }

    public void setOnSurfaceRenderListener(OnSurfaceRenderListener onSurfaceRenderListener) {
        mOnSurfaceRenderListener = onSurfaceRenderListener;
    }
}
