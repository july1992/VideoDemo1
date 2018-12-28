package com.vily.videodemo1.Camer1;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;

import com.vily.videodemo1.Camer1.utils.AvcEncoder;

/**
 * 视频录制：边录制边底层处理视频（旋转和裁剪）
 * 
 * @author yixia.com
 *
 */
public class MediaRecorderNative extends MediaRecorderBase implements MediaRecorder.OnErrorListener {

	/** 视频后缀 */
	private static final String VIDEO_SUFFIX = ".ts";
	private int cameraState = 1;


	/** 开始录制 */
	@Override
	public void startRecord() {
		mRecording = true;
		if (mAudioRecorder == null) {
			mAudioRecorder = new AudioRecorder(this);
			Log.i("sads", "startRecord: ---------音频开始录制");
			mAudioRecorder.start();
		}

	}

	public boolean getRecordState(){
		return mRecording;
	}

	/** 切换前置/后置摄像头 */
	public void switchCamera() {
		if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
			switchCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
			cameraState = 2;
		} else {
			switchCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
			cameraState = 1;
		}
	}

	/** 停止录制 */
	@Override
	public void stopRecord() {
		super.stopRecord();
	}



	/** 预览成功，设置视频输入输出参数 */
	@Override
	protected void onStartPreviewSuccess() {

	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		try {
			if (mr != null)
				mr.reset();
		} catch (IllegalStateException e) {
			Log.w("Yixia", "stopRecord", e);
		} catch (Exception e) {
			Log.w("Yixia", "stopRecord", e);
		}
		if (mOnErrorListener != null)
			mOnErrorListener.onVideoError(what, extra);
	}


	/** 数据回调 */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

		if(mOnVideoAndAudioByteListener!=null){

//			int ret = avcCodec.offerEncoder(data, h264);
			mOnVideoAndAudioByteListener.onVideoByte(data,camera);
		}
		super.onPreviewFrame(data, camera);
	}
	/** 接收音频数据，传递到底层 */
	@Override
	public void receiveAudioData(byte[] sampleBuffer, int len) {

		if(mOnVideoAndAudioByteListener!=null){
			mOnVideoAndAudioByteListener.onAudioByte(sampleBuffer,len);
		}
	}


	private OnVideoAndAudioByteListener mOnVideoAndAudioByteListener;
	public interface OnVideoAndAudioByteListener{

		void onVideoByte(byte[] data, Camera camera);
		void onAudioByte(byte[] data, int len);

	}

	public void setOnVideoAndAudioByteListener(OnVideoAndAudioByteListener onVideoAndAudioByteListener) {
		mOnVideoAndAudioByteListener = onVideoAndAudioByteListener;
	}
}
