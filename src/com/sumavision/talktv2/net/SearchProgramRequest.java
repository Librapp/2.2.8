package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.ConvertToUnicode;

/**
 * 
 * @author 郭鹏
 * @version 2.2
 * @createTime 2013-1-13
 * @description 节目搜索
 * @changeLog
 * 
 */
public class SearchProgramRequest extends JSONRequest {
	String searchKeyWords;

	public SearchProgramRequest(String searchKeyWords) {
		this.searchKeyWords = searchKeyWords;
	}

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "programSearch");
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("jsession", UserNow.current().jsession);
			holder.put("keyword",
					ConvertToUnicode.AllStrTOUnicode(searchKeyWords));
			holder.put("first", 0);
			holder.put("count", 10);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("SearchProgramRequest", holder.toString());
		}
		return holder.toString();
	}
}
