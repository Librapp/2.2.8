package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.StarData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-6-6
 * @description 明星详情解析类
 * @changeLog 修改为2.2版本 by 李梦思 2013-1-8
 */
public class StarDetailParser extends JSONParser {

	@Override
	public String parse(String s) {
		JSONObject jAData = null;
		JSONObject content = null;
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
				content = jAData.getJSONObject("content");
				StarData.current().name = content.getString("chineseName");
				StarData.current().nameEng = content.getString("englishName");
				StarData.current().photoBig_V = content.getString("pic");
				StarData.current().starType = content.getString("starSign");
				StarData.current().hobby = content.getString("hobby");
				StarData.current().intro = content.getString("intro");
				if (content.has("photo")) {
					Log.e("StarDetail", " photo");
					JSONArray photoList = content.getJSONArray("photo");
					StarData.current().picCount = photoList.length();
					String photo[] = new String[StarData.current().picCount];
					for (int i = 0; i < StarData.current().picCount; i++) {
						photo[i] = photoList.getJSONObject(i).getString("pic");
					}
					StarData.current().photo = photo;
				}
				StarData.current().programCount = content
						.getInt("programCount");
				if (StarData.current().programCount > 0) {
					List<VodProgramData> lp = new ArrayList<VodProgramData>();
					JSONArray programs = content.getJSONArray("program");
					for (int i = 0; i < programs.length(); ++i) {
						JSONObject program = programs.getJSONObject(i);
						VodProgramData p = new VodProgramData();
						p.id = program.getString("id");
						p.topicId = program.getString("topicId");
						p.pic = program.getString("pic");
						p.name = program.getString("name");
						p.contentTypeName = program.getString("typeName");
						lp.add(p);
					}
					StarData.current().setProgram(lp);
				}
			} else {
				msg = jAData.getString("msg");
			}
			UserNow.current().isTimeOut = false;
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			UserNow.current().isTimeOut = true;
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
