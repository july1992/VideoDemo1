package com.vily.videodemo1.Camer1;

import android.hardware.Camera;

import android.os.Bundle;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vily.videodemo1.Camer1.utils.AudioEncoder;
import com.vily.videodemo1.Camer1.utils.AvcEncoder;
import com.vily.videodemo1.Camer1.utils.StringUtils;
import com.vily.videodemo1.R;
import com.vily.videodemo1.camera2.AutoFitTextureView;
import com.vily.videodemo1.netty.NettyClient;
import com.vily.videodemo1.netty.NettyListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *  * description : 
 *  * Author : Vily
 *  * Date : 2018/12/21
 *  
 **/
public class RecordedActivity extends AppCompatActivity {

    private static final int REQUEST_KEY = 100;

    private static final String TAG = "RecordedActivity";

    private MediaRecorderNative mMediaRecorder;
    private FocusSurfaceView sv_ffmpeg;

    private ImageView iv_change_camera;

    private ImageView iv_change_flash;

    private NettyClient mNettyClient;
    private FocusSurfaceView mSv_surface2;

    private String tag = "big";
    private int mWidth;
    private int mHeight;
    private RelativeLayout mRl_top;
    private AutoFitTextureView mSv_surface3;
    private AvcEncoder mAvcEncoder;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ExecutorService audioExcutor = Executors.newSingleThreadExecutor();
    private long count = 0;
    private long beforeCount = 0;
    private long audioCount = 0;
    private AudioEncoder mAudioEncoder;
    private long mStartTime;

    private static int VIDEO_With=320;
    private static int VIDEO_Height=480;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_resord);


        if (mAvcEncoder == null) {    // 视频编码
            mAvcEncoder = new AvcEncoder(VIDEO_With, VIDEO_Height, 25, 2048);

        }
        if (mAudioEncoder == null) {   // 音频编码
            mAudioEncoder = new AudioEncoder();
        }
        initUI();
        initData();

        initMediaRecorder();
        initListener();
    }


    private void initUI() {

        sv_ffmpeg = findViewById(R.id.sv_ffmpeg);
        iv_change_flash = findViewById(R.id.iv_change_flash);
        iv_change_camera = findViewById(R.id.iv_change_camera);
        mSv_surface2 = findViewById(R.id.sv_surface2);
        mRl_top = findViewById(R.id.rl_top);

        mSv_surface3 = findViewById(R.id.sv_surface3);

        mSv_surface2.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        sv_ffmpeg.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }

    private void initData() {

        mWidth = getWindowManager().getDefaultDisplay().getWidth();
        mHeight = getWindowManager().getDefaultDisplay().getHeight();


    }

    /**
     * 初始化录制对象
     */
    private void initMediaRecorder() {

        mMediaRecorder = new MediaRecorderNative();
        //设置视频预览源
        mMediaRecorder.setSurfaceHolder(sv_ffmpeg.getHolder());
        //准备
        mMediaRecorder.prepare();


    }



    private void initListener() {

        iv_change_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaRecorder.changeFlash(RecordedActivity.this)) {
                    iv_change_flash.setImageResource(R.mipmap.video_flash_open);
                } else {
                    iv_change_flash.setImageResource(R.mipmap.video_flash_close);
                }
            }
        });

        iv_change_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaRecorder.switchCamera();
                iv_change_flash.setImageResource(R.mipmap.video_flash_close);
            }
        });

