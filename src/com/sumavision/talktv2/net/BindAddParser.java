package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.BadgeData;
import com.sumavision.talktv2.data.ClientData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-3-1
 * @description 绑定账号解析类
 * @changLog
 */
public class BindAddParser extends JSONParser {

	@Override
	public String parse(String s) {
		JSONObject jAData = null;
		JSONObject user = null;
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
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				user = jAData.getJSONObject("content");
				JSONArray clients = user.getJSONArray("client");
				SinaData.isSinaBind = false;
				List<ClientData> lc = new ArrayList<ClientData>();
				for (int i = 0; i < clients.length(); ++i) {
					JSONObject client = clients.getJSONObject(i);
					ClientData b = new ClientData();
					if (client.has("token"))
						b.token = client.getString("token");
					if (client.has("type"))
						b.type = client.getInt("type");
					if (b.type == 1) {
						SinaData.isSinaBind = true;
						UserNow.current().thirdUserId = client.getInt("id")
								+ "";
						SinaData.id = client.getString("userId");
						SinaData.accessToken = b.token;
					}
					lc.add(b);
				}
				UserNow.current().setClients(lc);

				JSONArray newBadge = user.optJSONArray("newBadge");
				if (newBadge != null) {
					List<BadgeData> lb = new ArrayList<BadgeData>();
					for (int i = 0; i < newBadge.length(); i++) {
						JSONObject badge = newBadge.getJSONObject(i);
						BadgeData b = new BadgeData();
						b.picPath = badge.getString("pic");
						b.name = badge.getString("name");
						lb.add(b);
					}
					UserNow.current().setNewBadge(lb);
				}
				UserNow.current().errorCode = 0;
			} else if (errCode == 2) {
				msg = jAData.getString("msg");
				UserNow.current().errorCode = 2;
			} else {
				msg = jAData.getString("msg");
				UserNow.current().errorCode = 1;
			}
			UserNow.current().isTimeOut = false;
		} catch (JSONException e) {
			UserNow.current().isTimeOut = true;
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
