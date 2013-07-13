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
 * @createTime 2013-1-18
 * @description 评论详情解析类
 * @changeLog
 */
public class CommentDetailParser extends JSONParser {

	@Override
	public String parse(String s) {

		JSONObject jAData = null;
		JSONObject content = null;
		JSONObject item = null;
		CommentData rootC = null;
		String msg = "";
		int errCode = -1;

		try {
			jAData = new JSONObject(s);
			if (jAData.has("code")) {
				errCode = jAData.getInt("code");
			} else if (jAData.has("errcode")) {
				errCode = jAData.getInt("errcode");
			}
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				content = jAData.getJSONObject("content");
				item = content.getJSONObject("talk");
				CommentData.current().topicID = item.getLong("topicId");
				CommentData.current().type = item.getInt("type");
				CommentData.current().objectId = item.getLong("objectId");
				CommentData.current().objectName = item.getString("topicName");

				CommentData.current().talkId = item.getInt("id");
				CommentData.current().actionType = item.getInt("actionType");
				CommentData.current().commentTime = item
						.getString("displayTime");
				// talkType：谈论类型，0=原创文字，1=图片，2=视频，3=台词，4=语音
				if (item.getInt("talkType") == 1) {
					CommentData.current().contentURL = item
							.getString("photoUrl");
					CommentData.current().content = item.getString("content");
				} else if (item.getInt("talkType") == 4) {
					CommentData.current().audioURL = item.getString("audioUrl");
				} else if (item.getInt("talkType") == 2) {
					CommentData.current().audioURL = "";
					CommentData.current().content = item.getString("content");
				} else if (item.getInt("talkType") == 3) {
					CommentData.current().audioURL = "";
					CommentData.current().content = item.getString("content");
				} else {
					CommentData.current().audioURL = "";
					CommentData.current().content = item.getString("content");
				}

				CommentData.current().replyCount = item.getInt("replyCount");
				CommentData.current().source = item.getString("source");
				CommentData.current().talkType = item.getInt("talkType");
				if (item.getInt("actionType") == 1) {
					if (!item.isNull("rootTalk") && item.has("rootTalk")) {
						rootC = new CommentData();
						JSONObject rootTalk = item.getJSONObject("rootTalk");
						if (rootTalk.has("id")) {
							if (rootTalk.getInt("id") != 0) {
								rootC.commentTime = rootTalk
										.getString("displayTime");
								rootC.forwardCount = rootTalk
										.getInt("forwardCount");
								rootC.userName = rootTalk.getJSONObject("user")
										.getString("name");
								rootC.replyCount = rootTalk
										.getInt("replyCount");
								rootC.source = rootTalk.getString("source");
								rootC.topicID = rootTalk.getLong("rootId");
								rootC.talkId = rootTalk.getInt("id");
								rootC.userURL = rootTalk.getJSONObject("user")
										.getString("pic");
								rootC.isAnonymousUser = rootTalk.getJSONObject(
										"user").getInt("isAnonymousUser");
								rootC.talkType = rootTalk.getInt("talkType");
								if (rootC.talkType == 1) {

									rootC.contentURL = rootTalk
											.getString("photoUrl");
									rootC.content = rootTalk
											.getString("content");
								} else if (rootC.talkType == 4) {

									rootC.audioURL = rootTalk
											.getString("audioUrl");
								} else {
									rootC.content = rootTalk
											.getString("content");
								}
							}
						} else {
							CommentData.current().isDeleted = true;
							rootC.commentTime = "";
							rootC.forwardCount = 0;
							rootC.userName = "";
							rootC.replyCount = 0;
							rootC.source = "";
							rootC.objectName = "";
							rootC.topicID = 0;
							rootC.userURL = "";
							rootC.content = "此条评论已被原作者删除";
						}
						CommentData.current().rootTalk = rootC;
						CommentData.current().hasRootTalk = true;
					}
					CommentData.current().forwardCount = item
							.getInt("forwardCount");
					CommentData.current().forwardId = item.getInt("forwardId");
				} else {
					CommentData.current().hasRootTalk = false;
				}

				CommentData.current().userName = item.getJSONObject("user")
						.getString("name");
				CommentData.current().userURL = item.getJSONObject("user")
						.getString("pic");
				CommentData.current().userId = item.getJSONObject("user")
						.getInt("id");
				CommentData.current().isAnonymousUser = item.getJSONObject(
						"user").getInt("isAnonymousUser");

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
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		return msg;
	}

}
