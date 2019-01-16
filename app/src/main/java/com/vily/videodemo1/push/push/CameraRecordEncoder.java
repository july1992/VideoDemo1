package com.vily.videodemo1.push.push;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.vily.videodemo1.push.egl.EglHelper;
import com.vily.videodemo1.push.egl.EGLSurfaceView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLContext;

public class CameraRecordEncoder {

    private static final String TAG = "CameraRecordEncoder";

    private Surface surface;
    private EGLContext eglContext;

    private int width;
    private int height;
    private int bit_rate=80000;
    private int framrate=25;

    private MediaCodec videoEncodec;
    private MediaFormat videoFormat;
    private MediaCodec.BufferInfo videoBufferinfo;

//    private MediaCodec audioEncodec;
//    private MediaFormat audioFormat;
//    private MediaCodec.BufferInfo audioBufferinfo;
//    private long audioPts = 0;
//    private int sampleRate;

    private WlEGLMediaThread wlEGLMediaThread;
    private VideoEncodecThread videoEncodecThread;
//    private AudioEncodecThread audioEncodecThread;
//    private WlAudioRecordUitl wlAudioRecordUitl;


    private EGLSurfaceView.WlGLRender wlGLRender;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    private OnMediaInfoListener onMediaInfoListener;

    public CameraRecordEncoder(){

    }
    public CameraRecordEncoder(Context context, int textureId) {
        CameraRecordRender wlEncodecPushRender = new CameraRecordRender(context, textureId);
        setRender(wlEncodecPushRender);
        setmRenderMode(CameraRecordEncoder.RENDERMODE_CONTINUOUSLY);
    }

    public void setRender(EGLSurfaceView.WlGLRender wlGLRender) {
        this.wlGLRender = wlGLRender;
    }

    public void setmRenderMode(int mRenderMode) {
        if(wlGLRender == null)
        {
            throw  new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener) {
        this.onMediaInfoListener = onMediaInfoListener;
    }

    public void initEncodec(EGLContext eglContext, int width, int height)
    {
        this.width = width;
        this.height = height;
        this.eglContext = eglContext;
        initMediaEncodec(width, height, 44100, 2);
    }

    public void startRecord()
    {
        Log.i(TAG, "startRecord: -------------:这里？");
        if(surface != null && eglContext != null)
        {

            Log.i(TAG, "startRecord: ---------------开始录制");
//            audioPts = 0;

            wlEGLMediaThread = new WlEGLMediaThread(new WeakReference<CameraRecordEncoder>(this));
            videoEncodecThread = new VideoEncodecThread(new WeakReference<CameraRecordEncoder>(this));
//            audioEncodecThread = new AudioEncodecThread(new WeakReference<CameraRecordEncoder>(this));
            wlEGLMediaThread.isCreate = true;
            wlEGLMediaThread.isChange = true;
            wlEGLMediaThread.start();
            videoEncodecThread.start();
//            audioEncodecThread.start();
//            wlAudioRecordUitl.startRecord();
        }
    }

    public void stopRecord()
    {
        if(wlEGLMediaThread != null && videoEncodecThread != null )
        {
//            wlAudioRecordUitl.stopRecord();
            videoEncodecThread.exit();
//            audioEncodecThread.exit();
            wlEGLMediaThread.onDestory();
            videoEncodecThread = null;
            wlEGLMediaThread = null;
//            audioEncodecThread = null;
        }
    }

    private void initMediaEncodec(int width, int height, int sampleRate, int channelCount)
    {
        Log.i(TAG, "initMediaEncodec: ------------------hhhhh");
        initVideoEncodec(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height);
//        initAudioEncodec(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount);
//        initPCMRecord();
    }

//    private void initPCMRecord()
//    {
//        wlAudioRecordUitl = new WlAudioRecordUitl();
//        wlAudioRecordUitl.setOnRecordLisener(new WlAudioRecordUitl.OnRecordLisener() {
//            @Override
//            public void recordByte(byte[] audioData, int readSize) {
//                if(wlAudioRecordUitl.isStart())
//                {
//                    putPCMData(audioData, readSize);
//                }
//            }
//        });
//    }


    private void initVideoEncodec(String mimeType, int width, int height)
    {
        try {
            videoBufferinfo = new MediaCodec.BufferInfo();
            videoFormat = MediaFormat.createVideoFormat(mimeType, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bit_rate);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framrate);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            videoEncodec = MediaCodec.createEncoderByType(mimeType);
            videoEncodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            Log.i(TAG, "initVideoEncodec: ---------------这里走道了嘛");
            surface = videoEncodec.createInputSurface();

        } catch (IOException e) {
            e.printStackTrace();
            videoEncodec = null;
            videoFormat = null;
            videoBufferinfo = null;
        }

    }

//    private void initAudioEncodec(String mimeType, int sampleRate, int channelCount)
//    {
//        try {
//            this.sampleRate = sampleRate;
//            audioBufferinfo = new MediaCodec.BufferInfo();
//            audioFormat = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount);
//            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
//            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
//            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096 * 10);
//
//            audioEncodec = MediaCodec.createEncoderByType(mimeType);
//            audioEncodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            audioBufferinfo = null;
//            audioFormat = null;
//            audioEncodec = null;
//        }
//    }
//
//    public void putPCMData(byte[] buffer, int size)
//    {
//        if(audioEncodecThread != null && !audioEncodecThread.isExit && buffer != null && size > 0)
//        {
//            int inputBufferindex = audioEncodec.dequeueInputBuffer(0);
//            if(inputBufferindex >= 0)
//            {
//                ByteBuffer byteBuffer = audioEncodec.getInputBuffers()[inputBufferindex];
//                byteBuffer.clear();
//                byteBuffer.put(buffer);
//                long pts = getAudioPts(size, sampleRate);
//                audioEncodec.queueInputBuffer(inputBufferindex, 0, size, pts, 0);
//            }
//        }
//    }

