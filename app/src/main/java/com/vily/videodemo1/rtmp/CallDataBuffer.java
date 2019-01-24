package com.vily.videodemo1.rtmp;

import java.util.concurrent.ArrayBlockingQueue;

/**
 *  * description : 
 *  * Author : Vily
 *  * Date : 2019/1/23
 *  
 **/

public class CallDataBuffer extends Thread {
    private ArrayBlockingQueue<CallMessage> list = new ArrayBlockingQueue(5000);

    public CallDataBuffer() {
    }

    public void clear() {
        this.list.clear();
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public void addQueue(CallMessage message) {
        this.list.add(message);
    }

    public CallMessage deQueue() {
        return !this.list.isEmpty() ? (CallMessage)this.list.poll() : null;
    }

    public int size() {
        return this.list.size();
    }
}