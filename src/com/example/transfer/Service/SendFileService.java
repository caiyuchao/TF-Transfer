package com.example.transfer.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.example.transfer.MainActivity;
import com.example.transfer.bean.Task;
import com.example.transfer.util.SocketThreadFactory;
import com.example.transfer.util.SqliteAdapter;
import com.example.transfer.util.WifiAdmin;

public class SendFileService extends Service implements Runnable{

	private static Queue<Task> tasks = new LinkedList<Task>();
	private static MainActivity current_activity;
	private boolean isRunning = true;
	
	@Override
	public void onCreate() {
		super.onCreate();
		new Thread(this).start();
	}
	
	/**
	 * 添加任务
	 * @param task
	 */
	public static void addTask(Task task){
		tasks.add(task);
	}
	
	public static void setActivity(Activity activity){
		current_activity = (MainActivity) activity;
	}
	
	@Override
	public void run() {
		Task task = null;
		while(isRunning){
			if(tasks != null && tasks.size() > 0){
				task = tasks.poll();
				doTask(task);
			}
		}
	}
	
	private void doTask(Task task) {
		int taskId = task.getTaskId();
		Message msg = handler.obtainMessage();
		msg.what = taskId;
		switch (taskId) {
			case Task.SEND_FILE:
				sendFile(task.getMap());
				break;
		}
		handler.sendMessage(msg);
	}
	
	/**
	 * 与activity通信的handler
	 */
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int id = msg.what;
			switch (id) {
//				case Task.SEND_FILE://发送文件
//					activity.refresh(Task.SEND_FILE, (ArrayList<Porter>)msg.obj);
//					break;
				case 0:
					Toast.makeText(current_activity, msg.obj+"", Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};
	
	//发送文件
	public void sendFile(Map<String, Object> map){
		//开启Wifi
		WifiAdmin wifiAdmin = new WifiAdmin(getApplicationContext());
		boolean is = wifiAdmin.setHotspot(true);
//		Message message = handler.obtainMessage(0);
//		message.obj = wifiAdmin.getIp();
//		handler.sendMessage(message);
		System.out.println("WIFI热点是否开启成功："+is);
		if(is){
			try {
				//获得id
				long id = (long) map.get("id");
				//得到文件列表
				SqliteAdapter adapter = new SqliteAdapter(getApplicationContext());
				ArrayList<String> list = adapter.getTaskDetail(id);
				//将文件列表添加到map中
				map.put("files", list);
				SocketThreadFactory.addTask(map);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
