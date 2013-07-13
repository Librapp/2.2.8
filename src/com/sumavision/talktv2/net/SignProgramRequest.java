package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.SignData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-6-14
 * @description 签到节目请求类
 * @changeLog 修改为2.2版本 by 李梦思 2013-1-4
 */
public class SignProgramRequest extends JSONRequest {
	int id;

	public SignProgramRequest(int id) {
		this.id = id;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "programSign");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("userId", UserNow.current().userID);
			holder.put("programId", id);
			holder.put("content", SignData.current().content);
			holder.put("sessionId", UserNow.current().sessionID);
			holder.put("jsession", UserNow.current().jsession);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("SignProgramRequest", holder.toString());
		return holder.toString();
	}
}
