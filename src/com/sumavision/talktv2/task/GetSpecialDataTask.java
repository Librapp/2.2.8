package com.sumavision.talktv2.task;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.ColumnData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.net.SubColumnListRequest;
import com.sumavision.talktv2.user.UserNow;

public class GetSpecialDataTask extends AsyncTask<Object, Integer, String> {
	private final static String TAG = "GetSpecialDataTask";
	private final NetConnectionListener listener;
	private final String method = "subColumnList";

	public GetSpecialDataTask(NetConnectionListener listener) {
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
		SubColumnListRequest request;
		String data;
		context = (Context) params[0];
		request = (SubColumnListRequest) params[1];
		@SuppressWarnings("unchecked")
		ArrayList<ColumnData> list = (ArrayList<ColumnData>) params[2];
		data = request.make();
		Log.e(TAG, data);

		String s = NetUtil.execute(context, data, null);
		if (s != null) {
			String result = parse(s, list);
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

	private String parse(String s, ArrayList<ColumnData> list) {
		JSONObject jAData = null;
		String msg = "";
		int errCode = -1;

		try {
			jAData = new JSONObject(s);
			if (jAData.has("code")) {
				errCode = jAData.getInt("code");
			} else if (jAData.has("errcode")) {
				errCode = jAData.getInt("errcode");
			}
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				JSONObject room = jAData.getJSONObject("content");
				JSONArray column = room.getJSONArray("subColumn");
				for (int j = 0; j < column.length(); ++j) {
					ColumnData sub = new ColumnData();
					JSONObject subColumn = column.getJSONObject(j);
					sub.id = subColumn.getInt("id");
					sub.name = subColumn.getString("name");
					sub.type = subColumn.getInt("type");
					sub.pic = subColumn.getString("pic");
					sub.intro = subColumn.getString("shortIntro");
					sub.playTimes = subColumn.getInt("playTimes");
					list.add(sub);
				}
			} else
				msg = jAData.getString("msg");
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		return msg;
	}
}
