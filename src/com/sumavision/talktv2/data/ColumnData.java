package com.sumavision.talktv2.data;

import java.util.List;

/**
 * @author 李梦思
 * @version 2.0
 * @createTime 2012-11-29
 * @description 栏目类
 * @changeLog
 */
public class ColumnData {

	public int id;
	public int type;
	public int offset = 0;
	public int pageCount = 10;
	public int programCount;
	public int playTimes = 0;
	public String intro;
	public String pic;
	public String name;

	private List<ColumnData> subColumn;
	private List<VodProgramData> program;

	public static ColumnData current = new ColumnData();

	public void setSubColumn(List<ColumnData> subColumn) {
		this.subColumn = subColumn;
	}

	public List<ColumnData> getSubColumn() {
		return subColumn;
	}

	public void setProgram(List<VodProgramData> program) {
		this.program = program;
	}

	public List<VodProgramData> getProgram() {
		return program;
	}
}
