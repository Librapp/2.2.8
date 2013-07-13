package com.sumavision.talktv2.dao;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sumavision.talktv2.data.VodProgramData;

public class AccessSearchProgram {
	private SearchProgramDataBaseHelper openHelper;

	public AccessSearchProgram(Context context) {
		this.openHelper = new SearchProgramDataBaseHelper(context);

	}

	public boolean isExisted(VodProgramData vodProgram, SQLiteDatabase db) {

		String querrySql = "select count(*) from searchprogram where id = ? ";
		String id = vodProgram.id;
		String[] bindArgs = { id };
		Cursor cursor = db.rawQuery(querrySql, bindArgs);
		if (cursor != null) {
			cursor.moveToFirst();
			long howLong = cursor.getLong(0);
			cursor.close();
			if (howLong > 0) {
				return true;
			}
		}
		return false;

	}

	public boolean save(VodProgramData program) {
		String sql = null;
		sql = "INSERT INTO searchprogram (id,topicid,programname,lastestintro) VALUES (?, ?,?,?)";
		SQLiteDatabase db = this.openHelper.getWritableDatabase();
		db.beginTransaction();
		try {

			String id = program.id;
			String topicId = program.topicId == null ? "" : program.topicId;
			String programname = program.name;
			String intro = program.updateName == null ? "" : program.updateName;

			if (!isExisted(program, db)) {
				Object[] bindArgs = { id, topicId, programname, intro };
				this.openHelper.getWritableDatabase().execSQL(sql, bindArgs);
			} else {
				update(program, db);
				Log.e("AccessProgramPlayPosition", "needUpdate");
			}

			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}
		return true;

	}

	public int find(VodProgramData program) {
		String sql = "";
		sql = "SELECT * FROM searchprogram where id = ? ";
		String id = program.id;
		String[] selectionArgs = { id };
		int position = 0;
		Cursor cursor = null;
		try {
			this.openHelper.getWritableDatabase().beginTransaction();
			cursor = this.openHelper.getReadableDatabase().rawQuery(sql,
					selectionArgs);
			while (cursor.moveToNext()) {
				position = cursor.getInt(6);
			}
			cursor.close();
			this.openHelper.getWritableDatabase().setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			this.openHelper.getWritableDatabase().endTransaction();
			this.openHelper.close();
		}
		return position;
	}

	private void update(VodProgramData program, SQLiteDatabase db) {
		String sql = "";
		sql = "update searchprogram set position=?  where id=?  ";
		String id = program.id;
		String[] bindArgs = { id };
		try {
			db.execSQL(sql, bindArgs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	public ArrayList<VodProgramData> findAll() {
		ArrayList<VodProgramData> temp = new ArrayList<VodProgramData>();
		String sql = "";
		sql = "SELECT * FROM searchprogram  limit 6";
		String[] selectionArgs = null;

		Cursor cursor = null;
		try {
			this.openHelper.getWritableDatabase().beginTransaction();
			cursor = this.openHelper.getReadableDatabase().rawQuery(sql,
					selectionArgs);
			while (cursor.moveToNext()) {
				VodProgramData tempProgramData = new VodProgramData();
				tempProgramData.id = String.valueOf(cursor.getInt(1));
				tempProgramData.topicId = cursor.getString(2);
				tempProgramData.name = cursor.getString(3);
				tempProgramData.updateName = cursor.getString(4);
				temp.add(tempProgramData);
			}
			cursor.close();
			this.openHelper.getWritableDatabase().setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			this.openHelper.getWritableDatabase().endTransaction();
			this.openHelper.close();
		}
		return temp;
	}

	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		this.openHelper.onUpgrade(db, oldVersion, newVersion);
	}

	public void dropTable(SQLiteDatabase db) {

		this.openHelper.dropTable(db, "searchprogram");

	}

	/**
	 * 关闭数据库
	 */
	public void close() {
		this.openHelper.close();
	}

	public void beginTransaction() {
		this.openHelper.getWritableDatabase().beginTransaction();

	}

	public void setTransactionSuccessful() {

		close();
		this.openHelper.getWritableDatabase().setTransactionSuccessful();

	}

	public void endTransaction() {
		this.openHelper.getWritableDatabase().endTransaction();

	}
}
