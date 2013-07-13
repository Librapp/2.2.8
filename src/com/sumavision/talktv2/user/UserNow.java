package com.sumavision.talktv2.user;

import java.util.List;

import android.graphics.Bitmap;

import com.sumavision.talktv2.data.AdvertisementData;
import com.sumavision.talktv2.data.AppData;
import com.sumavision.talktv2.data.BadgeData;
import com.sumavision.talktv2.data.ChaseData;
import com.sumavision.talktv2.data.ClientData;
import com.sumavision.talktv2.data.CommentData;
import com.sumavision.talktv2.data.EventData;
import com.sumavision.talktv2.data.MailData;
import com.sumavision.talktv2.data.MessageData;
import com.sumavision.talktv2.data.SignData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.JSONMessageType;

/**
 * @author 郭鹏
 * @createTime 2012-5-31
 * @description 当前用户信息实体类
 * @changeLog
 */
public class UserNow {

	private static UserNow current;

	// 用户名
	public String name = "";
	// 昵称
	public String nickName = "";
	// 密码
	public String passwd = "123456";
	// 等级
	public String level;
	// 电子邮件
	public String eMail = "";
	// 用户ID,用于网络服务器请求
	public int userID = 0;
	public int userIDTemp = 0;
	// 登录状态
	public boolean isLogedIn = false;
	// 粉过节目数量
	public int favoriteCount = 0;
	// 粉友数量
	public int fansCount = 0;
	// 未读私信数量
	public int unreadMail = 0;
	// 私信数量
	public int mailCount = 0;
	// 用户头像URL
	public String iconURL = "";
	// 网络超时消息
	public boolean isTimeOut = true;
	// 奖章数量
	public int badgeCount = 0;
	// 已取得的奖章数量
	public int badgesCount;

	public int channelCount;
	// 签到次数
	public int checkInCount = 0;
	// 评论次数
	public int commentCount = 0;
	// 用户最后查询的指定节目的评论的条数
	public int allCommentsCount;
	// 服务器返回的出错消息
	public String errMsg = JSONMessageType.SERVER_NETFAIL;
	// 标记是否为刷新列表
	public boolean isRefresh = false;
	// 标记是否为加载更多
	public boolean isMore = false;
	// 用户是否为登陆软件的用户
	public boolean isSelf = true;
	// 外网地址
	public static String myServerAddress = "http://59.151.82.78/clientProcess.do";

	public static String bitmapPath;

	// 微博类型
	public String openType = "sina";
	// 当前是否有网络
	public boolean hasNetNow = true;
	// 新版本节目指南界面使用
	public boolean isGuideLoading = false;
	// 启动时候是否显示帮助页
	public boolean isShowHelp = true;
	// 非启动时打开帮助
	public boolean isFromStartOpenHelp = false;
	// 欢迎界面图片
	public String welcomeUrl;
	// 欢迎界面图片id
	public int welcomeId;
	// 用户积分
	public int point;
	// 性别1=男2=女
	public int gender = 1;
	// IMEI
	public String imei;
	// 当前是否具有网络
	public boolean isHasNetNow;
	// 个性签名
	public String signature = "";

	// 好友事件数
	public int friendEventCount = 0;
	// 全部事件数
	public int eventCount = 0;

	// 从个人中心打开帮助
	public boolean isFromUserCenter2Help = false;

	public int privateMessageAllCount;

	public int privateMessageOnlyCount;
	// 手机号码
	public String phone = "";
	// 真实姓名
	public String realName = "";
	// 地址
	public String address = "";
	// 邮编
	public String postcode = "";
	// sessionID
	public String sessionID = "";
	// 记录网络通信的errorcode类型
	public int errorCode;
	// 打招呼的用户数量
	public int greetUserCount;
	// 用于统计用户数量
	public String jsession = "";
	public boolean useProxy = false;
	public Bitmap tempBitmap;
	// Mac地址
	public String mac = "";
	public String msn = "";
	// 用户动作获得的积分
	public int getPoint = 0;
	// 当前拍的图片路径
	public String picPath = "";
	// 获得经验
	public int getExp = 0;
	// 总经验值
	public int exp = 0;
	// 升级
	public int lvlUp = 0;
	// 搜索数量
	public int searchCount = 0;

