package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.ProgramDetailNewNewRequest;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 获取节目详情信息
 * @changLog
 */
public class GetProgramDetailTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "GetProgramDetailTask";
	private NetConnectionListener listener;
	private final String method = "programDetail";

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		ProgramDetailNewNewRequest request;
		String data;
		context = (Context) params[0];
		listener = (NetConnectionListener) params[1];
		request = (ProgramDetailNewNewRequest) params[2];
		data = request.make();
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, data);
		listener.onNetBegin(method);
		String s = NetUtil.execute(context, data, null);

		return s;
	}

	@Override
	protected void onCancelled() {
		try {
			listener.onCancel(method);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
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
