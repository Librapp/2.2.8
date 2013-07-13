package com.sumavision.talktv2.data;

import java.util.ArrayList;

public class HotPlayProgram {

	public static HotPlayProgram singleInstance;

	public static HotPlayProgram current() {
		if (singleInstance == null) {
			singleInstance = new HotPlayProgram();
		}
		return singleInstance;
	}

	public ArrayList<VodProgramData> hotProgramList = null;

}
