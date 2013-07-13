package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserModify;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.ConvertToUnicode;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-6-7
 * @description 用户资料更新请求
 * @changeLog 2012-12-17 改为2.2版本 by 李梦思
 */
public class UserUpdateRequest extends JSONRequest {
	@Override
	public String make() {

		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "userUpdate");
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("userId", UserNow.current().userID);
			holder.put("sessionId", UserNow.current().sessionID);
			if (UserModify.current().passwdNewFlag == 1) {
				holder.put("password", UserModify.current().passwdNew);
				holder.put("oldPassword", UserModify.current().passwdOld);
			}
			if (UserModify.current().nameNewFlag == 1)
				holder.put("userName", ConvertToUnicode
						.AllStrTOUnicode(UserModify.current().nameNew));
			if (UserModify.current().signFlag == 1)
				holder.put("signature", ConvertToUnicode
						.AllStrTOUnicode(UserModify.current().sign));
			if (UserModify.current().genderFlag == 1)
				holder.put("sex", UserModify.current().gender);
			if (UserModify.current().picFlag == 1)
				holder.put("pic", UserModify.current().pic_Base64);
			holder.put("jsession", UserNow.current().jsession);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("UserUpdateRequest", holder.toString());
		}
		return holder.toString();
	}
}
