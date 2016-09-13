package com.example.transfer.bean;

public class TaskRecord {

	private long id;
	private String qrCode_path;
	private String time;
	private int type;
	private String remark;
	
	public TaskRecord() {
		super();
	}

	public TaskRecord(long id, String qrCode_path, String time, int type, String remark) {
		super();
		this.id = id;
		this.qrCode_path = qrCode_path;
		this.time = time;
		this.type = type;
		this.remark = remark;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getQrCode_path() {
		return qrCode_path;
	}

	public void setQrCode_path(String qrCode_path) {
		this.qrCode_path = qrCode_path;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
}
