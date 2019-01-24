package com.vily.videodemo1.push.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.vily.videodemo1.push.egl.EGLSurfaceView;


public class CameraView extends EGLSurfaceView {

    private static final String TAG = "WlCameraView";
    private CameraRender wlCameraRender;
    private CameraHander camera;
    private Context mContext;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private int textureId = -1;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        wlCameraRender = new CameraRender(context);

        setRender(wlCameraRender);
        previewAngle(context);
        wlCameraRender.setOnSurfaceCreateListener(new CameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture, int tid) {
                Log.i(TAG, "onSurfaceCreate: ----------------tid:"+tid);
                if(camera==null){
                    camera = new CameraHander(context);
                    camera.initCamera(surfaceTexture, cameraId);


                }
                textureId = tid;
                if(mOnSurfaceRenderListener!=null){
                    mOnSurfaceRenderListener.onSurfaceRender(surfaceTexture,tid);
                }

            }

            @Override
            public void onSurfaceDestroy() {
                camera.destroyPreview();
                camera=null;
                if(mOnSurfaceRenderListener!=null){
                    mOnSurfaceRenderListener.onSurfaceDestroy();
                }
            }
        });
    }

    public void onDestory()
    {
        if(camera != null)
        {
            camera.destroyPreview();
        }
    }
    public void reStartPreview(){
        if(camera != null)
        {
            camera.reStartPreview();
        }
    }


    public void stopPreview(){
        if(camera != null)
        {
            camera.stopPreview();
        }
    }
    public void startPreview(){
        if(camera != null)
        {
            camera.startPreview();
        }
    }




    public void previewAngle(Context context)
    {


        int angle = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        Log.i(TAG, "previewAngle: --------走这里几次"+angle);
        wlCameraRender.resetMatrix();
        switch (angle)
        {
            case Surface.ROTATION_0:
                Log.d("ywl5320", "0");
                if(cameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    Log.i(TAG, "previewAngle: --------走这里几次后置"+angle);
                    wlCameraRender.setAngle(90, 0, 0, 1);
                    wlCameraRender.setAngle(180, 1, 0, 0);


//                    wlCameraRender.setAngle(180, 0, 0, 1);
//                    wlCameraRender.setAngle(270, 0, 1, 0);


                }
                else
                {
                    Log.i(TAG, "previewAngle: --------走这里几次前置"+angle);

                    wlCameraRender.setAngle(90f, 0f, 0f, 1f);
                    wlCameraRender.setAngle(180, 0, 1, 0);   // 左右镜像反的
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
        void onSurfaceDestroy();
    }

    public void setOnSurfaceRenderListener(OnSurfaceRenderListener onSurfaceRenderListener) {
        mOnSurfaceRenderListener = onSurfaceRenderListener;
    }

}
