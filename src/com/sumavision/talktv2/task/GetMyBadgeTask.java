package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.net.BadgeParser;
import com.sumavision.talktv2.net.BadgeRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2013-1-3
 * @description 我的勋章
 * @changLog
 */
public class GetMyBadgeTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "GetMyBadgeTask";
	private final NetConnectionListener listener;
	private final String method = "badgeList";

	public GetMyBadgeTask(NetConnectionListener listener) {
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
		BadgeRequest request;
		BadgeParser parser;
		String data;
		context = (Context) params[0];
		request = (BadgeRequest) params[1];
		parser = (BadgeParser) params[2];
		data = request.make();
		Log.e(TAG, data);

		String s = NetUtil.execute(context, data, null);
		if (s != null) {
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
		Log.e(TAG, "finished");
		super.onPostExecute(result);
	}
}
