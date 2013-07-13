package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.ColumnData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.RecommendData;
import com.sumavision.talktv2.data.RecommendPageData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-11-29
 * @description 推荐解析类
 * @changeLog
 */
public class RecommendPageParser extends JSONParser {

	@Override
	public String parse(String s) {

		if (OtherCacheData.current().isDebugMode)
			Log.e("RecommendPageParser", s);

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

				JSONArray recommend = room.getJSONArray("recommend");
				List<RecommendData> lr = new ArrayList<RecommendData>();
				for (int i = 0; i < recommend.length(); ++i) {
					RecommendData r = new RecommendData();
					JSONObject data = recommend.getJSONObject(i);
					r.id = data.getLong("objectId");
					r.name = data.getString("name");
					r.pic = data.getString("pic");
					r.type = data.getInt("type");
					r.topicId = data.optString("topicId");
					r.url = data.optString("url");
					lr.add(r);
				}
				RecommendPageData.current().setRecommend(lr);

				JSONArray column = room.getJSONArray("column");
				List<ColumnData> lc = new ArrayList<ColumnData>();
				for (int i = 0; i < column.length(); ++i) {
					ColumnData r = new ColumnData();
					JSONObject data = column.getJSONObject(i);
					r.id = data.getInt("id");
					r.name = data.getString("name");
					r.type = data.getInt("type");
					if ((data.has("subColumn"))
							&& (data.getJSONArray("subColumn").length() > 0)) {
						JSONArray subColumns = data.getJSONArray("subColumn");
						List<ColumnData> ls = new ArrayList<ColumnData>();
						for (int j = 0; j < subColumns.length(); ++j) {
							ColumnData sub = new ColumnData();
							JSONObject subColumn = subColumns.getJSONObject(i);
							sub.id = subColumn.getInt("id");
							sub.name = subColumn.getString("name");
							sub.type = subColumn.getInt("type");
							sub.playTimes = subColumn.getInt("playTimes");
							ls.add(sub);
						}
						r.setSubColumn(ls);
					}
					Log.e("columnData", "id=" + r.id + "name=" + r.name);
					lc.add(r);
				}
				RecommendPageData.current().setColumn(lc);

				RecommendPageData.current().liveProgramCount = room
						.getInt("hotLiveProgramCount");
				if (RecommendPageData.current().liveProgramCount > 0) {
					JSONArray liveProgram = room.getJSONArray("hotLiveProgram");
					List<VodProgramData> lp = new ArrayList<VodProgramData>();
					for (int i = 0; i < liveProgram.length(); i++) {
						VodProgramData r = new VodProgramData();
						JSONObject data = liveProgram.getJSONObject(i);
						r.id = data.getString("id");
						r.cpId = data.getLong("cpid");
						r.topicId = data.getString("topicId");
						r.point = data.getString("doubanPoint");
						r.name = data.getString("name");
						r.shortIntro = data.getString("shortIntro");
						r.channelName = data.getString("channelName");
						r.startTime = data.getString("startTime");
						r.endTime = data.getString("endTime");
						r.playTimes = data.getInt("playTimes");
						r.pic = data.getString("pic");
						r.isPlaying = data.getInt("isPlaying");
						if (data.has("playMinutes")
								&& !data.getString("playMinutes").equals(""))
							r.playMinutes = data.getString("playMinutes");
						r.playType = data.getInt("playType");
						r.playUrl = data.getString("playUrl");
						r.livePlay = 0;
						lp.add(r);
					}
					RecommendPageData.current().setLiveProgram(lp);
				}

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
						r.shortIntro = data.getString("shortIntro");
						double tempPoint = data.getDouble("doubanPoint");
						if (tempPoint > 1.0)
							r.point = String.valueOf(tempPoint);
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
