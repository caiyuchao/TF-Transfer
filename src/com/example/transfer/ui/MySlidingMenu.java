package com.example.transfer.ui;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.transfer.MainActivity;
import com.tf.transfer.R;
import com.example.transfer.ui.zxing.MipcaActivityCapture;
import com.example.transfer.util.WifiAdmin;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MySlidingMenu extends SlidingMenu implements OnClickListener{

//	private static MySlidingMenu mySlidingMenu;
	private Button button_send, button_receive, button_computer;
	private MainActivity activity;
	
	public MySlidingMenu(MainActivity activity, int slideStyle) {
		super(activity, slideStyle);
		this.activity = activity;
		//设置menu的属性
		setMode(SlidingMenu.RIGHT);//menu从左边滑出
		//设置menu占据的空间大小
		setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		//设置打开menu的触控点的阀值
		setTouchmodeMarginThreshold(10);
		setFadeDegree(0.35f);//设置mune出来的效果
		//设置背景颜色
		setBackgroundColor(getResources().getColor(R.color.theme_color));
		setBehindWidth(200);
		setBehindScrollScale(0.5f);
		//将布局文件放到menu
		setMenu(R.layout.slide_menu);
		button_send = (Button) findViewById(R.id.send_file);
		button_receive = (Button) findViewById(R.id.receive_file);
		button_computer = (Button) findViewById(R.id.computer);
		button_send.setOnClickListener(this);
		button_receive.setOnClickListener(this);
		button_computer.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.send_file:
			System.out.println("send");
			toggle();
//			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//			intent.setType("*/*");
//			intent.addCategory(Intent.CATEGORY_OPENABLE);
//			activity.startActivityForResult(Intent.createChooser(intent, "选择文件"), 1);
			activity.startActivity(new Intent(activity, SDFileExplorerActivity.class));
			break;
		case R.id.receive_file:
			System.out.println("receive");
			toggle();
			activity.showReciveDialog();
//			Intent intent = new Intent(activity, MipcaActivityCapture.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			activity.startActivityForResult(intent, 1);
//			Intent intent = new Intent(activity, QRCodeScanCameraActivity.class);
//          activity.startReceive();
			break;
		case R.id.computer:
			activity.showComputerDialog();
			WifiAdmin admin = new WifiAdmin(activity);
			admin.setHotspot(true);
			toggle();
			break;
		}
	}
	

}
