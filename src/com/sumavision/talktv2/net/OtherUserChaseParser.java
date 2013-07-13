package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.ChaseData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.user.UserOther;

/**
 * @author jianghao
 * @version v2.2
 * @createTime 2012-1-15
 * @description 追剧列表解析类
 * @changeLog
 */
public class OtherUserChaseParser {
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
				List<ChaseData> lu = new ArrayList<ChaseData>();
				if (content.has("chase")) {
					JSONArray chases = content.getJSONArray("chase");
					for (int i = 0; i < chases.length(); ++i) {
						JSONObject chase = chases.getJSONObject(i);
						ChaseData u = new ChaseData();
						u.id = chase.getLong("id");
						u.programId = chase.getLong("programId");
						u.programPic = chase.getString("programPic");
						u.programName = chase.getString("programName");
						if (chase.has("latestSubName")) {
							u.latestSubName = chase.getString("latestSubName");
						}
						u.isOver = chase.getInt("isOver");
						lu.add(u);
					}
				}
				uo.setChase(lu);
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
