package com.vily.videodemo1;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.vily.videodemo1.push.camera.WlCameraView;
import com.vily.videodemo1.push.push.WlBasePushEncoder;
import com.vily.videodemo1.push.push.WlPushEncodec;
import com.vily.videodemo1.push.util.DisplayUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LivePushActivity extends AppCompatActivity {


    private WlCameraView wlCameraView;
    private boolean start = true;
    private WlPushEncodec wlPushEncodec;
    private static final String TAG = "LivePushActivity";
    private Timer mTimer;

    private int vCount=0;
    private int aCount=0;
    private Button mBtn_record;

    private File file;
    private FileOutputStream fileOutputStream = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livepush);
        wlCameraView = findViewById(R.id.cameraview);
        mBtn_record = findViewById(R.id.btn_record);


        try {

            if (file == null) {
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/vilyxxx.h265");
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "run: ------------------视频每秒的码流："+vCount*8/1024);
                Log.i(TAG, "run: ------------------音频每秒的码流："+aCount*8/1024);
                aCount=0;
                vCount=0;
            }
        }, 0,1000);
        wlCameraView.setOnSurfaceRenderListener(new WlCameraView.OnSurfaceRenderListener() {
            @Override
            public void onSurfaceRender(SurfaceTexture surfaceTexture, int tid) {
                if(wlCameraView.getTextureId()!=-1){
                    wlPushEncodec = new WlPushEncodec(LivePushActivity.this, tid);
                    wlPushEncodec.initEncodec(wlCameraView.getEglContext(), 320, 240);
                    wlPushEncodec.startRecord();
                    wlPushEncodec.setOnMediaInfoListener(new WlBasePushEncoder.OnMediaInfoListener() {
                        @Override
                        public void onMediaTime(int times) {

                            Log.i(TAG, "onMediaTime: --------------:"+times);
                        }

                        @Override
                        public void onSPSPPSInfo(byte[] sps) {

                            Log.d(TAG, "-------------------hevc-head::" + DisplayUtil.byteToHex(sps));


                        }

                        @Override
                        public void onVideoInfo(byte[] data, boolean keyframe) {
                            Log.d(TAG, "-------------------video-data:" + DisplayUtil.byteToHex(data));
                            Log.i(TAG, "onVideoInfo: ---------------video:"+data.length);
                            vCount+=data.length;
                            write2LocalFile(data);
                        }

                        @Override
                        public void onAudioInfo(byte[] data) {
                            Log.d(TAG, "-------------------audio-data:" + DisplayUtil.byteToHex(data));
                            Log.i(TAG, "onAudioInfo: --------------audio:"+data.length);


                            aCount+=data.length;
                        }
                    });
                }
            }
        });
    }

    private void write2LocalFile(byte[] outData) {

        try {

            Log.i(TAG, "write2LocalFile: ----------读入本地H264文件");
            fileOutputStream.write(outData);

        } catch (FileNotFoundException e) {
            Log.e(TAG, "copyVideoResourceToMemory--------- FileNotFoundException : " + e);
        } catch (IOException e) {
            Log.e(TAG, "copyVideoResourceToMemory--------- IOException : " + e);
        }
    }
    public void startpush(View view) {

        if(start){

            mTimer.cancel();
            mTimer=null;
            if(wlPushEncodec!=null){
                wlPushEncodec.stopRecord();
                wlPushEncodec = null;
            }

        }else {
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(wlPushEncodec!=null){
            wlPushEncodec.stopRecord();
            wlPushEncodec = null;
        }
        wlCameraView.onDestory();
        wlCameraView=null;
        if(mTimer!=null){
            mTimer.cancel();
            mTimer=null;
            mTimer=null;
        }


        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
