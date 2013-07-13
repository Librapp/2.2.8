package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-9
 * @description 推荐页节目列表请求类
 * @changLog
 */
public class RecommendProgramListRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "hotProgramList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("RecommendProgramListRequest", holder.toString());
		return holder.toString();
	}
}
