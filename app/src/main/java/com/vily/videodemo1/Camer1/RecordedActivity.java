package com.vily.videodemo1.Camer1;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.vily.videodemo1.R;
import com.vily.videodemo1.netty.NettyClient;
import com.vily.videodemo1.netty.NettyListener;


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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_resord);

        initUI();
        initData();

//        connectNettyServer("192.168.93.113", 1994);
        initMediaRecorder();

    }

    private void connectNettyServer(String host, int port) {
        mNettyClient = new NettyClient(host, port);

        Log.i(TAG, "connectNettyServer");
        if (!mNettyClient.getConnectStatus()) {
            mNettyClient.setListener(new NettyListener() {
                @Override
                public void onMessageResponse(final Object msg) {
                    // 接收服务端发送过来的 json数据解析
                    Log.i(TAG, "onMessageResponse:" + msg);

                    //需要在主线程中刷新
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Log.i(TAG, "run: ----------------msg:"+msg);

                        }
                    });
                }

                @Override
                public void onServiceStatusConnectChanged(final int statusCode) {
                    /**
                     * 回调执行还在子线程中
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
                                Log.e(TAG, "STATUS_CONNECT_SUCCESS:");

                            } else {
                                Log.e(TAG, "onServiceStatusConnectChanged:" + statusCode);

                            }
                        }
                    });

                }
            });

            mNettyClient.connect();//连接服务器
        }
    }

    private void initUI() {

        sv_ffmpeg = findViewById(R.id.sv_ffmpeg);
        iv_change_flash = findViewById(R.id.iv_change_flash);
        iv_change_camera =  findViewById(R.id.iv_change_camera);

    }

    private void initData() {

        sv_ffmpeg.setTouchFocus(mMediaRecorder);

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
        mMediaRecorder.startRecord();


        mMediaRecorder.setOnVideoAndAudioByteListener(new MediaRecorderNative.OnVideoAndAudioByteListener() {

            @Override
            public void onVideoByte(byte[] data, Camera camera) {   // 1382400字节 == 1.3MB

                Log.i(TAG, "onVideoByte: -------视频流: "+data.length);
//                if (!mNettyClient.getConnectStatus()) {
//                    Toast.makeText(getApplicationContext(), "未连接,请先连接", LENGTH_SHORT).show();
//                } else {
//                      mNettyClient.sendMsgToServer(s, new ChannelFutureListener() {
//                        @Override
//                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                            if (channelFuture.isSuccess()) {                //4
//                                Log.d(TAG, "Write auth successful");
//                            } else {
//                                Log.d(TAG, "Write auth error");
//                            }
//                        }
//                    });
//                }

            }

            @Override
            public void onAudioByte(byte[] data, int len) {          //   3584
//                Log.i(TAG, "onAudioByte: ----------音频流："+data.length);
//                if (!mNettyClient.getConnectStatus()) {
//                    Toast.makeText(getApplicationContext(), "未连接,请先连接", LENGTH_SHORT).show();
//                } else {
//                   mNettyClient.sendMsgToServer(data, new ChannelFutureListener() {
//                        @Override
//                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                            if (channelFuture.isSuccess()) {                //4
//                                Log.d(TAG, "Write auth successful");
//                            } else {
//                                Log.d(TAG, "Write auth error");
//                            }
//                        }
//                    });
//                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaRecorder.startPreview();
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
//        mNettyClient.disconnect();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMediaRecorder.release();
//        mNettyClient.disconnect();

    }

}
