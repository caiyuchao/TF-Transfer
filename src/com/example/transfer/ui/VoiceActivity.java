package com.example.transfer.ui;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.tf.transfer.R;
import com.example.transfer.ui.voiceline.VoiceLineView;
import com.example.transfer.util.VoiceUtils;
import com.example.transfer.util.VoiceUtils.VoiceListener;

public class VoiceActivity extends Activity implements Runnable{
	
	static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
	
    private VoiceLineView voiceLineView;
    private VoiceUtils voiceUtils;
    private boolean flag = true;
    private int count = 0;
    
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            voiceLineView.setVolume(new Random().nextInt(20)+30);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        getActionBar().hide();
        Button button_back = (Button) findViewById(R.id.voice_back);
        button_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
				flag = false;
			}
		});
        voiceLineView = (VoiceLineView) findViewById(R.id.voice_line);
        
        //识别声波
        voiceUtils = new VoiceUtils(this);
        voiceUtils.reviceVoice(new VoiceListener() {
			
			@Override
			public void reciveVoice(String str) {
				System.out.println(str);
//				Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
				Intent intent = new Intent();
				intent.putExtra("result", str);
				setResult(RESULT_OK, intent);
				flag = false;
				finish();
			}
		});

        Thread thread = new Thread(this);
        thread.start();
    }

	@Override
	public void run() {
		while(flag){
            handler.sendEmptyMessage(0);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            	
            }
		}
		//耗时操作
		voiceUtils.stopRecive();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		flag = false;
	}

}
