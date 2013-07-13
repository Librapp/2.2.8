package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-9
 * @description 添加节目预订请求类
 * @changeLog
 */
public class RemindAddRequest extends JSONRequest {
	private int cpId;

	public RemindAddRequest(int cpId) {
		this.cpId = cpId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "remindAdd");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("cpId", cpId);
			holder.put("userId", UserNow.current().userID);
			holder.put("sessionId", UserNow.current().sessionID);
			holder.put("jsession", UserNow.current().jsession);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("RemindAddRequest", holder.toString());
		return holder.toString();
	}
}
