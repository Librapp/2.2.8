package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.user.UserOther;

public class OtherUserRemindParser {

	public String parse(String s, UserOther uo) {
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
				JSONArray chases = content.getJSONArray("remind");
				List<VodProgramData> lu = new ArrayList<VodProgramData>();
				for (int i = 0; i < chases.length(); ++i) {
					JSONObject chase = chases.getJSONObject(i);
					VodProgramData u = new VodProgramData();
					u.remindId = chase.getLong("id");
					u.cpId = chase.getLong("cpId");
					u.id = chase.getLong("programId") + "";
					u.pic = chase.getString("programPic");
					u.cpName = chase.getString("cpName");
					u.channelName = chase.getString("channelName");
					u.cpDate = chase.getString("cpDate");

					if (chase.has("cpStartTime"))
						u.startTime = chase.getString("cpStartTime");
					if (chase.has("cpEndTime"))
						u.endTime = chase.getString("cpEndTime");
					lu.add(u);
				}
				uo.setRemind(lu);
			} else
				msg = jAData.getString("msg");
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
