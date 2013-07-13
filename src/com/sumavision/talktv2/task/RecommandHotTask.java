package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;

import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.RecommendProgramListParser;
import com.sumavision.talktv2.net.RecommendProgramListRequest;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 热播列表
 * @changLog
 */
public class RecommandHotTask extends AsyncTask<Object, Void, String> {

	private NetConnectionListener listener;
	private final String method = "hotProgramList";

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		RecommendProgramListRequest request;
		RecommendProgramListParser parser;
		String data;
		context = (Context) params[0];
		listener = (NetConnectionListener) params[1];
		request = (RecommendProgramListRequest) params[2];
		parser = (RecommendProgramListParser) params[3];
		data = request.make();
		listener.onNetBegin(method);
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
