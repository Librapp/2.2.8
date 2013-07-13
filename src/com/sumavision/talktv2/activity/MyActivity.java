package com.sumavision.talktv2.activity;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.AsyncImageLoader.ImageCallback;
import com.sumavision.talktv2.data.AccessTokenKeeper;
import com.sumavision.talktv2.data.BindOpenAPIData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.data.VersionData;
import com.sumavision.talktv2.net.BindAddParser;
import com.sumavision.talktv2.net.BindAddRequest;
import com.sumavision.talktv2.net.BindDeleteRequest;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.LoginNewParser;
import com.sumavision.talktv2.net.LogoffParser;
import com.sumavision.talktv2.net.LogoffRequest;
import com.sumavision.talktv2.net.MySpaceRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.ResultParser;
import com.sumavision.talktv2.services.AppUpdateService;
import com.sumavision.talktv2.task.BindAccountTask;
import com.sumavision.talktv2.task.BindDeleteTask;
import com.sumavision.talktv2.task.GetAppNewVersionTask;
import com.sumavision.talktv2.task.GetUserInfoTask;
import com.sumavision.talktv2.task.LogoffTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.AppUtil;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.AccountAPI;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

/**
 * @author 姜浩
 * @description 我的界面
 * @createTime
 */
public class MyActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnSharedPreferenceChangeListener {

