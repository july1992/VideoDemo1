package com.vily.videodemo1.manage;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.vily.videodemo1.MyApplication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhangxd on 2018/7/6.
 */

public class DecoderManager2 {

    private static final String TAG = DecoderManager2.class.getSimpleName();


    private static String mFormate = MediaFormat.MIMETYPE_VIDEO_HEVC;
    private static DecoderManager2 instance;

    private MediaCodec mediaCodec;

    private MediaFormat mediaFormat;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private SpeedManager mSpeedController = new SpeedManager();


    public DecoderManager2() {
    }

    public static DecoderManager2 getInstance() {
        if (instance == null) {
            instance = new DecoderManager2();
        }
        return instance;
    }

    public void close() {
        try {
            Log.d(TAG, "close start");
            if (mediaCodec != null) {


                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
                mSpeedController.reset();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        DecodeH264File.getInstance().close();
        instance = null;
    }


    public void startH264Decode(int width, int height, Surface surface) {
        Log.i(TAG, "startH264Decode: ---------初始化解码");
        try {
            mediaCodec = MediaCodec.createDecoderByType(mFormate);
            mediaFormat = MediaFormat.createVideoFormat(mFormate, width, height);


        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaCodec.configure(mediaFormat, surface, null, 0);
        mediaCodec.start();
        Log.i(TAG, "startH264Decode: -----------初始化解码完成");

        executor.execute(new Runnable() {
            @Override
            public void run() {
                playDecode(null);
            }
        });
    }

    private int count = 0;

    /**
     * 解析播放H264码流
     */
    public void playDecode(byte[] input) {
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();

        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);


        Log.i("--", "playDecode: -----------inputBufferIndex:" + inputBufferIndex);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(input);
            long timepts = 1000000 * count / 20;
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, timepts, 0);

        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0); // -1 一直等  0 不等

        Log.i(TAG, "playDecode: -------------outputBufferIndex:" + outputBufferIndex);

        if (outputBufferIndex >= 0) {
//            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
//            final byte[] outData = new byte[bufferInfo.size];
//            outputBuffer.get(outData);

            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
//            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }

}