    // 将通过 surface = videoEncodec.createInputSurface(); 获取到到suface渲染出去
    static class WlEGLMediaThread extends Thread
    {
        private WeakReference<CameraRecordEncoder> encoder;
        private EglHelper eglHelper;
        private Object object;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        public WlEGLMediaThread(WeakReference<CameraRecordEncoder> encoder) {
            this.encoder = encoder;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();
            eglHelper = new EglHelper();
            eglHelper.initEgl(encoder.get().surface, encoder.get().eglContext);

            while(true)
            {
                if(isExit)
                {
                    release();
                    break;
                }

                if(isStart)
                {
                    if(encoder.get().mRenderMode == RENDERMODE_WHEN_DIRTY)
                    {
                        synchronized (object)
                        {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if(encoder.get().mRenderMode == RENDERMODE_CONTINUOUSLY)
                    {
                        try {
                            Thread.sleep(1000 / 25);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        throw  new RuntimeException("mRenderMode is wrong value");
                    }
                }
                onCreate();
                onChange(encoder.get().width, encoder.get().height);
                onDraw();
                isStart = true;
            }

        }

        private void onCreate()
        {
            if(isCreate && encoder.get().wlGLRender != null)
            {
                isCreate = false;
                encoder.get().wlGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height)
        {
            if(isChange && encoder.get().wlGLRender != null)
            {
                isChange = false;
                encoder.get().wlGLRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw()
        {
            if(encoder.get().wlGLRender != null && eglHelper != null)
            {
                encoder.get().wlGLRender.onDrawFrame();
                if(!isStart)
                {
                    encoder.get().wlGLRender.onDrawFrame();
                }
                eglHelper.swapBuffers();

            }
        }

        private void requestRender()
        {
            if(object != null)
            {
                synchronized (object)
                {
                    object.notifyAll();
                }
            }
        }

        public void onDestory()
        {
            isExit = true;
            requestRender();
        }

        public void release()
        {
            if(eglHelper != null)
            {
                eglHelper.destoryEgl();
                eglHelper = null;
                object = null;
                encoder = null;
            }
        }
    }

    static class VideoEncodecThread extends Thread
    {
        private WeakReference<CameraRecordEncoder> encoder;

        private boolean isExit;

        private MediaCodec videoEncodec;
        private MediaCodec.BufferInfo videoBufferinfo;

        private long pts;
        private byte[] sps;
        private byte[] pps;
        private boolean keyFrame = false;

        public VideoEncodecThread(WeakReference<CameraRecordEncoder> encoder) {
            this.encoder = encoder;
            Log.i(TAG, "VideoEncodecThread: ------------------ 构造方法");
            videoEncodec = encoder.get().videoEncodec;
            videoBufferinfo = encoder.get().videoBufferinfo;
        }

        @Override
        public void run() {
            super.run();
            pts = 0;
            isExit = false;
            videoEncodec.start();
            while(true)
            {
                if(isExit)
                {

                    videoEncodec.stop();
                    videoEncodec.release();
                    videoEncodec = null;
                    Log.d(TAG, "---------------------------视频录制完成");
                    break;
                }

                int outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                keyFrame = false;

                if(outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
                {
                    Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED-----------------");

                    ByteBuffer spsb = videoEncodec.getOutputFormat().getByteBuffer("csd-0");
                    sps = new byte[spsb.remaining()];
                    spsb.get(sps, 0, sps.length);

//                    ByteBuffer ppsb = videoEncodec.getOutputFormat().getByteBuffer("csd-1");
//                    pps = new byte[ppsb.remaining()];
//                    ppsb.get(pps, 0, pps.length);

                }
                else
                {
                    while (outputBufferIndex >= 0)
                    {
                        ByteBuffer outputBuffer = videoEncodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(videoBufferinfo.offset);
                        outputBuffer.limit(videoBufferinfo.offset + videoBufferinfo.size);
                        //
                        if(pts == 0)
                        {
                            pts = videoBufferinfo.presentationTimeUs;
                        }
                        videoBufferinfo.presentationTimeUs = videoBufferinfo.presentationTimeUs - pts;


                        byte[] data = new byte[outputBuffer.remaining()];
                        outputBuffer.get(data, 0, data.length);


                        if(videoBufferinfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME)
                        {
                            keyFrame = true;
                            if(encoder.get().onMediaInfoListener != null)
                            {
                                encoder.get().onMediaInfoListener.onSPSPPSInfo(sps);
                            }
                        }
                        if(encoder.get().onMediaInfoListener != null)
                        {
                            encoder.get().onMediaInfoListener.onVideoInfo(data, keyFrame);
                            encoder.get().onMediaInfoListener.onMediaTime((int) (videoBufferinfo.presentationTimeUs / 1000000));
                        }
                        videoEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);

                    }
                }
            }
        }

        public void exit()
        {
            isExit = true;
        }

    }

    public interface OnMediaInfoListener
    {
        void onMediaTime(int times);

        void onSPSPPSInfo(byte[] head);

        void onVideoInfo(byte[] data, boolean keyframe);


    }


    public static String byteToHex(byte[] bytes)
    {
        StringBuffer stringBuffer = new StringBuffer();
        for(int i = 0; i < bytes.length; i++)
        {
            String hex = Integer.toHexString(bytes[i]);
            if(hex.length() == 1)
            {
                stringBuffer.append("0" + hex);
            }
            else
            {
                stringBuffer.append(hex);
            }
            if(i > 20)
            {
                break;
            }
        }
        return stringBuffer.toString();
    }

}
