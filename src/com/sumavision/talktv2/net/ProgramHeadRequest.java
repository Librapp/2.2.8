package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-10
 * @description 查询指定节目头部信息请求类
 * @changeLog
 */
public class ProgramHeadRequest extends JSONRequest {
	int id;
	long cpId = 0;

	public ProgramHeadRequest(int id) {
		this(0, id);
	}

	public ProgramHeadRequest(int id, long cpId) {
		this.id = id;
		this.cpId = cpId;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "programHead");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			if (cpId != 0) {
				holder.put("cpid", cpId);
			}
			holder.put("programId", id);
			if (UserNow.current().userID != 0) {
				holder.put("userId", UserNow.current().userID);
				holder.put("sessionId", UserNow.current().sessionID);
			}
			holder.put("jsession", UserNow.current().jsession);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("ProgramHeadRequest", holder.toString());
		return holder.toString();
	}

}
