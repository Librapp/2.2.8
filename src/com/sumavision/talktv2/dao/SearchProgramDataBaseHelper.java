package com.sumavision.talktv2.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SearchProgramDataBaseHelper extends SQLiteOpenHelper {
	private static String DB_NAME = "talktv_search_program.db";
	private static final int version = 1;

	@Override
	public synchronized void close() {
		super.close();
	}

	public SearchProgramDataBaseHelper(Context context) {
		super(context, DB_NAME, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE IF NOT EXISTS searchprogram ("
				+ "_id integer primary key autoincrement," + "id varchar(10),"
				+ "topicid varchar(100)," + "programname varchar(100),"
				+ "lastestintro varchar(100))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS program");
		this.onCreate(db);
	}

	public String dropTable(SQLiteDatabase db, String tableName) {
		if (tableName == null) {
			return null;
		}

		try {
			String DROP_TABLE = "DROP TABLE IF EXISTS " + tableName;
			db.execSQL(DROP_TABLE);
		} catch (Exception ex) {
		}

		db.close();

		return tableName;
	}
}
