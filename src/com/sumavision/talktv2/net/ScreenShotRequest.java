package com.sumavision.talktv2.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.ScreenShotData;
import com.sumavision.talktv2.utils.ConvertToUnicode;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-10-24
 * @description 电视截屏请求组装类
 * @changLog 修改为2.2版本 by 李梦思 2013-1-5
 */
public class ScreenShotRequest extends JSONRequest {

	@Override
	public String make() {
		JSONObject holder = new JSONObject();
		try {
			holder.put("method", "screenShot");
			holder.put("channelId", ScreenShotData.channelId);
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("version", JSONMessageType.APP_VERSION);
			// lastPhotoPath String 用于连续截图时，前一次返回的最后一张图片完整路径 可选
			// TurnFlag int 翻转方向:1表示向右，-1表示向左（连续截图时必须传递） 可选
			if (ScreenShotData.direction != 0) {
				if (ScreenShotData.picPath != null) {
					holder.put("lastPhotoPath", ConvertToUnicode
							.AllStrTOUnicode(ScreenShotData.picPath));
				}
				holder.put("turnFlag", ScreenShotData.direction);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode) {
			Log.e("ScreenShotRequest", holder.toString());
		}
		return holder.toString();
	}
}
