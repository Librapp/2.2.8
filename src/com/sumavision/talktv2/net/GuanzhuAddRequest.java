package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;

/**
 * @author 李梦思
 * @version v2.0
 * @createTime 2012-6-14
 * @description 添加关注请求类
 * @changeLog
 */
public class GuanzhuAddRequest extends JSONRequest {

	private int userId;

	public GuanzhuAddRequest(int userId) {
		this.userId = userId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", Constants.guanZhuAdd);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("otherUserId", userId);
			holder.put("userId", UserNow.current().userID);
			holder.put("sessionId", UserNow.current().sessionID);
			holder.put("jsession", UserNow.current().jsession);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("GuanzhuAddRequest", holder.toString());
		}
		return holder.toString();
	}
}
