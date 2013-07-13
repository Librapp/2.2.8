package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.GuanzhuFriendParser;
import com.sumavision.talktv2.net.GuanzhuFriendRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 我的关注 好友页面的第一标签
 * @changLog
 */
public class GetGuanzhuFriendListTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "GetGuanzhuFriendListTask";
	private final NetConnectionListener listener;
	private final String method = "guanZhuFriendList";

	public GetGuanzhuFriendListTask(NetConnectionListener listener) {
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
		GuanzhuFriendRequest request;
		GuanzhuFriendParser parser;
		String data;
		context = (Context) params[0];
		request = (GuanzhuFriendRequest) params[1];
		parser = (GuanzhuFriendParser) params[2];
		data = request.make();
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, data);

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
