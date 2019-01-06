package com.vily.videodemo1.push.push;

import android.content.Context;

public class WlPushEncodec extends WlBasePushEncoder{

    private WlEncodecPushRender wlEncodecPushRender;

    public WlPushEncodec(Context context, int textureId) {
        super(context);
        wlEncodecPushRender = new WlEncodecPushRender(context, textureId);
        setRender(wlEncodecPushRender);
        setmRenderMode(WlBasePushEncoder.RENDERMODE_CONTINUOUSLY);
    }
}
