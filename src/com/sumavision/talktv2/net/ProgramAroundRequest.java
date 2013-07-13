package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.ProgramAroundData;
import com.sumavision.talktv2.user.UserNow;

/**
 * 
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-6-6
 * @description 节目周边信息详情请求组装类
 * @changeLog 改为2.2版本 by 李梦思 2013-1-5
 * 
 */
public class ProgramAroundRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "programAroundDetail");
			holder.put("id", ProgramAroundData.current().id);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("jsession", UserNow.current().jsession);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("ProgramAroundRequest", holder.toString());
		}
		return holder.toString();
	}
}
