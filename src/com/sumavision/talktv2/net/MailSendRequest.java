package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.MailData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.ConvertToUnicode;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-6-14
 * @description 发私信请求类
 * @changeLog 修改为2.2版本 by 李梦思 2012-12-24
 */
public class MailSendRequest extends JSONRequest {

	private int userId;

	public MailSendRequest(int userId) {
		this.userId = userId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "mailAdd");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("otherUserId", userId);
			holder.put("userId", UserNow.current().userID);
			holder.put("content", ConvertToUnicode.AllStrTOUnicode(MailData
					.current().content));
			holder.put("sessionId", UserNow.current().sessionID);
			holder.put("count", OtherCacheData.current().pageCount);
			holder.put("pic", MailData.current().pic);
			holder.put("jsession", UserNow.current().jsession);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("MailSendRequest", holder.toString());
		}
		return holder.toString();
	}
}
