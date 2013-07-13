package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sumavision.talktv2.data.BadgeData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-7
 * @description 徽章列表解析类
 * @changeLog
 */
public class BadgeParser extends JSONParser {
	private Context context;

	public BadgeParser() {

	}

	public BadgeParser(Context context) {
		this.context = context;
	}

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
			} else if (jAData.has("errorCode")) {
				errCode = jAData.getInt("errorCode");
			}
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				JSONObject content = jAData.getJSONObject("content");
				UserNow.current().badgeCount = content.getInt("badgeCount");
				UserNow.current().badgeRate = content.getString("rate");
				savePreference(UserNow.current().badgeCount);
				List<BadgeData> lu = new ArrayList<BadgeData>();
				if (UserNow.current().badgeCount > 0) {
					JSONArray chases = content.getJSONArray("badge");
					for (int i = 0; i < chases.length(); ++i) {
						JSONObject chase = chases.getJSONObject(i);
						BadgeData u = new BadgeData();
						u.id = chase.getLong("id");
						u.badgeId = chase.getLong("badgeId");
						u.picPath = chase.getString("badgePic");
						u.name = chase.getString("badgeName");
						u.createTime = chase.getString("getTime");
						lu.add(u);
					}
				}
				UserNow.current().setBadgesGained(lu);
			} else
				msg = jAData.getString("msg");
			UserNow.current().isTimeOut = false;
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			UserNow.current().isTimeOut = true;
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}

	private void savePreference(int count) {
		SharedPreferences spUser = context.getSharedPreferences("userInfo", 0);
		Editor spEd = spUser.edit();
		spEd.putInt("badgeCount", count);
		spEd.commit();
	}
}
