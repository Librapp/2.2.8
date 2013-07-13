package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-13
 * @description 被回复列表请求类
 * @changLog
 */
public class ReplyByListRequest extends JSONRequest {
	int cpId;

	public ReplyByListRequest(int cpId) {
		this.cpId = cpId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "replyByList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("jsession", UserNow.current().jsession);
			holder.put("userId", UserNow.current().userID);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			if (cpId != 0)
				holder.put("cpid", cpId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("ReplyByListRequest", holder.toString());
		return holder.toString();
	}
}
