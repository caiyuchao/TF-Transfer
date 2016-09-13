package com.example.transfer.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.example.transfer.MainActivity;
import com.tf.transfer.R;
import com.example.transfer.bean.TaskRecord;
import com.example.transfer.bean.TransferUser;
import com.example.transfer.util.SqliteAdapter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class SDFileExplorerActivity extends Activity{

	ListView listView;	
	boolean[] isCheck;	
	File currentParent;	
	File[] currentFiles;	
	List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();	
	ArrayList<Map<String, String>> selectItems = new ArrayList<Map<String,String>>();
	private boolean flag = false;//是否上传至服务器
	private ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState)	{		
		super.onCreate(savedInstanceState);	
		getActionBar().hide();
		setContentView(R.layout.file_explorer);		
		listView = (ListView) findViewById(R.id.explorer_list);		
		File root = new File("/mnt/");
		//sdcard/		
		if (root.exists()){		
			currentParent = root;			
			currentFiles = root.listFiles();			
			inflateListView(currentFiles);		
		}		
		listView.setOnItemClickListener(new OnItemClickListener(){	
			@Override		
			public void onItemClick(AdapterView<?> parent, View view,int position, long id){		
				if (currentFiles[position].isFile())
					return;		
				File[] tmp = currentFiles[position].listFiles();		
				if (tmp == null || tmp.length == 0){			
					Toast.makeText(SDFileExplorerActivity.this, "文件夹为空或无法进入",Toast.LENGTH_SHORT).show();			
				}else{			
					currentParent = currentFiles[position]; 
					currentFiles = tmp;				
					inflateListView(currentFiles);		
				}		
			}	
		});	
		Button select = (Button) findViewById(R.id.explorer_select);		
		select.setOnClickListener(new OnClickListener(){			
			@Override			
			public void onClick(View v){		
				for(int i=0;i<isCheck.length;i++){	
					if(isCheck[i]){		
						Map<String, String> selectItem = new HashMap<String,String>();	
						selectItem.put("file_name", listItems.get(i).get("fileName").toString());
//						Toast.makeText(getApplication(), listItems.get(i).get("fileName").toString(), Toast.LENGTH_SHORT).show();
						try {							
							selectItem.put("path", currentParent.getCanonicalPath());	
						} catch (IOException e) {			
							e.printStackTrace();			
						}		
						selectItems.add(selectItem);
					}			
				}			
				if(selectItems.isEmpty()){		
					Toast.makeText(SDFileExplorerActivity.this, "未选择文件",	Toast.LENGTH_SHORT).show();			
				}else{		
					if(flag){
						//上传文件
						showProgressDialog();
						final int length = selectItems.size();
						final ArrayList<String> list_id = new ArrayList<String>();
						for(int i = 0;i<length;i++){
							Map<String, String> map = selectItems.get(i);
				    		String path = map.get("path");
				    		String file_name = map.get("file_name");
				    		String filePath = path+"/"+file_name;
				    		try {
								final AVFile file = AVFile.withAbsoluteLocalPath(file_name, filePath);
								file.saveInBackground(new SaveCallback() {
									@Override
									public void done(AVException arg0) {
										System.out.println(list_id.size());
										list_id.add(file.getObjectId());
										System.out.println(list_id.size());
										if(list_id.size() == length){
											upload(list_id);
										}
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
				    	}
					}else{
						ArrayList<Object> list = insertLocalRecorde();
						Bundle bundle = new Bundle();
						bundle.putLong("id", (long) list.get(0));
						bundle.putString("qrCode_path", (String) list.get(1));
						bundle.putInt("flag", 1);
						//将文件添加到数据库
						insertFile(selectItems, (long) list.get(0));
						//跳转
						Intent intent = new Intent(SDFileExplorerActivity.this, MainActivity.class);
						intent.putExtras(bundle);
						startActivity(intent);
						finish();
					}
				}			
			}
		});			
		Button cancel = (Button) findViewById(R.id.explorer_cancel);		
		cancel.setOnClickListener(new OnClickListener(){			
			@Override			
			public void onClick(View source){			
				finish();
			}
		});	
		CheckBox checkBox = (CheckBox) findViewById(R.id.explorer_network);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if(isChecked){ 
					flag = true;
                }else{ 
                }
			}
		});
	}
	
	public void upload(ArrayList list_id){
		//上传记录
		final AVObject task = new AVObject("Task");
		task.put("files", list_id.toArray());
		task.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(AVException arg0) {
				String path = insertNetworkRecorde(task.getObjectId());
				//跳转
				Bundle bundle = new Bundle();
				bundle.putString("qrCode_path", path);
				bundle.putString("objectId", task.getObjectId());
				bundle.putInt("flag", 2);
				Intent intent = new Intent(SDFileExplorerActivity.this, MainActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK ){  
			try{					
				if (!currentParent.getCanonicalPath().equals("/mnt")){						
					currentParent = currentParent.getParentFile();			
					currentFiles = currentParent.listFiles();	
					inflateListView(currentFiles);	
				}				
			}catch (Exception e){		
				e.printStackTrace();			
			}		
        }  
		return false;
	}
	
	private void inflateListView(File[] files){		
		listItems.clear();		
		selectItems.clear();		
		isCheck = new boolean[files.length];	
		for (int i = 0; i < files.length; i++){		
			isCheck[i]=false;			
			Map<String, Object> listItem = new HashMap<String, Object>();		
			if (files[i].isDirectory())			{			
				listItem.put("icon", R.drawable.folder_64);			
			}else{			
				listItem.put("icon", R.drawable.file_64);		
			}			
			listItem.put("fileName", files[i].getName());		
			listItems.add(listItem);	
		}	
		BaseAdapter MyAdapter = new BaseAdapter(){		
			@Override		
			public int getCount(){	
				return listItems.size();			
			}			
			@Override		
			public Object getItem(int position) {		
				return null;		
			}			
			@Override		
			public long getItemId(int position) {		
				return position;		
			}			
			@Override		
			public View getView(int position, View convertView, ViewGroup parent) {			
				int icon;				
				final int index = position; 			
				String fileName;				
				ViewHolder holder = null;		
				if(convertView==null){					
					holder=new ViewHolder();					
					convertView = LayoutInflater.from(SDFileExplorerActivity.this).inflate(R.layout.item_explorer_list, null);		
					holder.image = (ImageView)convertView.findViewById(R.id.item_explorer_list_icon);				
					holder.text = (TextView)convertView.findViewById(R.id.item_explorer_list_file_name);			
					holder.checkBox = (CheckBox)convertView.findViewById(R.id.item_explorer_list_check_box);	
					convertView.setTag(holder);				
				}else{				
					holder = (ViewHolder)convertView.getTag();				
				}								
				icon = Integer.parseInt(listItems.get(position).get("icon").toString());				
				holder.image.setImageResource(icon);				
				fileName=listItems.get(position).get("fileName").toString();	
				holder.text.setText(fileName);				
				holder.checkBox.setChecked(isCheck[index]);		
				if(icon == R.drawable.folder_64){				
					holder.checkBox.setVisibility(View.GONE);			
				}else 
					holder.checkBox.setVisibility(0);		
				holder.checkBox.setOnClickListener(new OnClickListener(){		
					@Override					
					public void onClick(View v) {			
						isCheck[index]=!isCheck[index];				
					}										
				});	
				return convertView;		
			}		
		};		
		listView.setAdapter(MyAdapter);	
	}	
	
	static class ViewHolder	{		
		public ImageView image;	
		public TextView text;	
		public CheckBox checkBox;	
	}
	
	//插入本地数据到数据库
	public ArrayList<Object> insertLocalRecorde(){
		Date date = new Date();  
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String create_time = format.format(date);
		SqliteAdapter adapter = new SqliteAdapter(this);
		ContentValues values = new ContentValues();
		values.put("create_time", create_time);
		values.put("type", 1);
		long id = adapter.addTask(values);
		//生成qrCode
		String qrCode_path = getQRCodeCachePath(id, create_time);
		adapter.upadteQRCode(qrCode_path, id);
		ArrayList<Object> list = new ArrayList<Object>();
		list.add(id);
		list.add(qrCode_path);
		return list;
	}
	
	//插入网络数据到数据库
	public String insertNetworkRecorde(String objectId){
		Date date = new Date();  
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String create_time = format.format(date);
		SqliteAdapter adapter = new SqliteAdapter(this);
		ContentValues values = new ContentValues();
		values.put("create_time", create_time);
		values.put("type", 2);
		values.put("remark", objectId);
		String qrcode_path = getQRCodeCachePath(objectId, create_time);
		values.put("qr_code", qrcode_path);
		adapter.addTask(values);
		return qrcode_path;
	}
	
	public String getQRCodeCachePath(String str, String time){
		File file = null;
		//生成二维码
		Bitmap qrCode = generateQRCode(str);
		//保存在本地
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
    		File dir = new File(Environment.getExternalStorageDirectory()+"/Transfer/qrCode/");
            if(!dir.exists()){
                System.out.println(dir.mkdirs());
            }
            file = new File(Environment.getExternalStorageDirectory()+"/Transfer/qrCode/"+time+".jpg");
            if (!file.exists()){
            	try {
					boolean flag = file.createNewFile();
					if(flag){
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						qrCode.compress(Bitmap.CompressFormat.JPEG, 100, baos);
						byte[] bytes = baos.toByteArray();
						FileOutputStream fos = new FileOutputStream(file);
						fos.write(bytes);
						fos.flush();
						fos.close();
						System.out.println("二维码已保存");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
		}
		return file.getAbsolutePath();
	}
	
	//得到二维码的缓存地址
	public String getQRCodeCachePath(long id, String time){
		File file = null;
		//获得传输者的热点名称
		String username = TransferUser.getInstance().getUsername();
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		String code = telephonyManager.getDeviceId();
		System.out.println("设备唯一ID:"+code);
		String json = "{\"id\":"+id+", \"username\":\""+username+"\", \"code\":\""+code+"\", \"ip\":\"\"}";
		System.out.println("二维码内容："+json);
		//生成二维码
		Bitmap qrCode = generateQRCode(json);
		//保存在本地
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
    		File dir = new File(Environment.getExternalStorageDirectory()+"/Transfer/qrCode/");
            if(!dir.exists()){
                System.out.println(dir.mkdirs());
            }
            file = new File(Environment.getExternalStorageDirectory()+"/Transfer/qrCode/"+time+".jpg");
//            System.out.println(file);
            if (!file.exists()){
            	try {
					boolean flag = file.createNewFile();
					if(flag){
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						qrCode.compress(Bitmap.CompressFormat.JPEG, 100, baos);
						byte[] bytes = baos.toByteArray();
						FileOutputStream fos = new FileOutputStream(file);
						fos.write(bytes);
						fos.flush();
						fos.close();
						System.out.println("二维码已保存");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
		}
		return file.getAbsolutePath();
	}
	
	private Bitmap bitMatrix2Bitmap(BitMatrix matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[] rawData = new int[w * h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = Color.WHITE;
                if (matrix.get(i, j)) {
                    color = Color.BLACK;
                }
                rawData[i + (j * w)] = color;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
        bitmap.setPixels(rawData, 0, w, 0, 0, w, h);
        return bitmap;
    }

    private Bitmap generateQRCode(String content) {
        try {
        	QRCodeWriter writer = new QRCodeWriter();
            // MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 500, 500);
            return bitMatrix2Bitmap(matrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
	
    public void insertFile(ArrayList<Map<String, String>> list, long id){
    	SqliteAdapter adapter = new SqliteAdapter(this);
    	for(int i = 0;i<list.size();i++){
    		Map<String, String> map = list.get(i);
    		String path = map.get("path");
    		String file_name = map.get("file_name");
    		adapter.addTaskDetail(id, path+"/"+file_name);
    		System.out.println(path+"/"+file_name);
    	}
    }
    
    public void showProgressDialog(){
    	System.out.println("加载对话框");
    	progressDialog = ProgressDialog.show(this, "请稍后", "正在上传……", true);
		progressDialog.setCancelable(false);
    }
    
}
