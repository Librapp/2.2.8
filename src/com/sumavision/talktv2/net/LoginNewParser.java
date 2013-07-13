package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.ClientData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-9
 * @description 登录解析类
 * @changLog
 */
public class LoginNewParser extends JSONParser {

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

			if (jAData.has("sessionId")) {
				UserNow.current().sessionID = jAData.getString("sessionId");
			}

			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				user = jAData.getJSONObject("content");
				if (user != null) {
					UserNow.current().name = user.getString("userName");
					UserNow.current().userID = user.getInt("userId");
					UserNow.current().gender = user.getInt("sex");
					UserNow.current().exp = user.getInt("totalExp");
					UserNow.current().level = user.getString("level");
					UserNow.current().fansCount = user.getInt("fensiCount");
					UserNow.current().friendCount = user.getInt("guanzhuCount");
					UserNow.current().commentCount = user.getInt("talkCount");
					UserNow.current().chaseCount = user.getInt("chaseCount");

					UserNow.current().badgesCount = user.getInt("badgeCount");
					UserNow.current().atMeCount = user.getInt("talkAtCount");
					UserNow.current().replyMeCount = user
							.getInt("replyByCount");
					UserNow.current().channelCount = user
							.optInt("channelCount");
					UserNow.current().privateMessageAllCount = user
							.getInt("mailCount");
					UserNow.current().remindCount = user.getInt("remindCount");
					if (user.has("sessionId")) {
						UserNow.current().sessionID = user
								.getString("sessionId");
					}
					String signature = user.getString("signature");
					if (signature.equals("")) {
						UserNow.current().signature = "这个家伙神马也木有留下";
					} else {
						UserNow.current().signature = signature;
					}
					UserNow.current().iconURL = user.getString("pic");
					JSONArray clients = user.getJSONArray("client");
					SinaData.isSinaBind = false;
					List<ClientData> lb = new ArrayList<ClientData>();
					for (int i = 0; i < clients.length(); ++i) {
						ClientData b = new ClientData();
						JSONObject client = clients.getJSONObject(i);
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
						lb.add(b);
					}
					UserNow.current().setClients(lb);
					// if (user.has("newUserInfo")) {
					// JSONObject info = user.getJSONObject("newUserInfo");
					// if (info != null && info.has("point")) {
					// if (info.has("point"))
					// UserNow.current().getPoint = info
					// .getInt("point");
					// if (info.has("totalPoint"))
					// UserNow.current().point = info
					// .getInt("totalPoint");
					// if (info.has("exp"))
					// UserNow.current().getExp = info.getInt("exp");
					// if (info.has("totalExp"))
					// UserNow.current().exp = info.getInt("totalExp");
					// if (info.has("changeLevel"))
					// UserNow.current().lvlUp = info
					// .getInt("changeLevel");
					// if (info.has("level"))
					// UserNow.current().level = info
					// .getString("level");
					// }
					// }
					UserNow.current().isSelf = true;
					UserNow.current().isLogedIn = true;
				} else {
					msg = jAData.getString("msg");
					UserNow.current().isLogedIn = false;
				}
				UserNow.current().isTimeOut = false;
			} else {
				msg = jAData.getString("msg");
				UserNow.current().isLogedIn = false;
				UserNow.current().isTimeOut = false;
			}
		} catch (JSONException e) {
			UserNow.current().isTimeOut = true;
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		UserNow.current().errMsg = msg;
		return msg;
	}
}
