package com.vily.videodemo1.Camer1;

/**
 * 视频录制接口
 * 
 * @author yixia.com
 *
 */
public interface IMediaRecorder {


	// 开始录制
	public void startRecord();

	// 停止录制
	public void stopRecord();
	
	// 音频错误
	public void onAudioError(int what, String message);

	// 接收音频数据
	public void receiveAudioData(byte[] sampleBuffer, int len);
}
