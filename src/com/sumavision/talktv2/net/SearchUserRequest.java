package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.ConvertToUnicode;
import com.sumavision.talktv2.utils.Constants;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-8-9
 * @description 搜索用户请求组装类
 * @changLog 修改为2.2版本 by 李梦思 2012-12-26
 */
public class SearchUserRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();

		try {
			holder.put("method", Constants.searchUser);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("version", JSONMessageType.APP_VERSION);
			if (UserNow.current().userID != 0) {
				holder.put("userId", UserNow.current().userID);
				holder.put("userName",
						ConvertToUnicode.AllStrTOUnicode(User.current().name));
			}
			holder.put("first", OtherCacheData.current().offset);
			holder.put("count", OtherCacheData.current().pageCount);
			holder.put("jsession", UserNow.current().jsession);

			// if (User.current().gender != 0)
			// holder.put("sex", User.current().gender);
			// holder.put("level", User.current().level);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e("SearchUserRequest", holder.toString());
		return holder.toString();
	}
}
