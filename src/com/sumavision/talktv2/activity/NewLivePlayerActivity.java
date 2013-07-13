package com.sumavision.talktv2.activity;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnSeekCompleteListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import local.player.vi.widget.LibsChecker;
import local.player.vi.widget.VideoView;

import org.cybergarage.upnp.Device;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewStub;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.components.RotateStyle;
import com.sumavision.talktv2.dao.AccessProgram;
import com.sumavision.talktv2.data.BadgeData;
import com.sumavision.talktv2.data.ClientData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.ShareData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.dlna.DLNAControllActivity;
import com.sumavision.talktv2.dlna.DeviceAdapter;
import com.sumavision.talktv2.dlna.DeviceDataInSearchList;
import com.sumavision.talktv2.dlna.common.DeviceData;
import com.sumavision.talktv2.dlna.services.DlnaService;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.net.PlayVideoRequest;
import com.sumavision.talktv2.net.ResultParser;
import com.sumavision.talktv2.task.ActivityPlayVideoTask;
import com.sumavision.talktv2.task.PlayCountTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.BitmapUtils;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DLNAUtil;
import com.sumavision.talktv2.utils.DialogUtil;
import com.sumavision.tvfanmultiscreen.data.DLNAData;
import com.umeng.analytics.MobclickAgent;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

/**
 * 
 * @author jianghao
 * @description 播放器
 * 
 * 
 */

