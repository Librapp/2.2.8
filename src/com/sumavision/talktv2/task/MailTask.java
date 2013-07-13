package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;

import com.sumavision.talktv2.net.JSONRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-14
 * @description 私信详情
 * @changLog
 */
public class MailTask extends AsyncTask<Object, Void, String> {
	private final NetConnectionListener listener;
	private final String method = "mailDetail";

	public MailTask(NetConnectionListener listener) {
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
		String data;
		context = (Context) params[0];
		request = (JSONRequest) params[1];
		data = request.make();

		String s = NetUtil.execute(context, data, null);
		if (s != null) {
			// String result = parser.parse(s);
			// return result;

			return s;
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
