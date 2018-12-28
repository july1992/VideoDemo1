package com.vily.videodemo1.Camer1;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;


import com.vily.videodemo1.Camer1.utils.DeviceUtils;
import com.vily.videodemo1.Camer1.utils.FileUtils;
import com.vily.videodemo1.Camer1.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 视频录制抽象类
 *
 * @author yixia.com
 *
 */
public abstract class MediaRecorderBase implements Callback, PreviewCallback, IMediaRecorder {

	/** 视频宽度 */
	public static int VIDEO_WIDTH = 1280;
	/** 视频高度 */
	public static int VIDEO_HEIGHT = 720;

	/** 预览画布设置错误 */
	public static final int MEDIA_ERROR_CAMERA_SET_PREVIEW_DISPLAY = 101;
	/** 预览错误 */
	public static final int MEDIA_ERROR_CAMERA_PREVIEW = 102;
	/** 自动对焦错误 */
	public static final int MEDIA_ERROR_CAMERA_AUTO_FOCUS = 103;

	public static final int AUDIO_RECORD_ERROR_UNKNOWN = 0;
	/** 采样率设置不支持 */
	public static final int AUDIO_RECORD_ERROR_SAMPLERATE_NOT_SUPPORT = 1;
	/** 最小缓存获取失败 */
	public static final int AUDIO_RECORD_ERROR_GET_MIN_BUFFER_SIZE_NOT_SUPPORT = 2;
	/** 创建AudioRecord失败 */
	public static final int AUDIO_RECORD_ERROR_CREATE_FAILED = 3;

	/** 视频码率 1M */
	public static final int VIDEO_BITRATE_NORMAL = 1024;
	/** 视频码率 1.5M（默认） */
	public static final int VIDEO_BITRATE_MEDIUM = 1536;
	/** 视频码率 2M */
	public static final int VIDEO_BITRATE_HIGH = 2048;

	/** 最大帧率 */
	public static final int MAX_FRAME_RATE = 25;
	/** 最小帧率 */
	public static final int MIN_FRAME_RATE = 15;

	/** 摄像头对象 */
	protected Camera camera;
	/** 摄像头参数 */
	protected Camera.Parameters mParameters = null;
	/** 摄像头支持的预览尺寸集合 */
	protected List<Size> mSupportedPreviewSizes;
	/** 画布 */
	protected SurfaceHolder mSurfaceHolder;

	/** 声音录制 */
	protected AudioRecorder mAudioRecorder;

	/** 录制错误监听 */
	protected OnErrorListener mOnErrorListener;
	/** 录制已经准备就绪的监听 */
	protected OnPreparedListener mOnPreparedListener;

	/** 帧率 */
	protected int mFrameRate = MIN_FRAME_RATE;
	/** 摄像头类型（前置/后置），默认后置 */
	protected int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
	/** 视频码率 */
	protected int mVideoBitrate = VIDEO_BITRATE_HIGH;
	/** 状态标记 */
	protected boolean mPrepared, mStartPreview, mSurfaceCreated;
	/** 是否正在录制 */
	protected volatile boolean mRecording;
	/** PreviewFrame调用次数，测试用 */
	protected volatile long mPreviewFrameCallCount = 0;


	public MediaRecorderBase() {

	}

	public int getBitRate() {
		return mVideoBitrate;
	}


	/**
	 * 设置预览输出SurfaceHolder
	 * @param sh
	 */
	@SuppressWarnings("deprecation")
	public void setSurfaceHolder(SurfaceHolder sh) {

		if (sh != null) {
			sh.addCallback(this);
			if (!DeviceUtils.hasHoneycomb()) {
				sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			}
		}
	}


	/** 设置预处理监听 */
	public void setOnPreparedListener(OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/** 设置错误监听 */
	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;
	}

	/** 是否前置摄像头 */
	public boolean isFrontCamera() {
		return mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT;
	}

	public int getCameraType() {
		return mCameraId;
	}

	/** 是否支持前置摄像头 */
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static boolean isSupportFrontCamera() {
		if (!DeviceUtils.hasGingerbread()) {
			return false;
		}
		int numberOfCameras = Camera.getNumberOfCameras();
		if (2 == numberOfCameras) {
			return true;
		}
		return false;
	}

