package com.vily.videodemo1.Camer1.utils;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 *  * description : 
 *  * Author : Vily
 *  * Date : 2018/12/26
 *  
 **/
public class AudioEncoder {

    private  MediaCodec mMediaCodec;
    private  MediaCodec.BufferInfo mBufferInfo;

    public AudioEncoder() {
        try {
            //尝试是否能够进行初始化成功
            mMediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // AAC 硬编码器
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");//音频编码
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2); //声道数（这里是数字）
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100); //采样率
        format.setInteger(MediaFormat.KEY_BIT_RATE, 2048); //码率
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        //记录编码完成的buffer的信息
        mBufferInfo = new MediaCodec.BufferInfo();
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);// MediaCodec.CONFIGURE_FLAG_ENCODE 标识为编码器
        mMediaCodec.start();//开始工作
    }

    private static final String TAG = "AudioEncoder";

    public int offEncoder(byte[] data){

        int pos=0;
        int index=mMediaCodec.dequeueInputBuffer(-1);//拿空盒子，index 拿到的盒子序号
        Log.i(TAG, "offEncoder: -----------index"+index);
        if(index>=0){
            final ByteBuffer buffer=mMediaCodec.getInputBuffer(index);
            buffer.clear();
            buffer.put(data);
            mMediaCodec.queueInputBuffer(index,0,data.length,System.nanoTime()/1000,0);//往空盒子里塞要编码的数据
        }


        MediaCodec.BufferInfo mInfo=new MediaCodec.BufferInfo();


        //每次取出的时候，把所有加工好的都循环取出来
        int outIndex;
        do{
            outIndex=mMediaCodec.dequeueOutputBuffer(mInfo,0);//取出已经编码好的数据，outIndex 表示盒子的位置
            Log.i(TAG, "offEncoder: -----------outindex:"+outIndex);
            if(outIndex>=0){
                ByteBuffer buffer=mMediaCodec.getOutputBuffer(outIndex);
                buffer.position(mInfo.offset);
                //AAC编码，需要加数据头，AAC编码数据头固定为7个字节
                byte[] temp=new byte[mInfo.size+7];
                pos=temp.length;
                buffer.get(temp,7,mInfo.size);
                addADTStoPacket(temp,temp.length,2048, 1);//temp是处理后的acc数据，你可以把temp存储成一个文件就是一个acc的音频文件
                mMediaCodec.releaseOutputBuffer(outIndex,false);
            }else if(outIndex ==MediaCodec.INFO_TRY_AGAIN_LATER){
                //TODO something
            }else if(outIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                //TODO something
            }
        }while (outIndex>=0);

        return pos;

    }



//    sudo ./objs/ffmpeg/bin/ffmpeg -re -i ./doc/source.200kbps.768x320.flv \-vcodec copy \-f flv -y rtmp://192.168.93.113/srstest/teststream


    /** 添加头部信息
     * 上面有个方法是addADTStoPacket（）这个方法就是用来给编码之后的音频数据进行头数据的添加，否则不能够进行播放；具体如下
            * packet 数据
     * packetLen 数据长度
     * sampleInHz 采样率
     * chanCfgCounts 通道数
     **/
    private void addADTStoPacket(byte[] packet, int packetLen, int sampleInHz, int chanCfgCounts) {
        int profile = 2; // AAC LC
        int freqIdx = 8; // 16KHz    39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;

        switch (sampleInHz) {
            case 8000: {
                freqIdx = 11;
                break;
            }
            case 16000: {
                freqIdx = 8;
                break;
            }
            default:
                break;
        }
        int chanCfg = chanCfgCounts; // CPE
        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;

    }


    public void close() {
        try {
            mMediaCodec.stop();
            mMediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
