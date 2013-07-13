package com.sumavision.talktv2.data;

/**
 * @author 李梦思
 * @createTime 2012-6-14
 * @description 签到实体类
 * @changeLog
 */
public class SignData {
	private static SignData current;
	// 节目Id
	public int progId;
	// Id
	public int id;
	// 签到节目次数
	public int count = 0;
	// 签到评分
	public int mark = 0;
	// 节目名称
	public String name;
	// 内容
	public String content = "";
	// 节目图片
	public String photo;
	// 签到日期
	public String date;
	// 用户Id
	public int userId;
	// 签到对象类型：1=节目，2=演员，3=微影视
	public int type;

	public static SignData current() {
		if (current == null) {
			current = new SignData();
		}
		return current;
	}
}
