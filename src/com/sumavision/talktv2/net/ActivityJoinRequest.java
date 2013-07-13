package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-19
 * @description 参加活动请求类
 * @changeLog
 */
public class ActivityJoinRequest extends JSONRequest {
	private int id;

	public ActivityJoinRequest(int id) {
		method = "activityJoin";
		this.id = id;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", method);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("userId", UserNow.current().userID);
			holder.put("activityId", id);
			holder.put("jsession", UserNow.current().jsession);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return holder.toString();
	}
}
