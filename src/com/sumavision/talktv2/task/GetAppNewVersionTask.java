package com.sumavision.talktv2.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.VersionData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetUtil;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.AppUtil;
import com.sumavision.talktv2.utils.Constants;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2012-12-29
 * @description 获取软件新版本信息
 * @changLog
 */
public class GetAppNewVersionTask extends AsyncTask<Object, Void, String> {
	private final String TAG = "GetAppNewVersionTask";
	private NetConnectionListener listener;
	private final String method = "versionLatest";
	private VersionData versionData;

	@Override
	protected String doInBackground(Object... params) {
		Context context;
		String data;
		context = (Context) params[0];
		listener = (NetConnectionListener) params[1];
		versionData = (VersionData) params[2];
		data = getRequestData(context);
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, data);
		listener.onNetBegin(method);
		String s = NetUtil.execute(context, data, null);
		if (s != null) {
			if (OtherCacheData.current().isDebugMode)
				Log.e(TAG, s);
			String result = parse(s);
			return result;
		} else {
			return null;
		}
	}

	@Override
	protected void onCancelled() {
		listener.onCancel(method);
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "canceled");
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {

		try {
			listener.onNetEnd(result, method);
		} catch (NullPointerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "finished");
		super.onPostExecute(result);
	}

	public String getRequestData(Context context) {
		String vid = AppUtil.getAppVersionId(context);
		JSONObject holder = new JSONObject();
		try {
			if (vid != null) {
				holder.put("vid", vid);
			}
			holder.put("client", JSONMessageType.SOURCE);
			holder.put("method", Constants.versionLatest);
			holder.put("version", JSONMessageType.APP_VERSION);
			holder.put("jsession", UserNow.current().jsession);

		} catch (JSONException e) {
		}
		return holder.toString();
	}

	public String parse(String s) {
		JSONObject jAData = null;
		JSONObject content = null;
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
				if (jAData.has("content")) {
					content = jAData.getJSONObject("content");
					if (content != null) {
						versionData.versionId = content.getString("vid");
						versionData.info = content.getString("info");
						versionData.pubDate = content.getString("pubDate");
						versionData.size = content.getInt("size");
						versionData.downLoadUrl = content.getString("url");
					}
				} else {
					msg = jAData.getString("msg");
				}
			} else {
				msg = jAData.getString("msg");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			msg = null;
		}
		return msg;
	}

}
