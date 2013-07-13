package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-14
 * @description 节目详情信息请求类
 * @changeLog
 */
public class ProgramDetailNewNewRequest extends JSONRequest {
	int id;

	public ProgramDetailNewNewRequest(int id) {
		this.id = id;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "programDetail");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("programId", id);
			holder.put("jsession", UserNow.current().jsession);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("ProgramDetailNewNewRequest", holder.toString());
		return holder.toString();
	}

}
