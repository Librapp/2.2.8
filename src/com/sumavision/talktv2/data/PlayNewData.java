package com.sumavision.talktv2.data;

import java.util.List;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-25
 * @description 活动项目数据
 * @changeLog
 */
public class PlayNewData {
	public static PlayNewData current = new PlayNewData();

	public static final int WEB_SHOW = 1;

	public int id;
	public String name;
	public String typeName;
	public int typeId;
	public String intro;
	public String introShort;
	public String pic;
	// 1=未开始，2=进行中，3=已结束
	public int state = 2;
	// 1未参加2已参加3已完成
	public int joinStatus = 1;
	public int userCount = 0;
	public String timeDiff;
	// 1=节目，2=明星，3=微影视，4=粉播，5=投票，6=竞猜，7=摇奖，8=PK，9=用户，10=节目单
	public int targetType;
	public long targetId;
	// 对象类型：1=评论，2=回复，3=预约，4=节目签到，5=私信，6=招呼，7=好友
	public int objectType;
	// 如果是节目类型 需要programId 和topicId;
	public int programId;
	public int topicId;
	// 事件类型（决定页面跳转）：1003=上传头像，2010=节目签到，2003=发表评论，2009=关注好友，2011=预约节目单，2017=节目追剧，1004=绑定新浪微博，1007=绑定新浪微博，1006=绑定人人网
	public int eventTypeCode;
	// 当前进度描述
	public String schedule;
	/**
	 * 选项个数
	 */
	public int optionCount;

	private List<OptionData> options;
	/**
	 * 选项可选择数
	 */
	public int selectCount;

	/**
	 * @param 给选项赋值
	 */
	public void setOptions(List<OptionData> options) {
		this.options = options;
	}

	/**
	 * @return 选项
	 */
	public List<OptionData> getOptions() {
		return options;
	}

	// 是否是网页显示
	public int isWeb;
}
