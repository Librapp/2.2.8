package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;

/**
 * @author 姜浩
 * @version 2.2.4
 * @createTime 2012-12-26
 * @description 徽章详情接口
 * @changeLog
 */
public class BadgeDetailRequest extends JSONRequest {

	private int badgeId;

	public BadgeDetailRequest(int badgeId) {
		this.badgeId = badgeId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", Constants.badgeDetail);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("jsession", UserNow.current().jsession);
			holder.put("userId", UserNow.current().userID);
			holder.put("badgeId", badgeId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return holder.toString();
	}
}
