package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-3-1
 * @description 账号绑定请求类
 * @changeLog
 */
public class BindAddRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "bindAdd");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("thirdUserId", SinaData.id);
			holder.put("thirdToken", SinaData.accessToken);
			holder.put("thirdType", 1);
			holder.put("jsession", UserNow.current().jsession);
			holder.put("userId", UserNow.current().userID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("BindAddRequest", holder.toString());
		}
		return holder.toString();
	}
}
