package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-2-7
 * @description 推荐页节目列表列表请求类
 * @changLog
 */
public class RecommendVodProgramListRequest extends JSONRequest {
	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "hotVodProgramList");
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
