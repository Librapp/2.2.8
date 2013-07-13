package com.sumavision.talktv2.net;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.HotPlayProgram;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-6
 * @description 热门搜索节目解析类
 * @changeLog
 */
public class HotPlayParser extends JSONParser {

	@Override
	public String parse(String s) {

		if (OtherCacheData.current().isDebugMode)
			Log.e("HotPlayParser", s);
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

				ArrayList<VodProgramData> temp = new ArrayList<VodProgramData>();
				if (room.has("play")) {
					JSONArray plays = room.getJSONArray("play");
					for (int i = 0; i < plays.length(); i++) {
						VodProgramData tempProgram = new VodProgramData();
						JSONObject play = plays.getJSONObject(i);
						tempProgram.name = play.getString("programName");
						tempProgram.id = play.getString("programId");
						tempProgram.topicId = play.getString("topicId");
						temp.add(tempProgram);
					}
				}
				HotPlayProgram.current().hotProgramList = temp;
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
