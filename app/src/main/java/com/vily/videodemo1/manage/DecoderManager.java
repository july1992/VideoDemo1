package com.vily.videodemo1.manage;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;


import com.vily.videodemo1.MyApplication;
import com.vily.videodemo1.camera0.RecordActivity;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zhangxd on 2018/7/6.
 */

public class DecoderManager {

    private static final String TAG = DecoderManager.class.getSimpleName();


//    public static String PATH = "http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8";

    private static DecoderManager instance;

    private MediaCodec mediaCodec;

    private MediaFormat mediaFormat;

    private long frameIndex;

    private volatile boolean isDecodeFinish = false;

    private MediaExtractor mediaExtractor;

    private SpeedManager mSpeedController = new SpeedManager();


    private DecoderManager() {
    }

    public static DecoderManager getInstance() {
        if (instance == null) {
            instance = new DecoderManager();
        }
        return instance;
    }



    /**
     * 解析播放H264码流
     */
    public void playDecode(){
        long pts = 0;

        long startTime = System.nanoTime();
        while (!isDecodeFinish) {
            if (mediaCodec != null) {
                int inputIndex = mediaCodec.dequeueInputBuffer(-1);
                if (inputIndex >= 0) {
                    ByteBuffer byteBuffer = mediaCodec.getInputBuffer(inputIndex);
                    int sampSize = DecodeH264File.getInstance().readSampleData(byteBuffer);
                    long time = (System.nanoTime() - startTime) / 1000;
                    if (sampSize > 0 && time > 0) {
                        mediaCodec.queueInputBuffer(inputIndex, 0, sampSize, time, 0);
                        mSpeedController.preRender(time);
                    }

                }
            }
            BufferInfo bufferInfo = new BufferInfo();
            int outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (outIndex >= 0) {
                mediaCodec.releaseOutputBuffer(outIndex, true);
            }
        }

    }


    public void close() {
        try {
            Log.d(TAG, "close start");
            if (mediaCodec != null) {
                isDecodeFinish = true;

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


    public void startH264Decode(int width, int height, SurfaceHolder surfaceHolder2) {
        Log.i(TAG, "startH264Decode: ---------初始化解码");
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
            mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
            mediaExtractor = new MediaExtractor();
            //MP4 文件存放位置
            mediaExtractor.setDataSource(MyApplication.MP4_PLAY_PATH);
            Log.d(TAG, "getTrackCount: " + mediaExtractor.getTrackCount());
            for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                MediaFormat format = mediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                Log.d(TAG, "mime: " + mime);
                if (mime.startsWith("video")) {
                    mediaFormat = format;
                    mediaExtractor.selectTrack(i);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaCodec.configure(mediaFormat, surfaceHolder2.getSurface(), null, 0);
        mediaCodec.start();
        Log.i(TAG, "startH264Decode: -----------初始化解码完成");
    }

}
