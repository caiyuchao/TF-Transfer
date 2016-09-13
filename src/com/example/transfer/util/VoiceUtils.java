package com.example.transfer.util;

import java.io.UnsupportedEncodingException;

import com.libra.sinvoice.Common;
import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.SinVoicePlayer;
import com.libra.sinvoice.SinVoiceRecognition;

import android.app.Activity;
import android.content.Context;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

public class VoiceUtils implements SinVoiceRecognition.Listener, SinVoicePlayer.Listener{

	private Activity activity;
	
	private final static int MSG_SET_RECG_TEXT = 1;
    private final static int MSG_RECG_START = 2;
    private final static int MSG_RECG_END = 3;
    private final static int MSG_PLAY_TEXT = 4;

    private final static int[] TOKENS = { 32, 32, 32, 32, 32, 32 };
    private final static int TOKEN_LEN = TOKENS.length;

    private SinVoicePlayer mSinVoicePlayer;
    private SinVoiceRecognition mRecognition;
    private PowerManager.WakeLock mWakeLock;
    private static char mRecgs[] = new char[100];
    private static int mRecgCount;
    
    private VoiceListener voiceListener;
    
    //回调接口
    public interface VoiceListener{
    	public void reciveVoice(String str);
    }
    
	public VoiceUtils(Activity activity){
		this.activity = activity;

        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "VoiceUtils");
        mWakeLock.acquire();

        mSinVoicePlayer = new SinVoicePlayer();
        mSinVoicePlayer.init(activity);
        mSinVoicePlayer.setListener(this);

        mRecognition = new SinVoiceRecognition();
        mRecognition.init(activity);
        mRecognition.setListener(this);
        
	}

	public void reviceVoice(VoiceListener voiceListener){
		this.voiceListener = voiceListener;
		mRecognition.start(TOKEN_LEN, false);
	}
	
	public void stopRecive(){
		mRecognition.stop();
		mRecognition.uninit();
		mRecognition = null;
	}
	
	//发送声波
	public void sendVoice(String str){
		try {
            byte[] strs = str.getBytes("UTF8");
            if ( null != strs ) {
                int len = strs.length;
                int []tokens = new int[len];
                int maxEncoderIndex = mSinVoicePlayer.getMaxEncoderIndex();
                String encoderText = str;
                for ( int i = 0; i < len; ++i ) {
                    if ( maxEncoderIndex < 255 ) {
                        tokens[i] = Common.DEFAULT_CODE_BOOK.indexOf(encoderText.charAt(i));
                    } else {
                        tokens[i] = strs[i];
                    }
                }
                mSinVoicePlayer.play(tokens, len, false, 2000);
            } else {
                mSinVoicePlayer.play(TOKENS, TOKEN_LEN, false, 2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void stopSend(){
		mSinVoicePlayer.stop();
		mSinVoicePlayer.uninit();
	}
	
	@Override
    public void onSinVoiceRecognitionStart() {
		System.out.println("------------");
		handler.sendEmptyMessage(MSG_RECG_START);
    }

    @Override
    public void onSinVoiceRecognition(char ch) {
    	System.out.println("------------");
    	handler.sendMessage(handler.obtainMessage(MSG_SET_RECG_TEXT, ch, 0));
    }

    @Override
    public void onSinVoiceRecognitionEnd(int result) {
    	System.out.println("------------");
    	handler.sendMessage(handler.obtainMessage(MSG_RECG_END, result, 0));
    }
	
	@Override
	public void onSinVoicePlayStart() {
	}

	@Override
	public void onSinVoicePlayEnd() {
	}

	@Override
	public void onSinToken(int[] tokens) {
	}

	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case MSG_SET_RECG_TEXT:
                char ch = (char) msg.arg1;
                mRecgs[mRecgCount++] = ch;
                break;

            case MSG_RECG_START:
                mRecgCount = 0;
                break;

            case MSG_RECG_END:
                LogHelper.d("Voice", "recognition end gIsError:" + msg.arg1);
                if ( mRecgCount > 0 ) {
                    byte[] strs = new byte[mRecgCount];
                    for ( int i = 0; i < mRecgCount; ++i ) {
                        strs[i] = (byte)mRecgs[i];
                    }
                    try {
                        String strReg = new String(strs, "UTF8");
                        if (msg.arg1 >= 0) {
                            voiceListener.reciveVoice(strReg);
                        } else {
                            voiceListener.reciveVoice(strReg);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case MSG_PLAY_TEXT:
                break;
            }
		}
	};
	
	public AudioRecord getAudioRecord(){
    	return mRecognition.getAudioRecord();
    } 
	
}
