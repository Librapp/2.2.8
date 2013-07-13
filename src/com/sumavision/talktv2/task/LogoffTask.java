package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;

import com.sumavision.talktv2.net.LogoffParser;
import com.sumavision.talktv2.net.LogoffRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 注销
 * @changLog
 */
public class LogoffTask extends AsyncTask<Object, Void, String> {

	private final NetConnectionListener listener;
	private final String method = "cancel";

	public LogoffTask(NetConnectionListener listener) {
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
		LogoffRequest request;
		LogoffParser parser;
		String data;
		context = (Context) params[0];
		request = (LogoffRequest) params[1];
		parser = (LogoffParser) params[2];
		data = request.make();

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
