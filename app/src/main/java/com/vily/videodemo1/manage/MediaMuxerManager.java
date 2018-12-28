package com.vily.videodemo1.manage;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zhangxd on 2018/8/27.    MP4复写
 */

public class MediaMuxerManager {

    private static final String TAG = MediaMuxerManager.class.getSimpleName();

    private static final String MP4_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ganwu3.mp4";

    private static MediaMuxerManager instance;

    private MediaMuxer mediaMuxer;

    private  boolean isStart = false;

    private int trackCount = 0;

    public static MediaMuxerManager getInstance() {
        if (instance == null) {
            synchronized (MediaMuxerManager.class) {
                if (instance == null) {
                    instance = new MediaMuxerManager();
                }
            }
        }
        return instance;
    }

    public MediaMuxerManager() {

    }

    public void init() {
        try {
            Log.d(TAG, "------init----------"+MP4_PATH);
            mediaMuxer = new MediaMuxer(MP4_PATH, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            Log.e(TAG, "init :::" + e);
        }

    }

    public int addTrack(MediaFormat mediaFormat) {
        synchronized (instance) {
            Log.d(TAG, "addTrack--------");
            trackCount++;
            return mediaMuxer.addTrack(mediaFormat);
        }
    }

    public void writeSampleData(int traceIndex, ByteBuffer byteBuffer, BufferInfo bufferInfo) {
        synchronized (instance) {

            if (!isStart) {
                return;
            }
            Log.d(TAG, "writeSampleData--------正在写入复写器"+"---"+traceIndex);
            mediaMuxer.writeSampleData(traceIndex, byteBuffer, bufferInfo);
        }
    }

    public void start() {
        Log.i(TAG, "start1: ----------:"+trackCount+"---"+isStart);
        synchronized (instance) {
            //已经开始或者音视频都没有全部添加返回  trackCount=1  只有视频或者音频写入， =2 音频视频都写入
            if (isStart || trackCount != 1) {
                return;
            }
            mediaMuxer.start();
            isStart = true;

        }
    }

    public void close() {
        isStart = false;
        mediaMuxer.stop();
        mediaMuxer.release();
        instance = null;
    }

    public boolean isReady() {
        return trackCount == 2;
    }

    public String getPath(){

        return MP4_PATH;
    }

}
