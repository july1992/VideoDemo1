package com.vily.videodemo1.Camer1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.vily.videodemo1.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoshuang on 17/2/16.
 * 触摸对焦SurfaceView
 */

public class FocusSurfaceView extends SurfaceView {


    public FocusSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FocusSurfaceView(Context context) {
        super(context);
    }


    private int mRatioWidth=0;
    private int mRatioHeight=0;
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        if(mRatioWidth==0 && mRatioHeight==0){
            setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
        }else{
            setMeasuredDimension(mRatioWidth,mRatioHeight);
        }

    }

//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//
//        Log.i("---", "onLayout: -----------正在切换大小屏幕:"+changed+"--"+left+"---"+top+"---"+right+"---"+bottom);
//
//
//    }
}
