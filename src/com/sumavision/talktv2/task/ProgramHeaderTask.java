package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;

import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.ProgramHeadRequest;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 节目页上部信息
 * @changLog
 */
public class ProgramHeaderTask extends AsyncTask<Object, Void, String> {
	private NetConnectionListener listener;
	private final String method = "programHead";

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		ProgramHeadRequest request;
		String data;
		context = (Context) params[0];
		listener = (NetConnectionListener) params[1];
		request = (ProgramHeadRequest) params[2];
		data = request.make();
		listener.onNetBegin(method);
		String s = NetUtil.execute(context, data, null);
		if (s != null) {
			// String result = parser.parse(s);
			// return result;

			return s;
		} else {
			return null;
		}
	}

	@Override
	protected void onCancelled() {
		if (listener != null)
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
