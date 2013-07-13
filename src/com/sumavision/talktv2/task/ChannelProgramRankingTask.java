package com.sumavision.talktv2.task;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.NetPlayData;
import com.sumavision.talktv2.data.ShortChannelData;
import com.sumavision.talktv2.data.TypeChannelData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.talktv2.utils.Constants;

public class ChannelProgramRankingTask extends
		AsyncTask<Object, Integer, Integer> {
	private NetConnectionListenerNew listener;
	private String method;
	private boolean isLoadMore;
	private String errMsg = null;

	public ChannelProgramRankingTask(NetConnectionListenerNew listener,
			boolean isLoadMore) {
		method = Constants.channelProgramRanking;
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
		int color = context.getResources().getColor(R.color.red);
		span = new ForegroundColorSpan(color);
		int first = (Integer) params[2];
		int count = (Integer) params[3];
		int cctv = (Integer) params[4];
		String data = generateRequset(UserNow.current().userID, first, count,
				cctv);
		if (data != null) {
			String result = NetUtil.execute(context, data, null);
			if (result == null) {
				return Constants.fail_no_net;
			} else {
				@SuppressWarnings("unchecked")
				ArrayList<TypeChannelData> list = (ArrayList<TypeChannelData>) params[1];
				String msg = parse(list, result);
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
	private String generateRequset(int userId, int first, int count, int cctv) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("method", method);
			jsonObject.put("version", JSONMessageType.APP_VERSION);
			jsonObject.put("client", JSONMessageType.SOURCE);
			jsonObject.put("jsession", UserNow.current().jsession);
			if (userId != 0)
				jsonObject.put("userId", userId);
			jsonObject.put("first", first);
			jsonObject.put("count", count);
			jsonObject.put("cctv", cctv);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		String returnValue = jsonObject.toString();
		Log.e("ChannelProgramRankTask", returnValue);
		return returnValue;
	}

	/**
	 * 
	 * @param list
	 * @param s
	 * @return
	 */
	private String parse(ArrayList<TypeChannelData> list, String s) {

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
				JSONObject content = jsonObject.getJSONObject("content");
				if (content.has("column")) {
					JSONArray typeChannelArray = content.getJSONArray("column");
					for (int i = 0; i < typeChannelArray.length(); ++i) {
						JSONObject channelColumnObject = typeChannelArray
								.getJSONObject(i);
						TypeChannelData typeChannelData = new TypeChannelData();
						typeChannelData.channelTypeName = channelColumnObject
								.optString("name");
						if (channelColumnObject.has("items")) {
							ArrayList<ShortChannelData> programList = new ArrayList<ShortChannelData>();
							JSONArray channelProgramItems = channelColumnObject
									.getJSONArray("items");
							for (int j = 0; j < channelProgramItems.length(); ++j) {
								JSONObject channelProgram = channelProgramItems
										.getJSONObject(j);
								ShortChannelData tempShortChannelData = new ShortChannelData();
								tempShortChannelData.programId = channelProgram
										.optInt("programId");
								tempShortChannelData.channelId = channelProgram
										.optInt("channelId");
								String time = channelProgram
										.optString("timeInfo");
								if (time != null) {
									tempShortChannelData.timeInfo = time;
									setSpannelStyle(time, tempShortChannelData);
								}
								tempShortChannelData.programInfo = channelProgram
										.optString("programInfo");
								tempShortChannelData.topicId = channelProgram
										.optString("topicId");
								tempShortChannelData.channelName = channelProgram
										.optString("channelName");
								tempShortChannelData.programName = channelProgram
										.optString("programTitle");
								tempShortChannelData.channelPicUrl = channelProgram
										.optString("channelPhoto");
								tempShortChannelData.cpId = channelProgram
										.optInt("cpId");
								tempShortChannelData.flagMyChannel = channelProgram
										.optInt("flagMyChannel") == 1 ? true
										: false;
								tempShortChannelData.livePlay = channelProgram
										.optInt("livePlay") == 1 ? true : false;
								tempShortChannelData.channelType = channelProgram
										.optInt("channelType");
								if (channelProgram.has("play")) {
									JSONArray play = channelProgram
											.getJSONArray("play");
									ArrayList<NetPlayData> netPlayDatas = new ArrayList<NetPlayData>();
									for (int x = 0; x < play.length(); ++x) {
										NetPlayData netPlayData = new NetPlayData();
										JSONObject playItem = play
												.getJSONObject(x);
										netPlayData.name = playItem
												.optString("name");
										netPlayData.pic = playItem
												.optString("pic");
										netPlayData.url = playItem
												.optString("url");
										netPlayData.videoPath = playItem
												.optString("videoPath");
										netPlayData.channelName = tempShortChannelData.channelName;
										netPlayDatas.add(netPlayData);
									}
									tempShortChannelData.netPlayDatas = netPlayDatas;
								}

								// TODO: 仅用于机锋
								if (tempShortChannelData.programName
										.contains("喜羊羊")) {
									tempShortChannelData.programName = "没有节目数据";
									tempShortChannelData.programId = 0;
									tempShortChannelData.topicId = "0";
								}

								programList.add(tempShortChannelData);
							}
							typeChannelData.typeChannelData = programList;
						}

						list.add(typeChannelData);
					}
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

	ForegroundColorSpan span;

	private void setSpannelStyle(String str, ShortChannelData shortChannelData) {
		if (str == null)
			return;
		try {
			int end = str.indexOf("-");
			shortChannelData.spannableTimeInfo = CommonUtils
					.getSpannableString(str, 0, end, span);
		} catch (Exception e) {
			shortChannelData.spannableTimeInfo = null;
		}
	}
}
