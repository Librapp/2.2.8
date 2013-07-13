package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sumavision.talktv2.data.ChaseData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-7
 * @description 追剧列表解析类
 * @changeLog
 */
public class ChaseParser extends JSONParser {
	private Context context;

	public ChaseParser() {

	}

	public ChaseParser(Context context) {
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
				UserNow.current().chaseCount = content.getInt("chaseCount");
				savePreference(UserNow.current().chaseCount);
				List<ChaseData> lu = new ArrayList<ChaseData>();
				if (content.has("chase")) {
					JSONArray chases = content.getJSONArray("chase");
					for (int i = 0; i < chases.length(); ++i) {
						JSONObject chase = chases.getJSONObject(i);
						ChaseData u = new ChaseData();
						u.topicId = chase.getLong("topicId");
						u.id = chase.getLong("id");
						u.programId = chase.getLong("programId");
						u.programPic = chase.getString("programPic");
						if (!u.programPic.contains("http://")) {
							JSONMessageType.checkServerIP();
							u.programPic = JSONMessageType.URL_TITLE_SERVER
									+ u.programPic;
						}
						if (!u.programPic.contains(".jpg")) {
							u.programPic += ".jpg";
						}
						u.programName = chase.getString("programName");
						if (chase.has("latestSubName")) {
							u.latestSubName = chase.getString("latestSubName");
						}
						u.isOver = chase.getInt("isOver");
						lu.add(u);
					}
				}
				UserNow.current().setChase(lu);
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
		spEd.putInt("chaseCount", count);
		spEd.commit();
	}
}
