package com.vily.videodemo1.camera0;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import com.vily.videodemo1.Camer1.IMediaRecorder;
import com.vily.videodemo1.Camer1.MediaRecorderBase;

/**
 * 音频录制
 * 
 * @author yixia.com
 *
 */
public class AudioRecorder0 extends Thread {

	private AudioRecord mAudioRecord = null;
	/** 采样率 */
	private int mSampleRate = 44100;
	private IAudioRecord mMediaRecorder;

	public AudioRecorder0(IAudioRecord mediaRecorder) {
		this.mMediaRecorder = mediaRecorder;
	}

	/** 设置采样率 */
	public void setSampleRate(int sampleRate) {
		this.mSampleRate = sampleRate;
	}

	@Override
	public void run() {

		//   设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
		if (mSampleRate != 8000 && mSampleRate != 16000 && mSampleRate != 22050 && mSampleRate != 44100) {
			mMediaRecorder.onAudioError(1, "采样率设置不支持");
			return;
		}

		// 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
		final int mMinBufferSize = AudioRecord.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

		if (AudioRecord.ERROR_BAD_VALUE == mMinBufferSize) {
			mMediaRecorder.onAudioError(2, "最小缓存获取失败");
			return;
		}

		// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
		mAudioRecord = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC,mSampleRate , AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mMinBufferSize);
		if (null == mAudioRecord) {
			mMediaRecorder.onAudioError(3, "创建AudioRecord失败");
			return;
		}

		try {
			mAudioRecord.startRecording();
		} catch (IllegalStateException e) {
			mMediaRecorder.onAudioError(0, "位置错误");
			return;
		}

		byte[] sampleBuffer = new byte[mMinBufferSize];

		try {
			while (!Thread.currentThread().isInterrupted()) {
				int result = mAudioRecord.read(sampleBuffer, 0, mMinBufferSize);
				if (result > 0) {
					mMediaRecorder.receiveAudioData(sampleBuffer, result);
				}
			}
		} catch (Exception e) {
			String message = "";
			if (e != null)
				message = e.getMessage();
			mMediaRecorder.onAudioError(0, message);
		}

		mAudioRecord.release();
		mAudioRecord = null;
	}


	public void stopRecord() {
		if(mAudioRecord!=null){
			mAudioRecord.stop();
		}


	}

}
