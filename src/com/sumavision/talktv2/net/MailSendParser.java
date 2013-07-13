package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.MailData;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.user.UserOther;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-6-14
 * @description 私信列表解析类
 * @changeLog 修改为2.2版本 by 李梦思 2012-12-24 以后删除
 */
public class MailSendParser extends JSONParser {
	private UserOther uo;

	public MailSendParser(UserOther uo) {
		this.uo = uo;
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
				uo.mailCount = content.getInt("mailCount");
				JSONArray mails = content.getJSONArray("mail");
				List<MailData> lm = new ArrayList<MailData>();
				for (int i = 0; i < mails.length(); ++i) {
					JSONObject mail = mails.getJSONObject(i);
					MailData m = new MailData();
					// content.mail[].id long 私信主键id
					// content.mail[].content string 私信文字内容
					// content.mail[].pic string 私信图片绝对路径
					// content.mail[].createTime string 私信发生时间
					// content.mail[].sendUserId long 发送用户id
					// content.mail[].sendUserPic string 发送用户头像绝对路径
					// content. mail[].sendUserName string 发送用户名称
					m.id = mail.getLong("id");
					m.content = mail.getString("content");
					m.pic = mail.getString("pic");
					m.timeStemp = mail.getString("createTime");
					m.sid = mail.getInt("sendUserId");
					m.sUserName = mail.getString("sendUserName");
					m.sUserPhoto = mail.getString("sendUserPic");
					if (m.sid == UserNow.current().userID)
						m.isFromSelf = true;
					else
						m.isFromSelf = false;
					lm.add(m);
				}
				// 用户所有私信存在自己类中，与某个用户私信存在其他类中
				uo.setMail(lm);
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
