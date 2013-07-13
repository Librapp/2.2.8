package com.sumavision.talktv2.data;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-7
 * @description 推荐应用实体类
 * @changeLog
 */
public class AppData {
	public long id;
	public String name;
	public String shortIntro;
	public String pic;
	public String url;
	public static AppData current = new AppData();
}
