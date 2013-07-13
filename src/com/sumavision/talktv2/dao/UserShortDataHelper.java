package com.sumavision.talktv2.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author 郭鹏
 * 
 */
public class UserShortDataHelper extends SQLiteOpenHelper {

	private static String DB_NAME = "userShort.db";
	private static final int version = 1;

	@Override
	public synchronized void close() {
		super.close();
	}

	public UserShortDataHelper(Context context) {
		super(context, DB_NAME, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

//		Log.e("UserShortDataHelper-onCreate", "onCreate!!");

		db.execSQL("CREATE TABLE IF NOT EXISTS userShort_list("
				+ "_id integer primary key autoincrement,"
				+ "userName varchar(20)," + "password varchar(100),"
				+ "userID varchar(100))");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS custom_Program");
		this.onCreate(db);

	}

	public String dropTable(SQLiteDatabase db, String tableName) {
//		Log.e("CustomDataHelper", "called fun dropTable()");
		if (tableName == null) {
//			Log.e("CustomDataHelper", "table name is error! " + tableName
//					+ " is null !");
			return null;
		}

		try {
			String sql = "DROP TABLE " + tableName;
			db.execSQL(sql);
		} catch (Exception ex) {
//			Log.e("CustomDataHelper", "删除表异常:" + ex.getMessage());
		}

		try {
			String DROP_TABLE = "DROP TABLE IF EXISTS " + tableName;
			db.execSQL(DROP_TABLE);
//			Log.e("CustomDataHelper", DROP_TABLE);
		} catch (Exception ex) {
//			Log.e("CustomDataHelper", "删除表异常:" + ex.getMessage());
		}

//		Log.e("CustomDataHelper", "table/" + tableName
//				+ " is dropped successfully");

		return tableName;
	}

}
