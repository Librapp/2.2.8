package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;

/**
 * @author 李梦思
 * @version v2.0
 * @createTime 2012-11-30
 * @description 推荐点播节目请求类
 * @changeLog
 */
public class HotVodProgramListRequest extends JSONRequest {

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
		if (OtherCacheData.current().isDebugMode) {
			Log.e("HotVodProgramListRequest", holder.toString());
		}
		return holder.toString();
	}
}
