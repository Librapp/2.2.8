package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.0
 * @createTime 2012-7-5
 * @description 用户反馈解析类
 * @changeLog
 */
public class FeedbackParser extends JSONParser {

	@Override
	public String parse(String s) {
		JSONObject jAData = null;
		String msg = "";
		int errCode = -1;

		try {
			jAData = new JSONObject(s);
			if (jAData.has("code")) {
				errCode = jAData.getInt("code");
			} else if (jAData.has("errcode")) {
				errCode = jAData.getInt("errcode");
			}

			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode != JSONMessageType.SERVER_CODE_OK)
				msg = jAData.getString("msg");
			else {
				if (jAData.has("newUserInfo")) {
					JSONObject info = jAData.getJSONObject("newUserInfo");
					UserNow.current().getPoint = info.getInt("point");
					UserNow.current().point = info.getInt("totalPoint");
					UserNow.current().getExp = info.getInt("exp");
					UserNow.current().exp = info.getInt("totalExp");
					UserNow.current().lvlUp = info.getInt("changeLevel");
					UserNow.current().level = info.getString("level");
				}
			}
			UserNow.current().isTimeOut = false;
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			UserNow.current().isTimeOut = true;
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
