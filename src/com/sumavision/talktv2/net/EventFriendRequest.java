package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

public class EventFriendRequest extends JSONRequest {
	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "eventFriendList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			holder.put("jsession", UserNow.current().jsession);
			holder.put("userId", UserNow.current().userID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return holder.toString();
	}
}
