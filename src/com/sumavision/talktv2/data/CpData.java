package com.sumavision.talktv2.data;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-14
 * @description 节目单节目数据实体类
 * @changeLog
 */
public class CpData {
	public static CpData current = new CpData();
	// cpId
	public int id = 0;
	// cpName
	public String name = "暂无数据";
	// 开始时间
	public String startTime = "--:--";
	// 结束时间
	public String endTime = "--:--";
	// 播放方式：1=直接播放，2=网页播放
	public int playType;
	// 是否正在播放1=正在播出，2=将要播出
	public int isPlaying = 0;
	// 播放链接
	public String playUrl = "";
	// 节目类型
	public int type = 0;
	// 节目Id
	public String programId = "";
	// 是否预约
	public int order = 0;
	public String topicId;
}
