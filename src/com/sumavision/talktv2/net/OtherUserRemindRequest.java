package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-1-15
 * @description 其他用户预定请求
 * @changeLog
 */

public class OtherUserRemindRequest extends JSONRequest {
	private int userId;

	public OtherUserRemindRequest(int userId) {
		this.userId = userId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "remindList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			holder.put("sessionId", UserNow.current().sessionID);
			holder.put("jsession", UserNow.current().jsession);
			holder.put("userId", userId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("RemindRequest", holder.toString());
		}
		return holder.toString();
	}
}
