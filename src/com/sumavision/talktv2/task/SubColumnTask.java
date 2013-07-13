package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.SubColumnListParser;
import com.sumavision.talktv2.net.SubColumnListRequest;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 专题页面
 * @changLog
 */
public class SubColumnTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "SubColumnTask";
	private NetConnectionListener listener;
	private final String method = "subColumnList";

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		SubColumnListRequest request;
		SubColumnListParser parser;
		String data;
		context = (Context) params[0];
		listener = (NetConnectionListener) params[1];
		request = (SubColumnListRequest) params[2];
		parser = (SubColumnListParser) params[3];
		data = request.make();
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, data);
		listener.onNetBegin(method);
		String s = NetUtil.execute(context, data, null);
		if (s != null) {
			if (OtherCacheData.current().isDebugMode)
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
		if (OtherCacheData.current().isDebugMode)
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
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "finished");
		super.onPostExecute(result);
	}
}
