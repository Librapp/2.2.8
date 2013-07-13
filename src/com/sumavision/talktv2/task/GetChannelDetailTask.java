package com.sumavision.talktv2.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.ChannelData;
import com.sumavision.talktv2.data.ChannelNewData;
import com.sumavision.talktv2.data.CpData;
import com.sumavision.talktv2.data.NetPlayData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;

public class GetChannelDetailTask extends AsyncTask<Object, Integer, Integer> {

	private NetConnectionListenerNew listener;
	private String method;
	private boolean isLoadMore;
	private String errMsg = null;

	public GetChannelDetailTask(NetConnectionListenerNew listener,
			boolean isLoadMore) {
		method = Constants.channelContent;
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

		int userId = (Integer) params[1];
		int channelId = (Integer) params[2];
		String date = (String) params[3];
		ChannelData channelData = (ChannelData) params[4];
		whichDayType = (Integer) params[6];
		String data = generateRequset(userId, channelId, date);
		if (data != null) {
			String result = NetUtil.execute(context, data, null);
			if (result == null) {
				return Constants.fail_no_net;
			} else {
				@SuppressWarnings("unchecked")
				ArrayList<CpData> list = (ArrayList<CpData>) params[5];
				String msg = parse(channelData, list, result);
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
	 * @param first
	 *            开始位置
	 * @param count
	 *            个数
	 * @return 生成的JSON字段
	 */
	private String generateRequset(int userId, int channelId, String date) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", method);
			jsonObject.put("version", JSONMessageType.APP_VERSION);
			jsonObject.put("client", JSONMessageType.SOURCE);
			jsonObject.put("jsession", UserNow.current().jsession);
			if (userId != 0)
				jsonObject.put("userId", userId);
			jsonObject.put("date", date);
			jsonObject.put("style", 1);
			jsonObject.put("channelId", channelId);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		String returnValue = jsonObject.toString();
		Log.e("GetChannelDetailTask", returnValue);
		return returnValue;
	}

	/**
	 * 
	 * @param list
	 * @param s
	 * @return
	 */
	private String parse(ChannelData channelData, ArrayList<CpData> list,
			String s) {

		int errCode = 1;
		try {
			JSONObject jsonObject = new JSONObject(s);
			if (jsonObject.has("code")) {
				errCode = jsonObject.getInt("code");
			} else if (jsonObject.has("errcode")) {
				errCode = jsonObject.getInt("errcode");
			} else if (jsonObject.has("errorCode")) {
				errCode = jsonObject.getInt("errorCode");
			}
			if (jsonObject.has("jsession")) {
				UserNow.current().jsession = jsonObject.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				JSONArray content = jsonObject.getJSONArray("content");
				if (jsonObject.has("webPlay")) {
					JSONObject webPlay = jsonObject.getJSONObject("webPlay");
					JSONArray play = webPlay.getJSONArray("play");
					ArrayList<NetPlayData> netPlayDatas = new ArrayList<NetPlayData>();
					for (int x = 0; x < play.length(); x++) {
						NetPlayData netPlayData = new NetPlayData();
						JSONObject playItem = play.getJSONObject(x);
						netPlayData.name = playItem.optString("name");
						netPlayData.pic = playItem.optString("pic");
						netPlayData.url = playItem.optString("url");
						netPlayData.videoPath = playItem.optString("videoPath");
						netPlayDatas.add(netPlayData);
					}
					channelData.netPlayDatas = netPlayDatas;
				}
				for (int i = 0; i < content.length(); i++) {
					CpData p = new CpData();
					JSONObject item = content.getJSONObject(i);
					p.programId = item.getString("b");
					p.name = item.getString("a");
					p.topicId = item.getString("c");
					p.startTime = item.getString("e");
					p.endTime = item.getString("f");
					p.type = item.getInt("g");
					p.id = item.getInt("i");
					p.isPlaying = initPlayingType(p.startTime, p.endTime);

					// TODO: 仅用于机锋
					// if (p.name.contains("喜羊羊")) {
					// p.name = "没有节目数据";
					// p.programId = "0";
					// p.topicId = "0";
					// }

					if (p.isPlaying == 0)
						ChannelNewData.current.nowPlayingItemPosition = i;

					p.order = content.getJSONObject(i).getInt("o");
					list.add(p);
				}
			} else {
				return jsonObject.getString("msg");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return "parseErr";
		}
		return null;
	}

	private int whichDayType;

	// 0直播，1回看， 2尚未播放
	private int initPlayingType(String startS, String endS) {
		if (whichDayType == -1) {
			return 1;
		} else if (whichDayType == 1) {
			return 2;
		} else {
			int start = Integer.parseInt(startS.replace(":", ""));
			int end = Integer.parseInt(endS.replace(":", ""));
			SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
			int now = Integer.parseInt(sdf.format(new Date()));
			if (now > start && now < end) {
				return 0;
			} else if (now < start) {
				return 2;
			} else if (now > end) {
				return 1;
			}
		}

		return 3;
	}
}
