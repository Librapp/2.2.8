package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.AppData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-24
 * @description 推荐软件解析类
 * @changeLog
 */
public class RecommendAppParser extends JSONParser {

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
				JSONObject content = jAData.getJSONObject("content");
				List<AppData> lr = new ArrayList<AppData>();
				if (content.has("recommendApp")) {
					JSONArray recommendApps = content
							.getJSONArray("recommendApp");
					for (int i = 0; i < recommendApps.length(); ++i) {
						AppData r = new AppData();
						JSONObject data = recommendApps.getJSONObject(i);
						// content.recommendApp[].id long 主键id
						// content.recommendApp[].name string 应用名称
						// content.recommendApp[].shortIntro string 应用简介
						// content.recommendApp[].pic string 应用图标绝对路径
						// content.recommendApp[].url string 应用下载链接地址
						r.id = data.getLong("id");
						r.name = data.getString("name");
						r.pic = data.getString("pic");
						r.url = data.getString("url");
						r.shortIntro = data.getString("shortIntro");
						lr.add(r);
					}
				}
				OtherCacheData.current().setApp(lr);
			} else
				msg = jAData.getString("msg");
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		return msg;
	}
}
