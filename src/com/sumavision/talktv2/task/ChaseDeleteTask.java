package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.ChaseDeleteRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.ResultParser;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 取消追剧
 * @changLog
 */
public class ChaseDeleteTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "ChaseDeleteTask";
	private final NetConnectionListener listener;
	private final String method = "chaseDelete";

	public ChaseDeleteTask(NetConnectionListener listener) {
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
		ChaseDeleteRequest request;
		ResultParser parser;
		String data;
		context = (Context) params[0];
		request = (ChaseDeleteRequest) params[1];
		parser = (ResultParser) params[2];
		data = request.make();

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
