package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.NetPlayData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-10
 * @description 查询指定节目头部信息解析
 * @changeLog
 */
public class ProgramHeadParser {

	public String parse(String s, VodProgramData p) {
		String msg = "";
		try {
			JSONObject jAData = new JSONObject(s);
			int errCode = 0;
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
				p.id = content.getString("programId");
				p.topicId = content.getString("topicId");
				p.isSigned = content.getInt("isSigned");
				p.isChased = content.getInt("isChased");
				p.hasActivity = content.getInt("hasActivity");

				if (content.has("playType"))
					p.playType = content.getInt("playType");
				if (content.has("isLive"))
					p.livePlay = content.getInt("isLive");
				else
					p.livePlay = 0;

				p.signCount = content.getInt("signCount");
				p.pic = content.getString("programPic");
				p.name = content.getString("programName");
				p.nameHolder = p.name;
				p.playUrl = content.getString("playUrl");

				p.updateName = content.optString("updateName");

				JSONArray signUser = content.getJSONArray("signUser");
				List<User> lu = new ArrayList<User>();
				for (int i = 0; i < signUser.length(); ++i) {
					JSONObject user = signUser.getJSONObject(i);
					User u = new User();
					u.userId = user.getInt("id");
					u.name = user.getString("name");
					u.iconURL = user.getString("pic");
					lu.add(u);
				}
				if (content.has("play")) {
					JSONArray play = content.getJSONArray("play");
					p.netPlayDatas = new ArrayList<NetPlayData>();
					for (int x = 0; x < play.length(); x++) {
						NetPlayData netPlayData = new NetPlayData();
						JSONObject playItem = play.getJSONObject(x);
						netPlayData.name = playItem.optString("name");
						netPlayData.pic = playItem.optString("pic");
						netPlayData.url = playItem.optString("url");
						netPlayData.videoPath = playItem.optString("videoPath");
						p.netPlayDatas.add(netPlayData);
					}
				}
				p.setSignUser(lu);
				if (content.has("playVideo")) {
					JSONObject playVideo = content.getJSONObject("playVideo");
					p.playVideoActivityId = playVideo.optInt("activityId");
				}
			} else
				msg = jAData.getString("msg");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return msg;
	}

}
