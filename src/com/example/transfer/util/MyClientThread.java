package com.example.transfer.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.example.transfer.bean.NoFinishTask;

import android.os.Environment;

public class MyClientThread extends Thread{

	private long id;
	private String ip;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Socket socket;
	private FileListener listener;
	private int count = 0;//标记连接次数
	private NoFinishTask noFinishTask;
	
	public MyClientThread(long id, String ip, FileListener listener, NoFinishTask noFinishTask){
		this.id = id;
		this.ip = ip;
		this.listener = listener;
		this.noFinishTask = noFinishTask;
	}
	
	@Override
	public void run() {
		System.out.println("开始运行客户机！");
		//保存在本地
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			File dir = new File(Environment.getExternalStorageDirectory()+"/Transfer/files/");
	        if(!dir.exists()){
	            System.out.println("创建文件夹:"+dir.mkdirs());
	        }
		}
		while(socket == null){
			try {
				System.out.println("连接一次服务器:"+ip);
				socket = new Socket(ip, 9494);
			} catch (Exception e) {
				try {
					sleep(1000);
					count++;
					//如果连接次数达到5次都没成功  则取消连接
					if(count == 5){
						listener.cancelConnect(0, 0);
						return;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		File file = null;
		int index = 0;
		try {
			System.out.println("连接上了");
			listener.successConnect();
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			//传输id、开始文件、开始字节
			dos.writeLong(id);
			if(noFinishTask != null){
//				System.out.println("发送起始："+noFinishTask.getStart_file()+"  "+noFinishTask.getStart_length());
				dos.writeInt(noFinishTask.getStart_file());
				dos.writeLong(noFinishTask.getStart_length());
			}else{
				dos.writeInt(0);
				dos.writeLong(0);
			}
			//接收文件个数
			int length = dis.readInt();
			System.out.println("获取文件个数"+length);
			String[] fileNames = new String[length];
			long[] fileLength = new long[length];
			//获取文件列表   包括名称  大小
			for(int i = 0;i<length;i++){
				fileNames[i] = dis.readUTF();
				System.out.println("读取文件名"+fileNames[i]);
				fileLength[i] = dis.readLong();
				System.out.println("读取文件长度"+fileLength[i]);
			}
			listener.getFilesInfo(fileNames, fileLength);
			//接收文件
			for(int i = 0;i<length;i++){
				index = i;
				String file_name = dis.readUTF();
				long lenth = dis.readLong();
				long count = 0;
				FileOutputStream fos = null;
				if(i == 0){
					if(noFinishTask != null){
						file = new File(Environment.getExternalStorageDirectory()+"/Transfer/files/"+fileNames[i]);
						fos = new FileOutputStream(file, true);
					}else{
						file = new File(FileUtils.nameFile(Environment.getExternalStorageDirectory()+"/Transfer/files/"+fileNames[i]));
						fos = new FileOutputStream(file);
					}
				}else{
					file = new File(FileUtils.nameFile(Environment.getExternalStorageDirectory()+"/Transfer/files/"+fileNames[i]));
					fos = new FileOutputStream(file);
				}
				byte[] buf = new byte[8192];
				int read;
				while(count<lenth){
					if(lenth-count >= 8192){
						read = dis.read(buf);
						fos.write(buf, 0, read);
					}else{
						byte[] bufs = new byte[(int) (lenth-count)];
						read = dis.read(bufs);
						fos.write(bufs, 0, read);
					}
					listener.getReceiveInfo(read);
					count += read;
				}
				System.out.println("一个文件传输完成");
				listener.finishOneFile(file_name);
				fos.close();
			}
			dis.close();
			dos.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
			
			try {
				if(dis != null)
					dis.close();
				if(dos != null)
					dos.close();
				if(socket != null)
					socket.close();
				listener.cancelConnect(index, file.length());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public interface FileListener{
		public void getReceiveInfo(int read);
		public void finishOneFile(String file_name);
		public void getFilesInfo(String[] fileNames, long[] fileLength);
		public void cancelConnect(int file, long length);
		public void successConnect();
	}
	
}
