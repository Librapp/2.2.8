package com.sumavision.talktv2.data;

import java.util.ArrayList;

/**
 * 
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-14
 * @description 频道实体类
 * @changeLog
 * 
 */
public class ChannelNewData {

	public static ChannelNewData current = new ChannelNewData();
	// 频道名称
	public String name = "加载中...";
	// 频道id
	public int id = 0;
	// 频道分类类型，0：全部，1：央视，2：各省卫视
	public int channelType = 0;
	// 频道地域类型，0，全国，1：北京，2：上海，3：广州
	public int locationType = 0;
	// 正在播放
	public CpData now;
	// 即将播放
	public CpData next;
	// 直播地址
	public String url;
	// 播放方式
	public int playType = 1;
	// 当前正在播放
	public int nowPlayingItemPosition = 0;

	public ArrayList<NetPlayData> netPlayDatas = null;

}
