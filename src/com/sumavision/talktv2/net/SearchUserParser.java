package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-8-9
 * @description 搜索用户JSON解析类
 * @changeLog 修改为2.2版本 by 李梦思 2012-12-26
 */
public class SearchUserParser extends JSONParser {

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
				// JSONObject content = jAData.getJSONObject("content");
				UserNow.current().searchCount = jAData.getInt("count");
				List<User> lu = new ArrayList<User>();
				if (UserNow.current().searchCount > 0) {
					JSONArray users = jAData.getJSONArray("content");
					for (int i = 0; i < users.length(); i++) {
						JSONObject user = users.getJSONObject(i);
						User u = new User();
						u.userId = user.getInt("id");
						u.name = user.getString("name");
						u.iconURL = user.getString("pic");
						u.signature = user.getString("signature");
						u.isFriend = user.getInt("isGuanzhu");
						u.level = user.getString("level");
						lu.add(u);
					}
				}
				UserNow.current().setSearchUser(lu);
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
