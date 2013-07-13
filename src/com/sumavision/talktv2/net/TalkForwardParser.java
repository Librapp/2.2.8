package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.CommentData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 郭鹏
 * @version 2.2
 * @createTime 2013-1-12
 * @description 转发评论
 * @changeLog
 */
public class TalkForwardParser extends JSONParser {

	@Override
	public String parse(String s) {

		if (OtherCacheData.current().isDebugMode)
			Log.e("TalkForwardParser", s);

		JSONObject jAData = null;
		JSONObject content = null;
		JSONArray forwards = null;
		JSONObject forward = null;
		CommentData c = null;
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
				CommentData.current().forwardCount = content
						.getInt("forwardCount");
				if (CommentData.current().forwardCount > 0) {
					forwards = content.getJSONArray("forward");
					List<CommentData> lc = new ArrayList<CommentData>();
					for (int i = 0; i < forwards.length(); i++) {
						forward = forwards.getJSONObject(i);
						c = new CommentData();
						c.talkId = forward.getInt("id");
						c.content = forward.getString("content");
						c.actionType = forward.getInt("actionType");
						c.commentTime = forward.getString("displayTime");
						c.forwardCount = forward.getInt("forwardCount");
						if (forward.has("user")) {
							c.userURL = forward.getJSONObject("user")
									.getString("pic");
							c.userName = forward.getJSONObject("user")
									.getString("name");
						}

						if (forward.getInt("actionType") == 1) {
							if (!forward.isNull("rootTalk")
									&& forward.has("rootTalk")) {
								rootC = new CommentData();
								JSONObject rootTalk = forward
										.getJSONObject("rootTalk");
								if (rootTalk.has("id")) {
									if (rootTalk.getInt("id") != 0) {
										rootC.commentTime = rootTalk
												.getString("displayTime");
										rootC.forwardCount = rootTalk
												.getInt("forwardCount");
										rootC.userName = rootTalk
												.getJSONObject("user")
												.getString("name");
										rootC.replyCount = rootTalk
												.getInt("replyCount");
										rootC.source = rootTalk
												.getString("source");
										// rootC.objectName = rootTalk
										// .getString("topicName");
										rootC.topicID = rootTalk
												.getLong("rootId");
										rootC.talkId = rootTalk.getInt("id");
										rootC.userURL =
										// JSONMessageType.URL_TITLE_SERVER+
										rootTalk.getJSONObject("user")
												.getString("pic");
										rootC.isAnonymousUser = rootTalk
												.getJSONObject("user").getInt(
														"isAnonymousUser");
										rootC.talkType = rootTalk
												.getInt("talkType");
										if (rootC.talkType == 1) {
											rootC.contentURL =
											// JSONMessageType.URL_TITLE_SERVER+
											rootTalk.getString("photoUrl");
											rootC.content = rootTalk
													.getString("content");
										} else if (rootC.talkType == 4) {
											rootC.audioURL =
											// JSONMessageType.URL_TITLE_SERVER
											// +
											rootTalk.getString("audioUrl");
										} else {
											rootC.content = rootTalk
													.getString("content");
										}
									}
								} else {
									c.isDeleted = true;
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
								c.rootTalk = rootC;
								c.hasRootTalk = true;
							}
							c.forwardCount = forward.getInt("forwardCount");
							c.forwardId = forward.getInt("forwardId");
						}
						if (forward.getInt("forwardId") != 0) {
							c.forwardId = forward.getInt("forwardId");
						} else {
							c.forwardId = c.talkId;
						}
						lc.add(c);
					}
					CommentData.current().setForward(lc);
				}
			} else
				msg = jAData.getString("msg");
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		return msg;
	}

}
