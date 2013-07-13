package com.sumavision.talktv2.net;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.PlayNewData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-25
 * @description 活动列表解析类
 * @changeLog
 */
public class ActivityListParser {

	public String parse(String s, List<PlayNewData> list) {
		JSONObject jAData = null;
		String msg = "";
		int errCode = -1;

		try {
			jAData = new JSONObject(s);
			if (jAData.has("code")) {
				errCode = jAData.getInt("code");
			}
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				JSONObject content = jAData.getJSONObject("content");
				JSONArray activities = content.getJSONArray("activity");
				for (int i = 0; i < activities.length(); ++i) {
					PlayNewData a = new PlayNewData();
					JSONObject activity = activities.getJSONObject(i);
					a.id = activity.getInt("id");
					a.name = activity.getString("name");
					a.typeId = activity.getInt("typeId");
					a.typeName = activity.getString("typeName");
					a.intro = activity.getString("shortIntro");
					a.pic = activity.getString("pic");
					a.state = activity.getInt("status");
					a.joinStatus = activity.getInt("joinStatus");
					list.add(a);
				}
			} else {
				msg = jAData.getString("msg");
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
