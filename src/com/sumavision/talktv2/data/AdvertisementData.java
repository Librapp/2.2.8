package com.sumavision.talktv2.data;
/**
 * 
 * @author 李梦思
 * @version 2.0
 * @createTime 2012-6-4
 * @description 广告类
 * @changLog
 */
public class AdvertisementData {
	private static AdvertisementData current;
	// Id
	public int id;
	// 名字
	public String name;
	// 图片
	public String photo;
	// 链接
	public String url;
	
	public static AdvertisementData current() {
		if (current == null) {
			current = new AdvertisementData();
		}
		return current;
	}
}
