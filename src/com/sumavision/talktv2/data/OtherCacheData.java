package com.sumavision.talktv2.data;

import java.util.List;

import com.sumavision.talktv2.user.User;

/**
 * @author 郭鹏
 * @createTime
 * @description 非用户信息实体类
 * @changeLog
 */
public class OtherCacheData {

	private static OtherCacheData current;
	// 热门搜索节目数量
	public int playCount;
	// 给节目打分
	public String mark;
	// 查询数量
	public int pageCount = 15;
	// 查询起始位置
	public int offset = 0;
	// 欢迎页图片ID
	public int welcomeId;

	public boolean isFromProgram;
	// 欢迎页图片地址
	public String welcomeUrl;
	// 客户端当前首页Id
	public int logoVersion = 0;
	// 地区
	public int area = 0;
	// 类型:查询的用户动态信息类型
	public int style = 0;
	// 类型:
	public int type = 0;
	// 时间段
	public int timePart = 0;
	// 图片base64编码
	public String pic_Base64;
	// 查询日期
	// public String date = WeekGuideData.current().getNowWeekDate();
	// 查询时间段开始时间
	public String time1;
	// 查询时间段结束时间
	public String time2;
	// 用户最后查询的指定节目的评论列表
	private List<CommentData> allComments;
	// 明星秀列表
	private List<StarData> starShow;

	// 搜索结果
	// 搜索到的演员列表
	private List<StarData> searchResultS;

	// 客厅底部分类,用户类型
	private List<User> lrUserData;
	// 客厅底部分类,明星类型
	private List<StarData> lrStarData;
	// 话题签到用户
	private List<SignData> signUserList;
	// 话题签到用户
	private List<PhotoData> signUserPic;
	// 皮肤
	private List<PicAndTxtData> skin;
	// 推荐软件
	private List<AppData> app;

	// 正在保存栏目分类
	public boolean isSavingDB = false;
	// 需要继续加载节目单
	public boolean needLoadPlayBill = false;
	// 第三方token
	public String token;
	// token有效时间 单位：小时
	public int validTime;
	// 栏目内容数量
	public int count;
	// 系统为低配手机
	public boolean isLowAbilityDevice = false;
	// 系统为调试模式
	public boolean isDebugMode = true;
	// 是否是机锋版
	public boolean isForGFan = false;
	// 是否显示经验
	public boolean isShowExp = true;
	// 机锋市场版本
	public boolean forGFanRelease = false;

	public int currentCity;
	// SKIA BUG
	public boolean isLowSKIAVersion = false;
	// 开放平台类型 0:sina,1:tencent,3:souhu,4:renren,5:kaixin
	public int openType = 0;
	// 1=新浪微博；2=QQ；3=人人；4=腾讯微博；5=开心网；6=飞信；7=MSN；8=搜狐微博
	public int synType = 0;

	// 热搜词数量
	public int keywordsCount;
	// 热搜词
	public String[] keywords;

	// 用户从用户中心登陆
	public boolean isFromMyActivityToLogin = false;
	// 用户从用户中心登陆切登陆界面未关闭
	public boolean isFromMyActivityToLoginNotClose = false;
	// 节目搜索结果
	public List<VodProgramData> listSearchResult;

	// 是否注销了
	public static boolean isNeedUpdateActivityPageAll;
	// 是否需要更改好友页面
	public static boolean isNeedUpdateFriendActivity;

	// 图片大图展示数组
	public String[] bigPics;
	public int bigPicPosition = 0;

	public List<User> getLrUserData() {
		return lrUserData;
	}

	public void setLrUserData(List<User> lrUserData) {
		this.lrUserData = lrUserData;
	}

	public List<StarData> getLrStarData() {
		return lrStarData;
	}

	public void setLrStarData(List<StarData> lrStarData) {
		this.lrStarData = lrStarData;
	}

	public List<StarData> getSearchResultS() {
		return searchResultS;
	}

	public void setSearchResultS(List<StarData> searchResultS) {
		this.searchResultS = searchResultS;
	}

	public List<CommentData> getAllComments() {
		return allComments;
	}

	public void setAllComments(List<CommentData> allComments) {
		this.allComments = allComments;
	}

	public List<StarData> getStarShow() {
		return starShow;
	}

	public void setStarShow(List<StarData> starShow) {
		this.starShow = starShow;
	}

	public static OtherCacheData current() {
		if (current == null) {
			current = new OtherCacheData();
		}
		return current;
	}

	public void setSignUserList(List<SignData> signUserList) {
		this.signUserList = signUserList;
	}

	public List<SignData> getSignUserList() {
		return signUserList;
	}

	public void setSignUserPic(List<PhotoData> signUserPic) {
		this.signUserPic = signUserPic;
	}

	public List<PhotoData> getSignUserPic() {
		return signUserPic;
	}

	public void setSkin(List<PicAndTxtData> skin) {
		this.skin = skin;
	}

	public List<PicAndTxtData> getSkin() {
		return skin;
	}

	public void setApp(List<AppData> app) {
		this.app = app;
	}

	public List<AppData> getApp() {
		return app;
	}

}
