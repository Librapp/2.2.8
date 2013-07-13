package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-11-30
 * @description 节目频道添加记录请求类
 * @changeLog
 */
public class PlayVideoRequest extends JSONRequest {
	int id;

	public PlayVideoRequest(int id) {
		this.id = id;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "playVideo");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("programId", id);
			holder.put("jsession", UserNow.current().jsession);
			if (UserNow.current().userID != 0)
				holder.put("userId", UserNow.current().userID);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("PlayVideoRequest", holder.toString());
		}
		return holder.toString();
	}
}
