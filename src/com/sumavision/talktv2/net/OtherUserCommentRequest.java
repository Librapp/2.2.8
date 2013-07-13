package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.OtherCacheData;

public class OtherUserCommentRequest extends JSONRequest {
	private int userId;

	public OtherUserCommentRequest(int userId) {
		this.userId = userId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "userTalkList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			holder.put("userId", userId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return holder.toString();
	}
}
