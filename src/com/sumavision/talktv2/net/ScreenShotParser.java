package com.sumavision.talktv2.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.ScreenShotData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-10-24
 * @description 电视截屏解析类
 * @changLog 修改为2.2版本 by 李梦思 2013-1-5
 */
public class ScreenShotParser extends JSONParser {

	@Override
	public String parse(String s) {
		JSONObject jAData = null;
		String msg = "";
		int errCode = -1;

		try {
			jAData = new JSONObject(s);
			if (jAData.has("code")) {
				errCode = jAData.getInt("code");
			} else if (jAData.has("errcode")) {
				errCode = jAData.getInt("errcode");
			} else if (jAData.has("errorCode")) {
				errCode = jAData.getInt("errorCode");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				JSONObject content = jAData.getJSONObject("content");
				JSONArray pic = content.getJSONArray("pic");
				if (pic.length() > 0) {
					String lp[] = new String[pic.length()];
					for (int i = 0; i < pic.length(); i++) {
						lp[i] = pic.getString(i);
					}
					String lp1[] = new String[ScreenShotData.picCount
							+ pic.length()];
					switch (ScreenShotData.direction) {
					case -1:
						ScreenShotData.current = lp.length;
						System.arraycopy(lp, 0, lp1, 0, ScreenShotData.current);
						System.arraycopy(ScreenShotData.pic, 0, lp1,
								ScreenShotData.current, ScreenShotData.picCount);
						break;
					case 0:
						lp1 = lp;
						break;
					case 1:
						ScreenShotData.current = ScreenShotData.picCount;
						System.arraycopy(ScreenShotData.pic, 0, lp1, 0,
								ScreenShotData.current);
						System.arraycopy(lp, 0, lp1, ScreenShotData.current,
								lp.length);
						break;
					default:
						break;
					}
					ScreenShotData.pic = lp1;
					ScreenShotData.picCount = lp1.length;
				} else {
					msg = JSONMessageType.SERVER_NETFAIL;
				}
			} else
				msg = jAData.getString("msg");
			UserNow.current().isTimeOut = false;
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			UserNow.current().isTimeOut = true;
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
