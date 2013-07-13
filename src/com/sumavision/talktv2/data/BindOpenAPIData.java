package com.sumavision.talktv2.data;

/**
 * 
 * @author 郭鹏
 * @createTime
 * @description 微搏绑定数据
 * @changeLog
 * 
 */
public class BindOpenAPIData {

	private static BindOpenAPIData current;
	// 绑定后在电视粉平台新注册的的用户名字
	public String name;
	// 绑定后在电视粉平台新注册的的用户密码
	public String passwd = "123456";
	// 绑定后在电视粉平台新注册的的用户邮箱
	public String eMail = "";
	// 绑定的用户ID
	public int id;
	// 开放平台的用户ID
	public String openId;
	// 开放平台Token
	public String token;
	// 开放平台Token过期时间
	public String token_expires_in;
	// 开放平台用户名
	public String openName = "";
	// 开放平台头像
	public String openUserIcon = "";
	// 开放平台签名
	public String signature = "";
	// 第三方帐号类型：1=新浪微博；2=QQ；3=人人；4=腾讯微博；5=开心网；6=飞信；7=MSN
	public int openType = 1;
	// 第三方帐号类型：1=新浪微博；2=QQ；3=人人；4=腾讯微博；5=开心网；6=飞信；7=MSN
	public int nowLoginOpenType = -1;
	public boolean isOpenTypeLogin = false;
	// 微博类型
	public static final int SINA = 1;
	public static final int TECENT = 4;
	public static final int RENREN = 3;
	public static final int KAIXIN = 5;

	public void initUserInfo() {
		long time = System.currentTimeMillis();
		name = "电视粉丝" + time;
		passwd = "123456";
		// eMail = time + "@163.com";
	}

	public static BindOpenAPIData current() {
		if (current == null) {
			current = new BindOpenAPIData();
		}
		return current;
	}
}
