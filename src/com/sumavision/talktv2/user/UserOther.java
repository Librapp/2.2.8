package com.sumavision.talktv2.user;

import java.util.List;

import com.sumavision.talktv2.data.BadgeData;
import com.sumavision.talktv2.data.ChaseData;
import com.sumavision.talktv2.data.CommentData;
import com.sumavision.talktv2.data.EventData;
import com.sumavision.talktv2.data.MailData;
import com.sumavision.talktv2.data.VodProgramData;

/**
 * @author 郭鹏
 * @createTime 2012-5-31
 * @description 其他临时用户信息实体类
 * @changeLog
 */
public class UserOther {

	// private static UserOther current;

	public boolean isFromZuxiao;

	// 用户名
	public String name = "";
	// 昵称
	public String nickName = "";
	// 一句话介绍
	public String intro = "";
	// 密码
	public String passwd;
	// 积分
	public String integral;
	// 等级
	public String level;
	// 电子邮件
	public String eMail = "";
	// 用户ID,用于网络服务器请求
	public int userID;
	// 登录状态
	public boolean isLogedIn = false;
	// 粉过节目数量
	public int favoriteCount = 0;
	// 粉友数量
	public int fansCount = 0;
	// 黑名单数量
	public int blackListCount = 0;
	// 用户头像URL
	public String iconURL;
	// 网络超时消息
	public boolean isTimeOut = true;
	// 奖章数量
	public int badgeCount = 0;
	// 签到次数
	public int checkInCount = 0;
	// 评论次数
	public int commentCount = 0;
	// 用户最后查询的指定节目的评论的条数
	public int allCommentsCount;
	// 服务器返回的出错消息
	public String errMsg = "error";
	// 客厅推荐分类类型：0,1,2,3
	public int livingroom_Type;
	// 类型名
	public String hotProgramTypeNmae = "";
	// 谁在看用户数量
	public int whoWatchingCount;
	// 标记是否为刷新列表
	public boolean isRefresh = false;
	// 标记是否为加载更多
	public boolean isMore = false;
	// 用户是否为登陆软件的用户
	public boolean isSelf = true;
	// 版本发布用
	public static String myServerAddress = "http://59.151.82.78/JsonServlet.do";
	// 已取得的奖章数量
	public int badgesCount;
	// 与指定用户间私信数量
	public int privateMessageOnlyCount;
	// 所有私信数量
	public int privateMessageAllCount;
	// 微博类型
	public String openType = "sina";
	// 投票是否需要刷新
	public boolean PollRefreshNeeded = false;
	// 竞猜是否需要刷新
	public boolean RateRefreshNeeded = false;
	// 新版本节目指南界面使用，用于区分通讯类型
	public boolean isGuideNetOver = false;
	// 当前是否有网络
	public boolean hasNetNow = true;
	// 新版本节目指南界面使用
	public boolean isGuideLoading = false;
	// 启动时候是否显示帮助页
	public boolean isShowHelp = true;
	// 是否为好友
	public boolean isFriend = false;
	// 积分
	public int point = 0;
	// 用户个性签名
	public String signature = "就爱看电视";
	// <---------------------------------------------------------------
	// 与指定用户私信列表
	private List<CommentData> privateMessageList;
	// 私信收件箱列表
	private List<CommentData> privateMessageAllList;
	// 粉友列类表
	private List<User> fansList;
	// 待取消的粉友列表
	private List<User> waitDeleteAtten;
	// 黑名单列表
	private List<User> blackList;
	// 待移出黑名单的用户
	private List<User> waitDeleteUsers;
	// 奖章URL地址列表
	private List<String> badgeURL;
	// 用户自己发布的评论列表
	private List<CommentData> ownComments;
	// 待删除评论
	private List<CommentData> waitDeleteComments;
	// 谁在看用户
	private List<User> whoWatchingList;
	// 已取得的奖章
	private List<BadgeData> badgesGained;
	// 与当前用户私信列表
	private List<MailData> mail;
	// 性别 1男2女
	public int gender;
	// 与当前用户私信总数
	public int mailCount;

	public int exp;

	public int friendCount;

	public int talkCount;

	public int chaseCount;

	public int remindCount;
	// 0 未关注 1关注
	public int isGuanzhu;

	public int isFensi;

	public int eventCount;

	private List<EventData> event;

	public List<CommentData> getPrivateMessageList() {
		return privateMessageList;
	}

	public void setPrivateMessageList(List<CommentData> privateMessageList) {
		this.privateMessageList = privateMessageList;
	}

	public List<CommentData> getPrivateMessageAllList() {
		return privateMessageAllList;
	}

	public void setPrivateMessageAllList(List<CommentData> privateMessageAllList) {
		this.privateMessageAllList = privateMessageAllList;
	}

	public List<User> getFansList() {
		return fansList;
	}

	public void setFansList(List<User> fansList) {
		this.fansList = fansList;
	}

	public List<User> getWaitDeleteAtten() {
		return waitDeleteAtten;
	}

	public void setWaitDeleteAtten(List<User> waitDeleteAtten) {
		this.waitDeleteAtten = waitDeleteAtten;
	}

	public List<User> getBlackList() {
		return blackList;
	}

	public void setBlackList(List<User> blackList) {
		this.blackList = blackList;
	}

	public List<User> getWaitDeleteUsers() {
		return waitDeleteUsers;
	}

	public void setWaitDeleteUsers(List<User> waitDeleteUsers) {
		this.waitDeleteUsers = waitDeleteUsers;
	}

	public List<String> getBadgeURL() {
		return badgeURL;
	}

	public void setBadgeURL(List<String> badgeURL) {
		this.badgeURL = badgeURL;
	}

	public List<CommentData> getOwnComments() {
		return ownComments;
	}

	public void setOwnComments(List<CommentData> ownComments) {
		this.ownComments = ownComments;
	}

	public List<CommentData> getWaitDeleteComments() {
		return waitDeleteComments;
	}

	public void setWaitDeleteComments(List<CommentData> waitDeleteComments) {
		this.waitDeleteComments = waitDeleteComments;
	}

	public List<User> getWhoWatchingList() {
		return whoWatchingList;
	}

	public void setWhoWatchingList(List<User> whoWatchingList) {
		this.whoWatchingList = whoWatchingList;
	}

	public List<BadgeData> getBadgesGained() {
		return badgesGained;
	}

	public void setBadgesGained(List<BadgeData> badgesGained) {
		this.badgesGained = badgesGained;
	}

	// public static UserOther current() {
	// if (current == null) {
	// current = new UserOther();
	// }
	// return current;
	// }

	public String getMyServerAddress() {
		return myServerAddress;
	}

	public void setMail(List<MailData> mail) {
		this.mail = mail;
	}

	public List<MailData> getMail() {
		return mail;
	}

	public void setEvent(List<EventData> event) {
		this.event = event;
	}

	public List<EventData> getEvent() {
		return event;
	}

	private List<VodProgramData> remind;

	public void setRemind(List<VodProgramData> lu) {
		this.remind = lu;
	}

	public List<VodProgramData> getRemind() {
		return remind;
	}

	private List<ChaseData> chase;

	public void setChase(List<ChaseData> chase) {
		this.chase = chase;
	}

	public List<ChaseData> getChase() {
		return chase;
	}
}
