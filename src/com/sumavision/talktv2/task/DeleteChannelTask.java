package com.sumavision.talktv2.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;

public class DeleteChannelTask extends AsyncTask<Object, Integer, Integer> {
	private NetConnectionListenerNew listener;
	private String method;
	private boolean isLoadMore;
	private String errMsg = null;
	private static final String TAG = "DeleteChannelTask";

	public DeleteChannelTask(NetConnectionListenerNew listener,
			boolean isLoadMore) {
		method = Constants.deleteChannelTask;
		this.listener = listener;
		this.isLoadMore = isLoadMore;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (listener != null) {
			listener.onNetBegin(method, isLoadMore);
		}
	}

	@Override
	protected Integer doInBackground(Object... params) {
		Context context = (Context) params[0];
		int userId = (Integer) params[1];
		int channelId = (Integer) params[2];
		String data = generateRequset(userId, channelId);
		if (data != null) {
			String result = NetUtil.execute(context, data, null);
			if (result == null) {
				return Constants.fail_no_net;
			} else {
				String msg = parse(result);
				if (msg == null) {
					return Constants.sucess;
				} else if ("parseErr".equals(msg)) {
					return Constants.parseErr;
				} else {
					errMsg = msg;
					return Constants.fail_server_err;
				}
			}
		} else {
			return Constants.requestErr;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (listener != null) {
			listener.onNetEnd(result, errMsg, method, isLoadMore);
		}
	}

	/**
	 * 
	 * @param userId
	 *            用户ID
	 * @param cpId
	 * 
	 */
	private String generateRequset(int userId, int channelId) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", method);
			jsonObject.put("version", JSONMessageType.APP_VERSION);
			jsonObject.put("client", JSONMessageType.SOURCE);
			jsonObject.put("jsession", UserNow.current().jsession);
			if (userId != 0)
				jsonObject.put("userId", userId);
			jsonObject.put("channelId", channelId);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		String returnValue = jsonObject.toString();
		Log.e(TAG, returnValue);
		return returnValue;
	}

	/**
	 * 
	 * @param list
	 * @param s
	 * @return
	 */
	private String parse(String s) {

		int errCode = 1;
		try {
			JSONObject jsonObject = new JSONObject(s);
			if (jsonObject.has("code")) {
				errCode = jsonObject.getInt("code");
			} else if (jsonObject.has("errcode")) {
				errCode = jsonObject.getInt("errcode");
			} else if (jsonObject.has("errorCode")) {
				errCode = jsonObject.getInt("errorCode");
			}
			if (jsonObject.has("jsession")) {
				UserNow.current().jsession = jsonObject.getString("jsession");
			}
			if (errCode != JSONMessageType.SERVER_CODE_OK) {
				return jsonObject.getString("msg");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return "parseErr";
		}
		return null;
	}
}
