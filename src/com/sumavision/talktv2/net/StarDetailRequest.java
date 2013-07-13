package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.StarData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-6-6
 * @description 明星详情请求组装类
 * @changeLog 修改为2.2版本 by 李梦思 2013-1-8
 */
public class StarDetailRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "starDetail");
			holder.put("starId", StarData.current().stagerID);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("jsession", UserNow.current().jsession);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("StarDetailRequest", holder.toString());
		}
		return holder.toString();
	}
}
