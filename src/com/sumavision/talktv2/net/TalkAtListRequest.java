package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-17
 * @description 被@列表请求类
 * @changLog
 */
public class TalkAtListRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "talkAtList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("userId", UserNow.current().userID);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("TalkAtListRequest", holder.toString());
		return holder.toString();
	}
}
