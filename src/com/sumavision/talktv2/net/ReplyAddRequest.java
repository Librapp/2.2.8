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
 * @description 发表回复请求组装类
 * @changLog
 */
public class ReplyAddRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "replyAdd");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("jsession", UserNow.current().jsession);
			// String source = Build.MODEL;
			// if (source != null) {
			// holder.put("source", source);
			// } else {
			holder.put("source", ConvertToUnicode
					.AllStrTOUnicode(JSONMessageType.COMMENT_SOURCE));
			// }
			// talkId long 评论id 必选
			holder.put("talkId", CommentData.current().talkId);
			// replyId long 回复id 可选（回复回复时，此字段要传）
			int replyId = CommentData.replyComment().talkId;
			if (replyId != 0) {
				holder.put("replyId", CommentData.replyComment().talkId);
			}
			if (UserNow.current().userID != 0) {
				// userId long 登陆用户id(匿名用户不传该参数) 可选
				holder.put("userId", UserNow.current().userID);
				holder.put("sessionId", UserNow.current().sessionID);
			} else {
				// macAddress string 用户设备mac地址 可选
				holder.put("macAddress", UserNow.current().mac);
			}
			// replyUserId long 被回复的用户id 必选
			if (CommentData.current().isReply)
				holder.put("replyUserId", CommentData.replyComment().userId);
			else
				holder.put("replyUserId", CommentData.current().userId);

			if (CommentData.current().content != null
					&& !CommentData.current().content.equals(""))
				holder.put("content", ConvertToUnicode
						.AllStrTOUnicode(CommentData.current().content));
			if (CommentData.current().pic != null
					&& !CommentData.current().pic.equals(""))
				holder.put("pic", CommentData.current().pic);
			if (CommentData.current().audio != null
					&& !"".equals(CommentData.current().audio))
				holder.put("audio", CommentData.current().audio);
			// getContent int 是否要返回content对象：
			// 1=要返回，0=不要返回（快速回复）。 默认为1 可选
			// holder.put("getContent", 1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("ReplyAddRequest", holder.toString());
		}
		return holder.toString();
	}
}
