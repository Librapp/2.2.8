package com.sumavision.talktv2.data;


/**
 * 
 * @author 郭鹏
 * @createTime
 * @description 用户反馈数据
 * @changeLog
 * 
 */
public class FeedbackData {
	private static FeedbackData current;
	// 反馈类型
	public int type;
	// 反馈类型文字描述
	public String typeStr;
	// 反馈内容
	public String content;
	// 反馈用户邮箱
	public String email;

	public static FeedbackData current() {
		if (current == null) {
			current = new FeedbackData();
		}
		return current;
	}
}
