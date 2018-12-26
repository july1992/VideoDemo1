package com.vily.videodemo1.camera2;


import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import com.vily.videodemo1.Camer1.utils.AvcEncoder;
import com.vily.videodemo1.R;

import java.io.File;

import java.io.IOException;
import java.nio.ByteBuffer;

import java.util.Arrays;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import static android.media.MediaCodec.CONFIGURE_FLAG_ENCODE;
import static android.media.MediaFormat.KEY_BIT_RATE;
import static android.media.MediaFormat.KEY_COLOR_FORMAT;
import static android.media.MediaFormat.KEY_FRAME_RATE;
import static android.media.MediaFormat.KEY_I_FRAME_INTERVAL;
import static android.media.MediaFormat.KEY_MAX_INPUT_SIZE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AutoFitTextureView mTextureView;

    private Camera2Utils mCamera2Utils;
    private File mFile;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    //编码数量
    int encodeCount = 0;

    private ImageView mIv_change_flash;
    private ImageView mIv_change_camera;

    private MediaCodec mMediaCoder;
    private AutoFitTextureView mSv_surface2;
    private int mHeight;
    private int mWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mTextureView = findViewById(R.id.texture);
        mSv_surface2 = findViewById(R.id.sv_surface2);
        mIv_change_flash = findViewById(R.id.iv_change_flash);
        mIv_change_camera = findViewById(R.id.iv_change_camera);


        mCamera2Utils = new Camera2Utils(MainActivity.this, mTextureView);
        mWidth = getWindowManager().getDefaultDisplay().getWidth();
        mHeight = getWindowManager().getDefaultDisplay().getHeight();

        mSv_surface2.setAspectRatio(320,480);

        mSv_surface2.bringToFront();
        initMediaCodec();

        mCamera2Utils.prepare(mTextureView);
        initListener();
    }

    private void initMediaCodec() {
        int bitrate = 2 * 1280 * 720 * 20 / 20;
        try {

            MediaCodecInfo mediaCodecInfo = getMediaCodecInfoByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            int colorFormat = getColorFormat(mediaCodecInfo);
            mMediaCoder = MediaCodec.createByCodecName(mediaCodecInfo.getName());
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 1280, 720);
            format.setInteger(KEY_MAX_INPUT_SIZE, 0);
            format.setInteger(KEY_BIT_RATE, 1200000);
            format.setInteger(KEY_COLOR_FORMAT, colorFormat);
            format.setInteger(KEY_FRAME_RATE, 15);
            format.setInteger(KEY_I_FRAME_INTERVAL, 5);
            mMediaCoder.configure(format, null, null, CONFIGURE_FLAG_ENCODE);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getColorFormat(MediaCodecInfo mediaCodecInfo) {
        int matchedForamt = 0;
        MediaCodecInfo.CodecCapabilities codecCapabilities = mediaCodecInfo.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC);
        for (int i = 0; i < codecCapabilities.colorFormats.length; i++) {
            int format = codecCapabilities.colorFormats[i];
            if (format >= MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar && format <= MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar) {
                if (format >= matchedForamt) {
                    matchedForamt = format;
                }
            }
        }
        switch (matchedForamt) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                Log.i(TAG, "selected yuv420p");
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                Log.i(TAG, "selected yuv420pp");
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                Log.i(TAG, "selected yuv420sp");
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
                Log.i(TAG, "selected yuv420psp");
                break;

        }
        return matchedForamt;
    }

    private static MediaCodecInfo getMediaCodecInfoByType(String mimeType) {
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    private MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            //是否是编码器
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            Log.i(TAG, "selectCodec: -------:" + Arrays.toString(types));
            for (String type : types) {

                Log.i(TAG, "selectCodec: -------:" + mimeType.equalsIgnoreCase(type));
                if (mimeType.equalsIgnoreCase(type)) {

                    Log.i(TAG, "selectCodec: -------:" + codecInfo.getName());
                    return codecInfo;
                }
            }
        }
        return null;
    }

    private boolean isFlashOpen = false;  // 默认闪光灯是关闭的
    private String flag="big";

    private void initListener() {
        mIv_change_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 打开或关闭闪光灯
                if (isFlashOpen) {
                    isFlashOpen = false;
                    mIv_change_flash.setImageResource(R.mipmap.video_flash_close);
                    mCamera2Utils.closeFlash();
                } else {
                    mIv_change_flash.setImageResource(R.mipmap.video_flash_open);
                    mCamera2Utils.openFlash();
                    isFlashOpen = true;
                }

            }
        });

        mSv_surface2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: ---------");
                if("big".equals(flag)){
                    flag="small";
                    mTextureView.setAspectRatio(320,480);
                    mSv_surface2.setAspectRatio(mWidth,mHeight);
                    mTextureView.bringToFront();
                }
            }
        });
        mTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: -----------");
                if("small".equals(flag)){
                    flag="big";
                    mSv_surface2.setAspectRatio(320,480);
                    mTextureView.setAspectRatio(mWidth,mHeight);
                    mSv_surface2.bringToFront();
                }
            }
        });

