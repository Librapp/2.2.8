package com.sumavision.talktv2.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-7
 * @description 热搜词解析类
 * @changeLog
 */
public class HotSearchParser extends JSONParser {

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
			if (errCode == JSONMessageType.SERVER_CODE_OK) {

				JSONObject room = jAData.getJSONObject("content");

				OtherCacheData.current().keywordsCount = room
						.getInt("keywordsCount");
				if (OtherCacheData.current().keywordsCount > 0) {
					JSONArray playName = room.getJSONArray("keywords");
					OtherCacheData.current().keywords = new String[playName
							.length()];
					for (int i = 0; i < playName.length(); i++) {
						OtherCacheData.current().keywords[i] = playName
								.getString(i);
					}
				}
			} else
				msg = jAData.getString("msg");
			UserNow.current().isTimeOut = false;
		} catch (JSONException e) {
			UserNow.current().isTimeOut = true;
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
