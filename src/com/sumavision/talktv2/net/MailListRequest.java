package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-6-14
 * @description 私信列表请求类
 * @changeLog 修改为2.2版本 by 李梦思 2012-12-24
 */
public class MailListRequest extends JSONRequest {

	private int userId;

	public MailListRequest(int userId) {
		this.userId = userId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "mailDetail");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			holder.put("userId", UserNow.current().userID);
			holder.put("otherUserId", userId);
			holder.put("jsession", UserNow.current().jsession);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("MailListRequest", holder.toString());
		}
		return holder.toString();
	}
}
