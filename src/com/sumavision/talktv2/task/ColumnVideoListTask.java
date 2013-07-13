package com.sumavision.talktv2.task;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.ColumnData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 推荐页的2 3 4 标签
 * @changLog
 */
public class ColumnVideoListTask extends AsyncTask<Object, Void, String> {

	private final String method = "columnVideoList";
	private final String TAG = "ColumnVideoListTask";
	private NetConnectionListener listener;
	private int listType;

	private ArrayList<VodProgramData> list;

	@SuppressWarnings("unchecked")
	@Override
	protected String doInBackground(Object... params) {
		Context context;
		int id;
		int offset;
		int count;
		String data;
		context = (Context) params[0];
		listener = (NetConnectionListener) params[1];
		list = (ArrayList<VodProgramData>) params[2];
		id = (Integer) params[3];
		offset = (Integer) params[4];
		count = (Integer) params[5];
		listType = (Integer) params[6];
		data = make(id, offset, count);
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
		String s = null;
		if (result != null) {
			if (OtherCacheData.current().isDebugMode)
				Log.e(TAG, result);
			s = parse(result);
		}

		try {
			listener.onNetEnd(s, method, listType);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "finished");
		super.onPostExecute(result);
	}

	private String make(int id, int offset, int count) {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", method);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("columnId", id);
			holder.put("first", offset);
			holder.put("count", count);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("ColumnVideoRequest", holder.toString());
		return holder.toString();
	}

	protected String parse(String s) {
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

				ColumnData.current.programCount = room
						.getInt("columnVideoCount");
				if (ColumnData.current.programCount > 0) {
					JSONArray liveProgram = room.getJSONArray("columnVideo");
					list.clear();
					for (int i = 0; i < liveProgram.length(); i++) {
						VodProgramData r = new VodProgramData();
						JSONObject data = liveProgram.getJSONObject(i);
						r.id = data.getString("id");
						r.topicId = data.getString("topicId");
						r.name = data.getString("name");
						r.shortIntro = data.getString("shortIntro");
						r.playTimes = data.getInt("playTimes");
						r.pic = data.getString("pic");
						double tempPoint = data.getDouble("doubanPoint");
						if (tempPoint > 1.0) {
							r.point = String.valueOf(tempPoint);
						}
						r.updateName = data.getString("updateName");
						r.playType = data.getInt("playType");
						r.playUrl = data.getString("playUrl");
						list.add(r);
					}
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
