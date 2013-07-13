package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.ConvertToUnicode;

/**
 * @author 郭鹏
 * @version 2.2
 * @createTime 2012-6-1
 * @description 用户注册请求组装类
 * @changLog 2012-12-17 修改为2.2版本 by 李梦思
 */
public class RegisterRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("userName",
					ConvertToUnicode.AllStrTOUnicode(UserNow.current().name));
			holder.put("password", UserNow.current().passwd);
			holder.put("method", "register");
			holder.put("email", UserNow.current().eMail);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("jsession", UserNow.current().jsession);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return holder.toString();
	}
}
