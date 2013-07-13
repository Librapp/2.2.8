package com.sumavision.talktv2.data;

/**
 * @author 李梦思
 * @createTime 2012-6-8
 * @description 游戏选项实体类
 * @changeLog 修改 by 郭鹏 2012-6-9
 */
public class GameItemData {
	// Id
	public int id;
	// 文字说明
	public String content;
	// 图片
	public String pic;
	// 票数
	public int count;
	// 标题
	public String title;
	// 奖励总数
	public int total;
	// 获奖人数
	public int countUser;
	// 奖品名字
	public String gift;
	// 是否中奖项：0=不是中奖项；1=是中奖项
	public int win;
	// 是否选择
	public int chosen = 0;
	// pk支持方1红2蓝
	public int type;
	// 热度
	public int hot;
	// 声音路径
	public String url = "";

}
