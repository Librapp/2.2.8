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
 * @createTime 2012-6-18
 * @description 私信用户列表请求类
 * @changeLog 修改为2.2版本 by 李梦思 2012-12-24
 */
public class MailBoxRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "mailUserList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			holder.put("userId", UserNow.current().userID);
			holder.put("jsession", UserNow.current().jsession);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("MailBoxRequest", holder.toString());
		}
		return holder.toString();
	}
}
