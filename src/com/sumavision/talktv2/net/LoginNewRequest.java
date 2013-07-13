package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.ConvertToUnicode;

/**
 * 
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-11
 * @description 用户登录请求组装类
 * @changLog
 */
public class LoginNewRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("userName",
					ConvertToUnicode.AllStrTOUnicode(UserNow.current().name));
			holder.put("password", UserNow.current().passwd);
			holder.put("method", "login");
			holder.put("infoFlag", UserNow.current().infoFlag);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("jsession", UserNow.current().jsession);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("LoginNewRequest", holder.toString());
		}
		return holder.toString();
	}
}
