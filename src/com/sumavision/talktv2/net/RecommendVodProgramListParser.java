package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.RecommendPageData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.UserNow;

public class RecommendVodProgramListParser extends JSONParser {
	@Override
	public String parse(String s) {

		if (OtherCacheData.current().isDebugMode)
			Log.e("RecommendProgramListParser", s);
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
						double tempPoint = data.getDouble("doubanPoint");
						if (tempPoint > 1.0) {
							r.point = String.valueOf(tempPoint);
						}
						r.name = data.getString("name");
						r.shortIntro = data.getString("shortIntro");
						r.playTimes = data.getInt("playTimes");
						r.pic = data.getString("pic");
						r.updateName = data.getString("updateName");
						r.playType = data.getInt("playType");
						r.playUrl = data.getString("playUrl");
						r.livePlay = 1;
						lp.add(r);
					}
					RecommendPageData.current().setVodProgram(lp);
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
