package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sumavision.talktv2.data.MailData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.net.JSONMessageType;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-6-18
 * @description 私信用户列表解析类
 * @changeLog 修改为2.2版本 by 李梦思 2012-12-24
 */
public class MailBoxParser extends JSONParser {

	private Context context;

	public MailBoxParser() {

	}

	public MailBoxParser(Context context) {
		this.context = context;
	}

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
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				JSONObject content = jAData.getJSONObject("content");
				JSONArray mailUsers = content.getJSONArray("mailUser");
				UserNow.current().mailCount = content.getInt("mailUserCount");
				List<MailData> lm = new ArrayList<MailData>();
				savePreference(UserNow.current().mailCount);
				for (int i = 0; i < mailUsers.length(); ++i) {
					JSONObject mailUser = mailUsers.getJSONObject(i);
					MailData m = new MailData();
					// content. mailUser[].id long 最新私信主键id
					// content. mailUser[].content string 最新私信文字内容
					// content. mailUser[].createTime string 最新私信发生时间
					// content. mailUser[].userId long 用户id
					// content. mailUser[].otherUserId long 其他用户id
					// content. mailUser[].otherUserPic string 其他用户头像绝对路径
					// content. mailUser[].otherUserName string 其他用户名称
					m.id = mailUser.getLong("id");
					m.content = mailUser.getString("content");
					m.timeStemp = mailUser.getString("createTime");
					m.sid = mailUser.getInt("otherUserId");
					m.sUserName = mailUser.getString("otherUserName");
					m.sUserPhoto = mailUser.getString("otherUserPic");
					lm.add(m);
				}
				if (UserNow.current().getMail() != null) {
					UserNow.current().setMail(lm);
				} else
					UserNow.current().setMail(lm);
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

	private void savePreference(int count) {
		if (context != null) {
			SharedPreferences spUser = context.getSharedPreferences("userInfo",
					0);
			Editor spEd = spUser.edit();
			spEd.putInt("messageCount", count);
			spEd.commit();
		}
	}
}
