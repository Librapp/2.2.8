package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.RemindAddRequest;
import com.sumavision.talktv2.net.ResultParser;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 节目预约
 * @changLog
 */
public class RemindAddTask extends AsyncTask<Object, Void, String> {
	private final NetConnectionListener listener;
	private final String method = "remindAdd";

	public RemindAddTask(NetConnectionListener listener) {
		this.listener = listener;
	}

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		RemindAddRequest request;
		ResultParser parser;
		String data;
		context = (Context) params[0];
		request = (RemindAddRequest) params[1];
		parser = (ResultParser) params[2];
		data = request.make();
		if (OtherCacheData.current().isDebugMode)
			Log.e("RemindAddTask", data);
		String s = NetUtil.execute(context, data, null);
		String result = parser.parse(s);
		return result;
	}

	@Override
	protected void onCancelled() {
		listener.onCancel(method);
		if (OtherCacheData.current().isDebugMode)
			Log.e("RemindAddTask", "canceled");
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
			Log.e("RemindAddTask", "finished");
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		listener.onNetBegin(method);
		super.onPreExecute();
	}
}
