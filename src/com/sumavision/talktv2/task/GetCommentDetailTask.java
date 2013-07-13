package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.JSONParser;
import com.sumavision.talktv2.net.JSONRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-18
 * @description 取评论详情
 * @changLog
 */
public class GetCommentDetailTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "GetCommentDetailTask";
	private NetConnectionListener listener;
	private final String method = "talkDetail";

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		JSONRequest request;
		JSONParser parser;
		String data;
		context = (Context) params[0];
		listener = (NetConnectionListener) params[1];
		request = (JSONRequest) params[2];
		parser = (JSONParser) params[3];
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
