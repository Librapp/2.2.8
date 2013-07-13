package com.sumavision.talktv2.utils;

public class Constants {
	/**
	 * 方法名字常量
	 */
	public static final String searchUser = "userSearch";
	public static final String searchProgram = "programSearch";
	public static final String channelContent = "channelContent";
	public static final String feedbackAdd = "feedbackAdd";
	public static final String guanZhuAdd = "guanZhuAdd";
	public static final String guanZhuCancel = "guanZhuCancel";
	public static final String versionLatest = "versionLatest";
	public static final String channelProgramUser = "channelProgramUser";
	public static final String channelProgramWhole = "channelProgram";
	public static final String channelProgramRanking = "channelColumnList";
	public static final String remindAdd = "remindAdd";
	public static final String remindDelete = "remindDelete";
	public static final String deleteChannelTask = "channelUserDelete";
	public static final String addChannelTask = "channelUserAdd";
	public static final String activityPlayVideo = "activityPlayVideo";
	public static final String badgeDetail = "badgeDetail";
	public static final String feedBack = "feedbackAdd";
	public static final String logIn = "login";
	public static final String bindLogIn = "bindUser";
	/**
	 * preferences
	 */
	/** 推送 */
	public static final String pushMessage = "pushMessage";
	public static final String key_privateMsg = "privateMsg";
	public static final String key_fans = "fellow";
	public static final String key_reply = "reply";
	public static final String key_beiAt = "beiAt";
	public static final String key_msg_new = "newPushMsg";

	/**
	 * 错误标志
	 */
	public static final int requestErr = 0;
	public static final int parseErr = 1;
	public static final int sucess = 2;
	public static final int fail_no_net = 3;
	public static final int fail_server_err = 4;
	/**
	 * 错误信息和加载信息
	 */
	public static final String errText = "加载失败，点此重试";
	public static final String noChannel = "您还未添加任何频道!\n请去\"全部频道\"\"编辑\"来订制自己的频道吧!";
	public static final String noLogIn = "您还没有登录，点此登录";
	public static final String noRankingChannel = "暂无排行榜";
	/**
	 * 网络环境
	 */
	public static final String environment_net_wifi = "当前处于wifi网络，为您带来流畅播放体验";
	public static final String environment_net_not_wifi = "当前处于非wifi网络，播放视频可能耗费较大流量";
	/**
	 * version
	 */
	public static final String VERSION = "2.6";
	/**
	 * 星期
	 */
	public static final String week1 = "周一";
	public static final String week2 = "周二";
	public static final String week3 = "周三";
	public static final String week4 = "周四";
	public static final String week5 = "周五";
	public static final String week6 = "周六";
	public static final String week7 = "周日";
	public static final String today = "今天";
	/**
	 * errorMsg
	 */
	public static final String errMsg_s_addRemind = "预约成功!";
	public static final String errMsg_f_addRemind = "预约失败!";
	public static final String errMsg_s_deleteRemind = "取消预约成功!";
	public static final String errMsg_f_deleteRemind = "取消预约成功!";
	public static final String errMsg_s_addChannel = "添加成功!";
	public static final String errMsg_f_addChannel = "添加失败!";
	public static final String errMsg_s_deleteChannel = "取消成功!";
	public static final String errMsg_f_deleteChannel = "取消失败!";

	// 地址
	public static final String url = "http://tvfan.cn";
	public static final String host = // "http://218.89.192.146:80/clientProcess.do";
	// 成都服务器
	// "http://59.151.82.78:8180/clientProcess.do";
	"http://tvfan.cn/clientProcess.do";
	// "http://172.16.16.78:8180/clientProcess.do";

	/*
	 * sliding fragment type
	 */
	public static final int type_recommend = 0;
	public static final int type_tv = 1;
	public static final int type_oversea = 2;
	public static final int type_movie = 3;
	public static final int type_zongyi = 4;
	public static final int type_special = 5;
	public static final int type_live_all = 6;
	public static final int type_live_ranking = 7;
	public static final int type_friend_search = 8;
	public static final int type_friend_all = 9;
	public static final int type_medal = 10;
	public static final int max_type = type_medal;
	/**
	 * column id
	 */
	public static final int column_id_tv = 36;
	public static final int column_id_oversea = 38;
	public static final int column_id_zongyi = 49;
	public static final int column_id_special = 53;
	public static final int column_id_movie = 54;

	public static final int load_data = 1;
	public static final int animation_duration = 50;
}