	// <-------------------------复杂数据类型--------------------------------------
	// 与指定用户私信列表
	private List<CommentData> privateMessageList;
	// 私信收件箱列表
	private List<CommentData> privateMessageAllList;
	// 粉友列类表
	private List<User> fansList;
	// 待取消的粉友列表
	private List<User> waitDeleteAtten;
	// 奖章URL地址列表
	private List<String> badgeURL;
	// 用户自己发布的评论列表
	private List<CommentData> ownComments;
	// 待删除评论
	private List<CommentData> waitDeleteComments;
	// 已取得的奖章
	private List<BadgeData> badgesGained;
	// 用户得到的钱10个奖章
	private List<BadgeData> listBadges;
	// 投放广告
	private List<AdvertisementData> listAdvertisement;
	// 待删除项目Id
	private List<Integer> waitDeleteId;
	// 私信
	private List<MailData> mail;
	// 签到
	private List<SignData> signList;
	// 招呼
	private List<MessageData> message;
	// 好友动态
	private List<User> friendEvent;
	// 关注数量
	public int friendCount;
	// 评论数量
	public int talkCount;
	// 追剧数量
	public int chaseCount;
	// 预约数量
	public int remindCount;
	// 被@数量
	public int atMeCount;
	// 被回复数量
	public int replyMeCount;
	// 其他平台账号
	private List<ClientData> clients;
	// 推荐应用
	private List<AppData> apps;
	// 关注列表
	private List<User> friend;
	// 搜索结果列表
	private List<User> searchUser;
	// 追剧列表
	private List<ChaseData> chase;
	// 预约列表
	private List<VodProgramData> remind;
	// 是否获取详情
	public int infoFlag = 0;
	// 所有人事件
	private List<EventData> allEvent;
	// 获取事件0简略1完整
	public int eventStyle = 1;
	// 经度
	public String lat = "90.00";
	// 纬度
	public String lon = "90.00";
	// 领先多少用户
	public String badgeRate;
	// 账号类型 绑定的电视粉账号类型：1=注册新账号，2=已有老账号。
	public int userType = 1;
	// 第三方帐号类型 1=新浪微博；2=QQ；3=人人；4=腾讯微博；5=开心网；6=飞信；7=MSN；8=搜狐微博
	public int thirdType = 1;
	// 第三方账号token
	public String thirdToken;
	// 第三方账号Id
	public String thirdUserId;
	// 第三方账号头像
	public String thirdUserPic;
	// 第三方账号签名
	public String thirdSignature;
	// 第三方账号有效时间
	public String validTime;
	// 被@列表
	private List<CommentData> talkAtList;
	// 被回复列表
	private List<CommentData> replyList;
	// 新获得的徽章
	private List<BadgeData> newBadge;

	public List<BadgeData> getListBadges() {
		return listBadges;
	}

	public void setListBadges(List<BadgeData> lb) {
		this.listBadges = lb;
	}

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

	public List<BadgeData> getBadgesGained() {
		return badgesGained;
	}

	public void setBadgesGained(List<BadgeData> badgesGained) {
		this.badgesGained = badgesGained;
	}

	public static UserNow current() {
		if (current == null) {
			current = new UserNow();
		}
		return current;
	}

	public String getMyServerAddress() {
		return myServerAddress;
	}

	public void setListAdvertisement(List<AdvertisementData> listAdvertisement) {
		this.listAdvertisement = listAdvertisement;
	}

	public List<AdvertisementData> getListAdvertisement() {
		return listAdvertisement;
	}

	public void setWaitDeleteId(List<Integer> waitDeleteId) {
		this.waitDeleteId = waitDeleteId;
	}

	public List<Integer> getWaitDeleteId() {
		return waitDeleteId;
	}

	public void setMail(List<MailData> mail) {
		this.mail = mail;
	}

	public List<MailData> getMail() {
		return mail;
	}

	public void setSignList(List<SignData> signList) {
		this.signList = signList;
	}

	public List<SignData> getSignList() {
		return signList;
	}

	public void setMessage(List<MessageData> message) {
		this.message = message;
	}

	public List<MessageData> getMessage() {
		return message;
	}

	public void setFriendEvent(List<User> fe) {
		this.friendEvent = fe;
	}

	public List<User> getFriendEvent() {
		return friendEvent;
	}

	public void setAllEvent(List<EventData> allEvent) {
		this.allEvent = allEvent;
	}

	public List<EventData> getAllEvent() {
		return allEvent;
	}

	public void setClients(List<ClientData> clients) {
		this.clients = clients;
	}

	public List<ClientData> getClients() {
		return clients;
	}

	public void setApps(List<AppData> apps) {
		this.apps = apps;
	}

	public List<AppData> getApps() {
		return apps;
	}

	public void setFriend(List<User> friend) {
		this.friend = friend;
	}

	public List<User> getFriend() {
		return friend;
	}

	public void setChase(List<ChaseData> chase) {
		this.chase = chase;
	}

	public List<ChaseData> getChase() {
		return chase;
	}

	public void setRemind(List<VodProgramData> lu) {
		this.remind = lu;
	}

	public List<VodProgramData> getRemind() {
		return remind;
	}

	public void setSearchUser(List<User> searchUser) {
		this.searchUser = searchUser;
	}

	public List<User> getSearchUser() {
		return searchUser;
	}

	public void setTalkAtList(List<CommentData> talkAtList) {
		this.talkAtList = talkAtList;
	}

	public List<CommentData> getTalkAtList() {
		return talkAtList;
	}

	public void setReplyList(List<CommentData> replyList) {
		this.replyList = replyList;
	}

	public List<CommentData> getReplyList() {
		return replyList;
	}

	public void setNewBadge(List<BadgeData> newBadge) {
		this.newBadge = newBadge;
	}

	public List<BadgeData> getNewBadge() {
		return newBadge;
	}
}
