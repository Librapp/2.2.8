package com.sumavision.talktv2.net;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.EventData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.user.UserOther;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-28
 * @description 其他用户中心解析类
 * @changeLog
 */
public class OtherSpaceParser {

	public String parse(String s, UserOther uo) {
		JSONObject jAData = null;
		String msg = "";
		int errCode = -1;

		try {
			jAData = new JSONObject(s);
			if (jAData.has("code")) {
				errCode = jAData.getInt("code");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				JSONObject content = jAData.getJSONObject("content");
				// content.userId long 用户id
				// content.userName string 用户名称
				// content.pic string 用户头像绝对路径
				// content.sex int 用户性别：1=男，2=女
				// content.level int 用户等级
				// content.totalExp int 经验值
				// content.signature string 个性签名
				// content.guanzhuCount int 关注数量
				// content.fensiCount int 粉丝数量
				// content.badgeCount int 徽章数量
				// content.talkCount int 评论数量
				// content.mailCount int 私信数量
				// content.chaseCount int 追剧数量
				// content.remindCount int 预约数量
				// content.isGuanzhu int 是否关注，0=未关注，1=关注
				// content.isFensi int 是否粉丝，0=粉丝，1=不是粉丝
				// content.eventCount int 时间总数量
				// content.event[] array 时间数组
				// content.event[].content string 事件内容
				// content.event[].createTime string 事件发生时间
				uo.userID = content.getInt("userId");
				uo.name = content.getString("userName");
				uo.gender = content.getInt("sex");
				uo.level = content.getString("level");
				uo.iconURL = content.getString("pic");
				uo.exp = content.getInt("totalExp");
				uo.signature = content.getString("signature");
				uo.friendCount = content.getInt("guanzhuCount");
				uo.fansCount = content.getInt("fensiCount");
				uo.badgeCount = content.getInt("badgeCount");
				uo.talkCount = content.getInt("talkCount");
				uo.mailCount = content.getInt("mailCount");
				uo.chaseCount = content.getInt("chaseCount");
				uo.remindCount = content.getInt("remindCount");
				uo.isGuanzhu = content.getInt("isGuanzhu");
				uo.isFensi = content.getInt("isFensi");
				uo.eventCount = content.getInt("eventCount");
				if (uo.eventCount > 0) {
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
								temp.toObjectPicUrl = toObject
										.optString("photo");

								if (!temp.toObjectPicUrl.contains("http://")) {
									JSONMessageType.checkServerIP();
									temp.toObjectPicUrl = JSONMessageType.URL_TITLE_SERVER
											+ temp.toObjectPicUrl;
								}

								if (!temp.toObjectPicUrl.contains(".jpg")) {
									temp.toObjectPicUrl += ".jpg";
								}
							}
							list.add(temp);
						}
					}
					uo.setEvent(list);
				}
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
