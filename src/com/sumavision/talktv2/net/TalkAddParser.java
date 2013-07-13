package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.BadgeData;
import com.sumavision.talktv2.data.CommentData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-17
 * @description 发表评论解析类
 * @changeLog change more by 郭鹏 2012-6-16
 */
public class TalkAddParser {
	public final static int PROGRAM = 0;
	public final static int ACTIVITY = 1;
	public final static int STAR = 2;

	private final int type;

	public TalkAddParser(int type) {
		this.type = type;
	}

	public TalkAddParser() {
		type = PROGRAM;
	}

	public String parse(String s, VodProgramData p) {
		JSONObject jAData = null;
		JSONObject item = null;
		String msg = "";
		CommentData rootC = null;
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
				JSONArray newBadge = content.optJSONArray("newBadge");
				if (newBadge != null) {
					List<BadgeData> lb = new ArrayList<BadgeData>();
					for (int i = 0; i < newBadge.length(); i++) {
						JSONObject badge = newBadge.getJSONObject(i);
						BadgeData b = new BadgeData();
						b.picPath = badge.getString("pic");
						b.name = badge.getString("name");
						lb.add(b);
					}
					UserNow.current().setNewBadge(lb);
				}
				JSONArray talks = content.getJSONArray("talk");
				List<CommentData> lc = new ArrayList<CommentData>();
				for (int i = 0; i < talks.length(); i++) {
					CommentData c = new CommentData();
					item = talks.getJSONObject(i);
					c = new CommentData();
					c.commentTime = item.getString("displayTime");
					c.forwardCount = item.getInt("forwardCount");
					c.replyCount = item.getInt("replyCount");
					c.source = item.getString("source");
					c.talkId = item.getInt("id");

					c.talkType = item.getInt("talkType");
					// 评论内容是否有图片：1，图片 ，4，声音评论
					if (c.talkType == 1) {
						c.contentURL = item.getString("photoUrl");
						// 评论内容
						c.content = item.getString("content");
					} else if (c.talkType == 4) {
						c.audioURL = item.getString("audioUrl");
					} else {
						// 评论内容
						c.content = item.getString("content");
					}

					if (item.getInt("actionType") == 1) {
						if (!item.isNull("rootTalk") && item.has("rootTalk")) {
							rootC = new CommentData();
							JSONObject rootTalk = item
									.getJSONObject("rootTalk");
							if (rootTalk.has("id")) {
								if (rootTalk.getInt("id") != 0) {
									rootC.commentTime = rootTalk
											.getString("displayTime");
									rootC.forwardCount = rootTalk
											.getInt("forwardCount");
									rootC.userName = rootTalk.getJSONObject(
											"user").getString("name");
									rootC.replyCount = rootTalk
											.getInt("replyCount");
									rootC.source = rootTalk.getString("source");
									// rootC.objectName = rootTalk
									// .getString("topicName");
									rootC.topicID = rootTalk.getLong("rootId");
									rootC.talkId = rootTalk.getInt("id");
									rootC.userURL =
									// JSONMessageType.URL_TITLE_SERVER+
									rootTalk.getJSONObject("user").getString(
											"pic");
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
						c.forwardCount = item.getInt("forwardCount");
						c.forwardId = item.getInt("forwardId");
					}
					if (item.getInt("forwardId") != 0) {
						c.forwardId = item.getInt("forwardId");
					} else {
						c.forwardId = c.talkId;
					}
					c.userName = item.getJSONObject("user").getString("name");
					c.userURL = item.getJSONObject("user").getString("pic");
					c.userId = item.getJSONObject("user").getInt("id");
					c.isAnonymousUser = item.getJSONObject("user").getInt(
							"isAnonymousUser");
					lc.add(c);
				}
				switch (type) {
				case PROGRAM:
					p.talkCount = content.getInt("talkCount");
					p.setComment(lc);
					break;
				case ACTIVITY:

					break;
				case STAR:

					break;
				default:
					break;
				}
			} else
				msg = jAData.getString("msg");
			UserNow.current().isTimeOut = false;
		} catch (JSONException e) {
			UserNow.current().isTimeOut = true;
			e.printStackTrace();
			msg = JSONMessageType.SERVER_NETFAIL;
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
