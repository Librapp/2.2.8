package com.sumavision.talktv2.data;

/**
 * 
 * @author 郭鹏
 * @createTime
 * @description 图文混合数据实体
 * @changeLog
 * 
 */
public class PicAndTxtData {
	//thumb
	public String picUrl;
	//title
	public String txt;
	// 标记是节目，演员，用户，粉播，广告等类型 isDefault
	public int type;
	public int id;
	// 连接 path
	public String url;
    // 是否为本地SDCard图片
	public boolean isLocalSDcard = false;
}
