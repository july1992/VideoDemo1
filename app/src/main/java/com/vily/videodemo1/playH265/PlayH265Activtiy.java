package com.vily.videodemo1.playH265;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.vily.videodemo1.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *  * description : 
 *  * Author : Vily
 *  * Date : 2018/12/29
 *  
 **/
public class PlayH265Activtiy extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private SurfaceView testSurfaceView;


    private SurfaceHolder holder;
    //文件路径
    private String path0 = Environment.getExternalStorageDirectory() + "/vilyxxx.h265";
//    private String path0 = Environment.getExternalStorageDirectory() + "/ganwuhhh.h265";


    private String TAG = "H264FileDecodeActivity";
    private int width = 320, height = 240;
    //解码器
    private MediaCodec mCodec0;
    //    private boolean isFirst = true;
    private boolean isFirst0 = true;

    //文件读取完成标识
    private boolean isFinish = false;
    private boolean isFinish0 = false;
    //这个值用于找到第一个帧头后，继续寻找第二个帧头，如果解码失败可以尝试缩小这个值
//    private int FRAME_MIN_LEN = 8; //1024;
    //一般H264帧大小不超过200k,如果解码失败可以尝试增大这个值
    private static final int FRAME_MAX_LEN = 300 * 1024;
    //根据帧率获取的解码每帧需要休眠的时间,根据实际帧率进行操作
    private int PRE_FRAME_TIME = 1000 / 25;

    //保存完整数据帧
    byte[] frame = new byte[FRAME_MAX_LEN];

    int frameNum;

    private boolean isExit = false;
    private DecoderThread mDecoderThread;
    private CameraRecordDecoder mCameraRecordDecoder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_play_h265);

        testSurfaceView = findViewById(R.id.surfaceview);
        holder = testSurfaceView.getHolder();
        holder.addCallback(this);




        Button btn = findViewById(R.id.takePhoto);
        btn.setOnClickListener(this);

        nalu = new NaluUnit();
        isFinish = false;
        isFinish0 = false;


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        width = holder.getSurfaceFrame().width();
//        height = holder.getSurfaceFrame().height();
        Log.i(TAG, "surfaceCreated:  width = " + width + ", height = " + height);
