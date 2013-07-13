package com.sumavision.talktv2.data;

import java.util.List;

/**
 * @author 李梦思
 * @createTime 2012-6-14
 * @description 私信实体类
 * @changeLog
 */
public class MailData {
	private static MailData current;
	// 内容
	public String content = "";
	// 接收用户Id
	public int rid;
	// 发送用户Id
	public int sid;
	// Id
	public long id;
	// 0表示未阅读，1表示已经阅读
	public int flag = 0;
	// 发信时间
	public String timeStemp;
	// 接收用户名
	public String rUserName;
	// 接收用户头像
	public String rUserPhoto;
	// 发送用户名
	public String sUserName;
	// 发送用户头像
	public String sUserPhoto;
	// 内容图片
	public String pic = "";
	// 常用短语
	private List<String> phrases;
	// 是否是自己发的
	public boolean isFromSelf;

	public static MailData current() {
		if (current == null) {
			current = new MailData();
		}
		return current;
	}

	public void setPhrases(List<String> phrases) {
		this.phrases = phrases;
	}

	public List<String> getPhrases() {
		return phrases;
	}

}
