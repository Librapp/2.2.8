package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.FansParser;
import com.sumavision.talktv2.net.FansRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 获取我的粉丝列表
 * @changLog
 */
public class GetMyFansTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "GetMyFansTask";
	private NetConnectionListener listener;
	private final String method = "fensiList";

	public GetMyFansTask(NetConnectionListener listener) {
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
		FansRequest request;
		FansParser parser;
		String data;
		context = (Context) params[0];
		request = (FansRequest) params[1];
		parser = (FansParser) params[2];
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
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "canceled");
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {
		listener.onNetEnd(result, method);
		super.onPostExecute(result);
	}
}
