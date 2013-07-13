package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.FeedbackData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.ConvertToUnicode;

/**
 * @author 李梦思
 * @version v2.0
 * @createTime 2012-7-5
 * @description 用户反馈请求类
 * @changeLog
 */
public class FeedbackRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", Constants.feedbackAdd);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			if (UserNow.current().userID != 0)
				holder.put("userId", UserNow.current().userID);
			holder.put("content", ConvertToUnicode.AllStrTOUnicode(FeedbackData
					.current().content));
			holder.put("jsession", UserNow.current().jsession);
			holder.put("contactNumber", FeedbackData.current().email);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("FeedbackRequest", holder.toString());
		}
		return holder.toString();
	}
}
