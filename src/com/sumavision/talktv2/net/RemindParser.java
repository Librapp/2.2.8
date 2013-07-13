package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-7
 * @description 预约列表解析类
 * @changeLog
 */
public class RemindParser extends JSONParser {
	private Context context;

	public RemindParser() {

	}

	public RemindParser(Context context) {
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
				UserNow.current().remindCount = content.getInt("remindCount");
				savePreference(UserNow.current().remindCount);
				JSONArray chases = content.getJSONArray("remind");
				List<VodProgramData> lu = new ArrayList<VodProgramData>();
				for (int i = 0; i < chases.length(); ++i) {
					JSONObject chase = chases.getJSONObject(i);
					VodProgramData u = new VodProgramData();
					u.remindId = chase.getLong("id");
					u.cpId = chase.getLong("cpId");
					u.id = chase.getLong("programId") + "";
					u.pic = chase.getString("programPic");

					if (!u.pic.contains("http://")) {
						JSONMessageType.checkServerIP();
						u.pic = JSONMessageType.URL_TITLE_SERVER + u.pic;
					}
					if (!u.pic.contains(".jpg")) {
						u.pic += ".jpg";
					}

					u.cpName = chase.getString("cpName");
					u.channelName = chase.getString("channelName");
					u.cpDate = chase.getString("cpDate");

					if (chase.has("cpStartTime"))
						u.startTime = chase.getString("cpStartTime");
					if (chase.has("cpEndTime"))
						u.endTime = chase.getString("cpEndTime");
					lu.add(u);
				}
				UserNow.current().setRemind(lu);
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
		spEd.putInt("remindCount", count);
		spEd.commit();
	}
}
