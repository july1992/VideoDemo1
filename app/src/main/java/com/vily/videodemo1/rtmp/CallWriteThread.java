package com.vily.videodemo1.rtmp;

import android.util.Log;

/**
 *  * description : 
 *  * Author : Vily
 *  * Date : 2019/1/23
 *  
 **/

public class CallWriteThread extends Thread {
    private final String tag = "CallWriteThread";
    private CallDataBuffer callBuffer;
    private boolean flag = false;

    private int sendAudioSize;
    private int sendVideoSize;
    private int bitLen = 16;
    private int size = 4;
    private int length;
    byte[] sendData;
    private int dataIndex;

    public CallWriteThread() {
        this.length = this.bitLen * this.size;
        this.sendData = new byte[this.length];
        this.dataIndex = 0;
        this.callBuffer = new CallDataBuffer();

        this.flag = true;
        this.sendAudioSize = 0;
        this.sendVideoSize = 0;
    }

    public void addData(CallMessage msg) {
        this.callBuffer.addQueue(msg);
    }



    public int getSendAudioSize() {
        return this.sendAudioSize;
    }

    public int getSendVideoSize() {
        return this.sendVideoSize;
    }

    private void search(CallMessage msg) {
        if (msg != null) {
            byte[] data;
            if (msg.getType() == CallType.VOICE) {
                data = msg.getData();


                this.sendAudioData(msg.getUserId(), data);
            } else if (msg.getType() == CallType.VIDEO) {
                data = msg.getData();
                this.sendVideoSize += data.length;
                this.sendVideoData(msg.getUserId(), data);
            }

        }
    }

    public void sendAudioData(int userId, byte[] bytes) {
        System.arraycopy(bytes, 0, this.sendData, this.dataIndex * this.bitLen, bytes.length);

        ++this.dataIndex;
        if (this.dataIndex >= this.size) {
            this.dataIndex = 0;

        }

    }

    public void sendVideoData(int userId, byte[] bytes) {


    }

    public void run() {
        while(true) {
            try {
                if (this.flag) {
                    if (!this.callBuffer.isEmpty()) {
                        this.search(this.callBuffer.deQueue());
                    }

                    sleep(30L);
                    continue;
                }
            } catch (Exception var2) {
                var2.printStackTrace();
            }

            return;
        }
    }

    public void release() {
        this.flag = false;

        if (this.callBuffer != null) {
            this.callBuffer.clear();
            this.callBuffer = null;
        }

    }
}