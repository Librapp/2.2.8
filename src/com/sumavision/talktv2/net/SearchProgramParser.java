package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.UserNow;

/**
 * 
 * @author 郭鹏
 * @version 2.2
 * @createTime 2013-1-13
 * @description 节目搜索
 * @changeLog
 * 
 */
public class SearchProgramParser extends JSONParser {

	@Override
	public String parse(String s) {

		if (OtherCacheData.current().isDebugMode)
			Log.e("SearchProgramParser", s);

		JSONObject jAData = null;
		String msg = "";
		int errCode = -1;

		try {
			jAData = new JSONObject(s);
			if (jAData.has("code")) {
				errCode = jAData.getInt("code");
			} else if (jAData.has("errcode")) {
				errCode = jAData.getInt("errcode");
			}
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				if (jAData.has("content")) {
					JSONObject content = jAData.getJSONObject("content");
					if (content.has("program")) {
						JSONArray plays = content.getJSONArray("program");
						List<VodProgramData> lp = new ArrayList<VodProgramData>();
						for (int i = 0; i < plays.length(); i++) {
							JSONObject o = plays.getJSONObject(i);
							VodProgramData vpd = new VodProgramData();
							vpd.name = o.getString("name");
							vpd.id = o.getString("id");
							vpd.topicId = o.getString("topicId");
							vpd.pic = o.getString("pic");
							lp.add(vpd);
						}

						OtherCacheData.current().listSearchResult = lp;
						Log.e("Search",
								"on Here"
										+ OtherCacheData.current().listSearchResult
												.size());
					}

				}

			} else
				msg = jAData.getString("msg");
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		return msg;
	}
}
