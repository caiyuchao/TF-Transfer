package com.example.transfer;

import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.transfer.Service.SendFileService;
import com.example.transfer.bean.Task;
import com.example.transfer.bean.TransferUser;
import com.example.transfer.fragment.MainFragment;
import com.example.transfer.fragment.SendFileFragment;
import com.example.transfer.ui.MySlidingMenu;
import com.example.transfer.ui.ReceiveFileActivity;
import com.example.transfer.ui.VoiceActivity;
import com.example.transfer.ui.zxing.MipcaActivityCapture;
import com.example.transfer.util.WifiAdmin;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.tf.transfer.R;

public class MainActivity extends Activity {

	public MySlidingMenu menu;
	
	static {
        System.loadLibrary("sinvoice");
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.activity_main);
		menu = new MySlidingMenu(this, SlidingMenu.SLIDING_CONTENT);
		MainFragment fragment = new MainFragment();
		addFragment(fragment, true);
		Intent intent = new Intent(this, SendFileService.class);
		startService(intent);
		SendFileService.setActivity(this);
		//设置用户名
		SharedPreferences sharedPreferences = getSharedPreferences("info", Context.MODE_PRIVATE);
		String username = sharedPreferences.getString("username", "TF");
		TransferUser.getInstance().setUsername(username);
//		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//		telephonyManager.getDeviceId();
	}

	//打开或关闭侧滑菜单
	public void openSlidingMenu(){
		menu.toggle();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
//		System.out.println(intent);
//		System.out.println(intent.toString());
//		int flag = intent.getIntExtra("flag", 0);
//		System.out.println(flag);
		Bundle bundle = intent.getExtras();
		int flag = bundle.getInt("flag", 0);
		if(flag == 1){
			//切换到发送文件fragmentfragment
			switchFragment(new SendFileFragment(), true, intent.getExtras());
		}else if(flag == 2){
			showQRCodeDialog(bundle.getString("objectId"), bundle.getString("qrCode_path"));
		}
	}
	
	//添加fragment
	public void addFragment(Fragment fragment, boolean flag){
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(R.id.fragment_main, fragment);
		if(flag)
			transaction.addToBackStack(null);
		transaction.commit();
	}
	
	//切换fragment
	public void switchFragment(Fragment fragment, boolean flag, Bundle bundle){
		fragment.setArguments(bundle);
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_main, fragment);
		if(flag)
			transaction.addToBackStack(null);
		transaction.commit();
	}
	
	public void listener(int id, Map<String, Object> map) {
		if(id == Task.SEND_FILE){
			SendFileService.addTask(new Task(id, map));
		}
	}
	
	//刷新界面
	public void refresh(int id, Object...objects){
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//当fragment栈中只剩一个时退出activity
		if(keyCode == 4 && getFragmentManager().getBackStackEntryCount() == 1){
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case 1:
            if (resultCode == RESULT_OK) {
            	Bundle bundle = data.getExtras();
            	String json = bundle.getString("result");
            	char c = json.charAt(0);
            	if(c != '{'){
            		//根据objectId 从服务器下载文件
            		Intent intent = new Intent(MainActivity.this, ReceiveFileActivity.class);
					intent.putExtra("task_id", json);
					startActivity(intent);
            		return;
            	}
            	try {
					JSONObject object = new JSONObject(json);
					long id = object.getLong("id");
					String code = object.getString("code");
					String username = object.getString("username");
					String ip = object.getString("ip");
					System.out.println(id+"  "+code+"  "+username+"  "+ip);
					Intent intent = new Intent(MainActivity.this, ReceiveFileActivity.class);
					intent.putExtra("id", id);
					intent.putExtra("wifi_name", username);
					intent.putExtra("code", code);
					intent.putExtra("ip", ip);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
            	
            }
            break;
        case 2:
        	if(resultCode == RESULT_OK){
        		String str = data.getStringExtra("result");
        		System.out.println("识别到的内容："+str);
        		String[] strs = str.split("@");
        		if(strs.length == 3){
	        		String code = strs[0];
					String username = strs[1];
					long id = Long.parseLong(strs[2]);
					String ip = "";
					System.out.println(id+"  "+code+"  "+username+"  "+ip);
					Intent intent = new Intent(MainActivity.this, ReceiveFileActivity.class);
					intent.putExtra("id", id);
					intent.putExtra("wifi_name", username);
					intent.putExtra("code", code);
					intent.putExtra("ip", ip);
					startActivity(intent);
        		}else{
        			Toast.makeText(getApplicationContext(), "周围杂音太重识别异常，请重试！", Toast.LENGTH_SHORT).show();
        		}
        	}
        	break;
        }
    }
	
	//打开提示连接电脑的提示框
	public void showComputerDialog(){
		View view = View.inflate(getApplicationContext(), R.layout.dialog_computer, null);
		TextView textView = (TextView) view.findViewById(R.id.computer_name);
		textView.setText(TransferUser.getInstance().getUsername());
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setView(view);
        ad.create();
        ad.show();
	}
	
	//打开接收文件的提示框
	public void showReciveDialog(){
		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		View view = View.inflate(getApplicationContext(), R.layout.dialog_recive, null);
		Button button_scan = (Button) view.findViewById(R.id.revice_scan);
		button_scan.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this, MipcaActivityCapture.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, 1);
				dialog.dismiss();
			}
		});
		Button button_voice = (Button) view.findViewById(R.id.revice_voice);
		button_voice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this, VoiceActivity.class);
				startActivityForResult(intent, 2);
				dialog.dismiss();
			}
		});
        dialog.setView(view);
        dialog.show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		WifiAdmin admin = new WifiAdmin(getApplicationContext());
		admin.closeWifiAp();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		System.exit(0);
	}
	
	public void showQRCodeDialog(String objectId, String qrCode){
		View view = View.inflate(getApplicationContext(), R.layout.dialog_qrcode, null);
		TextView textView = (TextView) view.findViewById(R.id.qrcode_text);
		textView.setText(objectId);
		ImageView imageView = (ImageView) view.findViewById(R.id.qrcode_image);
		imageView.setImageBitmap(BitmapFactory.decodeFile(qrCode));
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        AlertDialog dialog = ad.create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int dpi = displayMetrics.densityDpi;
        params.width = 230*dpi/160;
        params.height = LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
	}
}