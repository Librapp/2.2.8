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
 * @description 解除绑定请求类
 * @changeLog
 */
public class BindDeleteRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "bindDelete");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("id", UserNow.current().thirdUserId);
			holder.put("jsession", UserNow.current().jsession);
			holder.put("userId", UserNow.current().userID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("BindDeleteRequest", holder.toString());
		}
		return holder.toString();
	}
}
