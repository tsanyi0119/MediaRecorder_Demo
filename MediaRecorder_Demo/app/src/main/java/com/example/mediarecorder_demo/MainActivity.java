package com.example.mediarecorder_demo;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1000;
    private MediaRecorder mediaRecorder;
    private String filePath , outPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setffmpeg();
            }
        });

        // 請求錄音權限
        requestPermission();

        // 初始化 MediaRecorder
        mediaRecorder = new MediaRecorder();

        AudioRecorder();

        // 設定錄音來源和輸出格式
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(filePath);

        // 設定錄音按鈕的點擊事件
        Button recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    // 開始錄音
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 如果尚未授權錄音權限，再次請求權限
                    requestPermission();
                }
            }
        });

        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 停止錄音
                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                } catch (IllegalStateException e) {
                    // 處理錯誤
                    e.printStackTrace();
                }
            }
        });

    }

    private void setffmpeg(){
        // 原始 MP3 文件路徑
        String src = filePath;

        // 轉換後 WAV 文件路徑
        String dst = outPath;

        // 轉換
//        FFmpeg.executeAsync(
//                "-y -i " + src + " -c:a pcm_s16le -ar 44100 -ac 2 " + dst, new ExecuteCallback() {
//            @Override
//            public void apply(final long executionId, final int returnCode) {
//                // 根據returnCode進行處理
//                if (returnCode == RETURN_CODE_SUCCESS) {
//                    // FFmpeg執行成功
//                    Log.e("TAG", "FFmpeg執行成功");
//                } else if (returnCode == RETURN_CODE_CANCEL) {
//                    // 使用者取消了執行
//                    Log.e("TAG", "使用者取消了執行");
//                } else {
//                    // 發生了錯誤
//                }
//            }
//        });

        FFmpeg.executeAsync(
                "-y -i " + src + " -c:a libmp3lame -b:a 96k " + dst, new ExecuteCallback() {
                    @Override
                    public void apply(final long executionId, final int returnCode) {
                        if (returnCode == RETURN_CODE_SUCCESS) {
                            Log.e("TAG", "FFmpeg執行成功");
                        } else if (returnCode == RETURN_CODE_CANCEL) {
                            Log.e("TAG", "使用者取消了執行");
                        } else {
                            Log.e("TAG", "FFmpeg執行失敗，錯誤碼：" + returnCode);
                        }
                    }
                });

    }

    private void AudioRecorder() {
        // 獲取外部存儲的目錄
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        // 檢查目錄是否存在，如果不存在，創建它
        if (!externalDir.exists()) {
            externalDir.mkdirs();
        }

        // 設置錄音的文件路徑
        filePath = new File(externalDir, "recorded_audio.mp3").getAbsolutePath();
        outPath = new File(externalDir, "recorded_audio2.mp3").getAbsolutePath();
//        filePath = new File(externalDir, "recorded_audio.wav").getAbsolutePath();

    }

    private void requestPermission() {
        // 請求錄音權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 檢查權限請求的結果
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 如果用戶同意權限，可以進行相應的操作
            }
        }
    }
}
