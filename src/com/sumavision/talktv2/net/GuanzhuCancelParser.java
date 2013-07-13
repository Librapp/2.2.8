package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.net.JSONMessageType;

/**
 * @author 李梦思
 * @version v2.0
 * @createTime 2012-6-14
 * @description 取消关注解析类
 * @changeLog
 */
public class GuanzhuCancelParser extends JSONParser {

	private SharedPreferences sp;

	public GuanzhuCancelParser(Context c) {
		sp = c.getSharedPreferences("userInfo", 0);
	}

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
			} else if (jAData.has("errorCode")) {
				errCode = jAData.getInt("errorCode");
			}
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				if (jAData.has("newUserInfo")) {
					JSONObject info = jAData.getJSONObject("newUserInfo");
					UserNow.current().getPoint = info.getInt("point");
					UserNow.current().point = info.getInt("totalPoint");
					UserNow.current().getExp = info.getInt("exp");
					UserNow.current().exp = info.getInt("totalExp");
					UserNow.current().lvlUp = info.getInt("changeLevel");
					UserNow.current().level = info.getString("level");
				}

				UserNow.current().fansCount = jAData.getInt("friendCount");
				Editor ed = sp.edit();
				ed.putInt("fansCount", UserNow.current().fansCount);
				ed.commit();
			} else
				msg = jAData.getString("msg");
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
