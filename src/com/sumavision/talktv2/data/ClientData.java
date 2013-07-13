package com.sumavision.talktv2.data;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-7
 * @description 其他平台账号实体类
 * @changeLog
 */
public class ClientData {
	public long id;
	// 微博类型
	public static final int SINA = 1;
	public static final int TECENT = 4;
	public static final int RENREN = 3;
	public static final int KAIXIN = 5;

	public int type = SINA;
	public String name;
	public String token;
	public String expire;
	public static ClientData current = new ClientData();
}
