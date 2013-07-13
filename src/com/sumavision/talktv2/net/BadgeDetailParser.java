package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.BadgeDetailData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 姜浩
 * @version 2.2.4
 * @createTime 2012-12-26
 * @description 徽章详情解析
 * @changeLog
 */
public class BadgeDetailParser {

	public String parse(String s, BadgeDetailData badgeDetailData) {
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
				JSONObject content = jAData.getJSONObject("content");
				badgeDetailData.id = content.optInt("id");
				badgeDetailData.name = content.optString("name");
				badgeDetailData.pic = content.optString("pic");
				badgeDetailData.intro = content.optString("intro");
				badgeDetailData.getCount = content.optInt("getCount");
				badgeDetailData.getTime = content.optInt("getTime");
			} else
				msg = jAData.getString("msg");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return msg;
	}
}
