package com.example.transfer.Service;

import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.example.transfer.bean.NoFinishTask;
import com.example.transfer.ui.ReceiveFileActivity;
import com.example.transfer.util.MyClientThread;
import com.example.transfer.util.SqliteAdapter;
import com.example.transfer.util.MyClientThread.FileListener;
import com.example.transfer.util.WifiAdmin;
import com.example.transfer.util.WifiAdmin.WifiThreadListener;

public class ReceiveFileService extends IntentService{

	public static ReceiveFileActivity activity;
	private int count;
	private String[] fileNames;
	private long[] fileLength;
	private int fileNumber;
	private long current_progress = 0;
	private long total_progress = 0;
	private long begin_time = 0;
	private String code;
	private long id;
	
	public ReceiveFileService() {
		super("com.example.transfer.Service.ReceiveFileService");
	}
	
	@Override
	protected void onHandleIntent(Intent arg0) {
		//开启Wifi
		final WifiAdmin wifiAdmin = new WifiAdmin(getApplicationContext());
		id = arg0.getLongExtra("id", 0);
		String wifi_name = arg0.getStringExtra("wifi_name");
		code = arg0.getStringExtra("code");
		String ip = arg0.getStringExtra("ip");
		System.out.println("ip地址："+ip);
		if(ip.equals("")){
			wifiAdmin.connect(wifi_name, new WifiThreadListener() {
				@Override
				public void onEvent(boolean flag) {
					System.out.println("返回了"+flag);
					if(flag){
						final String ip = wifiAdmin.getServerIp();
						System.out.println("服务器ip："+ip);
						//用线程去ping通服务器
						new Thread(){
							public void run() {
								boolean is = true;
								while(is){
									try{          
										InetAddress addr = InetAddress.getByName(ip); 
										//ping通后再发送链接请求
									    if (addr.isReachable(3000)){ 
									    	System.out.println("ping服务器成功");
									    	is = false;
									    	if(id != 0){
												System.out.println("任务id："+id);
												SqliteAdapter adapter = new SqliteAdapter(getApplicationContext());
												System.out.println("1      "+code);
												NoFinishTask noFinishTask = adapter.getNoFinishTask(code, id);
												new MyClientThread(id, ip, listener, noFinishTask).start();
											} 
									    }    
									}catch (Exception e){
								    	e.printStackTrace();
								    }      
								}
							}
						}.start();
					}else{
						activity.finish();
						handler.sendEmptyMessage(4);
					}
				}
			});
		}else{
			System.out.println("任务id："+id);
			SqliteAdapter adapter = new SqliteAdapter(getApplicationContext());
			System.out.println("1      "+code);
			NoFinishTask noFinishTask = adapter.getNoFinishTask(code, id);
			new MyClientThread(id, ip, listener, noFinishTask).start();
		}
	}

	FileListener listener = new FileListener() {
		
		@Override
		public void getReceiveInfo(int read) {
			Message msg = handler.obtainMessage();
			msg.what = 1;
			msg.obj = read;
			handler.sendMessage(msg);
		}
		
		@Override
		public void getFilesInfo(String[] fileNames, long[] fileLength) {
			Message msg = handler.obtainMessage();
			msg.what = 2;
			ArrayList<Object> list = new ArrayList<Object>();
			list.add(fileNames);
			list.add(fileLength);
			msg.obj = list;
			handler.sendMessage(msg);
		}
		
		@Override
		public void finishOneFile(String file_name) {
			handler.sendEmptyMessage(3);
		}

		@Override
		public void cancelConnect(int file, long length) {
			if(file == 0 && length == 0){
				//没连接上  取消传输
				handler.sendEmptyMessage(4);
			}else{
				SqliteAdapter adapter = new SqliteAdapter(getApplicationContext());
				System.out.println("连接中断，已传输:"+id+"  "+code+"  "+file+"  "+length);
				adapter.addNoFinishTask(new NoFinishTask(id, code, file, length));
				handler.sendEmptyMessage(5);
			}
			activity.finish();
		}

		@Override
		public void successConnect() {
			//连接成功
			handler.sendEmptyMessage(6);
		}
	};
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int id = msg.what;
			switch (id) {
			case 1:
				//设置传输进度
				current_progress += (int)msg.obj;
				int and = (int) ((current_progress*100)/total_progress);
				activity.setWaveProgress(and);
				//设置传输速度
				long current_time = System.currentTimeMillis();
				long time = current_time-begin_time+1;
				DecimalFormat decimalFormat=new DecimalFormat("0.00");
				activity.setSpeed(decimalFormat.format((current_progress/time)/1048.576));
				break;
			case 2:
				ArrayList<Object> list = (ArrayList<Object>) msg.obj;
				fileNames = (String[]) list.get(0);
				fileLength = (long[]) list.get(1);
				fileNumber = fileNames.length;
				for(int i = 0;i<fileNumber;i++){
					total_progress += fileLength[i];
				}
				begin_time = System.currentTimeMillis();
				activity.setProcess("（"+count+"/"+fileNumber+"）");
				break;
			case 3:
				count++;
				activity.setProcess("（"+count+"/"+fileNumber+"）");
				break;
			case 4:
				activity.showToast("连接失败！");
				break;
			case 5:
				activity.showToast("与服务器断开连接！");
				break;
			case 6:
				activity.showDialog(false);
				break;
			}
		}
	};
	
}
