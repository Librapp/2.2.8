package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.CommentData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.user.UserOther;

public class OtherUserCommentParser {

	public String parse(String s, UserOther uo) {
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
				JSONObject jsonObject = jAData.getJSONObject("content");

				int talkCount = jsonObject.getInt("talkCount");
				if (UserNow.current().isSelf)
					UserNow.current().talkCount = talkCount;
				else
					uo.talkCount = talkCount;
				List<CommentData> lc = new ArrayList<CommentData>();
				if (talkCount > 0) {
					JSONArray content = jsonObject.getJSONArray("talk");
					for (int i = 0; i < content.length(); i++) {
						JSONObject item = content.getJSONObject(i);

						CommentData c = new CommentData();

						c.talkId = item.getInt("id");
						c.actionType = item.getInt("actionType");
						c.commentTime = item.getString("displayTime");
						c.talkType = item.getInt("talkType");

						// talkType：谈论类型，0=原创文字，1=图片，2=视频，3=台词，4=语音
						if (c.talkType == 1) {
							c.contentURL =
							// JSONMessageType.URL_TITLE_SERVER+
							item.getString("photoUrl");
							c.content = item.getString("content");
						} else if (c.talkType == 4) {
							c.audioURL =
							// JSONMessageType.URL_TITLE_SERVER+
							item.getString("audioUrl");
						} else if (c.talkType == 2) {
							c.audioURL = "";
							c.content = item.getString("content");
						} else if (c.talkType == 3) {
							c.audioURL = "";
							c.content = item.getString("content");
						} else {
							c.audioURL = "";
							c.content = item.getString("content");
						}

						c.replyCount = item.getInt("replyCount");
						c.forwardCount = item.getInt("forwardCount");
						c.source = item.getString("source");
						if (c.actionType == 1) {
							if (!item.isNull("rootTalk")
									&& item.has("rootTalk")) {
								CommentData rootC = new CommentData();
								JSONObject rootTalk = item
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
											rootC.content = rootTalk
													.getString("content");
											rootC.contentURL =
											// JSONMessageType.URL_TITLE_SERVER+
											rootTalk.getString("photoUrl");

										} else if (rootC.talkType == 4) {

											rootC.audioURL =
											// JSONMessageType.URL_TITLE_SERVER+
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
							c.forwardId = item.getInt("forwardId");
						} else {
							c.hasRootTalk = false;
						}

						c.userName = item.getJSONObject("user").getString(
								"name");
						c.userURL = JSONMessageType.URL_TITLE_SERVER
								+ item.getJSONObject("user").getString("pic");
						c.userId = item.getJSONObject("user").getInt("id");
						c.isAnonymousUser = item.getJSONObject("user").getInt(
								"isAnonymousUser");
						lc.add(c);
					}
				}
				uo.setOwnComments(lc);
			} else
				msg = jAData.getString("msg");
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		return msg;
	}
}
