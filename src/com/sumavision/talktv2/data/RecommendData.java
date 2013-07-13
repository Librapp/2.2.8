package com.sumavision.talktv2.data;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-11-29
 * @description 推荐类
 * @changeLog
 */
public class RecommendData {
	public long id;
	public int type;
	public String name;
	public String pic;
	public String url;
	public String topicId;

	public static RecommendData current = new RecommendData();
}
