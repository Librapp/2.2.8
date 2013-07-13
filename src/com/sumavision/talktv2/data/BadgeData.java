package com.sumavision.talktv2.data;

/**
 * 
 * @author 郭鹏
 * @createTime 2012-5-31
 * @description 奖章实体类
 * @changeLog
 * 
 */
public class BadgeData {

	public static BadgeData current = new BadgeData();
	public String name;
	public String intro;
	public String picPath;
	public String createTime;
	public String timeLimit;
	public String fileName = "";
	public int number;
	// 大图片
	public String picPath_b;
	// 主键Id
	public long id;
	// 奖章Id
	public long badgeId;

	public static BadgeData current() {
		if (current == null) {
			current = new BadgeData();
		}
		return current;
	}

}