public class NewLivePlayerActivity extends Activity implements OnClickListener,
		OnSeekBarChangeListener, OnTouchListener, OnBufferingUpdateListener,
		OnPreparedListener, OnCompletionListener, OnErrorListener,
		OnSeekCompleteListener, OnInfoListener, NetConnectionListener,
		RequestListener, NetConnectionListenerNew {

	private String path;
	private VideoView mVideoView;

	private static final int HIDE_NAME = 2;
	private static final int HIDE_CONTROLER = 33;
	private static final int HIDE_CONTROLER_LOCKIG = 34;
	private static final int CANCEL_HIDE_CONTROLER = 4;
	private static final int PROCESS_PLAY_HISTORY = 5;
	private static final int PROGRESS_CHANGED = 6;
	// private static final int REFRESH_NET_SPEED = 7;
	private static final int UNLOCK_ANIMAION = 8;
	private final int SEARCHDEVICE = 1;

	private RelativeLayout pb;
	private TextView pb_txt1, pb_txt2, netSpeed;
	private ImageView tipsBG;
	// 节目类型
	private int playType = 1;
	// 节目id
	private String programId;
	// private static final int LIVE_PLAY = 1;
	private static final int VOD_PLAY = 2;
	// 控制栏
	private LinearLayout controllerBottom;
	private RelativeLayout controllerTop;
	private TextView cotrollerTime;
	private TextView title;
	private Button back;
	private ImageButton lock;
	private ImageButton lockBig;
	private ImageButton shuai;
	private Button share;
	private ImageButton cotrollerPrev;
	private ImageButton cotrollerNext;
	private ImageButton cotrollerPlay;
	private SeekBar cotrollerProgressSeek;
	private String durationStr;
	private Animation close2Up;
	private Animation open2Up;
	private Animation open2Bottom;
	private Animation close2Bottom;
	private boolean hasShowned = true;
	private String titleName;
	private boolean isLocked = false;
	private Animation left2right;
	private AudioManager audioManager;
	// 新版出错提示
	private RelativeLayout errTips;
	// 重新加载按钮
	private ImageView reloadVideoBtn;
	private final boolean isHasPlugin = true;
	// 用于网速
	private long lastReadBytes = 0;
	private String strSpeed = "0K/S";
	// 内存空间是否足够
	private boolean hasAvailableSpace = true;

	private ImageButton scaleBtn;
	private boolean backBtnPressed = false;

	@Override
	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		hasAvailableSpace = hasAvailableSpace();
		if (!hasAvailableSpace) {
			if (OtherCacheData.current().isDebugMode)
				Log.e("NewLivePlayerActivity", "手机空间不足");
			Toast.makeText(getApplicationContext(), "手机空间不足，无法播放，请您清理空间后重试",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		} else {
			boolean hasPlug = LibsChecker.checkVitamioLibs(this,
					R.string.init_decoders);
			if (!hasPlug)
				return;
		}

		initAnimation();
		getNowProgramData();
		PlayVideoCount();
		initOthers();

		setContentView(R.layout.videoview);
		// 记录播放次数

		Intent intent = getIntent();
		// client 客户端类型1=安卓2=苹果；
		// playType 播放方式1=客户端播放2=网页播放
		// programId 节目id
		// playUrl 播放地址
		if (intent.hasExtra("programPic")) {
			programPic = intent.getStringExtra("programPic");
		}
		playType = intent.getIntExtra("playType", VOD_PLAY);
		path = intent.getStringExtra("path");
		programId = intent.getStringExtra("id");
		activityId = intent.getIntExtra("playVideoActivityId", 0);
		if (activityId != 0) {
			needSendAcitivytId = true;
		}
		Uri uri = this.getIntent().getData();
		if (uri != null) {
			Log.e("NewLivePlayer", "uri" + uri.toString());
			if (uri.toString().startsWith("tvfanplayurl")) {
				String str = "http://"
						+ uri.toString().substring(15, uri.toString().length());

				if (str != null && !str.equals("")) {
					path = str;
					Log.e("NewLivePlayer", path);
				}
			} else if (uri.toString().endsWith("tvfanplayurl")) {
				String str = uri.toString().substring(0,
						uri.toString().length() - 12);

				if (str != null && !str.equals("")) {
					path = str;
				}
			} else if (uri.toString().startsWith("tvfanvideoweixin")) {
				String str = uri.toString().substring(19,
						uri.toString().length());
				if (!str.startsWith("http:")) {
					StringBuffer sb = new StringBuffer(str);
					sb.insert(4, ":");
					str = sb.toString();
				}
				if (str != null && !str.equals("")) {
					path = str;
					Log.e("NewLivePlayer", path);
				}
			} else {
				Toast.makeText(getApplicationContext(), "暂不支持此链接",
						Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
		}

		if (path == null || path.equals("")) {
			DialogUtil.alertToast(getApplicationContext(), "播放地址不存在！");
			finish();
			return;
		}
		if (OtherCacheData.current().isDebugMode) {

			Log.e(TAG, path);
		}
		titleName = intent.getStringExtra("title");
		title = (TextView) findViewById(R.id.np_live_title);
		if (titleName != null && !titleName.equals("")) {
			title.setText(titleName);
		} else {
			title.setText("电视粉节目播放");
		}
		controllerTop = (RelativeLayout) findViewById(R.id.np_l_top_layout);
		lock = (ImageButton) findViewById(R.id.lock);
		lock.setOnClickListener(this);
		lockBig = (ImageButton) findViewById(R.id.lock_big);
		lockBig.setOnClickListener(this);
		back = (Button) findViewById(R.id.np_live_back);
		back.setOnClickListener(this);
		shuai = (ImageButton) findViewById(R.id.shuai);
		shuai.setOnClickListener(this);
		share = (Button) findViewById(R.id.share);
		share.setOnClickListener(this);
		share.setVisibility(View.INVISIBLE);

		tipsBG = (ImageView) findViewById(R.id.videoplay_tips_bg);
		tipsBG.setClickable(true);
		pb = (RelativeLayout) findViewById(R.id.videoplay_progress_tv);
		pb_txt1 = (TextView) findViewById(R.id.videoplay_progress_tv_txt);
		pb_txt2 = (TextView) findViewById(R.id.videoplay_progress_tv_txt1);
		netSpeed = (TextView) findViewById(R.id.netSpeedView);
		fromView = (TextView) findViewById(R.id.from);
		if (intent.hasExtra("fromString")) {
			fromString = intent.getStringExtra("fromString");
		}
		if (fromString != null && !fromString.equals("")) {
			fromView.setText("来自:" + fromString);
		}

		mVideoView = (VideoView) findViewById(R.id.surface_view);
		if (path.startsWith("http:")) {
			mVideoView.setVideoURI(Uri.parse(path));
		} else {
			mVideoView.setVideoPath(path);
		}

		mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
		mVideoView.setBufferSize(8192);
		mVideoView.setKeepScreenOn(true);
		mVideoView.setOnErrorListener(this);
		mVideoView.setOnPreparedListener(this);
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnSeekCompleteListener(this);
		mVideoView.setOnBufferingUpdateListener(this);
		mVideoView.setOnTouchListener(this);
		mVideoView.setOnInfoListener(this);

		errTips = (RelativeLayout) findViewById(R.id.np_l_error_tips_layout);
		reloadVideoBtn = (ImageView) findViewById(R.id.np_l_error_tips_layout_btn);
		reloadVideoBtn.setOnClickListener(this);
		shareLayout = (LinearLayout) findViewById(R.id.share_layout);
		weixinView = (TextView) findViewById(R.id.weixin);
		weiboView = (TextView) findViewById(R.id.sina);
		weixinView.setOnClickListener(this);
		weiboView.setOnClickListener(this);
		volSeekBar = (VerticalSeekBar) findViewById(R.id.vol);
		volSeekBar.setOnSeekBarChangeListener(volListener);
		volSeekBar.setMax(100);
		soundBtn = (ImageButton) findViewById(R.id.tool_vol);
		soundBtn.setOnClickListener(this);

		scaleBtn = (ImageButton) findViewById(R.id.scale);
		scaleBtn.setOnClickListener(this);

		initController();
		timeTextView = (TextView) findViewById(R.id.time);
		batteryImageView = (ImageView) findViewById(R.id.battery);
		timer = new Timer();
		try {
			timer.schedule(task, 0, 60 * 1000);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, intentFilter);

	}

	private void initAnimation() {

		left2right = AnimationUtils.loadAnimation(this, R.anim.leftright);
		close2Up = AnimationUtils.loadAnimation(this, R.anim.close2up);
		open2Up = AnimationUtils.loadAnimation(this, R.anim.open2up);
		close2Bottom = AnimationUtils.loadAnimation(this, R.anim.close2bottom);
		open2Bottom = AnimationUtils.loadAnimation(this, R.anim.open2bottom);
	}

	private void initOthers() {

		accessProgram = new AccessProgram(this);
	}

	private static final String TAG = "NewLivePlayerActivity";

	private void errorTips() {
		errTips.setVisibility(View.VISIBLE);
	}

	private void initController() {

		controllerBottom = (LinearLayout) findViewById(R.id.mpl_play_bottom);
		cotrollerTime = (TextView) findViewById(R.id.tool_timezone);
		cotrollerPrev = (ImageButton) findViewById(R.id.tool_back);
		cotrollerPrev.setOnClickListener(this);
		cotrollerNext = (ImageButton) findViewById(R.id.tool_advance);
		cotrollerNext.setOnClickListener(this);
		cotrollerPlay = (ImageButton) findViewById(R.id.tool_play);
		cotrollerPlay.setOnClickListener(this);
		cotrollerProgressSeek = (SeekBar) findViewById(R.id.tool_progress);
		cotrollerProgressSeek.setOnSeekBarChangeListener(this);
		findViewById(R.id.share).setOnClickListener(this);
		if (playType != VOD_PLAY) {
			cotrollerProgressSeek.setEnabled(false);
			cotrollerPlay.setEnabled(false);
			cotrollerNext.setEnabled(false);
			cotrollerPrev.setEnabled(false);
			cotrollerTime.setVisibility(View.GONE);
		} else {
			cotrollerTime.setVisibility(View.GONE);// 控件拿出来的总长度不对 ，暂时先不显示
													// 征程情况该显示
		}
		initStartVol();
	}

	private void initStartVol() {

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		streamMaxVolume = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		streamNowVolume = audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		// SharedPreferences sp = getSharedPreferences("playerVol", 0);
		// int progress = sp.getInt("volProgress", streamNowVolume);
		int progress = streamNowVolume * 100 / streamMaxVolume;
		volSeekBar.setProgress(progress);
		setVol(progress * streamMaxVolume / 100);
		if (progress <= 30 && progress > 0) {
			soundBtn.setImageResource(R.drawable.player_sound_min);
		} else if (progress > 70) {
			soundBtn.setImageResource(R.drawable.player_sound_max);
		} else if (progress <= 70 && progress > 30) {
			soundBtn.setImageResource(R.drawable.player_sound_middle);
		} else {
			soundBtn.setImageResource(R.drawable.player_sound_disable);
		}
	}

	private void setVol(int value) {

		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
	}

	private void hideController() {

		lockBig.setImageResource(R.drawable.liveplaye_bigr_locked);
		lockBig.setVisibility(View.GONE);
		controllerBottom.setAnimation(close2Bottom);
		close2Bottom.startNow();
		controllerBottom.setVisibility(View.GONE);
		controllerTop.setAnimation(close2Up);
		close2Up.startNow();
		controllerTop.setVisibility(View.GONE);
		shareLayout.setVisibility(View.GONE);
		volSeekBar.setVisibility(View.GONE);
		hasShowned = false;
		if (isLocked)
			lock.setVisibility(View.VISIBLE);
		else
			lock.setVisibility(View.GONE);
	}

	private void openController() {

		controllerBottom.setAnimation(open2Bottom);
		open2Bottom.startNow();
		controllerBottom.setVisibility(View.VISIBLE);
		controllerTop.setVisibility(View.VISIBLE);
		controllerTop.setAnimation(open2Up);
		open2Up.startNow();
		hasShowned = true;
		lock.setVisibility(View.VISIBLE);
		lock.setAnimation(open2Bottom);
	}

	private void cancelDelayHide() {

		handler.removeMessages(HIDE_CONTROLER);
	}

	private static final int HIDE_DELAY_TIME = 5000;
	protected static final int MSG_CLOSE_ACTIVITY = 0;

	private void hideControllerDelay() {

		handler.sendEmptyMessageDelayed(HIDE_CONTROLER, HIDE_DELAY_TIME);
	}

	// 视频锁的处理
	private void hideLockBig() {
		handler.sendEmptyMessageDelayed(HIDE_CONTROLER_LOCKIG, 2000);
	}

	private void cancelHideLockBig() {
		handler.removeMessages(HIDE_CONTROLER_LOCKIG);

	}

	private void dismisLockBig() {
		lockBig.setVisibility(View.GONE);
	}

	// 缓冲后自动播放次数
	private int isBufferingCount = 0;
	private final int BUFFERING_COUNT = 4;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			// case REFRESH_NET_SPEED:
			// // if (mVideoView.isPlaying()) {
			// // Log.e("REFRESH_NET_SPEED", "isPlaying");
			// // } else if (mVideoView.isBuffering()) {
			// // Log.e("REFRESH_NET_SPEED", "isBuffering");
			// // } else {
			// // Log.e("REFRESH_NET_SPEED", "unknow status!");
			// //
			// // }
			//
			// try {
			// if (pb_txt1.isShown()) {
			// long curReadBytes = CommonUtils.getNetSpeed();
			// if (OtherCacheData.current().isDebugMode)
			// Log.e("REFRESH_NET_SPEED-lastReadBytes",
			// lastReadBytes + "\n" + curReadBytes);
			// strSpeed = (curReadBytes - lastReadBytes) / 1024
			// + "K/S";// kbps
			// netSpeed.setText(netSpeedPre + strSpeed + ")");
			//
			// if (curReadBytes - lastReadBytes == 0) {
			// isBufferingCount++;
			// if (isBufferingCount > BUFFERING_COUNT) {
			// Toast.makeText(getApplicationContext(),
			// "当前网络不稳定，请您耐心等待或选择其他视频源后重试...",
			// Toast.LENGTH_LONG).show();
			// isBufferingCount = 0;
			// // mVideoView.stopPlayback();
			// // timer.cancel();
			// // onCreate(null);
			// }
			// }
			//
			// lastReadBytes = curReadBytes;
			// }
			// handler.sendEmptyMessageDelayed(REFRESH_NET_SPEED, 1000);
			// } catch (NullPointerException e) {
			// e.printStackTrace();
			// }
			// break;
			case UNLOCK_ANIMAION:
				lockBig.setVisibility(View.GONE);
				openController();
				hideControllerDelay();
				break;
			case PROCESS_PLAY_HISTORY:
				long historyPosition = getHistoryPlayPosition();
				if (historyPosition != 0) {
					mVideoView.seekTo(historyPosition);
				}
				break;
			case PROGRESS_CHANGED:
				updateUI();
				break;

			case HIDE_CONTROLER:
				hideController();
				break;
			case HIDE_CONTROLER_LOCKIG:
				dismisLockBig();
				break;
			case HIDE_NAME:
				break;
			case UPDATE_TIME_BATTERY:
				setTimeValue();
				break;
			default:
				break;
			}
		};
	};

	private String getTimeString(long allSecond) {

		long hour = allSecond / 3600;
		long minute = (allSecond - hour * 3600) / 60;
		long sencond = (allSecond - hour * 3600 - minute * 60);
		String currentTime = "";
		if (hour < 10) {
			currentTime += "0" + hour + ":";
		} else {
			currentTime += hour + ":";
		}
		if (minute < 10) {
			currentTime += "0" + minute + ":";
		} else {
			currentTime += minute + ":";
		}
		if (sencond < 10) {
			currentTime += "0" + sencond;
		} else {
			currentTime += sencond;
		}
		return currentTime;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	private AccessProgram accessProgram;
	private long dbPosition;
	private long videoDuration;
	// 节目页面传递过来的节目数据，用于存储播放历史
	private VodProgramData vpd;

	private void getNowProgramData() {

		Intent i = getIntent();
		vpd = new VodProgramData();
		vpd.topicId = i.getStringExtra("topicId");
		vpd.id = i.getStringExtra("id");
		vpd.updateName = i.getStringExtra("updateName");
		if (i.hasExtra("nameHolder"))
			vpd.nameHolder = i.getStringExtra("nameHolder");

	}

	private void saveProgram() {

		if (videoDuration - dbPosition <= 10000) {
			dbPosition = 0;
		}
		VodProgramData temp = new VodProgramData();
		temp.topicId = vpd.topicId;
		temp.id = vpd.id;
		temp.updateName = vpd.updateName;
		temp.dbposition = dbPosition;
		temp.dbUrl = path;
		temp.name = vpd.nameHolder;
		accessProgram.save(temp);
	}

	private long getHistoryPlayPosition() {

		long historyPosition;
		VodProgramData temp = new VodProgramData();
		temp.dbUrl = path;
		temp.id = vpd.id;
		historyPosition = accessProgram.find(temp);
		return historyPosition;
	}

	@Override
	protected void onDestroy() {

		// 没有按下返回按键，自动重新打开
		// if (!backBtnPressed && path != null && !path.equals("")) {
		// Toast.makeText(getApplicationContext(),
		// "当前网络不稳定，请您耐心等待或选择其他视频源后重试...", Toast.LENGTH_LONG).show();
		// Intent i = new Intent(this, NewLivePlayerActivity.class);
		// i.putExtra("playType", playType);
		// i.putExtra("path", path);
		// i.putExtra("id", programId);
		// i.putExtra("title", titleName);
		// startActivity(i);
		// }
		super.onDestroy();
		if (isHasPlugin && hasAvailableSpace) {
			stopUpdateUIThread();
			cancelDelayHide();
			handler.removeMessages(CANCEL_HIDE_CONTROLER);
			handler.removeMessages(PROGRESS_CHANGED);
			if (playType == VOD_PLAY) {
				saveProgram();
			}
			if (mVideoView != null) {
				mVideoView.stopPlayback();
				mVideoView.suspend();
				mVideoView = null;
			}
		}
		setResult(RESULT_OK);
		if (timer != null) {
			timer.cancel();
			timer = null;
			unregisterReceiver(batteryReceiver);
		}
		if (receiver != null) {
			unregisterReceiver(receiver);
		}

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.shuai:
			if (DeviceData.getInstance().getSelectedDevice() == null) {
				Intent iN = new Intent(this, NetWorkNewActivity.class);
				startActivityForResult(iN, SEARCHDEVICE);
			} else {
				DLNAData.current().nowProgramLiveAddress = path;
				Intent intent = new Intent(NewLivePlayerActivity.this,
						DLNAControllActivity.class);
				// intent.setAction(DlnaService.DEVICE_SELECTED);
				intent.putExtra("selectedDevice", new DeviceDataInSearchList());
				intent.putExtra("playAddress", path);
				intent.putExtra("titleName", titleName);
				startActivity(intent);
				// openDLNAControlerActivity(0);
			}
			// if (dlnaLayout == null) {
			// receiver = new DeviceFoundReceiver();
			// registerReceiver(receiver, new IntentFilter(
			// DlnaService.NEW_DEVICES_FOUND));
			// inflateDlnaLayout();
			// showSearchLayout();
			// } else {
			// showSearchLayout();
			// }
			break;
		case R.id.lock_big:
			if (isLocked) {
				// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				lockBig.setImageResource(R.drawable.liveplaye_bigr_unlock);
				cancelDelayHide();
				playUnlockAnimation(lockBig, 1);
				handler.sendEmptyMessageDelayed(UNLOCK_ANIMAION, 1000);
			}
			isLocked = !isLocked;
			break;
		case R.id.lock:
			if (!isLocked) {
				lock.setImageResource(R.drawable.liveplayer_locked);
				cancelDelayHide();
				hideController();
				lockBig.setVisibility(View.VISIBLE);
				cancelHideLockBig();
				hideLockBig();
			}
			isLocked = !isLocked;
			break;
		case R.id.np_live_back:
			backBtnPressed = true;
			finish();
			break;
		case R.id.tool_play:
			if (playType != VOD_PLAY) {
				return;
			}
			if (mVideoView.isPlaying()) {
				mVideoView.pause();
				handler.removeMessages(PROGRESS_CHANGED);
				pb.setVisibility(View.VISIBLE);
				pb_txt1.setVisibility(View.GONE);
				netSpeed.setVisibility(View.GONE);
				cancelDelayHide();
				cotrollerPlay.setImageResource(R.drawable.cp_play_tool_play_bg);
			} else {
				cancelDelayHide();
				hideControllerDelay();
				mVideoView.start();
				handler.sendEmptyMessage(PROGRESS_CHANGED);

				pb.setVisibility(View.GONE);
				cotrollerPlay
						.setImageResource(R.drawable.cp_play_tool_pause_bg);
			}
			break;
		case R.id.tool_back:
			if (playType != VOD_PLAY) {
				return;
			}

			long p = mVideoView.getCurrentPosition() - 30000;
			if (p > 0)
				mVideoView.seekTo(p);
			break;
		case R.id.tool_advance:
			if (playType != VOD_PLAY) {
				return;
			}

			long currentPosition = mVideoView.getCurrentPosition();
			long duration = mVideoView.getDuration();
			if (currentPosition + 30000 < mVideoView.getDuration()) {
				mVideoView.seekTo(currentPosition + 30000);
			} else {
				mVideoView.seekTo(duration - 1500);
			}
			break;
		case R.id.share:
			cancelDelayHide();
			if (!shareLayout.isShown()) {
				shareLayout.setVisibility(View.VISIBLE);
			} else {
				shareLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.weixin:
			if (isPrepared && mVideoView.canPause()) {
				if (mVideoView.isPlaying()) {
					cotrollerPlay.performClick();
				}
				// mSharePosition = cotrollerProgressSeek.getProgress();
				// saveSharePosition(mSharePosition);
				if (Build.MODEL.contains("MI")) {
					mVideoView.stopPlayback();
				}
			}
			cancelDelayHide();
			// isShareState = true;
			// saveShareStatus(true);
			shareLayout.setVisibility(View.GONE);
			openShareWeixinActivity();
			break;
		case R.id.sina:
			if (isPrepared && mVideoView.canPause()) {
				if (mVideoView.isPlaying()) {
					cotrollerPlay.performClick();
				}
			}
			cancelDelayHide();
			shareLayout.setVisibility(View.GONE);
			openSendCommentActivity();
			break;
		case R.id.tool_vol:
			if (volSeekBar.isShown()) {
				volSeekBar.setVisibility(View.GONE);
			} else {
				volSeekBar.setVisibility(View.VISIBLE);
			}
			break;
		// 重新加载视频
		case R.id.np_l_error_tips_layout_btn:
			isLocked = false;
			lockBig.setVisibility(View.GONE);
			cancelDelayHide();
			openController();
			hideControllerDelay();
			pb.setVisibility(View.VISIBLE);
			tipsBG.setVisibility(View.GONE);
			errTips.setVisibility(View.GONE);
			mVideoView.setVideoPath(path);
			break;
		case R.id.scale:
			if (isScaled) {
				mVideoView.setVideoLayout(
						local.player.vi.widget.VideoView.VIDEO_LAYOUT_SCALE, 0);
				scaleBtn.setImageResource(R.drawable.video_scale_zoom);
			} else {
				mVideoView.setVideoLayout(
						local.player.vi.widget.VideoView.VIDEO_LAYOUT_ZOOM, 0);
				scaleBtn.setImageResource(R.drawable.video_scale_normal);
			}
			isScaled = !isScaled;
			break;
		default:
			break;
		}
	}

	private boolean isScaled = false;

	// 新浪微博先跳转到评论页面
	private void openSendCommentActivity() {
		Intent intent = new Intent(this, SendCommentActivity.class);
		intent.putExtra("fromWhere", 4);
		intent.putExtra("programName", titleName);
		if (programPic != null) {
			if (isFileExist(programPic)) {
				String fileFolder = Environment.getExternalStorageDirectory()
						+ "/TVFan/temp";
				if (programPic.contains("/") && programPic.contains(".")) {
					String name = programPic.substring(
							programPic.lastIndexOf("/") + 1,
							programPic.length());
					intent.putExtra("sinaPic", fileFolder + File.separator
							+ name);
				}
			}
		}
		startActivityForResult(intent, SendCommentActivity.NORMAL);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (OtherCacheData.current().isDebugMode)
			Log.e("lock", isLocked + "");
		cancelDelayHide();
		if (!isLocked) {
			if (hasShowned) {
				hideController();
			} else {
				if (!isLocked)
					openController();
				hideControllerDelay();
			}
		} else {

			if (lockBig.isShown())
				lockBig.setVisibility(View.GONE);
			else {
				lockBig.setVisibility(View.VISIBLE);
				cancelHideLockBig();
				hideLockBig();
			}
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		// initOthers();
		// //handler.removeMessages(REFRESH_NET_SPEED);
		// handler.sendEmptyMessage(REFRESH_NET_SPEED);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		// handler.removeMessages(REFRESH_NET_SPEED);

		if (mVideoView != null && isPrepared && mVideoView.canPause()) {
			mVideoView.pause();
			cotrollerPlay.setImageResource(R.drawable.cp_play_tool_play_bg);
		}
		Log.e("NewLivePlayer", "onPause");
	}

	@Override
	protected void onStart() {

		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();

		Log.e("NewLivePlayer", "onStop");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			setVol();
			return super.onKeyDown(keyCode, event);
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (hasShowned) {
				hideController();
			} else {
				cancelDelayHide();
				if (!isLocked)
					openController();
				hideControllerDelay();
			}
			return true;

		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!isLocked) {
				if (hasShowned) {
					hideController();
					return true;
				} else {
					backBtnPressed = true;
					return super.onKeyDown(keyCode, event);
				}
			} else {
				Toast.makeText(getApplicationContext(), "屏幕已锁定，请先解锁",
						Toast.LENGTH_SHORT).show();
				cancelDelayHide();
				if (!isLocked)
					openController();
				lockBig.setVisibility(View.VISIBLE);
				lockBig.startAnimation(left2right);
				cancelHideLockBig();
				hideLockBig();
				// hideControllerDelay();
				return true;
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private void setVol() {
		streamNowVolume = audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		int progress = streamNowVolume * 100 / streamMaxVolume;
		volSeekBar.setProgress(progress);
		// setVol(progress * streamMaxVolume / 100);
		if (progress <= 30 && progress > 0) {
			soundBtn.setImageResource(R.drawable.player_sound_min);
		} else if (progress > 70) {
			soundBtn.setImageResource(R.drawable.player_sound_max);
		} else if (progress <= 70 && progress > 30) {
			soundBtn.setImageResource(R.drawable.player_sound_middle);
		} else {
			soundBtn.setImageResource(R.drawable.player_sound_disable);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		switch (seekBar.getId()) {
		case R.id.tool_progress:
			if (fromUser) {
				seekPositionFromUser = true;
				nowPlayedProgress = progress;
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

		cancelDelayHide();
	}

	private boolean seekPositionFromUser = false;
	private int nowPlayedProgress = 0;
	private boolean isFromProgressBarSeek = false;// 标志播放缓冲提示字符的消失

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		hideControllerDelay();
		switch (seekBar.getId()) {
		case R.id.tool_progress:
			if (nowPlayedProgress > 0 && seekPositionFromUser) {
				seekPositionFromUser = false;
				// cotrollerProgressSeek.setProgress(nowPlayedProgress);
				// mVideoView.pause();
				// pb.setVisibility(View.VISIBLE);
				if (playType != VOD_PLAY) {
					cotrollerProgressSeek.setProgress(0);
				} else {
					isFromProgressBarSeek = true;
					mVideoView.seekTo(nowPlayedProgress * 1000);
					pb.setVisibility(View.VISIBLE);
					Log.e(TAG, "seeking");
					cotrollerPlay
							.setImageResource(R.drawable.cp_play_tool_pause_bg);
				}
				nowPlayedProgress = 0;
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		// if (OtherCacheData.current().isDebugMode)
		// Log.e(TAG, "onBufferingUpdate");

		pb_txt1.setVisibility(View.VISIBLE);
		pb_txt1.setText(arg1 + "%");
		pb.setVisibility(View.VISIBLE);
		pb_txt1.setVisibility(View.VISIBLE);
		pb_txt2.setVisibility(View.GONE);
		netSpeed.setVisibility(View.VISIBLE);
		fromView.setVisibility(View.GONE);

		if (mVideoView.isPlaying()) {
			mVideoView.pause();
		}
		if (arg1 > 97) {
			if (isFromProgressBarSeek) {
				isFromProgressBarSeek = false;
				cotrollerPlay.setEnabled(true);
			}
			pb.setVisibility(View.GONE);
			pb_txt1.setVisibility(View.GONE);
			netSpeed.setVisibility(View.GONE);
		}

	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		backBtnPressed = true;
		errorTips();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		backBtnPressed = true;
		mVideoView.stopPlayback();
		handler.removeMessages(PROGRESS_CHANGED);
		finish();
	}

	private boolean isPrepared;

	@Override
	public void onPrepared(MediaPlayer arg0) {

		if (OtherCacheData.current().isDebugMode) {
			Log.e(TAG, "onPrepared");
		}
		openController();
		isPrepared = true;
		pb.setVisibility(View.GONE);
		pb_txt1.setVisibility(View.GONE);
		netSpeed.setVisibility(View.GONE);
		pb_txt2.setVisibility(View.GONE);
		fromView.setVisibility(View.GONE);
		// cotrollerPlay.setImageResource(R.drawable.cp_play_tool_pause_bg);
		tipsBG.setVisibility(View.GONE);
		if (playType == VOD_PLAY) {
			cotrollerPlay.setEnabled(true);
			long ii = mVideoView.getDuration();
			videoDuration = ii;
			cotrollerProgressSeek.setMax((int) (ii / 1000));
			String currentTimeString = getTimeString(0 / 1000);
			durationStr = getTimeString(ii / 1000);
			cotrollerTime.setText(currentTimeString + "/" + durationStr);
			Log.e(TAG, durationStr);

			mVideoView.setVideoLayout(
					local.player.vi.widget.VideoView.VIDEO_LAYOUT_SCALE, 0);
			if (mVideoView.isNeedPlayHistory())
				handler.sendEmptyMessage(PROCESS_PLAY_HISTORY);
			setUpUpdateUIThread();
			hideControllerDelay();
			// 打开分享布局
			if ((programId == null) || (programId.equals("")))
				share.setVisibility(View.INVISIBLE);
			else {
				share.setVisibility(View.VISIBLE);
			}
		} else {
			hideControllerDelay();
		}
	}

	@Override
	public void onSeekComplete(MediaPlayer arg0) {

		if (OtherCacheData.current().isDebugMode)
			Log.e(TAG, "onSeekComplete");
		// if (isShareState) {
		// if (isPrepared && mVideoView.canPause()) {
		// cotrollerPlay.setEnabled(true);
		// cotrollerPlay.setImageResource(R.drawable.cp_play_tool_play_bg);
		// mVideoView.pause();
		// cancelDelayHide();
		// if (!isLocked) {
		// if (!hasShowned) {
		// openController();
		// hideControllerDelay();
		// }
		// }
		//
		// pb.setVisibility(View.VISIBLE);
		// // cotrollerPlay.performClick();
		// }
		// isShareState = false;
		// } else {
		// pb.setVisibility(View.GONE);
		// pb_txt1.setVisibility(View.GONE);
		// pb_txt2.setVisibility(View.GONE);
		// fromView.setVisibility(View.GONE);
		// if (!mVideoView.isPlaying()) {
		// cotrollerPlay.setEnabled(true);
		// mVideoView.start();
		// cotrollerPlay.setImageResource(R.drawable.cp_play_tool_play_bg);
		// }
		//
		// }

	}

	private void setUpUpdateUIThread() {

		if (updateUIThread == null) {
			updateUIThread = new UpdateUIThread();
			updateUIThread.start();
		} else {
			updateUIThread.interrupt();
			updateUIThread = new UpdateUIThread();
			updateUIThread.start();
		}
	}

	private void stopUpdateUIThread() {

		if (updateUIThread != null) {
			updateUIThread.interrupt();
			updateUIThread = null;
		}
	}

	private UpdateUIThread updateUIThread;

	private class UpdateUIThread extends Thread {

		@Override
		public void run() {

			while (true) {
				handler.sendEmptyMessage(PROGRESS_CHANGED);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
			}
			Log.e(TAG, "break update ui");
		}
	}

	private void updateUI() {
		boolean isbuffering;
		try {
			isbuffering = mVideoView.isBuffering();
		} catch (Exception e) {
			handler.removeMessages(PROGRESS_CHANGED);
			return;
		}
		if (!isbuffering) {
			cotrollerPlay.setEnabled(true);

			long i = mVideoView.getCurrentPosition();
			if (i != 0) {
				dbPosition = i;
			}
			if (needSendAcitivytId && UserNow.current().userID != 0
					&& i >= 30000) {
				sendActivityPlayAction(activityId, UserNow.current().userID);
				needSendAcitivytId = false;
			}
			cotrollerProgressSeek.setProgress((int) (i / 1000));
			String currentTimeString = getTimeString(i / 1000);
			cotrollerTime.setText(currentTimeString + "/" + durationStr);

			if (!mVideoView.isPlaying()) {
				pb.setVisibility(View.VISIBLE);
				pb_txt2.setVisibility(View.GONE);
				fromView.setVisibility(View.GONE);
				// cotrollerPlay.setImageResource(R.drawable.cp_play_tool_play_bg);
			} else {
				pb.setVisibility(View.GONE);
				pb_txt1.setVisibility(View.GONE);
				pb_txt2.setVisibility(View.GONE);
				fromView.setVisibility(View.GONE);
				cotrollerPlay
						.setImageResource(R.drawable.cp_play_tool_pause_bg);
			}
		}
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {

	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	// 记录播放次数
	private void PlayVideoCount() {

		UserNow.current().errorCode = -1;
		PlayCountTask playVideoCount = new PlayCountTask(this);
		try {
			int id = Integer.parseInt(vpd.id);
			playVideoCount.execute(this, new PlayVideoRequest(id),
					new ResultParser());
		} catch (Exception e) {

		}
	}

	// 分享布局
	private LinearLayout shareLayout;
	/** 分享到微信view */
	private TextView weixinView;
	/** 分享到微博view */
	private TextView weiboView;
	// 微博
	private Weibo mWeibo = null;
	private SsoHandler ssh = null;
	private StatusesAPI statusesAPI = null;

	private void shareToSinaWeibo() {
		if (mWeibo == null) {
			mWeibo = Weibo.getInstance("2064721383", "http://www.tvfan.cn");
			ssh = new SsoHandler(this, mWeibo);
		}
		if (!SinaData.isSinaBind) {
			ssh.authorize(new WeiboAuthListener() {

				@Override
				public void onWeiboException(WeiboException arg0) {
					sinaHandler.sendEmptyMessage(SINA_WEIBO_OAUTH_FAIL);
				}

				@Override
				public void onError(WeiboDialogError arg0) {
					sinaHandler.sendEmptyMessage(SINA_WEIBO_OAUTH_FAIL);
				}

				@Override
				public void onComplete(Bundle arg0) {
					ClientData c = new ClientData();
					c.token = arg0.getString("access_token");
					c.expire = arg0.getString("expires_in");
					SinaData.weibo().accessToken = new Oauth2AccessToken(
							c.token, c.expire);
					if (SinaData.weibo().accessToken.isSessionValid()) {

						SinaData.isSinaBind = true;
						sinaHandler.sendEmptyMessage(SINA_WEIBO_START);
						sendSinanaWeibo();
					}
				}

				@Override
				public void onCancel() {
					sinaHandler.sendEmptyMessage(SINA_WEIBO_CANCEL);
				}
			});
		} else {
			sinaHandler.sendEmptyMessage(SINA_WEIBO_START);
			sendSinanaWeibo();
		}
	}

	public void sendWeibOnlyTxt(String content) {
		statusesAPI = new StatusesAPI(SinaData.weibo().accessToken);
		statusesAPI.update(content, null, null, this);
	}

	public void sendWeibWithPic(String content, String picPath) {
		statusesAPI = new StatusesAPI(SinaData.weibo().accessToken);
		statusesAPI.upload(content, picPath, null, null, this);
	}

	private Handler sinaHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SINA_WEIBO_START:
				showTipsDialog();
				break;
			case SINA_WEIBO_OVER:
				hideTipsDialog();
				Toast.makeText(getApplicationContext(), "新浪微博发送成功!",
						Toast.LENGTH_SHORT).show();
				break;
			case SINA_WEIBO_FAIL:
				hideTipsDialog();
				Toast.makeText(getApplicationContext(), "新浪微博发送失败!",
						Toast.LENGTH_SHORT).show();
				break;
			case SINA_WEIBO_OAUTH_FAIL:
				hideTipsDialog();
				Toast.makeText(getApplicationContext(), "授权失败!",
						Toast.LENGTH_SHORT).show();
				break;
			case SINA_WEIBO_CANCEL:
				hideTipsDialog();
				Toast.makeText(getApplicationContext(), "授权取消!",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};
	private ProgressDialog tipsDialog;

	private void showTipsDialog() {
		if (tipsDialog != null) {
			if (tipsDialog.isShowing()) {
				tipsDialog.dismiss();
			}
			tipsDialog = null;
		}
		tipsDialog = ProgressDialog.show(this, "", "处理中，请稍后...", true, true,
				new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
					}
				});
	}

	private void hideTipsDialog() {
		if (tipsDialog != null) {
			if (tipsDialog.isShowing()) {
				tipsDialog.dismiss();
			}
			tipsDialog = null;
		}
	}

	private final int SINA_WEIBO_START = 111;
	private final int SINA_WEIBO_OVER = 112;
	private final int SINA_WEIBO_FAIL = 113;
	private final int SINA_WEIBO_OAUTH_FAIL = 114;
	private final int SINA_WEIBO_CANCEL = 115;

	@Override
	public void onComplete(String arg0) {
		sinaHandler.sendEmptyMessage(SINA_WEIBO_OVER);
	}

	@Override
	public void onError(WeiboException arg0) {
		sinaHandler.sendEmptyMessage(SINA_WEIBO_FAIL);
	}

	@Override
	public void onIOException(IOException arg0) {
		sinaHandler.sendEmptyMessage(SINA_WEIBO_FAIL);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (ssh != null)
			ssh.authorizeCallBack(requestCode, resultCode, data);
		else {
			switch (requestCode) {
			case SEARCHDEVICE:
				DLNAData.current().nowProgramLiveAddress = path;
				openDLNAControlerActivity(0);
				break;
			default:
				break;
			}
		}
	}

	/** 声音进度 */
	private VerticalSeekBar volSeekBar;
	private VerticalSeekBar.OnSeekBarChangeListener volListener = new VerticalSeekBar.OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(VerticalSeekBar vBar) {
			hideControllerDelay();
		}

		@Override
		public void onStartTrackingTouch(VerticalSeekBar vBar) {
			cancelDelayHide();
		}

		@Override
		public void onProgressChanged(VerticalSeekBar vBar, int progress,
				boolean fromUser) {
			if (fromUser) {
				currentVol = progress * streamMaxVolume / 100;
				setVol(currentVol);
				if (progress <= 30 && progress > 0) {
					soundBtn.setImageResource(R.drawable.player_sound_min);
				} else if (progress > 70) {
					soundBtn.setImageResource(R.drawable.player_sound_max);
				} else if (progress <= 70 && progress > 30) {
					soundBtn.setImageResource(R.drawable.player_sound_middle);
				} else {
					soundBtn.setImageResource(R.drawable.player_sound_disable);
				}
			}

		}
	};
	private int streamMaxVolume;
	private int streamNowVolume;
	private int currentVol;
	private ImageButton soundBtn;
	private String programPic;

	private boolean isFileExist(String url) {
		Drawable d = BitmapUtils.getSdCardFromDrawable(url);
		if (d != null) {
			return true;
		}
		return false;
	}

	private void sendSinanaWeibo() {
		// 微博文本
		String weiboTxt = "";

		if (titleName != null && !titleName.equals("")) {
			weiboTxt = "快来看" + "#" + titleName + "#，" + "真是太精彩了!" + "，观看地址>>>"
					+ ShareData.shareWeiboText + " (来自@电视粉)";
		} else {
			weiboTxt = "快来看看吧，" + "真是太精彩了!" + "，观看地址>>>" + ShareData.text
					+ " (来自@电视粉)";
		}
		if (programPic != null) {
			if (isFileExist(programPic)) {
				String fileFolder = Environment.getExternalStorageDirectory()
						+ "/TVFan/temp";
				if (programPic.contains("/") && programPic.contains(".")) {
					String name = programPic.substring(
							programPic.lastIndexOf("/"), programPic.length());

					sendWeibWithPic(weiboTxt, fileFolder + File.separator
							+ name);
				} else {
					sendWeibOnlyTxt(weiboTxt);
				}

			} else {
				sendWeibOnlyTxt(weiboTxt);
			}
		} else {
			sendWeibOnlyTxt(weiboTxt);
		}
	}

	private TextView fromView;
	private String fromString;

	private void openShareWeixinActivity() {
		Intent intent = new Intent(this, ShareToWeixinActivity.class);
		intent.putExtra("titleName", titleName);
		intent.putExtra("picUrl", programPic);
		startActivity(intent);
	}

	private void playUnlockAnimation(View v, int repeatCount) {
		RotateStyle animation = new RotateStyle(0, 360, 0, v.getWidth() / 2,
				v.getHeight() / 2);
		animation.setDuration(800);
		animation.setInterpolator(new AccelerateDecelerateInterpolator());
		animation.setRepeatCount(repeatCount);
		v.startAnimation(animation);
	}

	private boolean hasAvailableSpace() {
		if (OtherCacheData.current().isDebugMode) {
			Log.i("hasAvailableSpace",
					"内部可用存储空间是："
							+ Long.toString(CommonUtils
									.getAvailableInternalMemorySize()
									/ (1024 * 1024)));
			Log.i("hasAvailableSpace",
					"内部总共存储空间是："
							+ Long.toString(CommonUtils
									.getTotalInternalMemorySize()
									/ (1024 * 1024)));

			Log.i("hasAvailableSpace",
					"外部可用存储空间是："
							+ Long.toString(CommonUtils
									.getAvailableExternalMemorySize()
									/ (1024 * 1024)));
			Log.i("hasAvailableSpace",
					"外部总共存储空间是："
							+ Long.toString(CommonUtils
									.getTotalExternalMemorySize()
									/ (1024 * 1024)));

			Log.e("内部空间小于30M时", CommonUtils.getAvailableInternalMemorySize()
					/ (1024 * 1024) + "");
		}

		// 内部空间小于30M时，无法安装电视粉
		if (CommonUtils.getAvailableInternalMemorySize() / (1024 * 1024) < JSONMessageType.MIN_AVAILABLE_SPACE)
			return false;
		else
			return true;
	}

	@Override
	public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
		if (OtherCacheData.current().isDebugMode)
			Log.e("onInfo", "onInfo!");

		if (arg1 == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
			if (!mVideoView.isNeedPauseOnResume()) {
				cotrollerPlay.setEnabled(true);
				pb.setVisibility(View.GONE);
				cotrollerPlay
						.setImageResource(R.drawable.cp_play_tool_pause_bg);
				mVideoView.start();
			} else {
				cotrollerPlay.setImageResource(R.drawable.cp_play_tool_play_bg);
				mVideoView.pause();
			}

		}
		if (arg1 == MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED) {
			if (netSpeed.isShown()) {
				netSpeed.setText(netSpeedPre + arg2 + netSpeedSuf);
			}
		}
		return false;
	}

	private static final String netSpeedPre = "(";
	private static final String netSpeedSuf = "K/S)";
	private TextView timeTextView;

	private void setTimeValue() {
		Time time = new Time();
		time.setToNow();
		int hour = time.hour;
		int minute = time.minute;
		StringBuilder timeValue = new StringBuilder();
		if (hour < 10) {
			timeValue.append(0);
		}
		timeValue.append(hour).append(":");
		if (minute < 10) {
			timeValue.append(0);
		}
		timeValue.append(minute);
		timeTextView.setText(timeValue.toString());
	}

	private static final int UPDATE_TIME_BATTERY = 22;
	private Timer timer;

	private TimerTask task = new TimerTask() {
		@Override
		public void run() {
			handler.sendEmptyMessage(UPDATE_TIME_BATTERY);
		}
	};
	private ImageView batteryImageView;
	private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {

			int level = intent.getIntExtra("level", 0);
			int status = intent.getIntExtra("status",
					BatteryManager.BATTERY_HEALTH_UNKNOWN);
			boolean charging = false;
			if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
				charging = true;
			}
			if (!batteryImageView.isShown()) {
				batteryImageView.setVisibility(View.VISIBLE);
			}
			if (level >= 70) {
				if (!charging) {
					batteryImageView.setImageResource(R.drawable.battery_70);
				} else {
					batteryImageView
							.setImageResource(R.drawable.battery_charging_70);
				}
			} else if (level >= 40 && level < 70) {
				if (!charging) {
					batteryImageView.setImageResource(R.drawable.battery_40);
				} else {
					batteryImageView
							.setImageResource(R.drawable.battery_charging_40);
				}
			} else if (level >= 10 && level < 40) {
				if (!charging) {
					batteryImageView.setImageResource(R.drawable.battery_10);
				} else {
					batteryImageView
							.setImageResource(R.drawable.battery_charging_10);
				}
			} else if (level < 10) {
				if (!charging) {
					batteryImageView.setImageResource(R.drawable.battery_0);
				} else {
					batteryImageView
							.setImageResource(R.drawable.battery_charging_0);
				}
			}
			// }
		}

	};
	private ActivityPlayVideoTask activityPlayVideoTask;
	private int activityId = 0;
	private boolean needSendAcitivytId = false;
	private ArrayList<BadgeData> badgeList = null;

	private void sendActivityPlayAction(int activityId, int userId) {
		if (activityPlayVideoTask == null) {
			badgeList = new ArrayList<BadgeData>();
			activityPlayVideoTask = new ActivityPlayVideoTask(this, false);
			activityPlayVideoTask.execute(this, UserNow.current().userID,
					activityId, badgeList);
		}
	}

	@Override
	public void onNetBegin(String method, boolean isLoadMore) {

	}

	@Override
	public void onNetEnd(int code, String msg, String method, boolean isLoadMore) {
		if (Constants.activityPlayVideo.equals(method)) {
			switch (code) {
			case Constants.fail_no_net:
			case Constants.fail_server_err:
			case Constants.parseErr:
			case Constants.requestErr:
				break;
			case Constants.sucess:
				for (BadgeData badgeData : badgeList) {
					String name = badgeData.name;
					if (name != null) {
						DialogUtil.showBadgeAddToast(this, name);
					}
				}
				badgeList.clear();
				break;
			default:
				break;
			}
		}
	}

	private OnClickListener dlnaOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dlna_layout:
				hideDlnaLayout();
				if (isSearching) {
					dlnaHandler.removeMessages(MSG_SEARCH_END);
					dlnaHandler.sendEmptyMessage(MSG_SEARCH_END);
				}
				break;
			case R.id.search_btn:
				showSearchingLayout();
				startSearchDevice();
				break;
			case R.id.cancel_btn:
				hideDlnaLayout();
				break;
			default:
				break;
			}
		}

	};

	private RelativeLayout dlnaLayout = null;
	private Button searchBtn, cancelBtn;
	private LinearLayout searchLayout, searchingLayout, searchResultLayout;
	// 搜索到的dlna设备结果
	private ListView resultListView;

	private void inflateDlnaLayout() {
		ViewStub stub = (ViewStub) findViewById(R.id.dlna_stub);
		stub.setLayoutResource(R.layout.dlna_layout);
		dlnaLayout = (RelativeLayout) stub.inflate();
		searchBtn = (Button) dlnaLayout.findViewById(R.id.search_btn);
		cancelBtn = (Button) dlnaLayout.findViewById(R.id.cancel_btn);
		searchingLayout = (LinearLayout) dlnaLayout
				.findViewById(R.id.searching_layout);
		searchLayout = (LinearLayout) dlnaLayout
				.findViewById(R.id.search_layout);
		dlnaLayout.setOnClickListener(dlnaOnClickListener);
		searchBtn.setOnClickListener(dlnaOnClickListener);
		cancelBtn.setOnClickListener(dlnaOnClickListener);
		searchResultLayout = (LinearLayout) dlnaLayout
				.findViewById(R.id.search_result_layout);
		resultListView = (ListView) dlnaLayout
				.findViewById(R.id.result_listview);
	}

	/**
	 * dlna 搜索页面
	 */
	private void showSearchLayout() {
		dlnaLayout.setVisibility(View.VISIBLE);
		searchLayout.setVisibility(View.VISIBLE);
		searchingLayout.setVisibility(View.GONE);
		searchResultLayout.setVisibility(View.GONE);
	}

	/**
	 * 隐藏DLNA界面
	 */
	private void hideDlnaLayout() {
		dlnaLayout.setVisibility(View.GONE);
	}

	private void showSearchingLayout() {
		searchLayout.setVisibility(View.GONE);
		searchResultLayout.setVisibility(View.GONE);
		searchingLayout.setVisibility(View.VISIBLE);
	}

	private void showResultView() {
		dlnaLayout.setVisibility(View.VISIBLE);
		searchingLayout.setVisibility(View.GONE);
		searchLayout.setVisibility(View.GONE);
		searchResultLayout.setVisibility(View.VISIBLE);
		deviceAdapter = new DeviceAdapter(devicesList, this);
		resultListView.setAdapter(deviceAdapter);
		resultListView.setOnItemClickListener(dlnaClickListener);
	}

	/**
	 * 调用搜索服务的搜索功能
	 */
	private void startSearchDevice() {
		dlnaHandler.sendEmptyMessage(MSG_SEARCH_START);
		addSelectedDevice();
		startService(new Intent(DlnaService.SEARCH_DEVICES));
	}

	private boolean isDeviceExist(DeviceDataInSearchList device) {
		for (DeviceDataInSearchList temp : devicesList) {
			if (device.name.equals(temp.name)) {
				return true;
			}
		}
		return false;
	}

	// 直播节目进入播控界面无法操控进度
	private boolean isLivePlay = true;

	private void openDLNAControlerActivity(int resume) {
		Intent i = new Intent(getApplicationContext(),
				RemoteControllerActivityNew.class);
		/** 是否继续播放 0否1是 */
		i.putExtra("resume", resume);
		if (playType == VOD_PLAY)
			isLivePlay = false;
		i.putExtra("isLivePlay", isLivePlay);
		DLNAData.current().nowProgramName = title.getText().toString();
		DLNAData.current().nowProgramLiveAddress = path;
		DLNAData.current().nowSTBPlayDuration = getTimeString(mVideoView
				.getDuration() / 1000);
		backBtnPressed = true;
		startActivity(i);
	}

	private ArrayList<DeviceDataInSearchList> devicesList = new ArrayList<DeviceDataInSearchList>();
	private DeviceAdapter deviceAdapter;
	DeviceFoundReceiver receiver = null;
	// 表示是不是取消了接受结果
	private boolean isSearching;

	public class DeviceFoundReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!isSearching) {
				return;
			}
			String action = intent.getAction();
			if (DlnaService.NEW_DEVICES_FOUND.equals(action)) {
				DeviceDataInSearchList newDevice = (DeviceDataInSearchList) intent
						.getSerializableExtra("device");
				if (newDevice == null || newDevice.name == null) {
					return;
				}
				if (!isDeviceExist(newDevice)) {
					devicesList.add(newDevice);

				}
			}
		}
	}

	/**
	 * 添加已经选择的设备
	 */
	private void addSelectedDevice() {
		Device selectedDevice = DeviceData.getInstance().getSelectedDevice();
		if (selectedDevice != null) {
			DeviceDataInSearchList temp = new DeviceDataInSearchList();
			temp.name = selectedDevice.getFriendlyName();
			temp.address = selectedDevice.getLocation()
					+ selectedDevice.getDescriptionFilePath();
			String url = DLNAUtil.getIcon(selectedDevice);
			temp.iconUrl = url;
			temp.isSelected = true;
			devicesList.add(temp);
		}
	}

	private static final int MSG_SEARCH_START = 1;
	private static final int MSG_SEARCH_END = 2;
	private static final int MSG_CANCEL_SEARCH = 3;
	private Handler dlnaHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SEARCH_START:
				isSearching = true;
				dlnaHandler.sendEmptyMessageDelayed(MSG_SEARCH_END, 50000);
				break;
			case MSG_SEARCH_END:
				isSearching = false;
				showResultView();
				break;
			case MSG_CANCEL_SEARCH:
				isSearching = false;
				dlnaHandler.removeMessages(MSG_SEARCH_END);
				dlnaHandler.sendEmptyMessage(MSG_SEARCH_END);
				break;
			default:
				break;
			}
		}
	};
	private OnItemClickListener dlnaClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			DeviceDataInSearchList temp = (DeviceDataInSearchList) deviceAdapter
					.getItem(arg2);

			Intent initService = new Intent(NewLivePlayerActivity.this,
					DlnaService.class);
			initService.setAction(DlnaService.DEVICE_SELECTED);
			initService.putExtra("selectedDevice", temp);
			startService(initService);
			Intent intent = new Intent(NewLivePlayerActivity.this,
					DLNAControllActivity.class);
			intent.setAction(DlnaService.DEVICE_SELECTED);
			intent.putExtra("selectedDevice", temp);
			intent.putExtra("playAddress", path);
			intent.putExtra("titleName", titleName);
			startActivity(intent);
			finish();
		}
	};

	private Device getDeviceByShortData(DeviceDataInSearchList data) {
		Device[] list = DeviceData.getInstance().getDevices();
		for (int i = 0; i < list.length; i++) {
			Device device = list[i];
			if (data.name.equals(device.getFriendlyName())) {
				return device;
			}
		}
		return null;
	}
}
