package com.sumavision.talktv2.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.KeyEvent;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.VersionData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.services.AppUpdateService;
import com.sumavision.talktv2.services.NotificationService;
import com.sumavision.talktv2.task.GetAppNewVersionTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.AppUtil;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class SplashNewActivity extends Activity implements
		NetConnectionListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		MobclickAgent.onEvent(getApplicationContext(), "qidong");
		getUserLocalData();
		detectNewVersion();
		// Intent intent = new Intent();
		// intent.setAction(ConnectivityReceiver.startAction);
		// sendBroadcast(intent);
		if (!AppUtil.isNotifyServiceRunning(this, AppUtil.getPackageName(this)
				+ ".services.NotificationService")) {
			startNotificationService();
		}
	}

	private void startNotificationService() {

		Intent intent = new Intent(this, NotificationService.class);
		startService(intent);
	}

	private void getUserLocalData() {
		SharedPreferences spUser;
		spUser = getSharedPreferences("userInfo", 0);
		UserNow.current().userID = spUser.getInt("userID", 0);
		if (UserNow.current().userID != 0) {
			UserNow.current().nickName = spUser.getString("nickName", "");
			UserNow.current().eMail = spUser.getString("address", "");
			UserNow.current().sessionID = spUser.getString("sessionID", "");
			UserNow.current().checkInCount = spUser.getInt("checkInCount", 0);
			UserNow.current().commentCount = spUser.getInt("commentCount", 0);
			UserNow.current().fansCount = spUser.getInt("fansCount", 0);
			UserNow.current().privateMessageAllCount = spUser.getInt(
					"messageCount", 0);
			UserNow.current().privateMessageOnlyCount = spUser.getInt(
					"messagePeopleCount", 0);

			UserNow.current().iconURL = spUser.getString("iconURL", "");
			UserNow.current().signature = spUser.getString("signature", "");
			UserNow.current().point = (int) spUser.getLong("point", 0);
			UserNow.current().level = spUser.getString("level", "1");
			UserNow.current().gender = spUser.getInt("gender", 1);
			UserNow.current().exp = (int) spUser.getLong("exp", 0);

			UserNow.current().name = spUser.getString("name", "xxx");
			UserNow.current().friendCount = spUser.getInt("friendCount", 0);
			UserNow.current().remindCount = spUser.getInt("remindCount", 0);
			UserNow.current().chaseCount = spUser.getInt("chaseCount", 0);
			UserNow.current().passwd = spUser.getString("password", "");
			UserNow.current().commentCount = spUser.getInt("commentCount", 0);

			// 被@数量
			UserNow.current().atMeCount = spUser.getInt("atMeCount", 0);
			// 被回复数量
			UserNow.current().replyMeCount = spUser.getInt("replyMeCount", 0);
			UserNow.current().badgesCount = spUser.getInt("badgesCount", 0);

		} else {
			UserNow.current().isSelf = false;
		}

		OtherCacheData.current().isFromMyActivityToLogin = false;
		OtherCacheData.current().isFromMyActivityToLoginNotClose = false;
	}

	private void detectNewVersion() {
		if (AppUtil.isSystemUpdateServiceRunning(this,
				AppUtil.getPackageName(this) + ".services.AppUpdateService")) {
			DialogUtil.alertToast(getApplicationContext(), "正在下载新版本");
			openHelpActivity();
		} else {
			getAppNewVersion();
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
						DialogUtil.alertToast(getApplicationContext(),
								"新版本已经开始下载，您可在通知栏观看下载进度");
						openHelpActivity();// 打开主页
					}

				});
		builder.setNegativeButton("稍后再说",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						openHelpActivity();// 打开主页
					}
				});
		builder.setCancelable(false).create().show();
	}

	public void starAppDownloadService() {
		Intent intent = new Intent(this, AppUpdateService.class);
		intent.putExtra("url", versionData.downLoadUrl);
		intent.putExtra("size", versionData.size);
		startService(intent);
	}

	private void openMainTabActivity() {

		// Intent intent = new Intent(this, MainTabActivityNew.class);
		// startActivity(intent);
		// finish();
		handler.sendEmptyMessageDelayed(OPEN_MAIN_PAGE, 500);
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("versionLatest".equals(method)) {
			if (msg != null && msg.equals("")) {
				showNewVersionDialog(this, versionData.versionId,
						versionData.info);
			} else {
				if (msg == null) {
					msg = "网络不给力";
				}
				openHelpActivity();
			}
			getAppNewVersionTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	private void openHelpActivity() {
		SharedPreferences sp = getSharedPreferences("otherInfo", MODE_PRIVATE);
		boolean isNeedShowHelp = sp.getBoolean("isShowHelp", true);
		if (isNeedShowHelp) {
			Intent intent = new Intent(this, HelpActivity.class);
			startActivityForResult(intent, REQUEST_HELP);
		} else {
			openMainTabActivity();
		}
	}

	private static final int REQUEST_HELP = 10;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_HELP) {
			openMainTabActivity();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			close();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private void close() {
		if (getAppNewVersionTask != null) {
			getAppNewVersionTask.cancel(true);
			getAppNewVersionTask = null;
		}
		finish();
	}

	private final int OPEN_MAIN_PAGE = 1;
	private Handler handler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case OPEN_MAIN_PAGE:
				Intent intent = new Intent(SplashNewActivity.this,
						SlidingBaseActivity.class);
				startActivity(intent);
				finish();
				break;
			default:
				break;
			}
			return false;
		}
	});

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
