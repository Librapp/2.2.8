package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.ProgramAroundData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.0
 * @createTime 2012-6-6
 * @description 节目周边信息详情解析类
 * @changeLog 改为2.2版本 by 李梦思 2013-1-5
 */
public class ProgramAroundParser extends JSONParser {

	@Override
	public String parse(String s) {
		JSONObject jAData = null;
		JSONObject content = null;
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
				content = jAData.getJSONObject("content");
				ProgramAroundData.current().time = content.getString("pubDate");
				ProgramAroundData.current().content = content
						.getString("content");
				ProgramAroundData.current().title = content.getString("title");
				ProgramAroundData.current().detailPhoto = content
						.getString("photo");
				ProgramAroundData.current().url = content.getString("url");
				ProgramAroundData.current().summary = content
						.getString("summary");
				ProgramAroundData.current().source = content
						.getString("source");
				ProgramAroundData.current().photo = content.getString("photo");
			} else {
				msg = jAData.getString("msg");
			}
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
