package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.ScreenShotData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.ScreenShotParser;
import com.sumavision.talktv2.net.ScreenShotRequest;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-5
 * @description 截屏
 * @changLog
 */
public class ScreenShotTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "ScreenShotTask";
	private final NetConnectionListener listener;
	private final String method = "screenShot";

	public ScreenShotTask(NetConnectionListener listener) {
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {
		listener.onNetBegin(method);
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		ScreenShotRequest request;
		ScreenShotParser parser;
		String data;
		context = (Context) params[0];
		request = (ScreenShotRequest) params[1];
		parser = (ScreenShotParser) params[2];
		data = request.make();
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, data);

		String s = NetUtil.execute(context, data, ScreenShotData.address);
		if (s != null) {
			if (OtherCacheData.current().isDebugMode)
				Log.e(TAG, s);
			String result = parser.parse(s);
			return result;
		} else {
			return null;
		}
	}

	@Override
	protected void onCancelled() {
		listener.onCancel(method);
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "canceled");
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			listener.onNetEnd(result, method);
		} catch (NullPointerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "finished");
		super.onPostExecute(result);
	}
}
