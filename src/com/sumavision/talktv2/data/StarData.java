package com.sumavision.talktv2.data;

import java.util.List;

/**
 * @author 郭鹏
 * @createTime
 * @description 明星实体类
 * @changeLog
 */
public class StarData {

	private static StarData instance;
	public String name;
	public String nameEng;
	public String intro;
	public int stagerID;
	public String photoBig_V;
	public String hobby;
	// 星座
	public String starType;
	public int picCount = 0;
	public String[] photo;
	public int programCount = 0;
	private List<VodProgramData> program;

	public static void setCurrent(StarData instance) {
		StarData.instance = instance;
	}

	public static StarData current() {
		if (instance == null) {
			instance = new StarData();

		}
		return instance;
	}

	public void setProgram(List<VodProgramData> program) {
		if (program == null && this.program != null) {
			this.program.clear();
		}
		this.program = program;
	}

	public List<VodProgramData> getProgram() {
		return program;
	}
}
