package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-9
 * @description 删除节目预订请求类
 * @changeLog
 */
public class RemindDeleteRequest extends JSONRequest {
	private long cpId;

	public RemindDeleteRequest(long cpId) {
		this.cpId = cpId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "remindDelete");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("cpIds", cpId);
			holder.put("userId", UserNow.current().userID);
			holder.put("sessionId", UserNow.current().sessionID);
			holder.put("jsession", UserNow.current().jsession);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("RemindDeleteRequest", holder.toString());
		return holder.toString();
	}
}
