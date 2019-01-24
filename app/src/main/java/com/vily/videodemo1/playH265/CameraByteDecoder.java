package com.vily.videodemo1.playH265;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *  * description :  通过 medecodec 去创建解码器，解码输出后会自动渲染到surface页面
 *  * Author : Vily
 *  * Date : 2019/1/7
 *  
 **/
public class CameraByteDecoder {

    private static final String TAG = "CameraRecordDecoder";



    //解码器
    private MediaCodec mCodec0;
    private boolean isFinish = false;  // 是否解码结束

    private static final int FRAME_MAX_LEN = 300 * 1024;
    //根据帧率获取的解码每帧需要休眠的时间,根据实际帧率进行操作
    private int PRE_FRAME_TIME = 1000 / 25;

    //保存完整数据帧
    private byte[] frame = new byte[FRAME_MAX_LEN];

    private int frameNum;

    private String mPath;
    private DecoderThread mDecoderThread;

    public CameraByteDecoder() {
        nalu = new NaluUnit();
    }

    // 初始化编码器
    public void initCameraDecode(int width,int height,SurfaceHolder holder) {
        try {
            //根据需要解码的类型创建解码器
            mCodec0 = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC); //"video/hevc"
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height);

        //SurfaceView
        mCodec0.configure(mediaFormat, holder.getSurface(), null, 0); //直接解码送surface显示

        //开始解码
        mCodec0.start();

    }


    // 开始解码
    public void decodeStart(String path){
        mPath=path;
        Log.i(TAG, "decodeStart: ---------这里ma");
        mDecoderThread = new DecoderThread(mPath);
        mDecoderThread.start();
    }

    public void sendByte(byte[] data) {
        // 开始播放不
        Log.i(TAG, "sendByte: ------开始播放不");

        onFrame(data,0,data.length);
    }


    //  解码线程
    private class DecoderThread extends Thread {


        private File file = null;
        private boolean findFlag = false;


        public DecoderThread(String mPath ) {
            file = new File(mPath);
            if (!file.exists() || !file.canRead()) {
                Log.e(TAG, "failed to open h265 file.");
                return;
            }

        }

        @Override
        public void run() {
            super.run();
            int readlen = 0;
            int writelen = 0;
            int i = 0;
            int pos = 0;
            int index = 0;
            int index0 = 0;

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            //每次从文件读取的数据
            frameNum = 0;
            long startTime = System.currentTimeMillis();
            while (!isFinish) {
                try {
                    if (fis.available() > 0) {
                        readlen = fis.read(frame, pos, frame.length - pos);
                        if (readlen <= 0) {
                            break;
                        }
                        readlen += pos;

                        i = 0;
                        pos = 0;
                        writelen = readlen;

                        //while(i < readlen-4) {
                        for (i = 0; i < readlen - 4; i++) {
                            findFlag = false;
                            index = 0;
                            index0 = 0;
                            if (frame[i] == 0x00 && frame[i + 1] == 0x00 && frame[i + 2] == 0x01) {
                                pos = i;
                                if (i > 0) {
                                    if (frame[i - 1] == 0x00) { //start with 0x00 0x00 0x00 0x01
                                        index = 1;
                                    }
                                }
                                //while (pos < readlen-4) {
                                for (pos = i + 3; pos < readlen - 4; pos++) {
                                    if (frame[pos] == 0x00 && frame[pos + 1] == 0x00 && frame[pos + 2] == 0x01) {
                                        findFlag = true;
                                        if (frame[pos - 1] == 0x00) {//start with 0x00 0x00 0x00 0x01
                                            index0 = 1;
                                        }
                                        break;
                                    }
                                }

                                Log.i(TAG, "run: --------findFlag:"+findFlag+"----isfinish:"+isFinish);
                                if (findFlag && !isFinish) {
                                    nalu.type = (frame[i + 3] & 0x7E) >> 1;
                                    if (index == 1) {
                                        i = i - 1;
                                    }
                                    if (index0 == 1) {
                                        pos = pos - 1;
                                    }

                                    onFrame(frame, i, pos - i); // start code + nalu 送解码器
                                    i = pos;
                                    writelen = i;

                                    Log.i(TAG, " nalu type = " + nalu.type + ", nalu.size = " + i);

                                    frameNum++;
                                    //Log.i(TAG," frameNum = "+frameNum);
                                    long time = PRE_FRAME_TIME - (System.currentTimeMillis() - startTime);
                                    if (time > 0) {
                                        try {
                                            Thread.sleep(time);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    startTime = System.currentTimeMillis();
                                } else {
                                    writelen = i;
                                    break;
                                }
                            }
                        }

                        if (writelen > 0 && writelen < readlen) {
                            System.arraycopy(frame, writelen, frame, 0, readlen - writelen);
                            //Log.i(TAG, " readlen = "+readlen+", writelen = "+writelen);
                        }

                        pos = readlen - writelen;
                    } else {
                        isFinish = true;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG, "  error =  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            isFinish = false;
            Log.i(TAG, "         frameNum     " + frameNum);
        }
    }

    NaluUnit nalu;

    public class NaluUnit {
        byte[] data;
        int size;
        int type;

        public NaluUnit() {
            data = new byte[20 * 1024];
            size = 0;
        }
    }

    int mCount0 = 0;

    // 解码中
    public void onFrame(byte[] buf, int offset, int length) {
        //-1表示一直等待；0表示不等待；其他大于0的参数表示等待毫秒数
        //Log.e(TAG,"        onFrame start       ");

        Log.i(TAG, "onFrame: -------isfinish:"+isFinish);
        if (mCodec0 != null) {
            int inputBufferIndex = mCodec0.dequeueInputBuffer(-1);
            if (!isFinish && inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = mCodec0.getInputBuffer(inputBufferIndex);
                inputBuffer.clear();
                inputBuffer.put(buf, offset, length);
                //解码
                long timestamp = mCount0 * 1000000 / 25;
                mCodec0.queueInputBuffer(inputBufferIndex, 0, length, timestamp, 0);
                mCount0++;
            }
            //Log.e(TAG,"        onFrame middle      ");
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mCodec0.dequeueOutputBuffer(bufferInfo, 0); //10
            //循环解码，直到数据全部解码完成
            while (outputBufferIndex >= 0 && !isFinish) {
                //logger.d("outputBufferIndex = " + outputBufferIndex);
                mCodec0.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = mCodec0.dequeueOutputBuffer(bufferInfo, 0);
            }
        }

    }


    public void onDestroy(){
        isFinish = true;

        if(mCodec0!=null){
            try {
                mCodec0.stop();

                mCodec0.release();
                Log.i(TAG, "onDestroy: ------走这里流不");
                mCodec0 = null;

            } catch (Exception e) {
                e.printStackTrace();
                mCodec0 = null;
            }
        }
        if(mDecoderThread!=null){
            mDecoderThread=null;
        }

    }

}
