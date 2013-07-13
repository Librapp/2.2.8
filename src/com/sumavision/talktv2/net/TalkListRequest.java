package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-10
 * @description 评论列表请求类
 * @changLog
 */
public class TalkListRequest extends JSONRequest {
	int topicId;
	long cpId = 0;

	public TalkListRequest(int id) {
		this(0, id);
	}

	public TalkListRequest(long cpId, int topicId) {
		this.topicId = topicId;
		this.cpId = cpId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "talkList");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("topicId", topicId);
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			if (cpId != 0)
				holder.put("cpid", cpId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("TalkListRequest", holder.toString());
		return holder.toString();
	}
}
