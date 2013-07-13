package com.sumavision.talktv2.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * 
 * @author 郭鹏
 * @version 2.0
 * @createTime 2012-6-1
 * @description 图片工具
 * @changLog
 */
public class PicUtils {
	
	private Context c;
	private Activity a;
	
	public PicUtils(Context c, Activity a){
		this.c = c;
		this.a = a;
	}

	public String getAbsoluteImagePath(Uri uri) {
		String imagePath = "";
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = a.managedQuery(uri, proj, // Which columns to return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)

		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				imagePath = cursor.getString(column_index);
			}
		}

		return imagePath;
	}
	
	public Bitmap loadImgThumbnail(String imgName, int kind) {
		Bitmap bitmap = null;

		String[] proj = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DISPLAY_NAME };

		Cursor cursor = a.managedQuery(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj,
				MediaStore.Images.Media.DISPLAY_NAME + "='" + imgName + "'",
				null, null);

		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			ContentResolver crThumb = c.getContentResolver();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			bitmap = MediaStore.Images.Thumbnails.getThumbnail(crThumb,
					cursor.getInt(0), kind, options);
		}
		return bitmap;
	}
	
}
