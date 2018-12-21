package com.vily.videodemo1.media;

import android.annotation.SuppressLint;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;

import com.vily.videodemo1.Camer1.FocusSurfaceView;
import com.vily.videodemo1.R;

import java.io.File;
import java.io.IOException;

/**
 *  * description : 
 *  * Author : Vily
 *  * Date : 2018/12/21
 *  
 **/
public class MediaActivity extends AppCompatActivity {

    private MediaRecorder mRecorder;
    private SurfaceView mSuv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media);
        mSuv = findViewById(R.id.suv);

        init();
    }


    @SuppressLint("NewApi")
    private void init() {
        mRecorder = new MediaRecorder();

        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //设置输出格式，.THREE_GPP为3gp，.MPEG_4为mp4

        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//设置声音编码类型 mic

        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//设置视频编码类型，一般h263，h264

        mRecorder.setOutputFile("/sdcard/myVideo.3gp");

        mRecorder.setVideoSize(640,480);//设置视频分辨率，设置错误调用start()时会报错，可注释掉在运行程序测试，有时注释掉可以运行

//     mediarecorder.setVideoFrameRate(24);//设置视频帧率，可省略

        mRecorder.setVideoEncodingBitRate(10*1024*1024);//提高帧频率，录像模糊，花屏，绿屏可写上调试

//        mRecorder.setPreviewDisplay(mSuv.); //设置视频预览

        try {
            mRecorder.prepare();
            mRecorder.start();  // Recording is now started
        } catch (IOException e) {
            e.printStackTrace();

        }




    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecorder.stop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        mRecorder.release();// Now the object cannot be reused
    }
}
