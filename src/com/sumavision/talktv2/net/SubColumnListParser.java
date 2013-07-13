package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.ColumnData;
import com.sumavision.talktv2.data.RecommendPageData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-11
 * @description 专题列表解析类
 * @changeLog
 */
public class SubColumnListParser extends JSONParser {

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

				JSONArray column = room.getJSONArray("subColumn");
				List<ColumnData> ls = new ArrayList<ColumnData>();
				for (int j = 0; j < column.length(); ++j) {
					ColumnData sub = new ColumnData();
					JSONObject subColumn = column.getJSONObject(j);
					sub.id = subColumn.getInt("id");
					sub.name = subColumn.getString("name");
					sub.type = subColumn.getInt("type");
					sub.pic = subColumn.getString("pic");
					sub.intro = subColumn.getString("shortIntro");
					sub.playTimes = subColumn.getInt("playTimes");
					ls.add(sub);
				}
				RecommendPageData.current().setSubColumn(ls);
			} else
				msg = jAData.getString("msg");
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		return msg;
	}
}
