package com.example.transfer.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Task implements Serializable {

	//任务ID
	public static final int SEND_FILE = 1; //发送文件
	
	private int taskId;
	private Map<String, Object> map = new HashMap<String, Object>();
	
	public Task() {
		super();
	}
	public Task(int taskId, Map<String, Object> map) {
		super();
		this.taskId = taskId;
		this.map = map;
	}
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	public Map<String, Object> getMap() {
		return map;
	}
	public void setMap(HashMap<String, Object> map) {
		this.map = map;
	}
}
