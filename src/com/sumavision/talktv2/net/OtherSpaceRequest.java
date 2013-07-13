package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-28
 * @description 其他中心请求类
 * @changeLog
 */
public class OtherSpaceRequest extends JSONRequest {

	private int userId;

	public OtherSpaceRequest(int userId) {
		this.userId = userId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "userSpace");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			if (UserNow.current().userID != 0)
				holder.put("userId", UserNow.current().userID);
			holder.put("otherUserId", userId);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("OtherSpaceRequest", holder.toString());
		}
		return holder.toString();
	}
}
