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
 * @createTime 2012-12-17
 * @description 发表评论请求组装类
 * @changLog
 */
public class TalkAddRequest extends JSONRequest {
	String topicId;

	public TalkAddRequest(String topicId) {
		this.topicId = topicId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "talkAdd");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("topicId", topicId);
			if (UserNow.current().userID != 0) {
				holder.put("userId", UserNow.current().userID);
				holder.put("sessionId", UserNow.current().sessionID);
			} else {
				holder.put("macAddress", UserNow.current().mac);
			}
			holder.put("jsession", UserNow.current().jsession);
			holder.put("source", ConvertToUnicode
					.AllStrTOUnicode(JSONMessageType.COMMENT_SOURCE));
			if (!CommentData.current().content.equals(""))
				holder.put("content", ConvertToUnicode
						.AllStrTOUnicode(CommentData.current().content));
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			if (!CommentData.current().pic.equals(""))
				holder.put("pic", CommentData.current().pic);
			if (OtherCacheData.current().synType != 0)
				holder.put("synType", OtherCacheData.current().synType);

			if (CommentData.current().audio != null)
				holder.put("audio", CommentData.current().audio);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("TalkAddRequest", holder.toString());
		}
		return holder.toString();
	}
}
