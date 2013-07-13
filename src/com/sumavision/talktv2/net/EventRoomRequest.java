package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author jianghao
 * @version v2.2
 * @createTime 2012-1-14
 * @description 查询大厅事件请求 2.2版本好友第三个标签
 * @changeLog
 */
public class EventRoomRequest extends JSONRequest {
	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "eventRoomList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			holder.put("jsession", UserNow.current().jsession);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return holder.toString();
	}
}
