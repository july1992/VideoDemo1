package com.vily.videodemo1.push.push;

import android.content.Context;

public class WlPushEncodec extends CameraRecordEncoder {

    private CameraRecordRender wlEncodecPushRender;

    public WlPushEncodec(Context context, int textureId) {
        super(context,textureId);
//        wlEncodecPushRender = new CameraRecordRender(context, textureId);
//        setRender(wlEncodecPushRender);
//        setmRenderMode(CameraRecordEncoder.RENDERMODE_CONTINUOUSLY);
    }
}