	public boolean changeFlash(Context context) {
		boolean flashOn = false;
		if (flashEnable(context)) {
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

	public boolean flashEnable(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
				&& mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK;

	}

	/** 切换前置/后置摄像头 */
	public void switchCamera(int cameraFacingFront) {
		mCameraId = cameraFacingFront;
		stopPreview();
		startPreview();
	}

	/**
	 * 自动对焦
	 *
	 * @param cb
	 * @return
	 */
	public boolean autoFocus(AutoFocusCallback cb) {
		if (camera != null) {
			try {
				camera.cancelAutoFocus();

				if (mParameters != null) {
					String mode = getAutoFocusMode();
					if (StringUtils.isNotEmpty(mode)) {
						mParameters.setFocusMode(mode);
						camera.setParameters(mParameters);
					}
				}
				camera.autoFocus(cb);
				return true;
			} catch (Exception e) {
				if (mOnErrorListener != null) {
					mOnErrorListener.onVideoError(MEDIA_ERROR_CAMERA_AUTO_FOCUS, 0);
				}
				if (e != null)
					Log.e("Yixia", "autoFocus", e);
			}
		}
		return false;
	}

	/** 连续自动对焦 */
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

	/**
	 * 手动对焦
	 *
	 * @param focusAreas 对焦区域
	 * @return
	 */
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public boolean manualFocus(AutoFocusCallback cb, List<Area> focusAreas) {
		if (camera != null && focusAreas != null && mParameters != null && DeviceUtils.hasICS()) {
			try {
				camera.cancelAutoFocus();
				// getMaxNumFocusAreas检测设备是否支持
				if (mParameters.getMaxNumFocusAreas() > 0) {
					// mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);//
					// Macro(close-up) focus mode
					mParameters.setFocusAreas(focusAreas);
				}

				if (mParameters.getMaxNumMeteringAreas() > 0)
					mParameters.setMeteringAreas(focusAreas);

				mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
				camera.setParameters(mParameters);
				camera.autoFocus(cb);
				return true;
			} catch (Exception e) {
				if (mOnErrorListener != null) {
					mOnErrorListener.onVideoError(MEDIA_ERROR_CAMERA_AUTO_FOCUS, 0);
				}
				if (e != null)
					Log.e("Yixia", "autoFocus", e);
			}
		}
		return false;
	}

	/**
	 * 切换闪关灯，默认关闭
	 */
	public boolean toggleFlashMode() {
		if (mParameters != null) {
			try {
				final String mode = mParameters.getFlashMode();
				if (TextUtils.isEmpty(mode) || Camera.Parameters.FLASH_MODE_OFF.equals(mode))
					setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				else
					setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				return true;
			} catch (Exception e) {
				Log.e("Yixia", "toggleFlashMode", e);
			}
		}
		return false;
	}

	/**
	 * 设置闪光灯
	 *
	 * @param value
	 */
	private boolean setFlashMode(String value) {
		if (mParameters != null && camera != null) {
			try {
				if (Camera.Parameters.FLASH_MODE_TORCH.equals(value) || Camera.Parameters.FLASH_MODE_OFF.equals(value)) {
					mParameters.setFlashMode(value);
					camera.setParameters(mParameters);
				}
				return true;
			} catch (Exception e) {
				Log.e("Yixia", "setFlashMode", e);
			}
		}
		return false;
	}

	/** 设置码率 */
	public void setVideoBitRate(int bitRate) {
		if (bitRate > 0)
			mVideoBitrate = bitRate;
	}

	/** 设置帧数 */
	public void setVideoRota(int rota) {
		if (rota > 0)
			mFrameRate = rota;
	}

	/**
	 * 设置视频的宽高
	 */
	public void setVideoRecordedSize(int width, int height){
		VIDEO_WIDTH = width;
		VIDEO_HEIGHT = height;
	}

	/**
	 * 开始预览
	 */
	public void prepare() {
		mPrepared = true;
		if (mSurfaceCreated)
			startPreview();
	}


	public void stopRecord() {
		mRecording = false;

	}

	/** 停止所有块的写入 */
	private void stopAllRecord() {
		mRecording = false;

	}

	/** 检测是否支持指定特性 */
	private boolean isSupported(List<String> list, String key) {
		return list != null && list.contains(key);
	}

	/**
	 * 预处理一些拍摄参数
	 * 注意：自动对焦参数cam_mode和cam-mode可能有些设备不支持，导致视频画面变形，需要判断一下，已知有"GT-N7100", "GT-I9308"会存在这个问题
	 *
	 */
	@SuppressWarnings("deprecation")
	protected void prepareCameraParaments() {
		if (mParameters == null)
			return;

		List<Integer> rates = mParameters.getSupportedPreviewFrameRates();
		if (rates != null) {
			if (rates.contains(MAX_FRAME_RATE)) {
				mFrameRate = MAX_FRAME_RATE;
			} else {
				Collections.sort(rates);
				for (int i = rates.size() - 1; i >= 0; i--) {
					Log.i("sdadad", "prepareCameraParaments: ---------------rates.get(i)："+rates.get(i));
					if (rates.get(i) <= MAX_FRAME_RATE) {
						mFrameRate = rates.get(i);
						break;
					}
				}
			}
		}

		Size previewSize = mParameters.getPreviewSize();
		Log.i(TAG, "prepareCameraParaments: -------:"+mFrameRate);
		Log.i(TAG, "prepareCameraParaments: -------:"+getBitRate());
		mParameters.setPreviewFrameRate(mFrameRate);
		// mParameters.setPreviewFpsRange(15 * 1000, 20 * 1000);

		boolean flag = false;
		for (int x=0; x<mSupportedPreviewSizes.size(); x++){
			Size size = mSupportedPreviewSizes.get(x);
			if(size.width*size.height == MediaRecorderBase.VIDEO_WIDTH* MediaRecorderBase.VIDEO_HEIGHT){
				flag = true;
			}
		}

		if(flag){
			mParameters.setPreviewSize(MediaRecorderBase.VIDEO_WIDTH, MediaRecorderBase.VIDEO_HEIGHT);
		}else{
			MediaRecorderBase.VIDEO_WIDTH = 720;
			mParameters.setPreviewSize(MediaRecorderBase.VIDEO_WIDTH, MediaRecorderBase.VIDEO_HEIGHT);
		}

		// 设置输出视频流尺寸，采样率
		mParameters.setPreviewFormat(ImageFormat.NV21);

		//设置自动连续对焦
		String mode = getAutoFocusMode();
		Log.i(TAG, "prepareCameraParaments: ---------对焦："+mode);
		if (StringUtils.isNotEmpty(mode)) {
			mParameters.setFocusMode(mode);
		}

		//设置人像模式，用来拍摄人物相片，如证件照。数码相机会把光圈调到最大，做出浅景深的效果。而有些相机还会使用能够表现更强肤色效果的色调、对比度或柔化效果进行拍摄，以突出人像主体。
		//		if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT && isSupported(mParameters.getSupportedSceneModes(), Camera.Parameters.SCENE_MODE_PORTRAIT))
		//			mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);

		if (isSupported(mParameters.getSupportedWhiteBalance(), "auto"))
			mParameters.setWhiteBalance("auto");

		//是否支持视频防抖
		if ("true".equals(mParameters.get("video-stabilization-supported")))
			mParameters.set("video-stabilization", "true");

		//		mParameters.set("recording-hint", "false");
		//
		//		mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		if (!DeviceUtils.isDevice("GT-N7100", "GT-I9308", "GT-I9300")) {
			mParameters.set("cam_mode", 1);
			mParameters.set("cam-mode", 1);
		}
	}

    private static final String TAG = "MediaRecorderBase";
	/** 开始预览 */
	public void startPreview() {
        Log.i(TAG, "startPreview: ---------开始预览流吗");
		if (mStartPreview || mSurfaceHolder == null || !mPrepared)
			return;
		else
			mStartPreview = true;

		try {

			if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
				camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
				camera.setDisplayOrientation(90);
			} else {
				camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
				camera.setDisplayOrientation(90);
			}

			try {
				camera.setPreviewDisplay(mSurfaceHolder);
			} catch (IOException e) {
				if (mOnErrorListener != null) {
					mOnErrorListener.onVideoError(MEDIA_ERROR_CAMERA_SET_PREVIEW_DISPLAY, 0);
				}
			}

			//设置摄像头参数
			mParameters = camera.getParameters();
			mSupportedPreviewSizes = mParameters.getSupportedPreviewSizes();//	获取支持的尺寸
			List<Size> supportedPreviewSizes = mParameters.getSupportedPreviewSizes();

			for(Size size : supportedPreviewSizes){
				Log.i("size", "startPreview: -----size:"+size.height+"-----"+size);
			}

			prepareCameraParaments();
			camera.setParameters(mParameters);
			setPreviewCallback();
			camera.startPreview();

			onStartPreviewSuccess();
			if (mOnPreparedListener != null)
				mOnPreparedListener.onPrepared();
		} catch (Exception e) {
			e.printStackTrace();
			if (mOnErrorListener != null) {
				mOnErrorListener.onVideoError(MEDIA_ERROR_CAMERA_PREVIEW, 0);
			}
			Log.e("Yixia", "startPreview fail :" + e.getMessage());
		}
	}

