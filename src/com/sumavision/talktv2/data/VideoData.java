package com.sumavision.talktv2.data;

import java.util.ArrayList;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-6-12
 * @description 微影视项目数据
 * @changeLog 改为视频数据类 2012-12-12 by 李梦思
 */
public class VideoData {
	private static VideoData current;
	// Id
	public int id;
	// 名称
	public String name;
	// 图片
	public String photo;
	// 播放次数
	public String playCount;
	// 描述
	public String description;
	// 播放地址
	public String url;
	// 时长
	public String playLength;
	//
	public int isNew;
	// 话题Id
	public String topicId;
	// 评论数量
	public int talkCount;
	// 播放方式：1=直接播放，2=网页播放
	public int playType;

	// 视频来源
	public String fromString;

	public ArrayList<NetPlayData> netPlayDatas = null;

	public static VideoData current() {

		if (current == null) {
			current = new VideoData();
		}
		return current;
	}
}
