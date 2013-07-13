package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.ChaseData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-17
 * @description 删除追剧请求类
 * @changeLog
 */
public class ChaseDeleteRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "chaseDelete");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("userId", UserNow.current().userID);
			holder.put("programIds", ChaseData.current.id);
			holder.put("sessionId", UserNow.current().sessionID);
			holder.put("jsession", UserNow.current().jsession);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("ChaseDeleteRequest", holder.toString());
		}
		return holder.toString();
	}
}
