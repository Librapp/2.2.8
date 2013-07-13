package com.sumavision.talktv2.net;

import java.io.File;

import android.os.Build;
import android.os.Environment;

import com.sumavision.talktv2.user.UserNow;

/**
 * @author 郭鹏
 * @version 2.0
 * @createTime
 * @description JSON解析与系统其它常量表
 * @changLog
 */
public class JSONMessageType {

	public static String APP_VERSION = "2.2";
	// 客户端
	public static final int SOURCE = 1;

	// 网络通信开始消息
	public static final int NET_BEGIN = 1111;
	// 网络通信结束消息
	public static final int NET_END = 1112;

	// 非用户信息（程序信息数据）文件
	public static final String CONFIG_OTHER_INFO = "otherInfo";
	// 用户数据文件
	public static final String CONFIG_USER_INFO = "userInfo";

	public static final String SERVER_NETFAIL = "网络繁忙，请稍后重试";

	public static final String HAS_NO_NETWORK_ANSWER = "查询出错，点此重试";

	public static final String LOGO_SDCARD_FOLDER_SHORT = "TVFan/logo";
	public static final String MY_SDCARD_FOLDER_SHORT = "TVFan/uc";
	public static final String USERLOGO_SDCARD_FOLDER_SHORT = "TVFan/userlogo";
	public static final String LOGO_SDCARD_FOLDER_TMP = "TVFan/tempCamera";
	// 临时图片目录
	public static final String USER_PIC_SDCARD_FOLDER = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ LOGO_SDCARD_FOLDER_TMP;
	// 客厅栏目类型标记
	// 1=节目类栏目；2=活动类栏目；3=用户类栏目；4=演员类栏目；5=微影视类栏目；6=粉播；7=新片预告
	// 主目录
	public static final String USER_ALL_SDCARD_FOLDER = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/TVFan/";

	// 录音目录
	public static final String AUDIO_SDCARD_FOLDER = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "TVFan/audio" + File.separator;
	// 主题保存图片目录
	public static final String THEME_SAVED_PIC_SDCARD_FOLDER = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "TVFan/theme" + File.separator;
	// uc保存图片目录
	public static final String MY_SAVED_PIC_SDCARD_FOLDER = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "TVFan/uc" + File.separator;

	// 客厅分类数据库
	public static final String DATABASE_ROOM = "talktv2_room_column.db";
	public static final String DATABASE_ROOMRECOMMAD = "talktv2_room_recommand.db";

	public final static String MP3_FILE_EXTENTION = ".mp3";
	// 用于图片、中、小图的拼接
	public final static String BIG_JPG = "b.jpg";
	public final static String MIDDLE_JPG = "m.jpg";
	public final static String SMALL_JPG = "s.jpg";

	public static final int SERVER_CODE_OK = 0;
	public static final int SERVER_CODE_ERROR = 1;

	// 评论类型：0-文字、1-图片、2-视频、3-台词
	public static final int TEXT = 0;
	public static final int PIC = 1;
	public static final int VIDEO = 2;
	public static final int ACTORLINE = 3;
	// 评论类型1：0-原创、1-转发
	public static final int ORIGINAL = 0;
	public static final int FORWARD = 1;
	public static final String COMMENT_SOURCE = "来自"
			+ (Build.MODEL == null ? "电视粉" : Build.MODEL) + "客户端";

	// 图片绝对路径拼接
	// TODO:这个后面需要修改
	public static String URL_TITLE_SERVER;

	// For test
	public static void checkServerIP() {

		String str = UserNow.current().getMyServerAddress();
		URL_TITLE_SERVER = str.substring(0, str.length() - 17) + "/resource";
	}

	public static final String FILE_TYPE = ".jpg";

	// 私信列表加载更多按钮
	public static final int MAILLIST_BTN_MORE = 0x29;

	public static final String NOCHANNELPROGRAMDATA = "暂无节目数据";

	public static final int ISREPLY = 2;
	public static final int ISFORWARD = 1;

	public static final int LIVE = 0;
	public static final int VOD = 1;

	public static final long PIC_SIZE_LIMITE = 150000;
	public static final int PIC_SIZE_LIMITE_W = 150;
	public static final int PIC_SIZE_LIMITE_H = 150;

	public static final int REMOTE_FROM_WIFI = 1;
	public static final int REMOTE_FROM_BLIETOOTH = 2;

	public static final String SHARE_MESSAGE = "我正在使用\"电视粉\",这个软件很给力哦，查看推荐，畅谈节目，聊天交友，无所不能，地址:http://www.talktv.com.cn";

	public static final int VIBRATE_PERIOD = 100;
	public static final int NET_TIME_OUT_TIME = 15000;
	public static final int NET_READ_TIME_OUT_TIME = 20000;
	public final static float TARGET_HEAP_UTILIZATION = 0.75f;

	public static final String GUIDE_MON_MAP = "guideMonMap";
	public static final String GUIDE_TUE_MAP = "guideTueMap";
	public static final String GUIDE_WED_MAP = "guideWedMap";
	public static final String GUIDE_THUR_MAP = "guideThurMap";
	public static final String GUIDE_FRI_MAP = "guideFriMap";
	public static final String GUIDE_SAT_MAP = "guideSatMap";
	public static final String GUIDE_SUN_MAP = "guideSunMap";

	// 微博类型
	public static final int SINA = 1;

	public static final String SINA_WEIBO_AUTH_FAIL = "auth faild!";
	public static final String SINA_WEIBO_AUTH_FAIL1 = "com.weibo.net.WeiboException: auth faild!";
	public static final String SINA_WEIBO_AUTH_FAIL2 = "请重新授权微博";
	public static final String PLUGING_NAME = "io.vov.vitamio";
	public static final String PLUGING_URL = "http://www.tvfan.com.cn/plugin/io.vov.vitamio_1.apk";
	public static final String WEIBO_REPEAT_ERROR = "repeat content!";
	public static final String WEIBO_LOCKED_ERROR = "account is locked.";
	public static final String DOWN_URL = "http://www.tvfan.com.cn/client/download.action";
	public static final String OFFICIAL_URL = "http://www.tvfan.com.cn";
	public static final int MAIN_TAB_TIMEOUT_COUNT = 12;
	public static final int NETRETRY_COUNT = 0;

	// 需要至少保留的空间容量才可以播放
	public static final int MIN_AVAILABLE_SPACE = 30;
}
