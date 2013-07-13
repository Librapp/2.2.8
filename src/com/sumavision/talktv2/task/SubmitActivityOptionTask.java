package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.JSONRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-2-25
 * @description 参加活动
 * @changLog
 */
public class SubmitActivityOptionTask extends AsyncTask<Object, Void, String> {
	private final NetConnectionListener listener;
	private final String method = "activityJoinOption";

	public SubmitActivityOptionTask(NetConnectionListener listener) {
		this.listener = listener;
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
		return s;
	}

	@Override
	protected void onCancelled() {
		listener.onCancel(method);
		if (OtherCacheData.current().isDebugMode)
			Log.e("SubmitActivityOptionTask", "canceled");
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			listener.onNetEnd(result, method);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		listener.onNetBegin(method);
		super.onPreExecute();
	}
}
