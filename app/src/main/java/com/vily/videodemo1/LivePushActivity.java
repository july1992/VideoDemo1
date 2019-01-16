package com.vily.videodemo1;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.vily.videodemo1.playH265.CameraByteDecoder;
import com.vily.videodemo1.playH265.CameraRecordDecoder;
import com.vily.videodemo1.push.camera.CameraView;
import com.vily.videodemo1.push.push.CameraRecordEncoder;
import com.vily.videodemo1.push.util.DisplayUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class LivePushActivity extends AppCompatActivity {


    private CameraView wlCameraView;
    private boolean start = true;
    private CameraRecordEncoder wlPushEncodec;
    private static final String TAG = "LivePushActivity";
    private Timer mTimer;

    private int vCount=0;
    private int count=0;
    private Button mBtn_record;

    private File file;
    private FileOutputStream fileOutputStream = null;
    private SurfaceView mSurfaceview;
    private CameraByteDecoder mCameraByteDecoder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livepush);
        wlCameraView = findViewById(R.id.cameraview);
        mBtn_record = findViewById(R.id.btn_record);
        mSurfaceview = findViewById(R.id.surfaceview);



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
                Log.i(TAG, "run: ------------------视频每秒的大小："+count);
                vCount=0;
                count=0;
            }
        }, 0,1000);
        wlCameraView.setOnSurfaceRenderListener(new CameraView.OnSurfaceRenderListener() {
            @Override
            public void onSurfaceRender(SurfaceTexture surfaceTexture, int tid) {
                if(wlCameraView.getTextureId()!=-1){
                    wlPushEncodec = new CameraRecordEncoder(LivePushActivity.this, tid);
                    wlPushEncodec.initEncodec(wlCameraView.getEglContext(), 320, 240);
                    wlPushEncodec.startRecord();
                    wlPushEncodec.setOnMediaInfoListener(new CameraRecordEncoder.OnMediaInfoListener() {
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
                            count+=data.length;
//                            write2LocalFile(data);
                            if(mCameraByteDecoder!=null){
                                mCameraByteDecoder.sendByte(data);
                            }
                        }

                    });
                }
            }
        });

        listener();
    }

    private void listener() {
        mSurfaceview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mCameraByteDecoder = new CameraByteDecoder();
                mCameraByteDecoder.initCameraDecode(320,240,surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
    }

    private void write2LocalFile(byte[] outData) {

        try {


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


    @Override
    protected void onPause() {
        super.onPause();

        wlCameraView.onDestory();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        wlCameraView.onResume();
    }
}
