package com.vily.videodemo1.camera2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 *  * description : 
 *  * Author : Vily
 *  * Date : 2018/12/24
 *  
 **/
public class Camera2Utils {

    private static final String TAG = "Camera2Utils";

    private static final int REQUEST_CAMERA_PERMISSION = 10;
    private Context mContext;
    private CameraManager manager;
    private ImageReader mImageReader;

    private boolean mFlashSupported;
    private int mSensorOrientation;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String mCameraId;
    private CameraCaptureSession mCaptureSession;  // 拍照请求会话
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;

    //Showing camera preview.
    private static final int STATE_PREVIEW = 0;

    //Camera state: Waiting for the focus to be locked.
    private static final int STATE_WAITING_LOCK = 1;

    //amera state: Waiting for the exposure to be precapture state.
    private static final int STATE_WAITING_PRECAPTURE = 2;

    //Camera state: Waiting for the exposure state to be something other than precapture.
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    //Camera state: Picture was taken.
    private static final int STATE_PICTURE_TAKEN = 4;
    private static final int STATE_DISCOUNT = 5;   // 摄像头断开
    private static final int STATE_ERROR = 6;   // 摄像头错误
    private static final int STATE_STOP = 7;   // 摄像头错误

    //Max preview width that is guaranteed by Camera2 API
    private static final int MAX_PREVIEW_WIDTH = 1280;

    //Max preview height that is guaranteed by Camera2 API
    private static final int MAX_PREVIEW_HEIGHT = 720;


    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;


    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private int mState = STATE_PREVIEW;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

        }
    };

    private AutoFitTextureView mTextureView;

    public Camera2Utils(Context context, AutoFitTextureView textureView) {

        this.mContext = context;
        manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        this.mTextureView = textureView;

    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void prepare(AutoFitTextureView textureView) {

        Log.i(TAG, "prepare: -----------");
        if (manager == null) {
            manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        }
        startBackgroundThread();
        setUpCameraOutputs(textureView);

        configureTransform(textureView);


    }


    private void setUpCameraOutputs(AutoFitTextureView mTextureView) {

        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                // 获取摄像头支持的配置属性
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // 获取摄像头支持的最大尺寸
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.YV12)),
                        new CompareSizesByArea());

                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.YV12, /*maxImages*/1);  // 修改
                // TODO  预览数据反馈
                mImageReader.setOnImageAvailableListener(
                        new ImageReader.OnImageAvailableListener() {
                            @Override
                            public void onImageAvailable(ImageReader imageReader) {

                                if (mOnCameraStateListener != null) {
                                    mOnCameraStateListener.onDataBack(imageReader.acquireNextImage());
                                }
                            }
                        }, mBackgroundHandler);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = mTextureView.getWidth();
                int rotatedPreviewHeight = mTextureView.getHeight();
                int maxPreviewWidth = displaySize.x;  // 宽 分辨率
                int maxPreviewHeight = displaySize.y; // 高 分辨率

                if (swappedDimensions) {
                    rotatedPreviewWidth = mTextureView.getHeight();
                    rotatedPreviewHeight = mTextureView.getWidth();
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // 获取最佳的预览尺寸
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // // 根据选中的预览尺寸来调整预览组件（TextureView）的长宽比
                int orientation = mContext.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.

        }
    }

    public void configureTransform(AutoFitTextureView mTextureView) {

        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = ((MainActivity) mContext).getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, mTextureView.getWidth(), mTextureView.getHeight());
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) mTextureView.getHeight() / mPreviewSize.getHeight(),
                    (float) mTextureView.getWidth() / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // / 收集摄像头支持的大过预览Surface的分辨率
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // 如果找到多个预览尺寸，获取其中面积最小的
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public void openCamera() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale((MainActivity) mContext, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions((MainActivity) mContext, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);

            }
            return;
        }
        try {
            // 这个对象是信号量，在开关相机时需要对相机加锁，因为相机可能同时被几个应用或者进程访问，此时应当加锁。
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }


    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
            Log.i(TAG, "onOpened: --------cameraDevice:" + "摄像头打开的嘛");
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.i(TAG, "onDisconnected: -----------------摄像头断开啦");
//            mCameraOpenCloseLock.release();
//            cameraDevice.close();
//            mCameraDevice = null;
            mState = STATE_DISCOUNT;
            if (mOnCameraStateListener != null) {
                mOnCameraStateListener.onCameraState("onDisconnected");
            }
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            Log.i(TAG, "onError: ----------------------摄像头报错啦");
//            mCameraOpenCloseLock.release();
//            cameraDevice.close();
//            mCameraDevice = null;
            mState = STATE_ERROR;
            if (mOnCameraStateListener != null) {
                mOnCameraStateListener.onCameraState("onError");
            }

        }

    };

    //  当摄像头打开后  走这个方法
    // TODO CameraCaptureSession   表式Android Device（APP）与CameraDevice之间的会话层，类似于 http中的 session。
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mTextureView.getWidth(), mTextureView.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());  // 必须加入 获取到预览帧
            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }
                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // 设置自动对焦模式
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // 设置自动曝光模式
                                if (mFlashSupported) {
                                    // 设置自动曝光模式
                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                }

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);

//                                mCaptureSession.prepare(mImageReader.getSurface());
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.i(TAG, "onConfigureFailed: ------");
                        }
                    }, null
            );

//            SystemClock.sleep(500);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    private void takePicture() {
        lockFocus();
    }

    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            // 设置自动曝光模式
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    // 打开闪光灯
    @SuppressLint("NewApi")
    public void openFlash() {
        if(manager!=null){
            try {
                manager.setTorchMode(mCameraId,true);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
    // 关闭闪光灯
    @SuppressLint("NewApi")
    public void closeFlash(){
        if(manager!=null){
            try {
                manager.setTorchMode(mCameraId,false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    // // 为Size定义一个比较器Comparator
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            //   强转为long保证不会发生溢出
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }


    public void restartPreview() {


        try {
            if (mCaptureSession != null) {
                //执行setRepeatingRequest方法就行了，注意mCaptureRequest是之前开启预览设置的请求
                mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
                mState = STATE_PREVIEW;
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    public void stopPreview() {
        try {
            if (mCaptureSession != null) {

                mCaptureSession.stopRepeating();
                mCaptureSession.abortCaptures();

            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // TODO 结束录制
    public void closeCamera() {

        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {

                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }

        } catch (Exception e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
        manager = null;
        stopBackgroundThread();
    }


    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler.removeCallbacksAndMessages(null);
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    // 反馈接口类
    public OnCameraStateListener mOnCameraStateListener;

    public interface OnCameraStateListener {
        void onCameraState(String state);

        void onDataBack(Image image);
    }

    public void setOnCameraStateListener(OnCameraStateListener onCameraStateListener) {
        this.mOnCameraStateListener = onCameraStateListener;
    }
}
