package com.sumavision.talktv2.task;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.ConvertToUnicode;

public class SearchTask extends AsyncTask<Object, Integer, Integer> {

	private NetConnectionListenerNew listener;
	private String method;
	private boolean isLoadMore;
	private String errMsg = null;

	public SearchTask(NetConnectionListenerNew listener, boolean isLoadMore) {
		method = Constants.searchProgram;
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

		int first = (Integer) params[1];
		int count = (Integer) params[2];
		String keyWord = (String) params[3];
		String data = generateRequset(first, count, keyWord);
		if (data != null) {
			String result = NetUtil.execute(context, data, null);
			if (result == null) {
				return Constants.fail_no_net;
			} else {
				@SuppressWarnings("unchecked")
				ArrayList<VodProgramData> list = (ArrayList<VodProgramData>) params[4];
				String msg = parse(list, result);
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
	 * @param first
	 *            开始位置
	 * @param count
	 *            个数
	 * @return 生成的JSON字段
	 */
	private String generateRequset(int first, int count, String searchKeyWords) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", method);
			jsonObject.put("version", JSONMessageType.APP_VERSION);
			jsonObject.put("client", JSONMessageType.SOURCE);
			jsonObject.put("jsession", UserNow.current().jsession);
			jsonObject.put("keyword",
					ConvertToUnicode.AllStrTOUnicode(searchKeyWords));
			jsonObject.put("first", first);
			jsonObject.put("count", count);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		String returnValue = jsonObject.toString();
		return returnValue;
	}

	/**
	 * 
	 * @param list
	 * @param s
	 * @return
	 */
	private String parse(ArrayList<VodProgramData> lp, String s) {

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
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				if (jsonObject.has("content")) {
					JSONObject content = jsonObject.getJSONObject("content");
					if (content.has("program")) {
						JSONArray plays = content.getJSONArray("program");
						for (int i = 0; i < plays.length(); i++) {
							JSONObject o = plays.getJSONObject(i);
							VodProgramData vpd = new VodProgramData();
							vpd.name = o.getString("name");
							vpd.id = o.getString("id");
							vpd.topicId = o.getString("topicId");
							vpd.pic = o.getString("pic");
							lp.add(vpd);
						}
					}
				}
			} else {
				return jsonObject.getString("msg");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return "parseErr";
		}
		return null;
	}

}
