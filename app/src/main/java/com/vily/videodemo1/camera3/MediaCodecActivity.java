package com.vily.videodemo1.camera3;

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
import android.widget.ImageView;

import com.vily.videodemo1.R;
import com.vily.videodemo1.playH265.CameraByteDecoder;
import com.vily.videodemo1.push.camera.CameraView;
import com.vily.videodemo1.push.push.CameraRecordEncoder;
import com.vily.videodemo1.push.util.DisplayUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MediaCodecActivity extends AppCompatActivity {


    private SurfaceView wlCameraView;
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

//    private ImageView mIv_change_flash;
//    private ImageView mIv_change_camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livepush);
        wlCameraView = findViewById(R.id.cameraview);
//        mBtn_record = findViewById(R.id.btn_record);
        mSurfaceview = findViewById(R.id.surfaceview);
//        mIv_change_flash = findViewById(R.id.iv_change_flash);
//        mIv_change_camera = findViewById(R.id.iv_change_camera);


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



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    @Override
    protected void onPause() {
        super.onPause();


    }

    private boolean isFirst=true;
    @Override
    protected void onResume() {
        super.onResume();





    }
}
