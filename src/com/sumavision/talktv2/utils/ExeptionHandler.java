package com.sumavision.talktv2.utils;

import io.vov.utils.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

import com.sumavision.talktv2.data.OtherCacheData;

/**
 * 
 * @autExeptionHandler
 */
public class ExeptionHandler implements UncaughtExceptionHandler {
	public static final String TAG = "ExeptionHandler";
	private static ExeptionHandler INSTANCE = new ExeptionHandler();
	private Context mContext;
	@SuppressWarnings("unused")
	private Thread.UncaughtExceptionHandler mDefaultHandler;

	private ExeptionHandler() {

	}

	public static ExeptionHandler getInstance() {
		return INSTANCE;
	}

	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		System.out.println("uncaughtException+\n" + ex);

		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		ex.printStackTrace(pw);
		pw.close();
		String error = writer.toString();

		if (OtherCacheData.current().isDebugMode)
			Log.e("uncaughtException", error);
		// new Thread() {
		// @Override
		// public void run() {
		// Looper.prepare();
		// new AlertDialog.Builder(mContext)
		// .setIcon(R.drawable.icon_small)
		// .setTitle("电视粉温馨提示")
		// .setCancelable(false)
		// .setMessage("当前网页暂时无法播放，请尝试更换其他网页地址播放或稍后重试")
		// .setNeutralButton("知道了",
		// new android.content.DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog,
		// int which) {
		//
		// // ((MainWebPlayActivity) mContext)
		// // .sendUncaughtExceptionMessage();
		// // Thread.interrupted();
		// // System.exit(0);
		// }
		// }).create().show();
		// Looper.loop();
		// }
		// }.start();

		android.os.Process.killProcess(android.os.Process.myPid());

	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false
	 */
	@SuppressWarnings("unused")
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return true;
		}
		return true;
	}
}