//        mSv_surface2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Log.i(TAG, "onClick: ---------切换屏幕："+tag);
//                switch (tag){
//
//                    case "big" :
//                        tag="small"; // 切换到小屏
//                        mMediaRecorder.setNewSurface(mSv_surface2.getHolder());
//                        mMediaRecorder.stopPreview();
//                        mMediaRecorder.startPreview();
//                        mSv_surface2.bringToFront();
//                        mRl_top.bringToFront();
//                        break;
//                    case "small" :
//                        tag="big";
//                        mMediaRecorder.setNewSurface(sv_ffmpeg.getHolder());
//                        mMediaRecorder.stopPreview();
//                        mMediaRecorder.startPreview();
////                    mSv_surface2.bringToFront();
//                        mRl_top.bringToFront();
//                        break;
//                    default :
//                        break;
//                }
//
//            }
//        });

        mSv_surface2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("big".equals(tag)) {
                    tag = "small"; // 切换到小屏
                    sv_ffmpeg.setAspectRatio(0,0);
                    sv_ffmpeg.setAspectRatio(StringUtils.dp2px(RecordedActivity.this, 100), StringUtils.dp2px(RecordedActivity.this, 150));
                    mSv_surface2.setAspectRatio(mWidth, mHeight);
                    sv_ffmpeg.bringToFront();
                    mRl_top.bringToFront();
                }
            }
        });

        sv_ffmpeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("small".equals(tag)) {
                    tag = "big"; // 切换到小屏
                    mSv_surface2.setAspectRatio(StringUtils.dp2px(RecordedActivity.this, 100), StringUtils.dp2px(RecordedActivity.this, 150));
                    sv_ffmpeg.setAspectRatio(mWidth, mHeight);
                    mSv_surface2.bringToFront();
                    mRl_top.bringToFront();
                }
            }
        });
        mMediaRecorder.setOnVideoAndAudioByteListener(new MediaRecorderNative.OnVideoAndAudioByteListener() {

            @Override
            public void onVideoByte(final byte[] data, Camera camera) {
                //   720 * 1280   1382400字节 == 1.3MB -----6321  --  10秒  350M---1。8M
                //   320 * 480   1382400字节 == 1.3MB -----6321  --  10秒  342M--- 464k


                final long stopTime = System.currentTimeMillis();

                if(data!=null && data.length>0){
                    beforeCount+=data.length;

                    Log.i(TAG, "onVideoByte: ---------编码前ship帧长度：" + data.length + "总长度："+beforeCount+"----录制时间:" + (stopTime - mStartTime));
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            byte[] output = new byte[VIDEO_With * VIDEO_Height * 3 / 2];
                            int pos = mAvcEncoder.offerEncoder(data, output);
                            final long stopTime2 = System.currentTimeMillis();

                            count += pos;
                            Log.i(TAG, "run: --------编码后都视频byte 长度：" + pos + "------总和：" + count + "----编码时间" + (stopTime2-mStartTime));


                        }
                    });
                }



            }

            @Override
            public void onAudioByte(final byte[] data, int len) {          //   3584  -----   44  10秒  1M ---7.9K
//                final long stopTime = System.currentTimeMillis();
//
//                beforeCount+=data.length;
//
//                Log.i(TAG, "onVideoByte: ---------编码前音频长度：" + data.length + "总长度："+beforeCount+"----录制时间:" + (stopTime - mStartTime));
//
//                audioExcutor.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        int i = mAudioEncoder.offEncoder(data);
//                        final long stopTime2 = System.currentTimeMillis();
//                        audioCount += i;
//                        Log.i(TAG, "run: ----------------编码后音频长度：" + i + "-----总长度audioCount:" + audioCount+"----编码时间:" + (stopTime2 - mStartTime));
//                    }
//                });

            }
        });

    }


    private boolean first = true;

    @Override
    protected void onResume() {
        super.onResume();

        mStartTime = System.currentTimeMillis();

        Log.i(TAG, "onResume: ");
        if (first) {


            first = false;
        } else {
            mMediaRecorder.startPreview();
        }

        mMediaRecorder.startRecord();


    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaRecorder.stopPreview();
        mMediaRecorder.stopRecord();


    }

    @Override
    public void onBackPressed() {
        mMediaRecorder.release();


        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMediaRecorder.release();


//        mAvcEncoder.close();
        mAudioEncoder.close();
//        executor.shutdown();
        audioExcutor.shutdown();
    }


}
