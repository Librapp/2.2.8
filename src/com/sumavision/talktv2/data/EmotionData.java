package com.sumavision.talktv2.data;

/**
 * 
 * @author 郭鹏
 * @createTime 2012-5-31
 * @description 表情线实体类
 * @changeLog
 * 
 */
public class EmotionData {

	private static EmotionData current;

	private String phrase;// 表情使用的替代文字
	private String phraseOther = "";// 表情使用的替代文字
	private int orderNumber;// 该表情在系统中的排序号码
	private String category;// 表情分类
	private String imageName;// 表情名称
	private int id;

	public String getPhraseOther() {
		return phraseOther;
	}

	public void setPhraseOther(String phraseOther) {
		this.phraseOther = phraseOther;
	}

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	public int getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public static EmotionData current() {
		if (current == null) {
			current = new EmotionData();
		}
		return current;
	}

}
