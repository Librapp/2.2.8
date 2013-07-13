package com.sumavision.talktv2.data;

/**
 * 
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-4
 * @description 奖章实体类
 * @changeLog
 * 
 */
public class MedalData {
	// 名字
	public String name;
	// 介绍
	public String introdruction;
	// 图片小
	public String picS;
	// 图片大
	public String picB;
	// 领取时间
	public String time;
	// 相关节目Id
	public String programId;
	// Id
	public int id;
	// 状态
	public int state;
	// 类型
	public int type;
	// 多少人领取
	public int userCount;
	// 徽章相关活动

	public static MedalData current = new MedalData();

}
