package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.net.JSONParser;
import com.sumavision.talktv2.net.JSONRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-11
 * @description 第三方账号登录
 * @changLog
 */
public class BindAccountTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "BindAccountTask";
	private final NetConnectionListener listener;
	private final String method = "bindUser";

	public BindAccountTask(NetConnectionListener listener) {
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
		JSONRequest request;
		JSONParser parser;
		String data;
		context = (Context) params[0];
		request = (JSONRequest) params[1];
		parser = (JSONParser) params[2];
		data = request.make();
		Log.e(TAG, data);

		String s = NetUtil.execute(context, data, null);
		if (s != null) {
			String result = parser.parse(s);
			return result;
		} else {
			return null;
		}
	}

	@Override
	protected void onCancelled() {
		listener.onCancel(method);
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

		super.onPostExecute(result);
	}
}