//        startCodec0();

        mCameraRecordDecoder = new CameraRecordDecoder();
        mCameraRecordDecoder.initCameraDecode(320,240,holder);


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (!isFinish0) {
//            doDecoder();
            //doDecodec();

//            mDecoderThread = new DecoderThread();
//            mDecoderThread.start();

            if(mCameraRecordDecoder!=null){
                mCameraRecordDecoder.decodeStart(path0);
            }

        }
    }

    public void startCodec0() {
        if (isFirst0) {
            initDecoder0();
        }
    }

    //h265的nal unit header有两个字节构成
    //hevc的nal包结构与h264有明显的不同，hevc加入了nal所在的时间层的ＩＤ，取去除了nal_ref_idc，此信息合并到了naltype中，
    //通常情况下F为0，layerid为0,  TID为1
    //| F(1bit) | Type(6bits) | LayerId(6bits) | TID (3bits)|
    //H265的NALU类型
    //00 00 00 01 40 01  的nuh_unit_type的值为 32， 语义为视频参数集        VPS
    //00 00 00 01 42 01  的nuh_unit_type的值为 33， 语义为序列参数集         SPS
    //00 00 00 01 44 01  的nuh_unit_type的值为 34， 语义为图像参数集         PPS
    //00 00 00 01 4E 01  的nuh_unit_type的值为 39， 语义为补充增强信息       SEI
    //00 00 00 01 26 01  的nuh_unit_type的值为 19， 语义为可能有RADL图像的IDR图像的SS编码数据   IDR
    //00 00 00 01 02 01  的nuh_unit_type的值为1， 语义为被参考的后置图像，且非TSA、非STSA的SS编码数据

    //常用Nalu Type的定义
    //NAL_UNIT_CODED_SLICE_TRAIL_N= 0,// 0
    //NAL_UNIT_CODED_SLICE_TRAIL_R,   // 1
    //NAL_UNIT_CODED_SLICE_TSA_N,     // 2
    //NAL_UNIT_CODED_SLICE_TLA,       // 3   Current name in the spec: TSA_R
    //NAL_UNIT_CODED_SLICE_STSA_N,    // 4
    //NAL_UNIT_CODED_SLICE_STSA_R,    // 5
    //NAL_UNIT_CODED_SLICE_RADL_N,    // 6
    //NAL_UNIT_CODED_SLICE_DLP,       // 7   Current name in the spec: RADL_R
    //NAL_UNIT_CODED_SLICE_RASL_N,    // 8
    //NAL_UNIT_CODED_SLICE_TFD,       // 9   Current name in the spec: RASL_R
    //NAL_UNIT_CODED_SLICE_BLA,       // 16  Current name in the spec: BLA_W_LP
    //NAL_UNIT_CODED_SLICE_BLANT,     // 17  Current name in the spec: BLA_W_DLP
    //NAL_UNIT_CODED_SLICE_BLA_N_LP,  // 18
    //NAL_UNIT_CODED_SLICE_IDR,       // 19  Current name in the spec: IDR_W_DLP
    //NAL_UNIT_CODED_SLICE_IDR_N_LP,  // 20
    //NAL_UNIT_CODED_SLICE_CRA,       // 21

    //以下NAL_BLA_N_LP到NAL_IDR_W_RADL都算I帧
    //NAL_BLA_N_LP:
    //NAL_BLA_W_LP:
    //NAL_BLA_W_RADL:
    //NAL_CRA_NUT:
    //NAL_IDR_N_LP:
    //NAL_IDR_W_RADL

    /*
    ffmpeg 中 hevc.h定义：
    NAL_TRAIL_N = 0,
    NAL_TRAIL_R = 1,
    NAL_TSA_N = 2,
    NAL_TSA_R = 3,
    NAL_STSA_N = 4,
    NAL_STSA_R = 5,
    NAL_RADL_N = 6,
    NAL_RADL_R = 7,
    NAL_RASL_N = 8,
    NAL_RASL_R = 9,
    NAL_BLA_W_LP = 16,
    NAL_BLA_W_RADL = 17,
    NAL_BLA_N_LP = 18,
    NAL_IDR_W_RADL = 19,
    NAL_IDR_N_LP = 20,
    NAL_CRA_NUT = 21,
    NAL_VPS = 32,
    NAL_SPS = 33,
    NAL_PPS = 34,
    NAL_AUD = 35,
    NAL_EOS_NUT = 36,
    NAL_EOB_NUT = 37,
    NAL_FD_NUT = 38,
    NAL_SEI_PREFIX = 39,
    NAL_SEI_SUFFIX = 40,
    */

    //NALU类型为vps, sps, pps, 或者解码顺序为第一个AU的第一个NALU， 起始码前面再加一个0x00

    private static final int NAL_VPS = 32; // 码流中对应字节值 0x40
    private static final int NAL_SPS = 33; // 0x42
    private static final int NAL_PPS = 34; // 0x44

    private void initDecoder0() {
        try {
            //根据需要解码的类型创建解码器
            mCodec0 = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC); //"video/hevc"
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height);

        //CSD Codec-specific Data -- 是指跟特定编码算法相关的一些参数，比如AAC的ADTS、H.264的SPS/PPS等
        //H.265，CSD只需要“csd-0”参数，把VPS、SPS、PPS拼接到一起即可   IDR CRA
        //mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);

        //SurfaceView
        mCodec0.configure(mediaFormat, holder.getSurface(), null, 0); //直接解码送surface显示

        //开始解码
        mCodec0.start();
        isFirst0 = false;
    }


    int mCount0 = 0;

    public void onFrame(byte[] buf, int offset, int length) {
        //-1表示一直等待；0表示不等待；其他大于0的参数表示等待毫秒数
        //Log.e(TAG,"        onFrame start       ");

        if (isExit) {
            return;
        }
        if (mCodec0 != null) {
            int inputBufferIndex = mCodec0.dequeueInputBuffer(-1);
            if (!isExit && inputBufferIndex >= 0) {
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
            while (outputBufferIndex >= 0 && !isExit) {
                //logger.d("outputBufferIndex = " + outputBufferIndex);
                mCodec0.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = mCodec0.dequeueOutputBuffer(bufferInfo, 0);
            }
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

    private class DecoderThread extends Thread {


        private File file = null;
        private boolean findFlag = false;


        public DecoderThread( ) {
            file = new File(path0);
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
            while (!isFinish0) {
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

                                if (findFlag && !isFinish0) {
                                    nalu.type = (frame[i + 3] & 0x7E) >> 1;
                                    if (index == 1) {
                                        i = i - 1;
                                    }
                                    if (index0 == 1) {
                                        pos = pos - 1;
                                    }

                                    Log.i(TAG, "run: ---------findflag:"+findFlag+"------isfinish:"+isFinish0);
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
                        isFinish0 = true;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG, "  error =  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            isFinish0 = false;
            Log.i(TAG, "         frameNum     " + frameNum);
        }
    }


    public void stopCodec0() {
        //  放在 mCodec0.stop(); 前面  不然会报错
        if(mDecoderThread!=null){
            mDecoderThread=null;
        }
        if(mCodec0!=null){
            try {
                mCodec0.stop();

                isExit=true;
                isFirst0 = true;
                isFinish0 = true;
                mCodec0.release();
                mCodec0 = null;

            } catch (Exception e) {
                e.printStackTrace();
                mCodec0 = null;
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopCodec0();

        mCameraRecordDecoder.onDestroy();

    }

}
