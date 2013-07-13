package com.sumavision.talktv2.data;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-7
 * @description 追剧实体类
 * @changeLog
 */
public class ChaseData {
	public long id;
	public long programId;
	public long topicId;
	public String programPic;
	public String programName;
	public String latestSubName;
	public int isOver;

	public static ChaseData current = new ChaseData();
}
