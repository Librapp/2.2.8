package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-6-14
 * @description 删除私信请求类
 * @changeLog 修改为2.2版本 by 李梦思 2012-12-24
 */
public class MailDeleteRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		String choosed = "";
		for (int i = 0; i < UserNow.current().getWaitDeleteId().size(); i++) {
			if (i != UserNow.current().getWaitDeleteId().size() - 1) {
				choosed = choosed + UserNow.current().getWaitDeleteId().get(i)
						+ ",";
			} else {
				choosed = choosed + UserNow.current().getWaitDeleteId().get(i);
			}
		}
		try {
			holder.put("method", "mailDelete");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("otherUserIds", choosed);
			holder.put("jsession", UserNow.current().jsession);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("MailDeleteRequest", holder.toString());
		}
		return holder.toString();
	}
}
