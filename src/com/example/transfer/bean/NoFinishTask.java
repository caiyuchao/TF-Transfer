package com.example.transfer.bean;

import java.io.Serializable;

public class NoFinishTask implements Serializable{

	private long id;
	private String code;
	private int start_file;
	private long start_length;
	
	public NoFinishTask() {
		super();
	}

	public NoFinishTask(long id, String code, int start_file, long start_length) {
		super();
		this.id = id;
		this.code = code;
		this.start_file = start_file;
		this.start_length = start_length;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getStart_file() {
		return start_file;
	}

	public void setStart_file(int start_file) {
		this.start_file = start_file;
	}

	public long getStart_length() {
		return start_length;
	}

	public void setStart_length(long start_length) {
		this.start_length = start_length;
	}
	
}
