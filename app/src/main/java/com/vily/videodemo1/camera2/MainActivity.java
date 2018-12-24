package com.vily.videodemo1.camera2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.vily.videodemo1.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AutoFitTextureView mTextureView;

    private Camera2Utils mCamera2Utils;
    private File mFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mTextureView = findViewById(R.id.texture);


        mCamera2Utils = new Camera2Utils(MainActivity.this,mTextureView);
        mFile = new File(Environment.getExternalStorageDirectory(), "ssss.mp4");
        mCamera2Utils.prepare(mTextureView);
        initListener();
    }

    private void initListener() {
        mCamera2Utils.setOnCameraStateListener(new Camera2Utils.OnCameraStateListener() {
            @Override
            public void onCameraState(String state) {

                switch (state){

                    case "onDisconnected" :  // 摄像头断开
//                        mCamera2Utils.closeCamera();
//                        finish();
                        break;
                    case "onError" :    // 摄像头错误
//                        finish();
//                        mCamera2Utils.closeCamera();
                        break;
                    default :
                        break;
                }
            }

            @Override
            public void onDataBack(Image image) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                Log.i(TAG, "run: ------bytes视频预览帧：" + bytes.length);

//                image.close();
                buffer.get(bytes);
                FileOutputStream output=null;
                try {
                    output = new FileOutputStream(mFile);
                    output.write(bytes);

                    Log.i(TAG, "onDataBack: ---------file:"+mFile.getAbsolutePath());
                } catch (Exception e)
                {
                    e.printStackTrace();
                } finally {
                    image.close();
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume: ----------");
        if (mTextureView.isAvailable()) {

            Log.i(TAG, "onResume: ------------2");
            mCamera2Utils.restartPreview();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }


    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {


            mCamera2Utils.openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            Log.i(TAG, "onSurfaceTextureSizeChanged: ---------------");
            mCamera2Utils.configureTransform(mTextureView);

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {

        }

    };

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause: ----------onpause");
        mCamera2Utils.stopPreview();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy: -------");
        mCamera2Utils.closeCamera();
    }
}
