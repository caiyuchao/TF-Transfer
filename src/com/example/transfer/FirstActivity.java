package com.example.transfer;

import com.avos.avoscloud.AVOSCloud;
import com.tf.transfer.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FirstActivity extends Activity {

	private Button button;
	private EditText editText; 
	private SharedPreferences sharedPreferences;
	private int currentVersion;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AVOSCloud.initialize(this, "k3zgEvUo5V4oH9JY3olAEtn3-gzGzoHsz", "o4X1sgwy4TOjOD7IEwxQBNmn");
		setContentView(R.layout.activity_first);
		PackageInfo info = null;
		try {
			info = getPackageManager().getPackageInfo("com.tf.transfer", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		currentVersion = info.versionCode;
		sharedPreferences = getSharedPreferences("info", Context.MODE_APPEND);
		int lastVersion = sharedPreferences.getInt("version", 0);
		if (currentVersion > lastVersion) {
			
		}else{
			sharedPreferences.edit().putInt("version", currentVersion).commit();
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
		button = (Button) findViewById(R.id.first_next);
		editText = (EditText) findViewById(R.id.first_name);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//设置用户名
				SharedPreferences sharedPreferences = getSharedPreferences("info", Context.MODE_PRIVATE);
				Editor edit = sharedPreferences.edit();
				edit.putString("username", editText.getText().toString());
				boolean commit = edit.commit();
				if(commit){
					edit.putInt("version", currentVersion);
					edit.commit();
					Intent intent = new Intent(FirstActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}
				else{
					Toast.makeText(getApplicationContext(), "用户名不合法", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

}