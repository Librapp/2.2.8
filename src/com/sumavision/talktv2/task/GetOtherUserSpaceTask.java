package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.OtherSpaceRequest;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 获取其他人信息
 * @changLog
 */
public class GetOtherUserSpaceTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "GetOtherUserSpaceTask";
	private final NetConnectionListener listener;
	private final String method = "userSpace";

	public GetOtherUserSpaceTask(NetConnectionListener listener) {
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
		OtherSpaceRequest request;
		String data;
		context = (Context) params[0];
		request = (OtherSpaceRequest) params[1];
		data = request.make();
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, data);

		String s = NetUtil.execute(context, data, null);
		if (s != null) {
			if (OtherCacheData.current().isDebugMode)
				Log.e(TAG, s);
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
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "canceled");
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {

		try {
			listener.onNetEnd(result, method);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "finished");
		super.onPostExecute(result);
	}
}
