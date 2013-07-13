package com.sumavision.talktv2.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.sumavision.talktv2.data.ClientData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.ConvertToUnicode;

public class LogInTask extends AsyncTask<Object, Integer, Integer> {
	private NetConnectionListenerNew listener;
	private String method;
	private boolean isLoadMore;
	private String errMsg = null;

	public LogInTask(NetConnectionListenerNew listener, boolean isLoadMore) {
		method = Constants.logIn;
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
			jsonObject.put("userName",
					ConvertToUnicode.AllStrTOUnicode(UserNow.current().name));
			jsonObject.put("password", UserNow.current().passwd);
			jsonObject.put("infoFlag", UserNow.current().infoFlag);

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

		int errCode = 1;
		try {
			JSONObject jAData = new JSONObject(s);
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
				JSONObject user = jAData.getJSONObject("content");
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
					UserNow.current().channelCount = user
							.optInt("channelCount");
					UserNow.current().badgesCount = user.getInt("badgeCount");
					UserNow.current().atMeCount = user.getInt("talkAtCount");
					UserNow.current().replyMeCount = user
							.getInt("replyByCount");

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
					UserNow.current().isSelf = true;
					UserNow.current().isLogedIn = true;
				} else {
					UserNow.current().isLogedIn = false;
					return jAData.getString("msg");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return "parseErr";
		}
		return null;
	}
}
