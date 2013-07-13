package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.CommentData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.ConvertToUnicode;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-10
 * @description 转发评论请求组装类
 * @changeLog
 */
public class TalkForwardRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "talkForwardAdd");
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("forwardId", CommentData.current().talkId);
			if (CommentData.current().hasRootTalk) {
				holder.put("rootId", CommentData.current().rootTalk.talkId);
			} else {
				holder.put("rootId", CommentData.current().talkId);
			}
			if (UserNow.current().userID != 0) {
				holder.put("userId", UserNow.current().userID);
				holder.put("sessionId", UserNow.current().sessionID);
				holder.put("jsession", UserNow.current().jsession);
			} else {
				holder.put("macAddress", UserNow.current().mac);
			}
			holder.put("source", ConvertToUnicode
					.AllStrTOUnicode(JSONMessageType.COMMENT_SOURCE));
			holder.put("content", ConvertToUnicode.AllStrTOUnicode(CommentData
					.current().content));
			holder.put("synType", OtherCacheData.current().synType);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (OtherCacheData.current().isDebugMode) {
			Log.e("TalkAddRequest", holder.toString());
		}
		return holder.toString();
	}
}
