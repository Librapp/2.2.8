package com.sumavision.talktv2.task;

import android.content.Context;
import android.os.AsyncTask;

import com.sumavision.talktv2.data.BadgeDetailData;
import com.sumavision.talktv2.net.BadgeDetailParser;
import com.sumavision.talktv2.net.BadgeDetailRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;

/**
 * @author 姜浩
 * @version 2.2.4
 * @createTime 2012-12-26
 * @description 徽章详情task 需要传入request parser temp badgeData
 * @changeLog
 */
public class GetBadgeDetailTask extends AsyncTask<Object, Void, String> {
	private final NetConnectionListener listener;
	private String method;

	public GetBadgeDetailTask(NetConnectionListener listener, String method) {
		this.listener = listener;
		this.method = method;
	}

	@Override
	protected void onPreExecute() {
		listener.onNetBegin(method);
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		BadgeDetailRequest request;
		BadgeDetailParser parser;
		String data;
		context = (Context) params[0];
		request = (BadgeDetailRequest) params[1];
		parser = (BadgeDetailParser) params[2];
		BadgeDetailData badgeDetailData = (BadgeDetailData) params[3];
		data = request.make();

		String s = NetUtil.execute(context, data, null);
		if (s != null) {
			String result = parser.parse(s, badgeDetailData);
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
		listener.onNetEnd(result, method);
		super.onPostExecute(result);
	}
}
