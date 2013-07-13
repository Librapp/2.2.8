package com.sumavision.talktv2.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.sumavision.talktv2.data.BadgeData;
import com.sumavision.talktv2.data.ClientData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.ConvertToUnicode;

public class BindLogInTask extends AsyncTask<Object, Integer, Integer> {
	private NetConnectionListenerNew listener;
	private String method;
	private boolean isLoadMore;
	private String errMsg = null;

	public BindLogInTask(NetConnectionListenerNew listener, boolean isLoadMore) {
		method = Constants.bindLogIn;
		this.listener = listener;
		this.isLoadMore = isLoadMore;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (listener != null) {
			listener.onNetBegin(method, isLoadMore);
		}
	}

	@Override
	protected Integer doInBackground(Object... params) {
		Context context = (Context) params[0];
		String data = generateRequset();
		if (data != null) {
			String result = NetUtil.execute(context, data, null);
			if (result == null) {
				return Constants.fail_no_net;
			} else {
				String msg = parse(result);
				if (msg == null) {
					return Constants.sucess;
				} else if ("parseErr".equals(msg)) {
					return Constants.parseErr;
				} else {
					errMsg = msg;
					return Constants.fail_server_err;
				}
			}
		} else {
			return Constants.requestErr;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (listener != null) {
			listener.onNetEnd(result, errMsg, method, isLoadMore);
		}
	}

	/**
	 * 
	 * @param userId
	 *            用户ID
	 * @param content
	 * @param contactNum
	 * 
	 */
	private String generateRequset() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", method);
			jsonObject.put("version", JSONMessageType.APP_VERSION);
			jsonObject.put("client", JSONMessageType.SOURCE);
			jsonObject.put("jsession", UserNow.current().jsession);

			jsonObject.put("thirdType", UserNow.current().thirdType);
			jsonObject.put("thirdToken", UserNow.current().thirdToken);
			jsonObject.put("thirdUserId", SinaData.id);
			if (!TextUtils.isEmpty(UserNow.current().name)) {
				jsonObject.put("userName", ConvertToUnicode
						.AllStrTOUnicode(UserNow.current().name));
				jsonObject.put("userType", UserNow.current().userType);
				jsonObject.put("password", UserNow.current().passwd);
			}
			if (!TextUtils.isEmpty(UserNow.current().eMail))
				jsonObject.put("email", UserNow.current().eMail);
			if (!TextUtils.isEmpty(UserNow.current().thirdUserPic))
				jsonObject.put("thirdUserPic", UserNow.current().thirdUserPic);
			if (!TextUtils.isEmpty(UserNow.current().thirdSignature))
				jsonObject.put("thirdSignature",
						UserNow.current().thirdSignature);
			if (!TextUtils.isEmpty(UserNow.current().validTime))
				jsonObject.put("validTime", UserNow.current().validTime);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		String returnValue = jsonObject.toString();
		return returnValue;
	}

	/**
	 * 
	 * @param list
	 * @param s
	 * @return
	 */
	private String parse(String s) {

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
				UserNow.current().name = user.getString("userName");
				UserNow.current().userID = user.getInt("userId");
				UserNow.current().gender = user.getInt("sex");
				UserNow.current().exp = user.getInt("totalExp");
				UserNow.current().level = user.getString("level");
				UserNow.current().fansCount = user.getInt("fensiCount");
				UserNow.current().friendCount = user.getInt("guanzhuCount");
				UserNow.current().commentCount = user.getInt("talkCount");
				UserNow.current().chaseCount = user.getInt("chaseCount");
				UserNow.current().privateMessageAllCount = user
						.getInt("mailCount");
				UserNow.current().remindCount = user.getInt("remindCount");

				UserNow.current().badgesCount = user.getInt("badgeCount");
				UserNow.current().atMeCount = user.getInt("talkAtCount");
				UserNow.current().replyMeCount = user.getInt("replyByCount");

				if (user.has("sessionId")) {
					UserNow.current().sessionID = user.getString("sessionId");
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
				UserNow.current().isSelf = true;
				UserNow.current().isLogedIn = true;
				UserNow.current().errorCode = 0;
			} else if (errCode == 2) {
				UserNow.current().errorCode = 2;
				return jAData.getString("msg");
			} else {
				UserNow.current().errorCode = 1;
				return jAData.getString("msg");
			}
		} catch (JSONException e) {
			return "parserErr";
		}
		UserNow.current().errMsg = msg;
		return null;
	}
}
