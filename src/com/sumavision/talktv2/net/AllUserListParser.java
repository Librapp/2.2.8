package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.EventData;
import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-27
 * @description 全部好友列表解析类
 * @changeLog jianghao deprecated
 */
public class AllUserListParser extends JSONParser {

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
				JSONObject content = jAData.getJSONObject("content");
				UserNow.current().eventCount = content.getInt("eventCount");
				JSONArray events = content.getJSONArray("event");
				List<EventData> le = new ArrayList<EventData>();
				for (int i = 0; i < events.length(); ++i) {
					JSONObject event = events.getJSONObject(i);
					EventData e = new EventData();
					// e.content = event.getString("preMsg");
					// e.time = event.getString("createTime");
					JSONObject user = event.optJSONObject("toObject");
					if (user != null) {
						User u = new User();
						u.userId = user.getInt("id");
						u.name = user.getString("name");
						u.iconURL = user.getString("photo");
						// u.level = user.getString("level");
						// e.user = u;
					}
					le.add(e);
				}
				UserNow.current().setAllEvent(le);
			} else
				msg = jAData.getString("msg");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
