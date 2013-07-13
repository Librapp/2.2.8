package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.PlayNewData;
import com.sumavision.talktv2.user.UserNow;

/**
 * 
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-25
 * @description 活动详情请求组装类
 * @changLog
 */
public class ActivityDetailRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", "activityDetail");
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("jsession", UserNow.current().jsession);
			holder.put("activityId", PlayNewData.current.id);
			holder.put("userId", UserNow.current().userID);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("ActivityDetailRequest", holder.toString());
		}
		return holder.toString();
	}
}