	private SsoHandler ssh;
	private final int MSG_CLEAN_START = 109;
	private final int MSG_CLEAN_OVER = 110;
	private final int MSG_CLEAN_ERROR = 111;
	private final int SINA = 1;
	private int funcFlag = 0;
	private final int SINA_GET_UID = 2;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_CLEAN_START:
				showpb();
				break;
			case MSG_CLEAN_OVER:
				hidepb();
				alertToast("缓存清理成功!");
				break;
			case MSG_CLEAN_ERROR:
				hidepb();
				break;
			case SINA:
				switch (msg.arg1) {
				case SINA_GET_UID:
					showpb();
					AccountAPI accountAPI = new AccountAPI(Weibo.getInstance(
							Weibo.app_key, Weibo.redirecturl).accessToken);
					accountAPI.getUid(new RequestListener() {

						@Override
						public void onIOException(IOException arg0) {
							Message msg = new Message();
							msg.what = SINA;
							msg.obj = "新浪微博获取用户ID失败";
							if (OtherCacheData.current().isDebugMode)
								Log.e("新浪微博获取用户ID", arg0.getMessage());
							handler.sendMessage(msg);
						}

						@Override
						public void onError(WeiboException arg0) {
							Message msg = new Message();
							msg.what = SINA;
							msg.obj = "新浪微博获取用户ID失败";
							if (OtherCacheData.current().isDebugMode)
								Log.e("新浪微博获取用户ID", arg0.getMessage());
							handler.sendMessage(msg);
						}

						@Override
						public void onComplete(String arg0) {
							SinaData.id = arg0.substring(
									arg0.lastIndexOf(":") + 1,
									arg0.length() - 1);
							bindAccount();
						}
					});
					break;
				default:
					hidepb();
					alertToast(msg.obj.toString());
					break;
				}
				break;
			default:
				break;
			}
		};
	};

	// 绑定注册
	private void bindAccount() {
		UserNow.current().errorCode = -1;
		BindAccountTask bindAccountTask = new BindAccountTask(this);
		bindAccountTask
				.execute(this, new BindAddRequest(), new BindAddParser());
	}

	// 通信框
	private RelativeLayout connectBg;

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my);
		initOthers();
		initView();
		setListeners();
		if (UserNow.current().userID != 0) {
			getUserInfo();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		if (UserNow.current().userID != 0) {
			showLoginLayout();
			updateUserInfo();
			showTipImage();
		} else {
			showUnLoginLayout();
		}
	}

	private ImageView fansTipImageView, privateMsgTipIamgeView,
			replyTipImageView, beiAtTipImageView;

	private void showTipImage() {
		if (pushMsgPreferences.getBoolean(Constants.key_fans, false)) {
			fansTipImageView.setVisibility(View.VISIBLE);
		} else {
			fansTipImageView.setVisibility(View.GONE);
		}
		if (pushMsgPreferences.getBoolean(Constants.key_privateMsg, false)) {
			privateMsgTipIamgeView.setVisibility(View.VISIBLE);
		} else {
			privateMsgTipIamgeView.setVisibility(View.GONE);
		}
		if (pushMsgPreferences.getBoolean(Constants.key_reply, false)) {
			replyTipImageView.setVisibility(View.VISIBLE);
		} else {
			replyTipImageView.setVisibility(View.GONE);
		}
		if (pushMsgPreferences.getBoolean(Constants.key_beiAt, false)) {
			beiAtTipImageView.setVisibility(View.VISIBLE);
		} else {
			beiAtTipImageView.setVisibility(View.GONE);
		}
	}

	private AsyncImageLoader imageLoader;
	SharedPreferences spUser;
	SharedPreferences pushMsgPreferences;

	private void initOthers() {
		imageLoader = new AsyncImageLoader();
		spUser = getSharedPreferences("userInfo", 0);
		pushMsgPreferences = getSharedPreferences(Constants.pushMessage, 0);
		pushMsgPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	private void initView() {
		initBaseInfo();
		initEventInfo();
		hscroll = (HorizontalScrollView) findViewById(R.id.uc_horizontal_scrollview);
		// loginTextView = (TextView) findViewById(R.id.login);
		back = (Button) findViewById(R.id.back);
		bindSina = (ImageButton) findViewById(R.id.sina);
		if (SinaData.isSinaBind) {
			bindSina.setImageResource(R.drawable.sina_selected);
			bindSina.setOnClickListener(ubBindSinaListener);
			findViewById(R.id.bind_layout).setOnClickListener(
					ubBindSinaListener);
		} else {
			bindSina.setImageResource(R.drawable.sina);
			bindSina.setOnClickListener(this);
			findViewById(R.id.bind_layout).setOnClickListener(this);
		}
		editUserInfo = (Button) findViewById(R.id.edit);
		refreshProgress = (ProgressBar) findViewById(R.id.progressBar);
		scrollView = (ScrollView) findViewById(R.id.scollView);
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);

		loginBtn = (Button) findViewById(R.id.login_btn);
		registerBtn = (TextView) findViewById(R.id.register_btn);
		loginLayout = (LinearLayout) findViewById(R.id.login_layout);
		zuxiaoBtn = (TextView) findViewById(R.id.zuxiao);
		bindLayout = (RelativeLayout) findViewById(R.id.bind_layout);
		pushMsgIndicator = (ImageView) findViewById(R.id.indicator);
		setPushMsgIndicator(getPushMsgState());
		divider = (TextView) findViewById(R.id.diviver);
	}

	private void setListeners() {
		// loginTextView.setOnClickListener(this);
		back.setOnClickListener(this);
		findViewById(R.id.edit).setOnClickListener(this);
		findViewById(R.id.fellow_layout).setOnClickListener(this);
		findViewById(R.id.fans_layout).setOnClickListener(this);
		findViewById(R.id.book_layout).setOnClickListener(this);
		findViewById(R.id.medal_layout).setOnClickListener(this);
		findViewById(R.id.msg_layout).setOnClickListener(this);
		findViewById(R.id.zhuiju_layout).setOnClickListener(this);
		findViewById(R.id.comment_layout).setOnClickListener(this);
		findViewById(R.id.replyme_layout).setOnClickListener(this);
		findViewById(R.id.atme_layout).setOnClickListener(this);
		findViewById(R.id.about).setOnClickListener(this);
		findViewById(R.id.help).setOnClickListener(this);
		findViewById(R.id.new_version).setOnClickListener(this);
		findViewById(R.id.feedback).setOnClickListener(this);
		findViewById(R.id.score).setOnClickListener(this);// 评分
		findViewById(R.id.clearcache).setOnClickListener(this);
		findViewById(R.id.otherapp).setOnClickListener(this);
		findViewById(R.id.mychannel_layout).setOnClickListener(this);
		zuxiaoBtn.setOnClickListener(this);
		loginBtn.setOnClickListener(this);
		registerBtn.setOnClickListener(this);
		findViewById(R.id.notification_layout).setOnClickListener(this);
	}

	private ImageView icon;
	private TextView userNameTextView;
	private TextView userLevelTextView;
	private TextView signatureTextView;
	private ImageView gender;
	private RelativeLayout baseInfoLayout;
	private TextView zuxiaoBtn;

	private void initBaseInfo() {
		baseInfoLayout = (RelativeLayout) findViewById(R.id.baseinfo_layout);
		icon = (ImageView) findViewById(R.id.head_pic);
		userNameTextView = (TextView) findViewById(R.id.name);
		userLevelTextView = (TextView) findViewById(R.id.level);
		signatureTextView = (TextView) findViewById(R.id.signnature);
		gender = (ImageView) findViewById(R.id.gender);
	}

	private TextView fellowTextView;
	private TextView fansTextView;
	private TextView medalTextView;
	private TextView commentTextView;
	private TextView privateMsgTextView;
	private TextView zhuijuTextView;
	private TextView bookTextView;
	private TextView channelTextView;
	// 被@
	private TextView atMeTextView;
	// 被回复
	private TextView replyMeTextView;

	private void initEventInfo() {

		fellowTextView = (TextView) findViewById(R.id.fellowing);
		fansTextView = (TextView) findViewById(R.id.fans);
		medalTextView = (TextView) findViewById(R.id.medal);
		commentTextView = (TextView) findViewById(R.id.comment);
		privateMsgTextView = (TextView) findViewById(R.id.privatemsg);
		zhuijuTextView = (TextView) findViewById(R.id.zhuiju);
		bookTextView = (TextView) findViewById(R.id.book);

		atMeTextView = (TextView) findViewById(R.id.atme);
		replyMeTextView = (TextView) findViewById(R.id.replyme);

		fansTipImageView = (ImageView) findViewById(R.id.fans_notify_pic);
		privateMsgTipIamgeView = (ImageView) findViewById(R.id.privatemsg_notify_pic);
		replyTipImageView = (ImageView) findViewById(R.id.replyme_notify_pic);
		beiAtTipImageView = (ImageView) findViewById(R.id.atme_notify_pic);

		channelTextView = (TextView) findViewById(R.id.channel);

	}

	// private TextView loginTextView;
	private Button back;
	private Button editUserInfo;
	private ImageButton bindSina;
	private ProgressBar refreshProgress;
	private ScrollView scrollView;
	private HorizontalScrollView hscroll;
	private RelativeLayout bindLayout;

	private ImageView pushMsgIndicator;

	private Button loginBtn;
	private TextView registerBtn;
	private LinearLayout loginLayout;

	@Override
	public void onClick(View v) {
		Editor editor = spUser.edit();
		switch (v.getId()) {
		case R.id.replyme_layout:
			MobclickAgent.onEvent(this, "replyme");
			startActivity(new Intent(MyActivity.this, MyReplyActivity.class));
			break;
		case R.id.atme_layout:
			MobclickAgent.onEvent(this, "@me");
			startActivity(new Intent(MyActivity.this, MyAtActivity.class));
			break;
		case R.id.medal_layout:
			MobclickAgent.onEvent(this, "badge");
			startActivity(new Intent(MyActivity.this, MyBadgeActivity.class));
			break;
		case R.id.mychannel_layout:
			startActivity(new Intent(MyActivity.this, MyChannelActivity.class));
			break;
		case R.id.back:
			finish();
			break;
		case R.id.fellow_layout:
			MobclickAgent.onEvent(this, "follow");
			openMyFellowingActivity();
			break;
		case R.id.fans_layout:
			MobclickAgent.onEvent(this, "fans");
			openMyFansActivity();
			break;
		case R.id.book_layout:
			MobclickAgent.onEvent(this, "book");
			openMyBookActivity();
			break;
		case R.id.about:
			MobclickAgent.onEvent(this, "about");
			openAboutActivity();
			break;
		case R.id.help:
			MobclickAgent.onEvent(this, "help");
			openHelpActivity();
			break;
		case R.id.feedback:
			MobclickAgent.onEvent(this, "yijian");
			openFeedbackActivity();
			break;
		case R.id.new_version:
			MobclickAgent.onEvent(this, "jiance");
			if (AppUtil
					.isSystemUpdateServiceRunning(this,
							AppUtil.getPackageName(this)
									+ ".services.AppUpdateService")) {
				alertToast("正在下载新版本");
			} else {
				getAppNewVersion();
			}
			break;
		case R.id.score:
			MobclickAgent.onEvent(this, "pingfen");
			openScoreActivity();
			break;
		case R.id.clearcache:
			MobclickAgent.onEvent(this, "qingchu");
			if (checkSDCard()) {
				alertToast("清理缓存中...");
				deleteMemoryRoot();
			} else {
				alertToast("SD卡不存在，请插入SD卡后重试...");
			}
			break;
		case R.id.zuxiao:
			MobclickAgent.onEvent(this, "zhuxiao");
			UserNow.current().userIDTemp = UserNow.current().userID;
			UserNow.current().userID = 0;
			editor.putInt("userID", 0);
			editor.putString("sessionID", "");
			editor.putBoolean("newPushMsg", false);
			editor.putInt("addFenCount", 0);
			editor.putInt("privateMessageCount", 0);
			editor.putInt("sayhiCount", 0);
			editor.putString("level", "");
			editor.putLong("exp", 0);
			editor.putLong("point", 0);
			editor.putString("iconURL", "");
			editor.putString("signature", "");
			editor.putBoolean("bindSina", false);
			editor.putInt("openType", -1);
			editor.putBoolean("isOpenTypeLogin", false);
			editor.commit();
			UserNow.current().level = "";
			UserNow.current().exp = 0;
			UserNow.current().point = 0;
			UserNow.current().userID = 0;
			UserNow.current().isLogedIn = false;
			UserNow.current().isSelf = false;
			UserNow.current().signature = "";
			UserNow.current().iconURL = "";
			UserNow.current().name = "";
			UserNow.current().eMail = "";
			UserNow.current().thirdUserId = "";
			UserNow.current().thirdToken = "";
			OtherCacheData.isNeedUpdateActivityPageAll = true;
			OtherCacheData.isNeedUpdateFriendActivity = true;
			logOff();
			showUnLoginLayout();
			break;
		case R.id.otherapp:
			MobclickAgent.onEvent(this, "tuijian");
			openRecommendAppActivity();
			break;
		case R.id.renren:

			break;
		case R.id.bind_layout:
		case R.id.sina:
			MobclickAgent.onEvent(this, "bangding");
			if (!SinaData.isSinaBind)
				getSinaAuth();
			else {
				bindSina.setImageResource(R.drawable.sina_selected);
				bindSina.setOnClickListener(ubBindSinaListener);
				findViewById(R.id.bind_layout).setOnClickListener(
						ubBindSinaListener);
			}
			break;
		case R.id.msg_layout:
			MobclickAgent.onEvent(this, "mymes");
			openPrivateMsgActivity();
			break;
		case R.id.zhuiju_layout:
			MobclickAgent.onEvent(this, "myzhuiju");
			openMyZhuijuActivity();
			break;
		case R.id.comment_layout:
			MobclickAgent.onEvent(this, "mycomment");
			openMyCommentActivity();
			break;
		case R.id.edit:
			MobclickAgent.onEvent(this, "edit");
			openUserEditActivity();
			break;
		case R.id.login_btn:
			openLoginActivity();
			break;
		case R.id.register_btn:
			openRegisterActivity();
			break;
		case R.id.notification_layout:
			boolean isOn = getPushMsgState();
			savePushMsgState(!isOn);
			setPushMsgIndicator(!isOn);
			break;
		default:
			break;
		}
	}

	private void getSinaAuth() {
		showpb();
		funcFlag = SINA;
		ssh = new SsoHandler(this, SinaData.weibo());

		ssh.authorize(new WeiboAuthListener() {
			@Override
			public void onWeiboException(WeiboException arg0) {
				Message msg = new Message();
				msg.what = SINA;
				msg.obj = "新浪微博授权失败";
				if (OtherCacheData.current().isDebugMode)
					Log.e("新浪微博授权", arg0.getMessage());
				handler.sendMessage(msg);
			}

			@Override
			public void onError(WeiboDialogError arg0) {
				Message msg = new Message();
				msg.what = SINA;
				msg.obj = "新浪微博授权失败";
				if (OtherCacheData.current().isDebugMode)
					Log.e("新浪微博授权", arg0.getMessage());
				handler.sendMessage(msg);
			}

			@Override
			public void onComplete(Bundle arg0) {
				SinaData.accessToken = arg0.getString("access_token");
				SinaData.expires_in = arg0.getString("expires_in");
				SinaData.weibo().accessToken = new Oauth2AccessToken(
						SinaData.accessToken, SinaData.expires_in);
				AccessTokenKeeper.keepAccessToken(MyActivity.this,
						SinaData.weibo().accessToken);
				Message msg = new Message();
				msg.what = SINA;
				msg.arg1 = SINA_GET_UID;
				handler.sendMessage(msg);
			}

			@Override
			public void onCancel() {
				Message msg = new Message();
				msg.what = SINA;
				msg.obj = "新浪微博授权取消";
				handler.sendMessage(msg);
			}
		});
	}

	private void openMyFellowingActivity() {
		Intent intent = new Intent(this, MyFellowingActivity.class);
		startActivity(intent);
	}

	private void openMyFansActivity() {
		Intent intent = new Intent(this, MyFansActivity.class);
		startActivity(intent);
	}

	private void openMyBookActivity() {
		Intent intent = new Intent(this, MyBookActivity.class);
		startActivity(intent);
	}

	private void openAboutActivity() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	private void openHelpActivity() {
		Intent intent = new Intent(this, HelpActivity.class);
		startActivity(intent);
	}

	private void openFeedbackActivity() {
		Intent intent = new Intent(this, UserFeedbackActivity.class);
		startActivity(intent);
	}

	private void openRecommendAppActivity() {
		Intent intent = new Intent(this, RecommandAppActivity.class);
		startActivity(intent);
	}

	private void openScoreActivity() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=" + getPackageName()));
		startActivity(intent);
	}

	private void openPrivateMsgActivity() {
		Intent intent = new Intent(this, MyPrivateMsgActivity.class);
		startActivity(intent);
	}

	private void openMyZhuijuActivity() {
		Intent intent = new Intent(this, MyZhuijuActivity.class);
		startActivity(intent);
	}

	private void openMyCommentActivity() {
		Intent intent = new Intent(this, MyCommentActivity.class);
		startActivity(intent);
	}

	private void deleteMemoryRoot() {
		handler.sendEmptyMessage(MSG_CLEAN_START);
		try {
			Runtime.getRuntime().exec(
					"rm -r " + JSONMessageType.USER_ALL_SDCARD_FOLDER);
			try {
				Thread.sleep(200);
				handler.sendEmptyMessage(MSG_CLEAN_OVER);
			} catch (InterruptedException e) {
				handler.sendEmptyMessage(MSG_CLEAN_ERROR);
				e.printStackTrace();
			}
		} catch (IOException e) {
			handler.sendEmptyMessage(MSG_CLEAN_ERROR);
			e.printStackTrace();
		}
	}

	private boolean checkSDCard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}

	}

	private GetUserInfoTask userInfoTask;

	private void getUserInfo() {
		// TODO 以后得删除
		if (userInfoTask == null) {
			if (UserNow.current().userID != 0) {
				userInfoTask = new GetUserInfoTask();
				userInfoTask.execute(this, this, new MySpaceRequest(),
						new LoginNewParser());
				// refreshProgress.setVisibility(View.VISIBLE);
				// refresh.setVisibility(View.GONE);
			}
		}
	}

	private void updateUserInfo() {
		String name = UserNow.current().name;
		if (name != null) {
			userNameTextView.setText(name);
		}
		String lvl = UserNow.current().level;
		if (lvl != null) {
			userLevelTextView.setText("Lv " + lvl);
		}
		String signature = UserNow.current().signature;
		if (signature != null) {
			signatureTextView.setText(signature);
		}
		if (UserNow.current().gender == 2) {
			gender.setImageResource(R.drawable.uc_sex_female);
		} else
			gender.setImageResource(R.drawable.uc_sex_male);

		String fellowers = String.valueOf(UserNow.current().friendCount);
		fellowTextView.setText(fellowers);
		String fansCount = String.valueOf(UserNow.current().fansCount);
		fansTextView.setText(fansCount);
		String comments = String.valueOf(UserNow.current().commentCount);
		commentTextView.setText(comments);
		String medals = String.valueOf(UserNow.current().badgesCount);
		medalTextView.setText(medals);
		String privateMsgs = String
				.valueOf(UserNow.current().privateMessageAllCount);
		privateMsgTextView.setText(privateMsgs);
		String chaseCount = String.valueOf(UserNow.current().chaseCount);
		zhuijuTextView.setText(chaseCount);
		String remindsCount = String.valueOf(UserNow.current().remindCount);
		bookTextView.setText(remindsCount);
		String channelCounts = String.valueOf(UserNow.current().channelCount);
		channelTextView.setText(channelCounts);

		// @me
		String atMeCount = String.valueOf(UserNow.current().atMeCount);
		atMeTextView.setText(atMeCount);
		String replyMeCount = String.valueOf(UserNow.current().replyMeCount);
		replyMeTextView.setText(replyMeCount);

		String url = UserNow.current().iconURL;

		if (SinaData.isSinaBind) {
			bindSina.setImageResource(R.drawable.sina_selected);
			bindSina.setOnClickListener(ubBindSinaListener);
			findViewById(R.id.bind_layout).setOnClickListener(
					ubBindSinaListener);
		} else {
			bindSina.setImageResource(R.drawable.sina);
			bindSina.setOnClickListener(this);
			findViewById(R.id.bind_layout).setOnClickListener(this);
		}
		icon.setTag(url);
		if (url != null) {
			loadImage(icon, url);
		}
	}

	/**
	 * 加载图片
	 */
	private void loadImage(final ImageView imageView, String url) {
		final ImageView local = imageView;
		Drawable drawable = imageLoader.loadDrawable(url, new ImageCallback() {
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				local.setImageDrawable(imageDrawable);
			}
		});
		if (drawable != null && local.getTag().equals(url)) {
			local.setImageDrawable(drawable);
		}
	}

	private static final int LOGIN_REQUEST = 1;
	private static final int EDIT_REQUEST = 2;
	private static final int REGISTER_REQUEST = 3;

	private void openLoginActivity() {
		Intent intent = new Intent(MyActivity.this, LoginActivity.class);
		startActivityForResult(intent, LOGIN_REQUEST);
	}

	private void openRegisterActivity() {
		Intent intent = new Intent(MyActivity.this, RegisterActivity.class);
		startActivityForResult(intent, REGISTER_REQUEST);
	}

	private void openUserEditActivity() {
		Intent intent = new Intent(MyActivity.this, UserInfoEditActivity.class);
		startActivityForResult(intent, EDIT_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (funcFlag == SINA) {
			hidepb();
			funcFlag = 0;
			if (ssh != null) {
				ssh.authorizeCallBack(requestCode, resultCode, data);
			}
		} else {
			switch (requestCode) {
			case LOGIN_REQUEST:
			case REGISTER_REQUEST:
				if (resultCode == RESULT_OK) {
					showLoginLayout();
					updateUserInfo();
				}
				break;
			case EDIT_REQUEST:
				updateUserInfo();
				break;
			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private LogoffTask logoffTask;

	private void logOff() {
		if (logoffTask == null) {
			logoffTask = new LogoffTask(this);
			logoffTask.execute(this, new LogoffRequest(), new LogoffParser());
		}
	}

	@Override
	public void onNetBegin(String method) {

	}

	private void SaveUserData(boolean b) {
		SharedPreferences sp = getSharedPreferences("userInfo", 0);
		Editor spEd = sp.edit();

		if (b) {
			spEd.putInt("openType", BindOpenAPIData.current().openType);
			spEd.putBoolean("isOpenTypeLogin",
					BindOpenAPIData.current().isOpenTypeLogin);
			spEd.putBoolean("login", true);
			spEd.putBoolean("autologin", true);
			spEd.putString("username", UserNow.current().name);
			spEd.putString("name", UserNow.current().name);
			spEd.putString("nickName", UserNow.current().name);
			if (!BindOpenAPIData.current().isOpenTypeLogin) {
				spEd.putString("password", UserNow.current().passwd);
			} else {
				spEd.putString("password", "");
			}
			spEd.putString("address", UserNow.current().eMail);
			spEd.putString("sessionID", UserNow.current().sessionID);

			spEd.putInt("checkInCount", UserNow.current().checkInCount);
			spEd.putInt("commentCount", UserNow.current().commentCount);
			spEd.putInt("messageCount",
					UserNow.current().privateMessageAllCount);
			spEd.putInt("messagePeopleCount",
					UserNow.current().privateMessageOnlyCount);
			spEd.putInt("fansCount", UserNow.current().fansCount);
			spEd.putInt("friendCount", UserNow.current().friendCount);

			spEd.putString("iconURL", UserNow.current().iconURL);
			spEd.putInt("userID", UserNow.current().userID);
			spEd.putLong("point", UserNow.current().point);
			spEd.putString("level", UserNow.current().level);
			spEd.putInt("gender", UserNow.current().gender);
			spEd.putLong("exp", UserNow.current().exp);
			spEd.putString("signature", UserNow.current().signature);
			spEd.putInt("commentCount", UserNow.current().commentCount);
			spEd.putInt("chaseCount", UserNow.current().chaseCount);
			spEd.putInt("remindCount", UserNow.current().remindCount);

			// 被@数量
			spEd.putInt("atMeCount", UserNow.current().atMeCount);
			// 被回复数量
			spEd.putInt("replyMeCount", UserNow.current().replyMeCount);
			spEd.putInt("badgesCount", UserNow.current().badgesCount);
		} else {
			spEd.putBoolean("isOpenTypeLogin", false);
			spEd.putBoolean("login", false);
			spEd.putBoolean("autologin", false);
			spEd.putString("username", "");
			spEd.putString("password", "");
			spEd.putInt("userID", 0);
		}

		spEd.commit();
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("mySpace".equals(method)) {
			if (msg != null && msg.equals("")) {
				updateUserInfo();
				SaveUserData(true);
			} else {
				alertToast(msg);
			}
			userInfoTask = null;
			// refreshProgress.setVisibility(View.GONE);
			// refresh.setVisibility(View.VISIBLE);
		} else if ("versionLatest".equals(method)) {
			if (msg != null && msg.equals("")) {

				showNewVersionDialog(this, versionData.versionId,
						versionData.info);
			} else {
				alertToast(msg);
			}
			getAppNewVersionTask = null;
		} else if ("cancel".endsWith(method)) {
			logoffTask = null;
		} else if ("bindUser".equals(method)) {
			hidepb();
			if (msg != null && msg.equals("")) {
				SinaData.isSinaBind = true;
				bindSina.setImageResource(R.drawable.sina_selected);
				bindSina.setOnClickListener(ubBindSinaListener);
				findViewById(R.id.bind_layout).setOnClickListener(
						ubBindSinaListener);
				alertToast("新浪微博绑定成功");
			} else {
				alertToast(msg);
			}
		} else if ("bindDelete".equals(method)) {
			hidepb();
			if (msg != null && msg.equals("")) {
				SinaData.isSinaBind = false;
				bindSina.setImageResource(R.drawable.sina);
				bindSina.setOnClickListener(this);
				findViewById(R.id.bind_layout).setOnClickListener(this);
				alertToast("新浪微博解除绑定");
			} else {
				alertToast(msg);
			}
		}
		if (UserNow.current().getNewBadge() != null) {
			for (int i = 0; i < UserNow.current().getNewBadge().size(); i++) {
				String name = UserNow.current().getNewBadge().get(i).name;
				if (name != null) {
					DialogUtil.showBadgeAddToast(MyActivity.this, name);
				}
			}
			UserNow.current().setNewBadge(null);
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	private void alertToast(String msg) {
		if (msg != null) {
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private VersionData versionData;
	private GetAppNewVersionTask getAppNewVersionTask;

	private void getAppNewVersion() {
		if (getAppNewVersionTask == null) {
			if (versionData == null) {
				versionData = new VersionData();
			}
			getAppNewVersionTask = new GetAppNewVersionTask();
			getAppNewVersionTask.execute(this, this, versionData);
		}
	}

	public void showNewVersionDialog(Context context, String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton("现在更新",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						starAppDownloadService();
						alertToast("新版本已经开始下载，您可在通知栏观看下载进度");
					}

				});
		builder.setNegativeButton("稍后再说",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	public void starAppDownloadService() {
		Intent intent = new Intent(this, AppUpdateService.class);
		intent.putExtra("url", versionData.downLoadUrl);
		intent.putExtra("size", versionData.size);
		startService(intent);
	}

	protected void dialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("确定要退出吗?");
		builder.setCancelable(false);
		builder.setIcon(R.drawable.icon_small);
		builder.setPositiveButton("确定",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (connectBg.isShown()) {
				hidepb();
				return true;
			}
			return super.onKeyDown(keyCode, event);
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private final OnClickListener ubBindSinaListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			unbindDialog();
		}
	};

	// 微博解绑
	protected void unbindDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("确定解除与新浪微博绑定么？");
		builder.setCancelable(false);
		builder.setIcon(R.drawable.icon_small);
		builder.setPositiveButton("解绑",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						unBindAccount();
					}
				});
		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void unBindAccount() {
		UserNow.current().errorCode = -1;
		BindDeleteTask bindDeleteTask = new BindDeleteTask(this);
		bindDeleteTask.execute(this, new BindDeleteRequest(),
				new ResultParser());
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (Constants.key_privateMsg.equals(key)) {
			if (pushMsgPreferences.getBoolean(Constants.key_privateMsg, false)) {
				privateMsgTipIamgeView.setVisibility(View.VISIBLE);
			} else {
				privateMsgTipIamgeView.setVisibility(View.GONE);
			}
		}
		if (Constants.key_fans.equals(key)) {
			if (pushMsgPreferences.getBoolean(Constants.key_fans, false)) {
				fansTipImageView.setVisibility(View.VISIBLE);
			} else {
				fansTipImageView.setVisibility(View.GONE);
			}
		}
		if (Constants.key_reply.equals(key)) {
			if (spUser.getBoolean(Constants.key_reply, false)) {
				replyTipImageView.setVisibility(View.VISIBLE);
			} else {
				replyTipImageView.setVisibility(View.GONE);
			}
		}
		if (Constants.key_beiAt.equals(key)) {
			if (spUser.getBoolean(Constants.key_beiAt, false)) {
				beiAtTipImageView.setVisibility(View.VISIBLE);
			} else {
				beiAtTipImageView.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 打开未登录布局
	 */
	private void showUnLoginLayout() {
		loginLayout.setVisibility(View.VISIBLE);
		baseInfoLayout.setVisibility(View.GONE);
		zuxiaoBtn.setVisibility(View.INVISIBLE);
		bindLayout.setVisibility(View.GONE);
		// refresh.setVisibility(View.GONE);
		editUserInfo.setVisibility(View.GONE);
		hscroll.setVisibility(View.GONE);
		refreshProgress.setVisibility(View.GONE);
		divider.setVisibility(View.INVISIBLE);
		findViewById(R.id.diviver).setVisibility(View.INVISIBLE);
	}

	/**
	 * 打开登录的布局
	 */
	private void showLoginLayout() {
		loginLayout.setVisibility(View.GONE);
		baseInfoLayout.setVisibility(View.VISIBLE);
		zuxiaoBtn.setVisibility(View.VISIBLE);
		bindLayout.setVisibility(View.VISIBLE);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		int horizontalMargin = CommonUtils.dip2px(this, 10);
		params.setMargins(horizontalMargin, 0, horizontalMargin,
				CommonUtils.dip2px(this, 15));
		bindLayout.setLayoutParams(params);
		// refresh.setVisibility(View.VISIBLE);
		editUserInfo.setVisibility(View.VISIBLE);
		if (SinaData.isSinaBind) {
			bindSina.setImageResource(R.drawable.sina_selected);
			bindSina.setOnClickListener(ubBindSinaListener);
		}
		hscroll.setVisibility(View.VISIBLE);
		divider.setVisibility(View.VISIBLE);
		findViewById(R.id.diviver).setVisibility(View.VISIBLE);
	}

	private void setPushMsgIndicator(boolean isOn) {
		if (isOn) {
			pushMsgIndicator.setImageResource(R.drawable.my_fast_open);
		} else {
			pushMsgIndicator.setImageResource(R.drawable.my_fast_close);
		}
	}

	private void savePushMsgState(boolean isOn) {
		SharedPreferences sp = getSharedPreferences("pusMsgIndicator", 0);
		sp.edit().putBoolean("isOn", isOn).commit();
	}

	private boolean getPushMsgState() {
		SharedPreferences sp = getSharedPreferences("pusMsgIndicator", 0);
		return sp.getBoolean("isOn", true);
	}

	private TextView divider;
}
