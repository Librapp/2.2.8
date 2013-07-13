package com.sumavision.talktv2.data;

import java.util.ArrayList;

public class MainPageData {
	private static MainPageData mainPageData;

	public static MainPageData current() {
		if (mainPageData == null) {
			mainPageData = new MainPageData();
		}
		return mainPageData;
	}

	public ArrayList<EventData> eventDatas = null;

	public ArrayList<EventData> myFriendEventDatas = null;
}
