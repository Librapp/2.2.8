package com.sumavision.talktv2.dao;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.VodProgramData;

public class AccessProgram {
	private final ProgramDataBaseHelper openHelper;

	public AccessProgram(Context context) {
		this.openHelper = new ProgramDataBaseHelper(context);

	}

	public boolean isExisted(VodProgramData vodProgram, SQLiteDatabase db) {

		String querrySql = "select count(*) from program where id = ? and url= ?";
		String id = vodProgram.id;
		String url = vodProgram.dbUrl;
		String[] bindArgs = { id, url };
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
		sql = "INSERT INTO program (id,topicid,programname,lastestintro,url,position) VALUES (?, ?, ?, ?,?,?)";
		SQLiteDatabase db = this.openHelper.getWritableDatabase();
		db.beginTransaction();
		try {

			String id = program.id;
			String topicId = program.topicId == null ? "" : program.topicId;
			String programname = program.name == null ? "" : program.name;
			String intro = program.updateName == null ? "" : program.updateName;
			String url = program.dbUrl;

			if (OtherCacheData.current().isDebugMode)
				Log.e("AccessProgram-save", programname);

			long playPosition = program.dbposition;

			if (!isExisted(program, db)) {
				Object[] bindArgs = { id, topicId, programname, intro, url,
						playPosition };
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

	public long find(VodProgramData program) {
		String sql = "";
		sql = "SELECT * FROM program where id = ? and url= ?";
		String id = program.id;
		String url = program.dbUrl;
		String[] selectionArgs = { id, url };
		long position = 0;
		Cursor cursor = null;
		try {
			this.openHelper.getWritableDatabase().beginTransaction();
			cursor = this.openHelper.getReadableDatabase().rawQuery(sql,
					selectionArgs);
			while (cursor.moveToNext()) {
				position = cursor.getLong(6);
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
		sql = "update program set position=?  where id=? and url=? ";
		String id = program.id;
		String url = program.dbUrl;
		long dbposition = program.dbposition;
		String[] bindArgs = { String.valueOf(dbposition), id, url };
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
		sql = "SELECT * FROM program  limit 6";
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
				tempProgramData.dbUrl = cursor.getString(5);
				tempProgramData.dbposition = cursor.getInt(6);
				temp.add(tempProgramData);

				if (OtherCacheData.current().isDebugMode)
					Log.e("AccessProgram-findAll", tempProgramData.name);
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

	public void delete(VodProgramData program) {
		String sql = "";
		sql = "delete from program   where id=? and url=? ";
		String id = program.id;
		String url = program.dbUrl;
		String[] bindArgs = { id, url };
		SQLiteDatabase db = this.openHelper.getWritableDatabase();
		try {
			this.openHelper.getWritableDatabase().beginTransaction();
			db.execSQL(sql, bindArgs);
			this.openHelper.getWritableDatabase().setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.openHelper.getWritableDatabase().endTransaction();
			this.openHelper.close();
		}
	}

	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		this.openHelper.onUpgrade(db, oldVersion, newVersion);
	}

	public void dropTable(SQLiteDatabase db) {

		this.openHelper.dropTable(db, "program");

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
