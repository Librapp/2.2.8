package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.RecommendPageData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-11-29
 * @description 热门点播节目解析类
 * @changeLog
 */
public class HotVodProgramListParser extends JSONParser {

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
			}
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {

				JSONObject room = jAData.getJSONObject("content");

				RecommendPageData.current().vodProgramCount = room
						.getInt("hotVodProgramCount");
				if (RecommendPageData.current().vodProgramCount > 0) {
					JSONArray liveProgram = room.getJSONArray("hotVodProgram");
					List<VodProgramData> lp = new ArrayList<VodProgramData>();
					for (int i = 0; i < liveProgram.length(); i++) {
						VodProgramData r = new VodProgramData();
						JSONObject data = liveProgram.getJSONObject(i);
						r.id = data.getString("id");
						r.topicId = data.getString("topicId");
						r.name = data.getString("name");
						double tempPoint = data.getDouble("doubanPoint");
						if (tempPoint > 1.0)
							r.point = String.valueOf(tempPoint);
						r.shortIntro = data.getString("shortIntro");
						r.channelName = data.getString("channelName");
						r.startTime = data.getString("startTime");
						r.endTime = data.getString("endTime");
						r.playTimes = data.getInt("playTimes");
						r.pic = data.getString("pic");
						r.updateName = data.getString("updateName");
						r.playType = data.getInt("playType");
						r.playUrl = data.getString("playUrl");
						lp.add(r);
					}
					RecommendPageData.current().setVodProgram(lp);
				}
			} else
				msg = jAData.getString("msg");
			UserNow.current().isTimeOut = false;
		} catch (JSONException e) {
			UserNow.current().isTimeOut = true;
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
