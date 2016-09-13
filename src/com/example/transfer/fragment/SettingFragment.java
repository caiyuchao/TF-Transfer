package com.example.transfer.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.transfer.MainActivity;
import com.tf.transfer.R;
import com.example.transfer.bean.TransferUser;

public class SettingFragment extends Fragment implements OnClickListener{

	private Activity activity;
	private Button button_back, button_confirm;
	private EditText editText;
	private SharedPreferences sharedPreferences;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity) activity;
		sharedPreferences = activity.getSharedPreferences("info", Context.MODE_PRIVATE);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setting, null);
		editText = (EditText) view.findViewById(R.id.setting_name);
		editText.setText(sharedPreferences.getString("username", ""));
		button_back = (Button) view.findViewById(R.id.setting_back);
		button_confirm =  (Button) view.findViewById(R.id.setting_confirm);
		button_back.setOnClickListener(this);
		button_confirm.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.setting_back:
			activity.getFragmentManager().popBackStack();
			break;
		case R.id.setting_confirm:
			//修改用户名
			Editor edit = sharedPreferences.edit();
			edit.putString("username", editText.getText().toString());
			boolean commit = edit.commit();
			if(commit){
				TransferUser.getInstance().setUsername(editText.getText().toString());
				Toast.makeText(activity, "修改成功", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(activity, "修改失败", Toast.LENGTH_SHORT).show();
			}
			//返回
			activity.getFragmentManager().popBackStack();
			break;
		}
	}
	
}