	public Camera getCamera(){
		return camera;
	}

	/** 预览调用成功，子类可以做一些操作 */
	protected void onStartPreviewSuccess() {

	}

	/** 设置回调 */
	protected void setPreviewCallback() {
		Size size = mParameters.getPreviewSize();
		if (size != null) {
			PixelFormat pf = new PixelFormat();
			PixelFormat.getPixelFormatInfo(mParameters.getPreviewFormat(), pf);
//			int buffSize = size.width * size.height * pf.bitsPerPixel / 8;   // 1382400
			int buffSize = size.width * size.height * 3 / 2;   // 1382400
			Log.i(TAG, "setPreviewCallback: ---------size:"+size.width+"----"+size.height);
			Log.i(TAG, "setPreviewCallback: ---------buffsize:"+buffSize);
			try {
				camera.addCallbackBuffer(new byte[buffSize]);
				camera.addCallbackBuffer(new byte[buffSize]);
				camera.addCallbackBuffer(new byte[buffSize]);
				camera.setPreviewCallbackWithBuffer(this);
			} catch (OutOfMemoryError e) {
				Log.e("Yixia", "startPreview...setPreviewCallback...", e);
			}
			Log.e("Yixia", "startPreview...setPreviewCallbackWithBuffer...width:" + size.width + " height:" + size.height);
		} else {
			camera.setPreviewCallback(this);
		}
	}

