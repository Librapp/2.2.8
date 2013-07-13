package com.sumavision.talktv2.data;
/**
 * 
 * @author 李梦思
 * @version v2.0
 * @createTime 2012-6-6
 * @description 图片数据类
 * @changeLog 
 * 
 */
public class PhotoData {
	private static PhotoData current;
	// Id
	public int id;
	// 标题
	public String title;
	// 链接
	public String url;
	public static PhotoData current() {
		if (current == null) {
			current = new PhotoData();
		}
		return current;
	}
}
