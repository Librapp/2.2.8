package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;

import com.sumavision.talktv2.net.HotPlayParser;
import com.sumavision.talktv2.net.HotPlayRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2013-1-10
 * @description 热门搜索节目
 * @changLog
 */
public class HotPlayTask extends AsyncTask<Object, Void, String> {
	private final NetConnectionListener listener;
	private final String method = "hotPlay";

	public HotPlayTask(NetConnectionListener listener) {
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
		HotPlayRequest request;
		HotPlayParser parser;
		String data;
		context = (Context) params[0];
		request = (HotPlayRequest) params[1];
		parser = (HotPlayParser) params[2];
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
