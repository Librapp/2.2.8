package com.sumavision.talktv2.data;

import com.weibo.sdk.android.Weibo;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-10
 * @description SINA授权信息
 * @changLog
 */
public class SinaData {
	private static final String CUSTOMER_KEY = "2064721383";
	private static final String REDIRECT_URL = "http://www.tvfan.cn";

	public static String accessToken = "";
	public static String expires_in = "";
	// 是否已经绑定
	public static boolean isSinaBind = false;
	// 微博文字
	public static String content;
	// 微博图片
	public static String pic;
	// 微博类型0文字1带图片
	public static int type = 0;
	// SINA用户ID
	public static String id;
	// SINA用户名
	public static String name;
	// SINA用户头像
	public static String icon;
	// SINA用户性别
	public static int gender = 1;
	// SINA用户签名
	public static String description = "电视粉";

	private static Weibo weibo = null;

	public static Weibo weibo() {
		if (null == weibo) {
			weibo = Weibo.getInstance(CUSTOMER_KEY, REDIRECT_URL);
		}
		return Weibo.getInstance(CUSTOMER_KEY, REDIRECT_URL);
	}
}
