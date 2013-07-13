package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-11-29
 * @description 推荐页请求类
 * @changLog
 */
public class RecommendPageRequest extends JSONRequest {

	@Override
	public  String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "recommendDetail");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("RecommendPageRequest", holder.toString());
		return holder.toString();
	}
}
