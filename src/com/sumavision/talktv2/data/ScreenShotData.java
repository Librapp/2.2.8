package com.sumavision.talktv2.data;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-5
 * @description 电视截屏信息类
 * @changLog
 */
public class ScreenShotData {
	public static final String FILENAME = "screenshot.jpg";
	public static String picPath;
	// 方向 1表示向右，-1表示向左
	public static int direction;
	public static String pic[];
	public static int picCount = 0;
	public static int current = 0;
	public static long channelId;
	public static String address;
}
