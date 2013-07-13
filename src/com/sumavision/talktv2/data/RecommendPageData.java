package com.sumavision.talktv2.data;

import java.util.List;

/**
 * @author 李梦思
 * @version 2.0
 * @createTime 2012-11-29
 * @description 推荐页类
 * @changeLog
 */
public class RecommendPageData {

	private static RecommendPageData current;

	public static synchronized RecommendPageData current() {
		if (current == null) {
			current = new RecommendPageData();
		}
		return current;
	}

	public int liveProgramCount;
	public int vodProgramCount;

	private List<RecommendData> recommend;
	private List<ColumnData> column;
	private List<ColumnData> subColumn;
	private List<VodProgramData> liveProgram;
	private List<VodProgramData> vodProgram;

	public List<RecommendData> getRecommend() {
		return recommend;
	}

	public void setRecommend(List<RecommendData> recommend) {
		this.recommend = recommend;
	}

	public List<VodProgramData> getLiveProgram() {
		return liveProgram;
	}

	public void setLiveProgram(List<VodProgramData> lp) {
		this.liveProgram = lp;
	}

	public List<VodProgramData> getVodProgram() {
		return vodProgram;
	}

	public void setVodProgram(List<VodProgramData> vodProgram) {
		this.vodProgram = vodProgram;
	}

	public void setColumn(List<ColumnData> column) {
		this.column = column;
	}

	public List<ColumnData> getColumn() {
		return column;
	}

	public void setSubColumn(List<ColumnData> subColumn) {
		this.subColumn = subColumn;
	}

	public List<ColumnData> getSubColumn() {
		return subColumn;
	}

}
