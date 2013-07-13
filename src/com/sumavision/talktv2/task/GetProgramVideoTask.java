package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.ProgramVideoListRequest;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 节目页视频标签内容
 * @changLog
 */
public class GetProgramVideoTask extends AsyncTask<Object, Void, String> {

	private final String TAG = "GetProgramVideoTask";
	private NetConnectionListener listener;
	private final String method = "programVideoList";

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		ProgramVideoListRequest request;
		String data;
		context = (Context) params[0];
		listener = (NetConnectionListener) params[1];
		request = (ProgramVideoListRequest) params[2];
		data = request.make();
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, data);
		listener.onNetBegin(method);
		String s = NetUtil.execute(context, data, null);
		return s;
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
