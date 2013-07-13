package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.ColumnData;
import com.sumavision.talktv2.data.OtherCacheData;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-9
 * @description 栏目节目请求类
 * @changeLog
 */
public class ColumnVideoRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "columnVideoList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("columnId", ColumnData.current.id);
			holder.put("first", ColumnData.current.offset);
			holder.put("count", ColumnData.current.pageCount);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("ColumnVideoRequest", holder.toString());
		}
		return holder.toString();
	}
}
