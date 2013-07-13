package com.sumavision.talktv2.data;

import java.util.List;

/**
 * @author 李梦思
 * @version v2.0
 * @createTime 2012-6-6
 * @description 节目周边信息类
 * @changeLog 2012-7-10 by 郭鹏
 */
public class ProgramAroundData {
	private static ProgramAroundData current;
	// Id
	public int id;
	// 图片
	public String photo;
	// 标题
	public String title;
	// 内容
	public String content;
	// 包含的图片组
	private List<PhotoData> lp;
	// 时间
	public String time;
	// 超链接
	public String url;
	// 大图
	public String detailPhoto;
	// 摘要
	public String summary;
	// 来源
	public String source;

	public static ProgramAroundData current() {
		if (current == null) {
			current = new ProgramAroundData();
		}
		return current;
	}

	public void setLp(List<PhotoData> lp) {
		this.lp = lp;
	}

	public List<PhotoData> getLp() {
		return lp;
	}
}
