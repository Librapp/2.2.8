package com.sumavision.talktv2.utils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class AppUtil {
	public static String getAppVersionId(Context context) {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(),
					0);
			return packInfo.versionName;
		} catch (NameNotFoundException e) {
			return null;
		}

	}

	public static boolean isSystemUpdateServiceRunning(Context context,
			String serviceName) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> serviceInfo = activityManager
				.getRunningServices(100);
		for (RunningServiceInfo info : serviceInfo) {
			String name = info.service.getClassName();
			if (name.equals(serviceName)) {
				// serviceName example
				// "com.sumavision.talktv.services.SystemUpdateService"
				return true;
			}
		}
		return false;

	}

	public static boolean isNotifyServiceRunning(Context context,
			String serviceName) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> serviceInfo = activityManager
				.getRunningServices(100);
		for (RunningServiceInfo info : serviceInfo) {
			String name = info.service.getClassName();
			if (name.equals(serviceName)) {
				return true;
			}
		}
		return false;

	}

	public static String getPackageName(Context context) {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(),
					0);
			return packInfo.packageName;
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	public static boolean hasPlugin(Activity c, String packageName) {
		PackageInfo pi;
		try {
			pi = c.getPackageManager().getPackageInfo(packageName, 0);
			// pi =
			// c.getPackageManager().getPackageInfo("com.sumavision.talktv2",
			// 0);
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.setPackage(pi.packageName);
			PackageManager pManager = c.getPackageManager();
			List<ResolveInfo> apps = pManager.queryIntentActivities(
					resolveIntent, 0);

			Log.e("AppUtils", "step - 1" + packageName);

			ResolveInfo ri = null;
			try {
				Log.e("AppUtils", "step - 2" + packageName);
				ri = apps.iterator().next();
			} catch (NoSuchElementException e) {
				Log.e("AppUtils", "step - 3" + packageName);
				return false;
			}
			if (ri != null) {
				Log.e("AppUtils", "step - 4" + packageName);
				return true;
			} else {
				Log.e("AppUtils", "step - 5" + packageName);
				return false;
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Log.e("AppUtils", "step - 6");
			return false;
		}
	}

	public static boolean isEmail(String strEmail) {
		String strPattern = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(strEmail);
		return m.matches();
	}

	public static boolean isUserName(String userName) {
		String strPattern = "^([u4e00-u9fa5]|[ufe30-uffa0]|[a-zA-Z0-9_]){3,12}$";

		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(userName);
		return m.matches();
	}
}
