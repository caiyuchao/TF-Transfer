package com.example.transfer.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MyServerThread extends Thread{
	
	private Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	
	public MyServerThread(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			// 封装为数据流
			dis = new DataInputStream(is);
			dos = new DataOutputStream(os);
			//获取需要传输的文件列表id
			long id = dis.readLong();
			//得到文件列表
			ArrayList<String> list_path = SocketThreadFactory.getTaskList(id);
			//得到需要从哪个文件的哪个字节开始传
			int start_file = dis.readInt();  //从0开始
			long start_length = dis.readLong(); //从0开始
			//发送文件个数
			dos.writeInt(list_path.size() - start_file);
			ArrayList<File> list_file = new ArrayList<File>();
			//遍历列表  将路径转化为文件  并存入文件列表中
			for(int i = start_file;i<list_path.size();i++){
				File file = new File(list_path.get(i));
				dos.writeUTF(file.getName());
				dos.flush();
				list_file.add(file);
				if(i == start_file)
					dos.writeLong(file.length() - start_length);
				else
					dos.writeLong(file.length());
				dos.flush();
			}
			File file;
			//遍历列表发送文件
			for(int i = 0;i<list_file.size();i++){
				file = list_file.get(i);
				dos.writeUTF(file.getName());
				dos.flush();
				if(i == 0){
					dos.writeLong(file.length() - start_length);
					dos.flush();
				}else{
					dos.writeLong(file.length());
					dos.flush();
				}
				//获取文件输入流  并创建数组
				FileInputStream fis = new FileInputStream(file);
				//跳过已传输的字节
			    while(start_length > 0) {  
			    	long amt = fis.skip(start_length);  
			        if (amt == -1) {  
			        	break;
//			        	throw new RuntimeException(file + ": unexpected EOF");  
			        }  
			        start_length -= amt;  
			    }
			    System.out.println("已跳过字节");
				byte[] buf = new byte[8192];
				int read;
				while((read = fis.read(buf)) != -1){
					dos.write(buf, 0, read);
				}
				dos.flush();
				fis.close();
			}
			dis.close();
			dos.close();
			socket.close();
			SocketThreadFactory.deleteClient(this);
		} catch (Exception e) {
			e.printStackTrace();
			SocketThreadFactory.deleteClient(this);
		}
	}

}
