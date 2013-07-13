package com.sumavision.talktv2.user;

import java.util.List;

import android.graphics.drawable.Drawable;

import com.sumavision.talktv2.data.EventData;

/**
 * @author 郭鹏
 * @createTime 2012-5-31
 * @description 粉友，黑名单等用户类
 * @changeLog
 */
public class User {

	private static User current;
	// 用户名
	public String name;
	// 昵称
	public String nickName;
	// 邮箱
	public String eMail;
	// userId
	public int userId = 0;
	// 头像URL地址ַ
	public String iconURL;
	// 用户类型：粉丝，关注
	public int type;
	// 一句话介绍
	public String intro;
	// 密码
	public String passWord;
	// 参加游戏时间
	public String playTime;
	// 参加PK次数
	public int pkCount;
	// 级别
	public String level = "";
	// 积分
	public int point;
	// 签名
	public String signature;
	// 粉友数量
	public int fansCount;
	// 加好友/黑名单时间
	public String friendTime;
	// 性别1=男2=女
	public int gender = 0;
	// 奖章数量
	public int budgeCount;
	// 用户当前选中的背景
	public int nowBg;
	// 用户SDCARD 背景图
	public Drawable sdcardThemeDrawable;
	public boolean needChangeBackground = false;
	// 是否为用户好友
	public int isFriend = 0;
	// sessionID
	public String sessionID = "";
	// 是否为粉丝
	public int isFans;
	// 动态信息
	private List<EventData> event;

	public static User current() {
		if (current == null) {
			current = new User();
		}
		return current;
	}

	public void setEvent(List<EventData> event) {
		this.event = event;
	}

	public List<EventData> getEvent() {
		return event;
	}
}
