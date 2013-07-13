package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-7
 * @description 关注列表解析类
 * @changeLog
 */
public class FriendParser extends JSONParser {
	private Context context;

	public FriendParser() {

	}

	public FriendParser(Context context) {
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
				UserNow.current().friendCount = content.getInt("guanzhuCount");
				savePreference(UserNow.current().friendCount);
				JSONArray users = content.getJSONArray("guanzhu");
				List<User> lu = new ArrayList<User>();
				for (int i = 0; i < users.length(); ++i) {
					JSONObject user = users.getJSONObject(i);
					User u = new User();
					u.userId = user.getInt("userId");
					u.name = user.getString("userName");
					u.iconURL = user.getString("userPic");
					u.isFans = user.getInt("isFensi");
					u.signature = user.getString("signature");
					// JSONArray events = user.getJSONArray("event");
					// List<EventData> le = new ArrayList<EventData>();
					// for (int j = 0; j < events.length(); ++j) {
					// JSONObject event = events.getJSONObject(j);
					// EventData e = new EventData();
					// e.content = event.getString("content");
					// e.time = event.getString("time");
					// le.add(e);
					// }
					// u.setEvent(le);
					lu.add(u);
				}
				UserNow.current().setFriend(lu);
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
		spEd.putInt("friendCount", count);
		spEd.commit();
	}
}
