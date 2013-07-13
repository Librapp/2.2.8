package com.sumavision.talktv2.data;

/**
 * @author 李梦思
 * @createTime 2013-2-22
 * @description 选项数据实体类
 * @changeLog
 */
public class OptionData {
	/**
	 * 选项id
	 */
	public long id;
	/**
	 * 选项文字
	 */
	public String content;
	/**
	 * 选项被用户竞猜数量
	 */
	public int countUser;
	/**
	 * 是否为用户所猜选项，0=否，1=是
	 */
	public int isChosed;
	/**
	 * 选项类型，0=非答案选项，1=答案选项
	 */
	public int type;

}
