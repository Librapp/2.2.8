package com.sumavision.talktv2.data;

import java.util.List;

/**
 * 
 * @author 郭鹏
 * @createTime
 * @description 自动更新实体类
 * @changeLog
 * 
 */
public class UpdateData {

	private static UpdateData instance;
	// 本地程序版本
	public String versionCodeNow;
	// 服务器端程序版本
	public String versionCodeServer;
	public String versionMini;
	// 程序更新细节
	public String updateDetail;
	public String fileSize;
	// 程序下载地址
	public String appDownURL;
	// 程序更新时间
	public String uploadDate;
	// 是否需要更新程序
	public boolean isNeedUpdateApp = false;;

	// 本地欢迎页版本
	public String logoCodeNow;
	// 服务器端欢迎页版本
	public String logoCodeServer;
	// 新欢迎页下载地址
	public String logoDownURL;
	// Logo名字
	public String logoFileName;
	// 是否需要下载最新欢迎页
	public boolean isNeedUpdateLogo = false;
	// 是否已经下载了最新的logo文件
	public boolean hasLogoFile = false;

	// 客厅页推荐分类本地版本
	public String LRCodeNow;
	// 客厅页推荐分类服务器版本
	public String LRCodeServer;
	// 客厅页推荐分类是否需要更新
	public boolean isNeedUpdateLivingRoomCategory = false;

	// 主题本地版本
	public String localTheme;
	// 主题服务器版本
	public String serverTheme;
	// 主题是否需要更新
	public boolean isNeedUpdateTheme = false;
	// 已下载的服务器主题图片名字列表
	public List<String> themePicNames;
	// 已下载的服务器主题图片URL列表
	public List<String> themePicURLs;
	// 已知服务器端新主题图片数量
	public int themeCount;

	// 需要更新网页播放状态，关闭网页
	public boolean isNeedCloseWebPlay = false;

	public static UpdateData current() {
		if (instance == null) {
			instance = new UpdateData();
		}
		return instance;
	}
}
