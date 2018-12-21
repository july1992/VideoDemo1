package com.vily.videodemo1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.vily.videodemo1.Camer1.RecordedActivity;
import com.vily.videodemo1.camera2.MainActivity;
import com.vily.videodemo1.media.MediaActivity;

/**
 *  * description : 
 *  * Author : Vily
 *  * Date : 2018/12/21
 *  
 **/
public class HomeActivtiy extends AppCompatActivity implements View.OnClickListener {

    private Button mBtn_media_recorder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        Button btn_camera1 = findViewById(R.id.btn_camera1);
        Button btn_camera2 = findViewById(R.id.btn_camera2);
        mBtn_media_recorder = findViewById(R.id.btn_media_recorder);


        btn_camera1.setOnClickListener(this);
        btn_camera2.setOnClickListener(this);
        mBtn_media_recorder.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.btn_camera1 :
                if(initPermission()){
                    Intent intent=new Intent(HomeActivtiy.this,RecordedActivity.class);

                    startActivity(intent);
                }
                break;
            case R.id.btn_camera2 :
                if(initPermission()){
                    Intent intent=new Intent(HomeActivtiy.this,MainActivity.class);

                    startActivity(intent);
                }

                break;
            case R.id.btn_media_recorder:

                if(initPermission()){
                    Intent intent=new Intent(HomeActivtiy.this,MediaActivity.class);

                    startActivity(intent);
                }

                break;
            default :
                break;
        }
    }

    private static String[] PERMISSION_AUDIO = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    protected boolean initPermission() {
        if (ContextCompat.checkSelfPermission(HomeActivtiy.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //CAMERA  WRITE_EXTERNAL_STORAGE  READ_EXTERNAL_STORAGE 权限
            ActivityCompat.requestPermissions(  HomeActivtiy.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        } else if (ContextCompat.checkSelfPermission(HomeActivtiy.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //CAMERA  WRITE_EXTERNAL_STORAGE  READ_EXTERNAL_STORAGE 权限
            ActivityCompat.requestPermissions((Activity) HomeActivtiy.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        } else if (ContextCompat.checkSelfPermission(HomeActivtiy.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //CAMERA  WRITE_EXTERNAL_STORAGE  READ_EXTERNAL_STORAGE 权限
            ActivityCompat.requestPermissions((Activity) HomeActivtiy.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        } else {
            return true;
        }
        return false;

    }
}
