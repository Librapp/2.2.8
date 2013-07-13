package com.sumavision.talktv2.data;

/**
 * @author 李梦思
 * @createTime 2012-6-15
 * @description 招呼信息实体类
 * @changeLog
 */
public class MessageData {
	private static MessageData current;

	// 发送用户名
	public String sname;
	// 发送用户Id
	public int sid;
	// 发送用户头像
	public String sPhoto;
	// 内容
	public String content;
	// 时间
	public String time;
	// 

	public static MessageData current() {
		if (current == null) {
			current = new MessageData();
		}
		return current;
	}
}
