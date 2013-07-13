package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.net.JSONMessageType;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-7
 * @description 预约列表请求类
 * @changeLog
 */
public class RemindRequest extends JSONRequest {

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
			if (UserNow.current().isSelf)
				if (UserNow.current().userID != 0) {
					holder.put("userId", UserNow.current().userID);
				} else if (UserNow.current().userID != 0) {
					holder.put("userId", UserNow.current().userID);
				}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("RemindRequest", holder.toString());
		}
		return holder.toString();
	}
}
