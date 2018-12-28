package com.vily.videodemo1.Camer1.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.app.Application;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import com.vily.videodemo1.camera0.RecordActivity;
import com.vily.videodemo1.manage.MediaMuxerManager;

import static com.vily.videodemo1.MyApplication.H264_PLAY_PATH;

public class AvcEncoder {

    private static final String TAG = "AvcEncoder";

    private MediaCodec mediaCodec;
    int m_width;
    int m_height;
    byte[] m_info = null;

    private int mColorFormat;
    private MediaCodecInfo codecInfo;
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private byte[] yuv420 = null;
    private int count = 0;
    private int mTrackIndex;


    private int mAudioTrackIndex;

    @SuppressLint("NewApi")
    public AvcEncoder(int width, int height, int framerate, int bitrate) {

        m_height = height;
        yuv420 = new byte[width * height * 3 / 2];
        m_width = width;
        try {
            codecInfo = getMediaCodecInfoByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            mColorFormat = getColorFormat(codecInfo);
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
//            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);


            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);//关键帧间隔时间 单位s

            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static int getColorFormat(MediaCodecInfo mediaCodecInfo) {
        Log.i(TAG, "getColorFormat: ------sss");
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
                Log.i(TAG, "---------selected yuv420p");
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                Log.i(TAG, "---------selected yuv420pp");
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                Log.i(TAG, "----------selected yuv420sp");
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
                Log.i(TAG, "-----------selected yuv420psp");
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


    @SuppressLint("NewApi")
    public void close() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 视频编码
    @SuppressLint("NewApi")
    public int offerEncoder(byte[] input, byte[] output) {
        long startTime = System.nanoTime();
        int pos = 0;
        nV21ToNV12(input, yuv420, m_width, m_height);

        try {
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);


            Log.i("--", "offerEncoder: -----------inputBufferIndex:" + inputBufferIndex);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(yuv420);
                long timepts = 1000000 * count / 20;
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv420.length, timepts  , 0);

            }
            count++;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0); // -1 一直等  0 不等

            Log.i(TAG, "offerEncoder: -------------outputBufferIndex:" + outputBufferIndex);
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat mediaFormat = mediaCodec.getOutputFormat();
                mTrackIndex = MediaMuxerManager.getInstance().addTrack(mediaFormat);
                Log.i(TAG, "offerEncoder: ------走这里le吗");
                MediaMuxerManager.getInstance().start();
            }
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                outputBuffer.position(bufferInfo.offset);
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                if (m_info != null) {
                    System.arraycopy(outData, 0, output, pos, outData.length);
                    pos += outData.length;

                } else {//保存pps sps 只有开始时 第一个帧里有， 保存起来后面用

                    ByteBuffer spsPpsBuffer = ByteBuffer.wrap(outData);

                    if (spsPpsBuffer.getInt() == 0x00000001) {
                        m_info = new byte[outData.length];
                        System.arraycopy(outData, 0, m_info, 0, outData.length);
                    } else {
                        return -1;
                    }
                }
                if (output[4] == 0x65) {//key frame 编码器生成关键帧时只有 00 00 00 01 65 没有pps sps， 要加上
                    System.arraycopy(m_info, 0, output, 0, m_info.length);
                    System.arraycopy(outData, 0, output, m_info.length, outData.length);
                }


                //  将编码后的数据写入到MP4复用器
                MediaMuxerManager.getInstance().writeSampleData(mTrackIndex, outputBuffer, bufferInfo);

                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }


        } catch (Throwable t) {
            t.printStackTrace();
        }

        return pos;
    }

//    private File file;
//    private FileOutputStream fileOutputStream = null;
//    private void write2LocalFile(byte[] outData) {
//
//        try {
//
//
//            if(file==null){
//                file = new File(H264_PLAY_PATH);
//            }
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//
//            fileOutputStream = new FileOutputStream(file);
//
//            fileOutputStream.write(outData);
//
//        } catch (FileNotFoundException e) {
//            Log.e(TAG, "copyVideoResourceToMemory FileNotFoundException : " + e);
//        } catch (IOException e) {
//            Log.e(TAG, "copyVideoResourceToMemory IOException : " + e);
//        } finally {
//            try {
//
//                if(fileOutputStream!=null){
//                    fileOutputStream.close();
//                }
//
//            } catch (IOException e) {
//                Log.e(TAG, "close stream IOException : " + e);
//            }
//
//        }
//    }

    //网友提供的，如果swapYV12toI420方法颜色不对可以试下这个方法，不同机型有不同的转码方式
    private void NV21toI420SemiPlanar(byte[] nv21bytes, byte[] i420bytes, int width, int height) {
        Log.v("xmc", "NV21toI420SemiPlanar:::" + width + "+" + height);
        final int iSize = width * height;
        System.arraycopy(nv21bytes, 0, i420bytes, 0, iSize);

        for (int iIndex = 0; iIndex < iSize / 2; iIndex += 2) {
            i420bytes[iSize + iIndex / 2 + iSize / 4] = nv21bytes[iSize + iIndex]; // U
            i420bytes[iSize + iIndex / 2] = nv21bytes[iSize + iIndex + 1]; // V
        }
    }

    //yv12 转 yuv420p  yvu -> yuv
    public static void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height) {
        Log.v("xmc", "swapYV12toI420:::" + width + "+" + height);
        Log.v("xmc", "swapYV12toI420:::" + yv12bytes.length + "+" + i420bytes.length + "+" + width * height);
        System.arraycopy(yv12bytes, 0, i420bytes, 0, width * height);
        System.arraycopy(yv12bytes, width * height + width * height / 4, i420bytes, width * height, width * height / 4);
        System.arraycopy(yv12bytes, width * height, i420bytes, width * height + width * height / 4, width * height / 4);
    }
    //public static void arraycopy(Object src,int srcPos,Object dest,int destPos,int length)
    //src:源数组；	srcPos:源数组要复制的起始位置；
    //dest:目的数组；	destPos:目的数组放置的起始位置；	length:复制的长度。

    private void nV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }

        yuv420 = nv12;
    }


}

