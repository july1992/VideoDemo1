package com.vily.videodemo1.camera0;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.vily.videodemo1.Camer1.FocusSurfaceView;
import com.vily.videodemo1.Camer1.MediaRecorderBase;
import com.vily.videodemo1.Camer1.RecordedActivity;
import com.vily.videodemo1.Camer1.utils.AudioEncoder;
import com.vily.videodemo1.Camer1.utils.AvcEncoder;
import com.vily.videodemo1.Camer1.utils.StringUtils;
import com.vily.videodemo1.R;
import com.vily.videodemo1.manage.DecoderManager;
import com.vily.videodemo1.manage.MediaMuxerManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by zhangxd on 2018/9/5.
 */

public class RecordActivity extends Activity {

    private static final String TAG = RecordActivity.class.getSimpleName();


    public static SurfaceHolder surfaceHolder;


    private FocusSurfaceView mSv_surface1;
    private static FocusSurfaceView mSv_surface2;
    private ImageView mIv_change_flash;
    private ImageView mIv_change_camera;
    private RelativeLayout mRl_top;

    private String tag = "big";
    private int mWidth;
    private int mHeight;
    private SurfaceHolder mSurfaceHolder2;
    private Camera1Utils mCamera1Utils;

    private static int VIDEO_With=720;
    private static int VIDEO_Height=480;
    private static int BIT_RATE=800000;
    private static int FRAME_RATE=20;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ExecutorService audioExcutor = Executors.newSingleThreadExecutor();
    private ExecutorService playExcutor = Executors.newSingleThreadExecutor();
    private AvcEncoder mAvcEncoder;


    private int breforCount=0;
    private int afterCount=0;
    private AudioEncoder mAudioEncoder;
    private long mStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initView();
        initSurfaceHolder();
        initManager();
        initLietener();
    }


    private void initView() {
        mSv_surface1 = findViewById(R.id.sv_surface1);
        mSv_surface2 = findViewById(R.id.sv_surface2);
        mRl_top = findViewById(R.id.rl_top);
        mIv_change_flash = findViewById(R.id.iv_change_flash);
        mIv_change_camera = findViewById(R.id.iv_change_camera);


    }

    private void initManager() {
        MediaMuxerManager.getInstance().init();

        mCamera1Utils = Camera1Utils.getInstance(RecordActivity.this);
        mWidth = getWindowManager().getDefaultDisplay().getWidth();
        mHeight = getWindowManager().getDefaultDisplay().getHeight();

        // 视频编码
        mAvcEncoder = new AvcEncoder(VIDEO_With,VIDEO_Height,FRAME_RATE,BIT_RATE);
        // 音频编码
        mAudioEncoder = new AudioEncoder();

    }

    private void initSurfaceHolder() {
        surfaceHolder = mSv_surface1.getHolder();
        mSurfaceHolder2 = mSv_surface2.getHolder();

        mSurfaceHolder2.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                Log.i(TAG, "surfaceCreated: ---------播放直播对方的画面");
                // 开线程解码
                playExcutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // 解析本地的h264视频文件
//                        DecoderManager.getInstance().startH264Decode(mWidth,mHeight,mSurfaceHolder2);
//                        DecoderManager.getInstance().playDecode();
                    }
                });

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {


            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                Log.i(TAG, "surfaceDestroyed: ------------2");
            }
        });

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(TAG, "surfaceCreated: --------suface1");
                if("big".equals(tag)){
                    mCamera1Utils.initCamera(surfaceHolder);
                    mCamera1Utils.startPreview();

                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(TAG, "surfaceDestroyed: ------------2");
            }
        });


    }
    private void initLietener() {

        mIv_change_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera1Utils.getInstance(RecordActivity.this).changeFlash()) {
                    mIv_change_flash.setImageResource(R.mipmap.video_flash_open);
                } else {
                    mIv_change_flash.setImageResource(R.mipmap.video_flash_close);
                }
            }
        });

        mIv_change_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera1Utils.getInstance(RecordActivity.this).switchCamera();
                mIv_change_flash.setImageResource(R.mipmap.video_flash_close);
            }
        });

        mSv_surface2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (tag) {

                    case "big":
                        tag = "small"; // 切换到小屏

                        mCamera1Utils.destroyCamera();

                        mSv_surface1.setVisibility(View.INVISIBLE);
                        mSv_surface1.setVisibility(View.VISIBLE);

                        mCamera1Utils.initCamera(mSurfaceHolder2);

                        mCamera1Utils.startPreview();


                        break;
                    case "small":
                        tag = "big"; // 切换到大屏

                        mCamera1Utils.destroyCamera();
                        mCamera1Utils.initCamera(surfaceHolder);
                        mCamera1Utils.startPreview();
                        mSv_surface2.bringToFront();

                        break;
                    default:
                        break;
                }
//                mSv_surface1.setAspectRatio(320,480);
//                mSv_surface2.setAspectRatio(mWidth,mHeight);

            }
        });
        mCamera1Utils.setOnPreviewVedioAudioCallBack(new Camera1Utils.OnPreviewVedioAudioCallBack() {
            @Override
            public void onVedioCallBack(final byte[] bytes) {

                //  1280*720----10秒  204M  帧：1350kb     编码后：0.75M   13秒延迟   BIT_RATE:800000

                //  960*540 ----10秒  115M  帧：760 kb     编码后：0.75M  3.7秒延迟   BIT_RATE:800000
                //                                        编码后：0.2MB  3.5秒延迟   BIT_RATE:200000

                //  720*480 ----10秒  77M   帧：506.25kb   编码后：0.73M  0.08秒延迟  BIT_RATE:800000
                //                                        编码后：4.58M  0.1秒延迟   BIT_RATE:5000000

                //  640*480 ----10秒  67M   帧：450kb      编码后：0.72M  0.02秒延迟  BIT_RATE:800000
                //  320*240 ----10秒  16M   帧：112.5kb    编码后：0.67M  0.02秒延迟  BIT_RATE:800000 （极度模糊）
                breforCount+=bytes.length;
//
                Log.i(TAG, "onVedioCallBack: ----------视频："+bytes.length+"-----"+breforCount+"----录制时间"+(System.currentTimeMillis()-mStartTime));
                executor.execute(new Runnable() {
                    @Override
                    public void run() {

                        byte[] bytes1=new byte[VIDEO_Height*VIDEO_With*3/2];
                        int pos = mAvcEncoder.offerEncoder(bytes, bytes1);

                        afterCount+=pos;
                        Log.i(TAG, "run: -----------视频:"+pos+"------bytes1:"+bytes1.length+"-----afterCount:"+afterCount+"----编码"+(System.currentTimeMillis()-mStartTime));
                    }
                });
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();


        mCamera1Utils.stopCamera();

    }


    @Override
    protected void onResume() {
        super.onResume();

        mCamera1Utils.startPreview();

        mStartTime = System.currentTimeMillis();
        mCamera1Utils.prepareAudio(new IAudioRecord() {
            @Override
            public void onAudioError(int what, String message) {

            }

            @Override
            public void receiveAudioData(final byte[] sampleBuffer, int len) {

                //   3584  -----   44  10秒  1M ---7.9K

//                Log.i(TAG, "receiveAudioData: ---------音频："+sampleBuffer.length);
//                executor.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        mAudioEncoder.offEncoder(sampleBuffer);
//                    }
//                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera1Utils.destroyCamera();
        mCamera1Utils.stopRecord();

        MediaMuxerManager.getInstance().close();
    }


    public  static Surface getSurface() {
        return mSv_surface2.getHolder().getSurface();
    }
}
