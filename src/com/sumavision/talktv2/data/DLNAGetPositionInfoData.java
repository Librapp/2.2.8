package com.sumavision.talktv2.data;

/**
 * 
 * @author 郭鹏
 * @description GetPositionInfo数据
 * 
 */
public class DLNAGetPositionInfoData {
	
	private static DLNAGetPositionInfoData current;
	// Track
	public String Track;
	// TrackDuration
	public String TrackDuration;
	// TrackMetaData
	public String TrackMetaData;
	// TrackURI
	public String TrackURI;
	// RelTime
	public String RelTime;
	// AbsTime
	public String AbsTime;
	// RelCount
	public String RelCount;
	// AbsCount
	public String AbsCount;

	public static DLNAGetPositionInfoData current() {
		if (current == null) {
			current = new DLNAGetPositionInfoData();
		}
		return current;
	}
}
