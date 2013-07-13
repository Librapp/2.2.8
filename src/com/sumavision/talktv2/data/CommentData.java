package com.sumavision.talktv2.data;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * @author 郭鹏
 * @createTime 2012-5-31
 * @description 评论实体类
 * @changeLog
 */
public class CommentData {

	private static CommentData current;
	private static CommentData replyComment;
	// 评论的节目
	public VodProgramData program;
	// 评论人姓名
	public String userName;
	// 评论人头像：测试时用URL
	public String userURL;
	// 评论人id
	public int userId;
	// 评论对象的名字：电影名，电视剧名，演员名，私信接受者名字
	public String objectName = "";
	// 评论对象Id
	public long objectId = 0;
	// 评论内容
	public String content = "";
	// 评论时间
	public String commentTime;
	// 此条评论的回复次数
	public int replyCount;
	// 此条评论的转发次数
	public int forwardCount;
	// 评论内容图片：测试时用URL
	public String contentURL = "";
	// 此条评论来源：网页版，Android手机版
	public String source;
	// 评论对象(节目、演员)的ID
	public long topicID;
	// 转发评论的根评论
	public CommentData rootTalk;
	// 回复的评论
	public CommentData replyTalk;
	public boolean hasRootTalk = false;
	// 回复所属于的评论talk的id
	public int talkId;
	// 被回复的用户的ID
	public int repUserId;
	// 转发时，上一级talk的 ID
	public int forwardId;
	// 0：非转发评论，1：为转发评论
	public int actionType;
	// 标记转发时的根评论是否被删除
	public boolean isDeleted = false;
	// 节目ID
	public long programId;
	// 图片base64编码
	public String pic = "";
	public Bitmap picBitMap = null;
	public Drawable picDrawable = null;
	public String picAllName = "";
	public String picAllNameLogo = "";
	public boolean isFromSelf = true;
	public int privateMessageCount;
	// talkType：谈论类型，0=原创，1=图片，2=视频，3=台词，4=语音
	public int talkType;
	// 是否是回复
	public boolean isReply = false;
	// 评论中图片
	private byte[] picLogo = null;
	// 常用短语
	private List<String> phrases;
	// 语音base64编码
	public String audio;
	// 语音URL
	public String audioURL = "";
	// 语音文件的名字
	public String audioFileName;
	// 评论的回复
	private List<CommentData> reply;
	// 评论的转发
	private List<CommentData> forward;
	// 粉播图片URL
	public String fenPlayPicURL = "";
	// 评论类型话题类型：0=自由创建的话题；1=节目话题；2=演员话题; 3=微影视; 4=粉播',5=投票，
	// 6=竞猜，7=摇奖，8=PK，9=专题模版，10=专题视频
	public int type = 1;
	// 是否为匿名用户发布 0不是1是
	public int isAnonymousUser = 0;
	// 如果是回复，这是回复Id
	public int replyId;

	public byte[] getPicLogo() {
		return picLogo;
	}

	public void setPicLogo(byte[] picLogo) {
		this.picLogo = picLogo;
	}

	public List<String> getPhrases() {
		return phrases;
	}

	public void setPhrases(List<String> phrases) {
		this.phrases = phrases;
	}

	public static CommentData current() {
		if (current == null) {
			current = new CommentData();
		}
		return current;
	}

	public void setReply(List<CommentData> reply) {
		this.reply = reply;
	}

	public List<CommentData> getReply() {
		return reply;
	}

	public static CommentData replyComment() {
		if (replyComment == null) {
			replyComment = new CommentData();
		}
		return replyComment;
	}

	public void setForward(List<CommentData> forward) {
		this.forward = forward;
	}

	public List<CommentData> getForward() {
		return forward;
	}

}
