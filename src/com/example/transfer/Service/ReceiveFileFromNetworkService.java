package com.example.transfer.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.GetFileCallback;
import com.avos.avoscloud.ProgressCallback;
import com.example.transfer.bean.NoFinishTask;
import com.example.transfer.ui.ReceiveFileActivity;
import com.example.transfer.util.FileUtils;
import com.example.transfer.util.MyClientThread;
import com.example.transfer.util.SqliteAdapter;
import com.example.transfer.util.MyClientThread.FileListener;
import com.example.transfer.util.WifiAdmin;
import com.example.transfer.util.WifiAdmin.WifiThreadListener;

public class ReceiveFileFromNetworkService extends IntentService{

	public static ReceiveFileActivity activity;
	private int count = 0;//当前传输第几个文件
	private String task_id;
	private long[] fileLength;//文件大小数组
	private int fileNumber;//文件数量
	private long current_progress = 0;//当前传输字节数
	private long total_progress = 0;//总共需传输字节数
	private long begin_time = 0;//开始时间
	private ArrayList<AVFile> list = new ArrayList<AVFile>();
	private JSONArray array;
	
	public ReceiveFileFromNetworkService() {
		super("com.example.transfer.Service.ReceiveFileFromNetworkService");
	}
	
	@Override
	protected void onHandleIntent(Intent arg0) {
		task_id = arg0.getStringExtra("task_id");
		AVQuery<AVObject> query = new AVQuery<AVObject>("Task");
		query.getInBackground(task_id, new GetCallback<AVObject>() {
			
			@Override
			public void done(AVObject object, AVException arg1) {
				if(arg1==null){
					array = object.getJSONArray("files");
					fileNumber = array.length();
					fileLength = new long[fileNumber];
					prepare();
				}else{
					activity.finish();
					handler.sendEmptyMessage(4);
				}
			}
		});
	}

	//下载前准备
	public void prepare(){
		if(count<fileNumber){
			//获得文件信息
			try {
				String objectId = (String) array.get(count);
				AVFile.withObjectIdInBackground(objectId, new GetFileCallback<AVFile>() {
					
					@Override
					public void done(AVFile file, AVException arg1) {
						list.add(file);
						fileLength[count] = file.getSize();
						total_progress += file.getSize();
						count++;
						prepare();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("准备结束");
			handler.sendEmptyMessage(3);
			count = 0;
			download();
		}
	}
	
	public void download(){
		if(count<fileNumber){
			final AVFile file = list.get(count);
			file.getDataInBackground(new GetDataCallback() {
				
				@Override
				public void done(byte[] arg0, AVException arg1) {
					String fileName = file.getOriginalName();
					try {
						FileOutputStream stream = new FileOutputStream(FileUtils.nameFile(Environment.getExternalStorageDirectory()+"/Transfer/files/"+fileName));
						stream.write(arg0);
						stream.flush();
						stream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					handler.sendEmptyMessage(2);
					current_progress += fileLength[count];
					count++;
					download();
				}
			}, 
			new ProgressCallback() {
				
				@Override
				public void done(Integer arg0) {
					long current = fileLength[count] * arg0 /100 + current_progress;
					int progress = (int) ((current*100)/total_progress);
					Message msg = handler.obtainMessage();
					msg.what = 1;
					msg.obj = current;
					msg.arg1 = progress;
					handler.sendMessage(msg);
				}
			});
			
		}else{
			
		}
	}
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int id = msg.what;
			switch (id) {
			case 1:
				//设置传输进度
				int progreass = msg.arg1;
				long current = (long) msg.obj;
				activity.setWaveProgress(progreass);
				//设置传输速度
				long current_time = System.currentTimeMillis();
				long time = current_time-begin_time+1;
				DecimalFormat decimalFormat=new DecimalFormat("0.00");
				activity.setSpeed(decimalFormat.format((current/time)/1048.576));
				break;
			case 2:
				activity.setProcess("（"+count+"/"+fileNumber+"）");
				break;
			case 3:
				activity.showDialog(false);
				begin_time = System.currentTimeMillis();
				break;
			case 4:
				activity.showToast("连接失败，请检查网络连接！");
				break;
			}
		}
	};
	
}
