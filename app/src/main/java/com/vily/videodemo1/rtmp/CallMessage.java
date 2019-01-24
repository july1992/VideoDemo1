package com.vily.videodemo1.rtmp;

/**
 *  * description : 
 *  * Author : Vily
 *  * Date : 2019/1/23
 *  
 **/
public class CallMessage {
    private int userId;
    private byte[] data;
    private CallType type;

    public CallMessage() {
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public CallType getType() {
        return this.type;
    }

    public void setType(CallType type) {
        this.type = type;
    }
}
