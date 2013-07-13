package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.SearchProgramParser;
import com.sumavision.talktv2.net.SearchProgramRequest;
import com.sumavision.talktv2.utils.Constants;

/**
 * @author 郭鹏
 * @version 2.2
 * @createTime 2013-1-13
 * @description 搜索节目
 * @changLog
 */
public class SearchProgramTask extends AsyncTask<Object, Void, String> {

	private final String TAG = "SearchProgramTask";
	private final NetConnectionListener listener;
	private final String method = Constants.searchProgram;

	public SearchProgramTask(NetConnectionListener listener) {
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
		SearchProgramRequest request;
		SearchProgramParser parser;
		String data;
		context = (Context) params[0];
		request = (SearchProgramRequest) params[1];
		parser = (SearchProgramParser) params[2];
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
