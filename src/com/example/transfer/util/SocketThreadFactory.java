package com.example.transfer.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class SocketThreadFactory {
	
	private static ArrayList<Map<String, Object>> list_task = new ArrayList<Map<String, Object>>();
	private static ArrayList<MyServerThread> list_client = new ArrayList<MyServerThread>();
	private static ServerSocketThread socketThread;
	
	public static void addTask(Map<String, Object> map) throws IOException{
		if(socketThread == null){
			socketThread = new ServerSocketThread(9494);
			new Thread(socketThread).start();
			System.out.println("启动服务器");
		}
		list_task.add(map);
		System.out.println("添加任务");
	}
	
	public static void deleteTask(long id){
		for(int i = 0;i<list_task.size();i++){
			Map<String, Object> map = list_task.get(i);
			System.out.println((long)map.get("id")+"     "+id);
			if((long)map.get("id") == id){
				list_task.remove(i);
			}
		}
		if(list_task.size() == 0){
			try {
				socketThread.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			socketThread = null;
		}
	}
	
	//返回相应id的任务列表
	public static ArrayList<String> getTaskList(long id){
		for(int i = 0;i<list_task.size();i++){
			System.out.println("返回了");
			//得到任务id
			long task_id = (long) list_task.get(i).get("id");
			//匹配id
			if(task_id == id){
				ArrayList<String> list_path = (ArrayList<String>) list_task.get(i).get("files");
				return list_path;
			}
		}
		return new ArrayList<String>();
	}
	
	//添加连接
	public static void addClient(MyServerThread client){
		list_client.add(client);
	}
	
	//删除连接
	public static void deleteClient(MyServerThread client){
		list_client.remove(client);
	}
	
	public static ArrayList<Map<String, Object>> getCurrentTaskList(){
		return list_task;
	}
	
}
