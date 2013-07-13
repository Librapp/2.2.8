package com.sumavision.talktv2.net;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.EventData;
import com.sumavision.talktv2.data.MainPageData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author jianghao
 * @version v2.2
 * @createTime 2012-1-14
 * @description 查询大厅事件解析 2.2版本好友第三个标签
 * @changeLog
 */
public class EventRoomParser extends JSONParser {
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
				ArrayList<EventData> list = new ArrayList<EventData>();
				JSONArray events = content.optJSONArray("event");
				if (events != null) {
					for (int i = 0; i < events.length(); i++) {
						EventData temp = new EventData();
						JSONObject event = events.getJSONObject(i);
						temp.id = event.getInt("id");
						temp.createTime = event.getString("createTime");
						temp.preMsg = event.getString("preMsg");

						JSONObject user = event.getJSONObject("user");
						temp.userId = user.getInt("id");
						temp.userName = user.getString("name");
						temp.userPicUrl = user.getString("photo");

						temp.toObjectType = event.getInt("toObjectType");
						if (temp.toObjectType != 0) {
							JSONObject toObject = event
									.getJSONObject("toObject");
							temp.toObjectId = toObject.getInt("id");
							temp.toObjectPicUrl = toObject.optString("photo");
						}
						list.add(temp);
					}
				}
				MainPageData.current().eventDatas = list;
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
