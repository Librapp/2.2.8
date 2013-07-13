package com.sumavision.talktv2.user;

/**
 * @author 郭鹏
 * @createTime 2012-5-31
 * @description 用户修改资料时，缓存
 * @changeLog
 */
public class UserModify {

	private static UserModify current;

	// 新用户名
	public String nameNew = "";
	// 旧密码
	public String passwdOld = "";
	// 一句话介绍
	public String intro = "";
	// 新密码
	public String passwdNew = "";
	// 新头像
	public String pic = "";
	// 性别1男2女
	public int gender;
	// 图片base64编码
	public String pic_Base64;
	// Email
	public String eMail;
	// MSN
	public String msn;
	// 签名
	public String sign;

	public int nameNewFlag = 0;

	public int introflag = 0;

	public int signFlag = 0;

	public int genderFlag = 0;

	public int picFlag = 0;

	public int passwdNewFlag = 0;

	public int eMailFlag = 0;

	public int msnFlag = 0;

	public static UserModify current() {
		if (current == null) {
			current = new UserModify();
		}
		return current;
	}
}
