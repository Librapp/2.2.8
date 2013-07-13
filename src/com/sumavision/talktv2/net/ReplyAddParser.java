package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.CommentData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-10
 * @description 回复评论解析类
 * @changeLog
 */
public class ReplyAddParser extends JSONParser {

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
				if (content.has("newUserInfo")) {
					JSONObject info = content.getJSONObject("newUserInfo");
					if (info.has("point")) {
						UserNow.current().getPoint = info.getInt("point");
						UserNow.current().point = info.getInt("totalPoint");
						UserNow.current().getExp = info.getInt("exp");
						UserNow.current().exp = info.getInt("totalExp");
						UserNow.current().lvlUp = info.getInt("changeLevel");
						UserNow.current().level = info.getString("level");
						UserNow.current().userID = info.getInt("userId");

					}
				}

				List<CommentData> lc = new ArrayList<CommentData>();
				CommentData.current().replyCount = content.getInt("replyCount");
				if (content.has("reply")) {
					JSONArray reply = content.getJSONArray("reply");
					for (int i = 0; i < reply.length(); i++) {
						JSONObject c = reply.getJSONObject(i);
						CommentData comment = new CommentData();
						comment.talkId = c.getInt("id");
						comment.commentTime = c.getString("createTime");
						comment.content = c.optString("content");
						comment.contentURL = c.optString("photoUrl");
						if (!"".equals(comment.contentURL))
							comment.talkType = 1;
						comment.audioURL = c.optString("audioUrl");
						if (!"".equals(comment.audioURL))
							comment.talkType = 4;
						comment.source = c.getString("source");
						JSONObject user = c.getJSONObject("user");
						comment.userName = user.getString("name");
						comment.userURL = user.getString("pic");
						comment.userId = user.getInt("id");
						comment.isAnonymousUser = user
								.getInt("isAnonymousUser");
						lc.add(comment);
					}
				}
				CommentData.current().setReply(lc);
			} else
				msg = jAData.getString("msg");
			UserNow.current().isTimeOut = false;
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			UserNow.current().isTimeOut = true;
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
