package com.vily.videodemo1.camera0;

/**
 *  * description : 
 *  * Author : Vily
 *  * Date : 2018/12/27
 *  
 **/
public interface IAudioRecord {
    // 音频错误
    public void onAudioError(int what, String message);

    // 接收音频数据
    public void receiveAudioData(byte[] sampleBuffer, int len);
}
