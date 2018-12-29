package com.vily.videodemo1.playH264;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.View;

import com.vily.videodemo1.Camer1.FocusSurfaceView;
import com.vily.videodemo1.R;
import com.vily.videodemo1.manage.DecoderManager;
import com.vily.videodemo1.manage.DecoderManager2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  * description : 
 *  * Author : Vily
 *  * Date : 2018/12/29
 *  
 **/
public class PlayH264Activtiy extends AppCompatActivity{

    private FocusSurfaceView mSv_surface;
    private static int VIDEO_With=640;
    private static int VIDEO_Height=480;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_play_h264);

        init();
        initData();
        initListener();
    }

    private void init() {
        mSv_surface = findViewById(R.id.sv_surface);
    }
    private void initData() {

    }

    private void initListener() {
        mSv_surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

                //  解码
                DecoderManager.getInstance().startH264Decode(VIDEO_With,VIDEO_Height,surfaceHolder);

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });

    }


}