//        mIv_change_camera.setOnClickListener(new View.OnClickListener() {
//
//
//
//            @Override
//            public void onClick(View view) {
//
//                switch (flag){
//
//                    case "big" :
//                        flag="small";
//                        mTextureView.setAspectRatio(320,480);
//                        mSv_surface2.setAspectRatio(mWidth,mHeight);
//                        mTextureView.bringToFront();
//                        break;
//                    case "small" :
//                        flag="big";
//                        mSv_surface2.setAspectRatio(320,480);
//                        mTextureView.setAspectRatio(mWidth,mHeight);
//                        mSv_surface2.bringToFront();
//                        break;
//                    default :
//                        break;
//                }
//
//
//            }
//        });
        mCamera2Utils.setOnCameraStateListener(new Camera2Utils.OnCameraStateListener() {
            @Override
            public void onCameraState(String state) {

                switch (state) {

                    case "onDisconnected":  // 摄像头断开
                        mCamera2Utils.closeCamera();
                        break;
                    case "onError":    // 摄像头错误
                        mCamera2Utils.closeCamera();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onDataBack(Image image) {
//                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                final byte[] bytes = new byte[buffer.remaining()];
//                Log.i(TAG, "run: ------bytes视频预览帧：" + bytes.length);
//                executor.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        long encodeTime = System.currentTimeMillis();
//                        flvPackage(bytes);
//
//                        Log.i(TAG, "run: ----------" + "编码第:" + (encodeCount++) + "帧，耗时:" + (System.currentTimeMillis() - encodeTime));
//
//                    }
//                });
//
//
////                buffer.get(bytes);
//
                image.close();

            }
        });
    }


    private void flvPackage(byte[] buf) {
        final int LENGTH = 720 * 1280;



        ByteBuffer[] inputBuffers = mMediaCoder.getInputBuffers();
        ByteBuffer[] outputBuffers = mMediaCoder.getOutputBuffers();


        try {
            //查找可用的的input buffer用来填充有效数据
            Log.i(TAG, "flvPackage: ---------正在编码");

            int bufferIndex = mMediaCoder.dequeueInputBuffer(-1);
            Log.i(TAG, "flvPackage: ---------bufferIndex:" + bufferIndex);

            if (bufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[bufferIndex];
                inputBuffer.clear();
                inputBuffer.put(buf, 0, buf.length);
                mMediaCoder.queueInputBuffer(bufferIndex, 0, buf.length, 0, 0);
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mMediaCoder.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

                Log.i(TAG, "flvPackage: -----------outputBufferIndex");
                mMediaCoder.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCoder.dequeueOutputBuffer(bufferInfo, 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume: ----------");
        if (mTextureView.isAvailable()) {

            Log.i(TAG, "onResume: ------------2");
            mMediaCoder.start();
            mCamera2Utils.restartPreview();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }


    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {


            mCamera2Utils.openCamera();
            mMediaCoder.start();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            Log.i(TAG, "onSurfaceTextureSizeChanged: ---------------");
            mCamera2Utils.configureTransform(mTextureView);

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {

        }

    };

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause: ----------onpause");
        mCamera2Utils.stopPreview();
        mMediaCoder.stop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy: -------");
        mCamera2Utils.closeCamera();
        mMediaCoder.stop();
        mMediaCoder.release();
    }
}
