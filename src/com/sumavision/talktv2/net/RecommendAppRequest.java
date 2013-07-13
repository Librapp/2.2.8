package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-24
 * @description 推荐软件列表请求类
 * @changeLog
 */
public class RecommendAppRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "recommendAppList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("count", OtherCacheData.current().pageCount);
			holder.put("first", OtherCacheData.current().offset);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("RecommendAppRequest", holder.toString());
		return holder.toString();
	}
}
