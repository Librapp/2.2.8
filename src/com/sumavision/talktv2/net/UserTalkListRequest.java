package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-8
 * @description 用户发表的评论请求组装类
 * @changLog
 */
public class UserTalkListRequest extends JSONRequest {

	private int userId;

	/**
	 * 
	 * @param 其他用户用
	 */
	public UserTalkListRequest(int userId) {
		this.userId = userId;
	}

	/**
	 * 
	 * @param 当前用户用
	 */
	public UserTalkListRequest() {
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "userTalkList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			if (UserNow.current().isSelf)
				holder.put("userId", UserNow.current().userID);
			else
				holder.put("userId", userId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return holder.toString();
	}

}
