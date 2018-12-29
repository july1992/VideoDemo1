package com.vily.videodemo1;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;



/**
 * Created by zhangxd on 2018/9/6.
 */

public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getSimpleName();

    public static final String MP4_PLAY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video.mp4";

    public static  String H264_PLAY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +"/test.h264";
    public static  String H264_GanWu = Environment.getExternalStorageDirectory().getAbsolutePath() +"/ganwu.h264";
    public static  String H265_GanWu = Environment.getExternalStorageDirectory().getAbsolutePath() +"/ganwu.HEVC";


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        Log.i(TAG, "onCreate: -----------"+H264_PLAY_PATH);
//        copyResourceToMemory(R.raw.video, MP4_PLAY_PATH);
        copyResourceToMemory(R.raw.test, H264_PLAY_PATH);
    }

    private void copyResourceToMemory(int srcPath, String destPath) {
        InputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = getResources().openRawResource(srcPath);
            File file = new File(destPath);
            if (file.exists()) {
                return;
            }
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            while ((fileInputStream.read(bytes)) > 0) {
                fileOutputStream.write(bytes);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "copyVideoResourceToMemory FileNotFoundException : " + e);
        } catch (IOException e) {
            Log.e(TAG, "copyVideoResourceToMemory IOException : " + e);
        } finally {
            try {
                if(fileInputStream!=null){
                    fileInputStream.close();
                }
                if(fileOutputStream!=null){
                    fileOutputStream.close();
                }

            } catch (IOException e) {
                Log.e(TAG, "close stream IOException : " + e);
            }

        }


    }

}
