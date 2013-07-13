package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.BadgeData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.parserUtils.BadgeDataListParser;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-19
 * @description 参加活动解析类
 * @changeLog
 */
public class ActivityJoinParser {

	public String parse(String s) {
		if (OtherCacheData.current().isDebugMode)
			Log.e("ActivityJoinParser", s);
		JSONObject jAData = null;
		String msg = "";
		int errCode = -1;

		try {
			jAData = new JSONObject(s);
			if (jAData.has("code")) {
				errCode = jAData.getInt("code");
			}
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				JSONObject content = jAData.getJSONObject("content");
				JSONArray newBadge = content.optJSONArray("newBadge");
				if (newBadge != null) {
					List<BadgeData> lb = new ArrayList<BadgeData>();
					msg = new BadgeDataListParser().parser(newBadge, lb);
					UserNow.current().setNewBadge(lb);
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
