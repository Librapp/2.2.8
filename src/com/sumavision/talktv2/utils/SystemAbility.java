package com.sumavision.talktv2.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;

/**
 * 
 * @author 郭鹏
 * @description 系统能力
 * 
 */

public class SystemAbility {

	public static String getAvailMemory(Context c) {

		ActivityManager am = (ActivityManager) c
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);

		return Formatter.formatFileSize(c, mi.availMem);
	}

	public static String getTotalMemory(Context c) {
		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();

			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
				Log.i(str2, num + "\t");
			}

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
			localBufferedReader.close();

		} catch (IOException e) {
		}
		return Formatter.formatFileSize(c, initial_memory);
	}

}
