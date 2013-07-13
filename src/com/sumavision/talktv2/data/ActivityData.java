package com.sumavision.talktv2.data;

import java.util.List;

/**
 * @author 李梦思
 * @version 2.0
 * @createTime 2012-6-12
 * @description 活动项目数据
 * @changeLog
 */
public class ActivityData {
	public static ActivityData current;
	// Id
	public int id = 0;
	// 名字
	public String name;
	// 图片
	public String photo;
	// 参与人数
	public int userCount;
	// 类型
	public int type;
	// 预告及花絮数量
	public int preVideoCount;
	// 往期视频数量
	public int pastVideoCount;
	// 明星数量
	public int starCount;
	// 投票数量
	public int voteCount;
	// 投票介绍
	public String voteIntro;
	//
	public VodProgramData p;
	//
	private List<VideoData> preVideo;
	//
	private List<VideoData> pastVideo;
	//
	private List<StarData> star;
	//
	private List<CommentData> starTalk;
	//
	public int starTalkCount;
	// 模版列表
	private List<ModelData> model;

	public static ActivityData current() {

		if (current == null) {
			current = new ActivityData();
		}
		return current;
	}

	public List<VideoData> getPreVideo() {
		return preVideo;
	}

	public void setPreVideo(List<VideoData> preVideo) {
		this.preVideo = preVideo;
	}

	public List<VideoData> getPastVideo() {
		return pastVideo;
	}

	public void setPastVideo(List<VideoData> pastVideo) {
		this.pastVideo = pastVideo;
	}

	public List<StarData> getStar() {
		return star;
	}

	public void setStar(List<StarData> star) {
		this.star = star;
	}

	public void setStarTalk(List<CommentData> starTalk) {
		this.starTalk = starTalk;
	}

	public List<CommentData> getStarTalk() {
		return starTalk;
	}

	public void setModel(List<ModelData> model) {
		this.model = model;
	}

	public List<ModelData> getModel() {
		return model;
	}

}
