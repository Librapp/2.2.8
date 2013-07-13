package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-2-22
 * @description 参加投票竞猜类活动请求类
 * @changeLog
 */
public class ActivityJoinOptionRequest extends JSONRequest {
	private int id;
	private String optionIds;

	public ActivityJoinOptionRequest(int id, String optionIds) {
		method = "activityJoinOption";
		this.id = id;
		this.optionIds = optionIds;
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
			holder.put("optionIds", optionIds);
			holder.put("jsession", UserNow.current().jsession);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return holder.toString();
	}
}
