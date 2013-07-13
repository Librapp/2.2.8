package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.ConvertToUnicode;

/**
 * 
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-9
 * @description 绑定登录请求组装类
 * @changLog
 */
public class BindUserRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {

			holder.put("method", "bindUser");
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("jsession", UserNow.current().jsession);

			holder.put("thirdType", UserNow.current().thirdType);
			holder.put("thirdToken", UserNow.current().thirdToken);
			holder.put("thirdUserId", SinaData.id);
			if (!TextUtils.isEmpty(UserNow.current().name)) {
				holder.put("userName", ConvertToUnicode.AllStrTOUnicode(UserNow
						.current().name));
				holder.put("userType", UserNow.current().userType);
				holder.put("password", UserNow.current().passwd);
			}
			if (!TextUtils.isEmpty(UserNow.current().eMail))
				holder.put("email", UserNow.current().eMail);
			if (!TextUtils.isEmpty(UserNow.current().thirdUserPic))
				holder.put("thirdUserPic", UserNow.current().thirdUserPic);
			if (!TextUtils.isEmpty(UserNow.current().thirdSignature))
				holder.put("thirdSignature", UserNow.current().thirdSignature);
			if (!TextUtils.isEmpty(UserNow.current().validTime))
				holder.put("validTime", UserNow.current().validTime);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("BindUserRequest", holder.toString());
		}
		return holder.toString();
	}
}
