package com.example.transfer.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.transfer.MainActivity;
import com.tf.transfer.R;
import com.example.transfer.bean.Task;
import com.example.transfer.bean.TransferUser;
import com.example.transfer.ui.RippleLayout;
import com.example.transfer.util.SocketThreadFactory;
import com.example.transfer.util.VoiceUtils;

public class SendFileFragment extends Fragment implements OnClickListener{

	private Button button_cancel, button_min, button_qrcode, button_voice;
	private TextView textView_id, textView_title;
	private RelativeLayout layout;
	private MainActivity activity;
	private long task_id;
	private String qrCode_path;
	private boolean flag = false;
	private VoiceUtils voiceUtils;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_send_file, null);
		layout = (RelativeLayout) view.findViewById(R.id.send_file_qrcode);
		button_cancel = (Button) view.findViewById(R.id.send_file_cancel);
		button_min = (Button) view.findViewById(R.id.send_file_min);
		button_qrcode = (Button) view.findViewById(R.id.send_qrcode);
		button_voice = (Button) view.findViewById(R.id.send_voice);
		textView_id = (TextView) view.findViewById(R.id.send_file_id);
		textView_title = (TextView) view.findViewById(R.id.send_file_title);
		button_cancel.setOnClickListener(this);
		button_min.setOnClickListener(this);
		button_qrcode.setOnClickListener(this);
		button_voice.setOnClickListener(this);
		Bundle bundle = getArguments();
		task_id = bundle.getLong("id");
		qrCode_path = bundle.getString("qrCode_path");
		//显示ID
		textView_id.setText("ID:"+task_id);
		setQrcode();
		//根据flag判断是否存在任务
		boolean flag = existTask(task_id);
		if(!flag){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", task_id);
			activity.listener(Task.SEND_FILE, map);
		}
		voiceUtils = new VoiceUtils(activity);
		return view;
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.send_file_cancel:
			flag = false;
			voiceUtils.stopSend();
			SocketThreadFactory.deleteTask(task_id);
			activity.getFragmentManager().popBackStack();
			break;
		case R.id.send_file_min:
			flag = false;
			voiceUtils.stopSend();
			activity.getFragmentManager().popBackStack();
			break;
		case R.id.send_qrcode:
			voiceUtils.stopSend();
			setQrcode();
			break;
		case R.id.send_voice:
			setVoice();
			break;
		}
	}
	
	public boolean existTask(long id){
		ArrayList<Map<String, Object>> list = SocketThreadFactory.getCurrentTaskList();
		for(int i = 0;i<list.size();i++){
			Map<String, Object> map = list.get(i);
			if((long)map.get("id") == id){
				return true;
			}
		}
		return false;
	}
	
	//设置二维码
	public void setQrcode(){
		flag = false;
		button_qrcode.setBackgroundResource(R.drawable.corners_bg_button_left);
		button_voice.setBackgroundResource(R.drawable.corners_bg_button_right_no);
		button_qrcode.setTextColor(0xffffffff);
		button_voice.setTextColor(getResources().getColor(R.color.theme_color));
		textView_title.setText("扫描该二维码接收文件");
		ImageView imageView = new ImageView(activity);
		imageView.setImageBitmap(BitmapFactory.decodeFile(qrCode_path));
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp1.addRule(RelativeLayout.CENTER_IN_PARENT);
		layout.removeAllViews();
		layout.addView(imageView, lp1);
	}
	
	//设置声波
	public void setVoice(){
		flag = true;
		button_qrcode.setBackgroundResource(R.drawable.corners_bg_button_left_no);
		button_voice.setBackgroundResource(R.drawable.corners_bg_button_right);
		button_qrcode.setTextColor(getResources().getColor(R.color.theme_color));
		button_voice.setTextColor(0xffffffff);
		textView_title.setText("调大音量靠近接收文件的手机");
		XmlPullParser parser = activity.getResources().getXml(R.layout.ripple_layout);  
        AttributeSet attributes = Xml.asAttributeSet(parser);  
		RippleLayout rippleLayout = new RippleLayout(activity, attributes);
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp1.addRule(RelativeLayout.CENTER_IN_PARENT);
		layout.removeAllViews();
		layout.addView(rippleLayout, lp1);
		rippleLayout.startRippleAnimation();
		
		TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(activity.TELEPHONY_SERVICE);
		String code = telephonyManager.getDeviceId();
		
		//开始发送声波
		StringBuffer sb = new StringBuffer();
		sb.append(code);
		sb.append("@");
		sb.append(TransferUser.getInstance().getUsername());
		sb.append("@");
		sb.append(task_id);
		final String str = sb.toString();
		new Thread(){
			@Override
			public void run() {
				while(flag){
					voiceUtils.sendVoice(str);
					try {
						sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
}
