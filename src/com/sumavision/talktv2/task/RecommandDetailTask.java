package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;

import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.RecommendPageParser;
import com.sumavision.talktv2.net.RecommendPageRequest;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 推荐页
 * @changLog
 */
public class RecommandDetailTask extends AsyncTask<Object, Void, String> {
	private NetConnectionListener listener;
	private final String method = "recommendDetail";

	public RecommandDetailTask(NetConnectionListener listener) {
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		listener.onNetBegin(method);
	}

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		RecommendPageRequest request;
		RecommendPageParser parser;
		String data;
		context = (Context) params[0];
		request = (RecommendPageRequest) params[1];
		parser = (RecommendPageParser) params[2];
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
