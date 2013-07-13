package com.sumavision.talktv2.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.user.UserNow;

/**
 * 
 * @author @郭鹏
 * @date 2011-12-5
 * 
 */
public class InfomationHelper {

	public final static String FOLDERNAME = "TVFan/sendPic";
	public final static String SDCARD_MNT = "/mnt/sdcard";
	public final static String FILE_EXTENTION = ".jpg";
	public final static String SDCARD = "/sdcard";
	public static String SD_PATH = "";

	public static String getFileName() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SS");
		String fileName = format.format(new Timestamp(System
				.currentTimeMillis()));
		return fileName;
	}

	public static String getCamerPath() {
		return Environment.getExternalStorageDirectory() + "/TVFan/tempCamera/";
	}

	public static String getAbsolutePathFromNoStandardUri(Uri mUri) {
		String filePath = null;

		String mUriString = mUri.toString();
		mUriString = Uri.decode(mUriString);

		String pre1 = "file://" + SDCARD + File.separator;
		String pre2 = "file://" + SDCARD_MNT + File.separator;

		if (mUriString.startsWith(pre1)) {
			filePath = Environment.getExternalStorageDirectory().getPath()
					+ File.separator + mUriString.substring(pre1.length());
		} else if (mUriString.startsWith(pre2)) {
			filePath = Environment.getExternalStorageDirectory().getPath()
					+ File.separator + mUriString.substring(pre2.length());
		}
		return filePath;
	}

	public static boolean checkNetWork(Context context) {
		boolean newWorkOK = false;
		ConnectivityManager connectManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectManager.getActiveNetworkInfo() != null) {
			newWorkOK = true;
		}
		return newWorkOK;
	}

	public static Bitmap getScaleBitmap(Context context, String filePath) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, opts); // 此时返回bm为空
		opts.inJustDecodeBounds = false;
		int ratio = (int) (opts.outHeight / (float) 200);
		if (ratio <= 0)
			ratio = 1;

		opts.inSampleSize = ratio;
		bitmap = BitmapFactory.decodeFile(filePath, opts);

		if (FileInfoUtils.getFileSize(filePath) > JSONMessageType.PIC_SIZE_LIMITE) {
			savePic2SD(bitmap, filePath, JSONMessageType.USER_PIC_SDCARD_FOLDER);
		}
		return bitmap;
	}

	public static Bitmap getFinalScaleBitmap(Context context, String filePath) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, opts); // 此时返回bm为空
		opts.inJustDecodeBounds = false;
		// opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
		int ratio = (int) (opts.outHeight / (float) 200);
		if (ratio <= 0)
			ratio = 1;

		if (FileInfoUtils.getFileSize(filePath) > JSONMessageType.PIC_SIZE_LIMITE) {
			opts.inSampleSize = ratio;
		} else {
			opts.inSampleSize = 1;
		}

		bitmap = BitmapFactory.decodeFile(filePath, opts);
		SD_PATH = Environment.getExternalStorageDirectory() + File.separator
				+ FOLDERNAME + File.separator;

		String s = SD_PATH + getFileName() + FILE_EXTENTION;
		UserNow.current().bitmapPath = s;
		// savePic2SD(bitmap, s, SD_PATH);
		return bitmap;
	}

	public static Bitmap getFinalScaleBitmapForSohu(Context context,
			String filePath) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, opts);
		opts.inJustDecodeBounds = false;

		Long size = FileInfoUtils.getFileSize(filePath);
		int ratio = (int) (size / (float) 60000);
		if (ratio <= 0)
			ratio = 1;

		if (OtherCacheData.current().isDebugMode) {
			Log.e("Infohelper", size + "" + "\n" + ratio);
		}

		if (size > 120000) {
			opts.inSampleSize = ratio;
		} else {
			opts.inSampleSize = 1;
		}

		bitmap = BitmapFactory.decodeFile(filePath, opts);
		return bitmap;
	}

	public static Bitmap getFinalScaleBitmapBigPic(Context context,
			String filePath) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, opts); // 此时返回bm为空
		opts.inJustDecodeBounds = false;

		int ratio = (int) (opts.outHeight / (float) 200);
		if (ratio <= 0)
			ratio = 1;

		Log.e("InfomationHelper", ratio + "");

		if (FileInfoUtils.getFileSize(filePath) > JSONMessageType.PIC_SIZE_LIMITE) {
			opts.inSampleSize = ratio;
		} else {
			opts.inSampleSize = 3;
		}

		bitmap = BitmapFactory.decodeFile(filePath, opts);
		SD_PATH = Environment.getExternalStorageDirectory() + File.separator
				+ FOLDERNAME + File.separator;

		String s = SD_PATH + getFileName() + FILE_EXTENTION;
		UserNow.current().bitmapPath = s;

		return bitmap;
	}

	public static Bitmap getFinalScaleBitmapAndSave(Context context,
			String filePath) {
		BitmapFactory.Options opts = new BitmapFactory.Options();

		opts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, opts); // 此时返回bm为空
		opts.inJustDecodeBounds = false;

		int ratio = (int) (opts.outHeight / (float) 200);
		if (ratio <= 0)
			ratio = 1;

		if (FileInfoUtils.getFileSize(filePath) > JSONMessageType.PIC_SIZE_LIMITE) {
			opts.inSampleSize = ratio;
		} else {
			opts.inSampleSize = 1;
		}

		bitmap = BitmapFactory.decodeFile(filePath, opts);
		SD_PATH = Environment.getExternalStorageDirectory() + File.separator
				+ FOLDERNAME + File.separator;

		String s = SD_PATH + getFileName() + FILE_EXTENTION;
		UserNow.current().bitmapPath = s;
		savePic2SD(bitmap, s, SD_PATH);

		return bitmap;
	}

	public static void savePic2SD(Bitmap bitmap, String path, String folder) {

		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			File fileDir = new File(folder);
			if (!fileDir.exists()) {
				fileDir.mkdir();
			}
		}

		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(file);

			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

}