	/** 停止预览 */
	public void stopPreview() {
		if (camera != null) {
			try {
				camera.stopPreview();
				camera.setPreviewCallback(null);
				// camera.lock();
				camera.release();
			} catch (Exception e) {
				Log.e("Yixia", "stopPreview...");
			}
			camera = null;
		}
		mStartPreview = false;
	}

	/** 释放资源 */
	public void release() {
		stopAllRecord();
		// 停止视频预览
		stopPreview();
		// 停止音频录制
		if (mAudioRecorder != null) {
			mAudioRecorder.interrupt();
			mAudioRecorder = null;
		}

		mSurfaceHolder = null;
		mPrepared = false;
		mSurfaceCreated = false;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.mSurfaceHolder = holder;
		this.mSurfaceCreated = true;
		if (mPrepared && !mStartPreview)
			startPreview();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		this.mSurfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mSurfaceHolder = null;
		mSurfaceCreated = false;
	}

	@Override
	public void onAudioError(int what, String message) {
		if (mOnErrorListener != null)
			mOnErrorListener.onAudioError(what, message);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		mPreviewFrameCallCount++;
		camera.addCallbackBuffer(data);
	}

	/**
	 * 测试PreviewFrame回调次数，时间1分钟
	 *
	 */
	public void testPreviewFrameCallCount() {
		new CountDownTimer(1 * 60 * 1000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				Log.e("[Vitamio Recorder]", "testFrameRate..." + mPreviewFrameCallCount);
				mPreviewFrameCallCount = 0;
			}

			@Override
			public void onFinish() {

			}

		}.start();
	}



	public void setNewSurface(SurfaceHolder sv_surface2) {
		if(sv_surface2!=null){
			sv_surface2.addCallback(new Callback() {
				@Override
				public void surfaceCreated(SurfaceHolder surfaceHolder) {
					mSurfaceHolder=surfaceHolder;
				}

				@Override
				public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

				}

				@Override
				public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

				}
			});
			mSurfaceHolder=sv_surface2;
		}

	}

	/**
	 * 预处理监听
	 *
	 */
	public interface OnPreparedListener {
		/**
		 * 预处理完毕，可以开始录制了
		 */
		void onPrepared();
	}

	/**
	 * 错误监听
	 *
	 */
	public interface OnErrorListener {
		/**
		 * 视频录制错误
		 *
		 * @param what
		 * @param extra
		 */
		void onVideoError(int what, int extra);

		/**
		 * 音频录制错误
		 *
		 * @param what
		 * @param message
		 */
		void onAudioError(int what, String message);
	}

}
