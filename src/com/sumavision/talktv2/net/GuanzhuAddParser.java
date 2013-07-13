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
 * @version v2.0
 * @createTime 2012-6-14
 * @description 添加关注解析类
 * @changeLog
 */
public class GuanzhuAddParser extends JSONParser {
	private final SharedPreferences sp;

	public GuanzhuAddParser(Context c) {
		sp = c.getSharedPreferences("userInfo", 0);
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

				UserNow.current().fansCount = content.getInt("friendCount");
				Editor ed = sp.edit();
				ed.putInt("fansCount", UserNow.current().fansCount);
				ed.commit();
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
}
