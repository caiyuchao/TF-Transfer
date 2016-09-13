package com.example.transfer.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteOpenHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;
	private static final String DB_NAME = "task_record.db";
	private static final String TASK_TB = "create table task(_id integer primary key autoincrement, type integer, qr_code text, create_time text, remark text)";
	private static final String TASK_DETAIL_TB = "create table task_detail(_id integer primary key autoincrement, file_path text, task_id integer not null)";
	private static final String NO_FINISH_TASK_TB = "create table no_finish_task(_id integer primary key autoincrement, task_id integer, file integer, length integer, code text)";
	
	private static final String DROP_TASK_TB = "drop table if exists TASK";
	private static final String DROP_TASK_DETAIL_TB = "drop table if exists TASK_DETAIL";
	private static final String DROP_NO_FINISH_TASK_TB = "drop table if exists NO_FINISH_TASK";
	public SqliteOpenHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TASK_TB);
		db.execSQL(TASK_DETAIL_TB);
		db.execSQL(NO_FINISH_TASK_TB);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(newVersion > oldVersion){
			db.execSQL(DROP_TASK_TB);
			db.execSQL(DROP_TASK_DETAIL_TB);
			db.execSQL(DROP_NO_FINISH_TASK_TB);
			db.execSQL(TASK_TB);
			db.execSQL(TASK_DETAIL_TB);
			db.execSQL(NO_FINISH_TASK_TB);
		}
	}

}
