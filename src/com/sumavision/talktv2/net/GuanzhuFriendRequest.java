package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.net.JSONMessageType;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-26
 * @description 好友页面关注列表请求类
 * @changeLog
 */
public class GuanzhuFriendRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "guanZhuFriendList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			holder.put("sessionId", UserNow.current().sessionID);
			holder.put("jsession", UserNow.current().jsession);
			holder.put("userId", UserNow.current().userID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("FriendRequest", holder.toString());
		}
		return holder.toString();
	}
}
