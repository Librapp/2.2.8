package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.BadgeData;
import com.sumavision.talktv2.data.OptionData;
import com.sumavision.talktv2.data.PlayNewData;
import com.sumavision.talktv2.net.parserUtils.BadgeDataListParser;
import com.sumavision.talktv2.net.parserUtils.OptionListParser;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-25
 * @description 活动详情解析类
 * @changeLog
 */
public class ActivityDetailParser {

	public String parse(String s, PlayNewData playNewData) {
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
				playNewData.name = content.getString("activityName");
				playNewData.intro = content.getString("intro");
				playNewData.pic = content.getString("pic");
				playNewData.userCount = content.getInt("userCount");
				playNewData.state = content.getInt("status");
				playNewData.timeDiff = content.getString("timeDiff");
				playNewData.eventTypeCode = content.getInt("eventTypeCode");
				if (playNewData.eventTypeCode > 0) {
					playNewData.programId = content.optInt("programId");
					playNewData.topicId = content.optInt("topicId");
				}
				playNewData.joinStatus = content.getInt("joinStatus");
				playNewData.schedule = content.getString("schedule");
				playNewData.targetType = content.getInt("toObjectType");
				playNewData.targetId = content.getLong("toObjectId");
				if (playNewData.targetType == 5) {
					JSONObject vote = content.getJSONObject("vote");
					playNewData.selectCount = vote.getInt("selectCount");
					JSONArray options = vote.getJSONArray("option");
					List<OptionData> list = new ArrayList<OptionData>();
					msg = new OptionListParser().parser(options, list);
					playNewData.setOptions(list);
				} else if (playNewData.targetType == 6) {
					JSONObject vote = content.getJSONObject("guess");
					playNewData.selectCount = vote.getInt("selectCount");
					JSONArray options = vote.getJSONArray("option");
					List<OptionData> list = new ArrayList<OptionData>();
					msg = new OptionListParser().parser(options, list);
					playNewData.setOptions(list);
				}
				playNewData.isWeb = content.optInt("isWeb");// 0不跳转到网页 1跳转到网页

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
