package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-10
 * @description 节目视频列表请求类
 * @changLog
 */
public class ProgramVideoListRequest extends JSONRequest {
	int id;

	public ProgramVideoListRequest(int id) {
		this.id = id;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "programVideoList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("jsession", UserNow.current().jsession);
			holder.put("programId", id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("ProgramVideoListRequest", holder.toString());
		return holder.toString();
	}
}
