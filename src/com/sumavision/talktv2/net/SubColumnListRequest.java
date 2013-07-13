package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-11
 * @description 专题列表请求类
 * @changLog
 */
public class SubColumnListRequest extends JSONRequest {
	private int id;
	private int offset;
	private int pageCount;

	public SubColumnListRequest(int id, int offset, int pageCount) {
		this.id = id;
		this.offset = offset;
		this.pageCount = pageCount;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "subColumnList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("columnId", id);
			holder.put("first", offset);
			holder.put("count", pageCount);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("SubColumnListRequest", holder.toString());
		return holder.toString();
	}
}
