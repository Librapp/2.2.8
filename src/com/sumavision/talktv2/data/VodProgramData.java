package com.sumavision.talktv2.data;

import java.util.ArrayList;
import java.util.List;

import com.sumavision.talktv2.user.User;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-25
 * @description 节目数据
 * @changeLog
 */
public class VodProgramData {
	// 节目id
	public String id;
	// 话题id
	public String topicId;
	// 节目名称
	public String name;
	// 节目名称，用于保存播放历史
	public String nameHolder;
	// 一句话介绍
	public String shortIntro;
	// 播放频道名称
	public String channelName;
	// 开始播放时间
	public String startTime;
	// 结束播放时间
	public String endTime;
	// 节目图片绝对路径
	public String pic;
	// 播放链接：直接播放时为直接播放链接，网页播放时为网址
	public String playUrl;
	// 播放次数
	public int playTimes;
	// 是否正在播放：0=未开始播放，1=正在播放
	public int isPlaying;
	// 开始播放时间与当前服务器时间之差的绝对值，单位为分钟
	public String playMinutes;
	// 播放方式：1=直接播放，2=网页播放
	public int playType;
	// 直播点播
	public int livePlay = 0;
	// 最新期刊（集数）名称
	public String updateName;
	// 所属栏目名称
	public String columnName;
	// 预约Id
	public long remindId;
	// 节目单名称
	public String cpName;
	// 是否追剧0否1是
	public int isChased;
	// 是否有活动0否1是
	public int hasActivity;
	// 签到数量
	public int signCount;
	// 客户端显示方式：1=剧集类显示，2=综艺类显示
	public int showPattern;
	// 分季
	public int showSeason;
	// 视频总数
	public int videoCount;
	public String cpDate;
	// 签到用户
	private List<User> signUser;
	public int isSigned;
	private List<ParentVideoData> video;
	// 正在播出的电视台
	private List<ChannelNewData> channel;
	// 周边新闻数量
	public int aroundCount;
	private List<ProgramAroundData> around;
	private List<StarData> star;
	private List<PlayNewData> activity;
	// 周边明星数量
	public int starCount;
	// 剧照数量
	public int photoCount;
	public String[] photos;
	public String stagerName;
	// 节目类型名称
	public String contentTypeName;
	public String intro;
	public long cpId = 0;
	// 评论数量
	public int talkCount;
	// 评论
	private List<CommentData> comment;
	// 是否可以截屏0否1是
	public int canShot;

	// dbposition 断点续播的断点
	public long dbposition;
	// dbUrl
	public String dbUrl;
	// 节目搜索关键字
	public String searchKeyWords;
	// 豆瓣评分
	public String point = null;
	// 来自
	public String fromString;

	public ArrayList<NetPlayData> netPlayDatas = null;

	// public static VodProgramData current = new VodProgramData();

	public void setSignUser(List<User> signUser) {
		this.signUser = signUser;
	}

	public List<User> getSignUser() {
		return signUser;
	}

	public void setChannel(List<ChannelNewData> channel) {
		this.channel = channel;
	}

	public List<ChannelNewData> getChannel() {
		return channel;
	}

	public void setAround(List<ProgramAroundData> around) {
		this.around = around;
	}

	public List<ProgramAroundData> getAround() {
		return around;
	}

	public void setStar(List<StarData> star) {
		this.star = star;
	}

	public List<StarData> getStar() {
		return star;
	}

	public void setVideo(List<ParentVideoData> video) {
		this.video = video;
	}

	public List<ParentVideoData> getVideo() {
		return video;
	}

	public void setActivity(List<PlayNewData> activity) {
		this.activity = activity;
	}

	public List<PlayNewData> getActivity() {
		return activity;
	}

	public void setComment(List<CommentData> comment) {
		this.comment = comment;
	}

	public List<CommentData> getComment() {
		return comment;
	}

	public int playVideoActivityId = 0;
}
