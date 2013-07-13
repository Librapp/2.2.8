package com.sumavision.talktv2.activity;

/**
 * 节目详情页  2012 12-10 jianghao

 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.LayoutParams;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.AsyncImageLoader.ImageCallback;
import com.sumavision.talktv2.activity.MyScrollView.OnScrollTopListener;
import com.sumavision.talktv2.adapter.NetPlayDataListAdapter;
import com.sumavision.talktv2.data.ChannelNewData;
import com.sumavision.talktv2.data.ChaseData;
import com.sumavision.talktv2.data.CommentData;
import com.sumavision.talktv2.data.CpData;
import com.sumavision.talktv2.data.NetPlayData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.ParentVideoData;
import com.sumavision.talktv2.data.ProgramAroundData;
import com.sumavision.talktv2.data.ShareData;
import com.sumavision.talktv2.data.StarData;
import com.sumavision.talktv2.data.VideoData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.ChaseAddRequest;
import com.sumavision.talktv2.net.ChaseDeleteRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.ProgramDetailNewNewParser;
import com.sumavision.talktv2.net.ProgramDetailNewNewRequest;
import com.sumavision.talktv2.net.ProgramHeadParser;
import com.sumavision.talktv2.net.ProgramHeadRequest;
import com.sumavision.talktv2.net.ProgramVideoListParser;
import com.sumavision.talktv2.net.ProgramVideoListRequest;
import com.sumavision.talktv2.net.RemindAddRequest;
import com.sumavision.talktv2.net.RemindDeleteRequest;
import com.sumavision.talktv2.net.ResultParser;
import com.sumavision.talktv2.net.SignProgramParser;
import com.sumavision.talktv2.net.SignProgramRequest;
import com.sumavision.talktv2.net.TalkListNewParser;
import com.sumavision.talktv2.net.TalkListRequest;
import com.sumavision.talktv2.task.ChaseDeleteTask;
import com.sumavision.talktv2.task.ChaseProgramTask;
import com.sumavision.talktv2.task.GetCommentTask;
import com.sumavision.talktv2.task.GetProgramDetailTask;
import com.sumavision.talktv2.task.GetProgramVideoTask;
import com.sumavision.talktv2.task.ProgramHeaderTask;
import com.sumavision.talktv2.task.RemindAddTask;
import com.sumavision.talktv2.task.RemindDeleteTask;
import com.sumavision.talktv2.task.SignProgramTask;
import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.sumavision.talktv2.utils.Txt2Image;
import com.umeng.analytics.MobclickAgent;

public class ProgramNewActivity extends Activity implements
		NetConnectionListener, OnClickListener, OnScrollListener,
		OnPageChangeListener, OnScrollTopListener, OnRatingBarChangeListener {

	private String programId;
	private String topicId;
	private ViewPager viewPager;
	private VodProgramData vpd;
	// 节目cpId
	private long cpId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.program_new);
		Intent intent = getIntent();
		vpd = new VodProgramData();
		programId = intent.getStringExtra("programId");
		if (intent.hasExtra("topicId")) {
			topicId = intent.getStringExtra("topicId");
			vpd.topicId = topicId;
		} else {
			topicId = "0";// 推送过来的时候 不会给topicId 默认给0 会加载失败
		}
		cpId = intent.getLongExtra("cpId", 0);
		vpd.id = programId;
		vpd.cpId = cpId;
		if (intent.hasExtra("fromNotification")) {
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			try {
				int notificationId = Integer.valueOf(programId);
				manager.cancel(notificationId);
			} catch (Exception e) {

			}
		}
		initOthers();
		initViews();
		setListener();
		// 加载 节目头部
		getProgramHeader();
		int from = intent.getIntExtra("from", 0);
		if (from == 1) {
			getComment();
			lastPagePosion = 0;
		} else {
			viewPager.setCurrentItem(1);
			lastPagePosion = 1;
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		vpd = null;
	}

	private AsyncImageLoader imageLoader;
	private int whiteColor;
	private int pdVideoDefaultColor;

	private void initOthers() {
		imageLoader = new AsyncImageLoader();
		getMoveStep();
		Resources res = getResources();
		whiteColor = res.getColor(R.color.white);
		pdVideoDefaultColor = res.getColor(R.color.jishu_textColor);
	}

	private ImageView imageView;
	private TextView title;
	private ProgressBar programProgressBar;
	private TextView errText;
	private RelativeLayout errLayout;
	private ProgressBar refreshProgressBar;
	private Button refreshBtn;

	private void initViews() {
		title = (TextView) findViewById(R.id.program_title);
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);

		imageView = (ImageView) findViewById(R.id.imageview);
		programProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		errText = (TextView) findViewById(R.id.err_text);
		errLayout = (RelativeLayout) findViewById(R.id.errLayout);
		refreshBtn = (Button) findViewById(R.id.refresh);
		refreshProgressBar = (ProgressBar) findViewById(R.id.header_progressBar);

		signBtn = (ImageButton) findViewById(R.id.sign_btn);
		zhuiBtn = (ImageButton) findViewById(R.id.fellow_btn);
		liveBtn = (ImageButton) findViewById(R.id.live_btn);
		activityBtn = (ImageButton) findViewById(R.id.activity_btn);
		signUserGallery = (GridView) findViewById(R.id.signusergallery);
		signUserCount = (TextView) findViewById(R.id.signuser_count);

		tagIndicator = (ImageView) findViewById(R.id.tab_imageView);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(step,
				LayoutParams.WRAP_CONTENT);
		tagIndicator.setLayoutParams(params);
		commentTag = (TextView) findViewById(R.id.comment_btn);
		videoTag = (TextView) findViewById(R.id.video_btn);
		detailTag = (TextView) findViewById(R.id.detail_btn);

		ProgramDetailLayout layout = (ProgramDetailLayout) findViewById(R.id.program_id);
		RelativeLayout imageView = (RelativeLayout) findViewById(R.id.imagelayout);
		RelativeLayout outer = (RelativeLayout) findViewById(R.id.outer);
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		initViewPager();
		layout.setChilden(imageView, outer, scrollView);
		initNetLiveLayout();
	}

	private void initViewPager() {
		AwesomeAdapter awesomeAdapter;
		ArrayList<View> views;
		views = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.pd_viewpager_comment, null);
		initCommentView(view);
		View detailView = inflater.inflate(R.layout.pd_viewpager_detail, null);
		initDetailView(detailView);
		View videoview = inflater.inflate(R.layout.pd_viewpager_video, null);
		initVideoView(videoview);
		views.add(view);
		views.add(videoview);
		views.add(detailView);
		awesomeAdapter = new AwesomeAdapter(views);
		viewPager.setAdapter(awesomeAdapter);
		viewPager.setOnPageChangeListener(this);

	}

	private ImageButton signBtn;
	private ImageButton zhuiBtn;
	private ImageButton activityBtn;
	private ImageButton liveBtn;

	private void setListener() {
		findViewById(R.id.back).setOnClickListener(this);
		refreshBtn.setOnClickListener(this);
		errText.setOnClickListener(this);
		commentTag.setOnClickListener(this);
		videoTag.setOnClickListener(this);
		detailTag.setOnClickListener(this);
		commentErr.setTag(0);
		commentErr.setOnClickListener(errListener);
		videoErr.setTag(1);
		videoErr.setOnClickListener(errListener);
		detailErr.setTag(2);
		detailErr.setOnClickListener(errListener);
		commentListView.setOnScrollListener(this);
		signBtn.setOnClickListener(functionOnClickListener);
		zhuiBtn.setOnClickListener(functionOnClickListener);
		activityBtn.setOnClickListener(functionOnClickListener);
		liveBtn.setOnClickListener(functionOnClickListener);

	}

	OnClickListener functionOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {

			case R.id.fellow_btn:// 追剧按钮
				MobclickAgent.onEvent(ProgramNewActivity.this, "zhui");
				if (UserNow.current().userID != 0) {
					// if (VodProgramData.current.isChased == 1) {
					if (vpd.isChased == 1) {
						// DialogUtil.alertToast(getApplicationContext(),
						// "您已经追了该剧");

						ChaseData.current.id = Integer
						// .parseInt(VodProgramData.current.id);
								.parseInt(vpd.id);
						cancelChase();

					} else {
						chaseProgram();
					}
				} else {
					openLogInActivity();
				}
				break;
			case R.id.sign_btn:// 签到按钮
				MobclickAgent.onEvent(ProgramNewActivity.this, "checkin");
				if (UserNow.current().userID != 0) {
					// if (VodProgramData.current.isSigned == 1) {
					if (vpd.isSigned == 1) {
						DialogUtil
								.alertToast(getApplicationContext(), "您已经签到了");
					} else {
						signProgram();
					}
				} else {
					openLogInActivity();
				}
				break;
			case R.id.live_btn:
				// String path = VodProgramData.current.playUrl;
				String path = vpd.playUrl;
				MobclickAgent.onEvent(ProgramNewActivity.this, "zhibo");
				if (path == null || path.equals("")) {
					if (vpd.netPlayDatas != null) {
						isLive = 1;
						liveThroughNet(vpd.netPlayDatas);
					} else {
						DialogUtil
								.alertToast(getApplicationContext(), "暂时无法播放");
						liveBtn.setVisibility(View.GONE);
					}
				} else {
					switch (vpd.playType) {
					// 网页播放
					case 2:
						Intent intentWeb = new Intent(ProgramNewActivity.this,
								WebBrowserActivity.class);
						intentWeb.putExtra("url", path);
						String titleWeb = vpd.name;
						intentWeb.putExtra("title", titleWeb);
						if (path != null && !path.equals("")) {
							MobclickAgent.onEvent(ProgramNewActivity.this,
									"epgplaytv", titleWeb);
							startActivity(intentWeb);
						} else {
							DialogUtil.alertToast(getApplicationContext(),
									"暂时无法播放");
						}
						break;
					// 直接播放
					case 1:
						if (vpd.playUrl != null && !vpd.playUrl.equals(""))
							openLiveActivity(1);
						else {
							DialogUtil.alertToast(getApplicationContext(),
									"暂时无法播放");
						}
						// liveThroughNet(new VideoData());// TODO
						break;
					default:
						openLiveActivity(1);
						break;
					}
				}
				break;
			case R.id.activity_btn:
				MobclickAgent.onEvent(ProgramNewActivity.this, "huodong",
				// VodProgramData.current.name);
						vpd.name);
				// Toast.makeText(getApplicationContext(),
				// "当前节目有活动，别忘了去主页活动标签去参加哦！", Toast.LENGTH_SHORT).show();

				finish();
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.intro:
			// 节目详情页介绍----jieshao
			MobclickAgent.onEvent(this, "jieshao");
			break;
		case R.id.back:
			finish();
			break;
		case R.id.refresh:
			refreshBtn.setVisibility(View.GONE);
			refreshProgressBar.setVisibility(View.VISIBLE);
			getProgramHeader();
			getComment();
			break;
		case R.id.err_text:
			getProgramHeader();
			break;
		case R.id.comment_btn:
			MobclickAgent.onEvent(this, "comment");
			executeAnimation(0);
			viewPager.setCurrentItem(0);
			break;
		case R.id.video_btn:
			MobclickAgent.onEvent(this, "video");
			executeAnimation(1);
			viewPager.setCurrentItem(1);
			break;
		case R.id.detail_btn:
			MobclickAgent.onEvent(this, "more");
			executeAnimation(2);
			viewPager.setCurrentItem(2);
			break;
		default:
			break;
		}
	}

	private TextView commentTag;
	private TextView videoTag;
	private TextView detailTag;
	private ImageView tagIndicator;
	private int step;
	private int currentPosition;

	public void executeAnimation(int position) {
		Animation animation = null;
		switch (position) {
		case 0:
			if (currentPosition == 1) {
				animation = new TranslateAnimation(step, 0, 0, 0);
			} else if (currentPosition == 2) {
				animation = new TranslateAnimation(2 * step, 0, 0, 0);
			} else if (currentPosition == 3) {
				animation = new TranslateAnimation(3 * step, 0, 0, 0);
			} else if (currentPosition == 4) {
				animation = new TranslateAnimation(4 * step, 0, 0, 0);
			}
			break;
		case 1:
			if (currentPosition == 0) {
				animation = new TranslateAnimation(0, step, 0, 0);
			} else if (currentPosition == 2) {
				animation = new TranslateAnimation(2 * step, step, 0, 0);
			} else if (currentPosition == 3) {
				animation = new TranslateAnimation(3 * step, step, 0, 0);
			} else if (currentPosition == 4) {
				animation = new TranslateAnimation(4 * step, step, 0, 0);
			}
			break;
		case 2:
			if (currentPosition == 0) {
				animation = new TranslateAnimation(0, 2 * step, 0, 0);
			} else if (currentPosition == 1) {
				animation = new TranslateAnimation(step, 2 * step, 0, 0);
			} else if (currentPosition == 3) {
				animation = new TranslateAnimation(3 * step, 2 * step, 0, 0);
			} else if (currentPosition == 4) {
				animation = new TranslateAnimation(4 * step, 2 * step, 0, 0);
			}
			break;
		default:
			break;
		}
		if (animation != null) {
			currentPosition = position;
			animation.setDuration(200);
			animation.setFillAfter(true);
			tagIndicator.startAnimation(animation);
		}
	}

	public void getMoveStep() {
		android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;
		step = screenW / 3;

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	// 上次页面位置，用于友盟统计
	private int lastPagePosion = 1;

	@Override
	public void onPageSelected(int arg0) {
		ProgramViewEventDispatchedController.WhoFocus = arg0;
		executeAnimation(arg0);
		if (arg0 == 2) {
			if (lastPagePosion < arg0) {
				MobclickAgent.onEvent(ProgramNewActivity.this, "videoright");
			}
			if (!hasDetail) {
				getProgramDetail();
			}
		} else if (arg0 == 1) {
			// 从评论往右滑到视频
			if (lastPagePosion < arg0) {
				MobclickAgent.onEvent(ProgramNewActivity.this, "commentright");
			}
			// TODO: 从详情往左滑到视频
			else {
				MobclickAgent.onEvent(ProgramNewActivity.this, "videoleft");
			}

			if (!hasVideo) {
				getProgramVideo();
			}
		} else if (arg0 == 0) {

			// 从视频往左滑到评论
			if (lastPagePosion > arg0) {
				MobclickAgent.onEvent(ProgramNewActivity.this, "videoleft");
			}

			if (comments.size() == 0) {
				try {
					getComment();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}

		lastPagePosion = arg0;
	}

	private ProgramHeaderTask programHeaderTask;

	private void getProgramHeader() {
		if (programHeaderTask == null) {
			// VodProgramData.current.id = programId;
			if (vpd.id != null) {
				vpd.id = programId;
				programHeaderTask = new ProgramHeaderTask();
				programHeaderTask.execute(this, this, new ProgramHeadRequest(
						Integer.parseInt(vpd.id), vpd.cpId));
				refreshProgressBar.setVisibility(View.VISIBLE);
				refreshBtn.setVisibility(View.GONE);
			}
		}
	}

	private void upateProgramHeader() {
		refreshProgressBar.setVisibility(View.GONE);
		refreshBtn.setVisibility(View.VISIBLE);
		errLayout.setVisibility(View.GONE);
		// title.setText(VodProgramData.current.name);
		title.setText(vpd.name);
		loadImage(imageView, vpd.pic,
		// VodProgramData.current.pic,
				R.drawable.program_pic_default);
		int signed = vpd.isSigned;
		// VodProgramData.current.isSigned;
		int zhuiju = vpd.isChased;
		// VodProgramData.current.isChased;
		if (signed == 1) {
			signBtn.setImageResource(R.drawable.pd_yisigned_ressed);
		} else {
			signBtn.setImageResource(R.drawable.pd_sign_btn);
		}
		if (zhuiju == 1) {
			zhuiBtn.setImageResource(R.drawable.pd_yizhui_pressed);
		} else {
			zhuiBtn.setImageResource(R.drawable.pd_zhuiju_btn);
		}
		int hasActivity = vpd.hasActivity;
		// VodProgramData.current.hasActivity;
		if (hasActivity == 1) {
			activityBtn.setImageResource(R.drawable.pd_activity_btn);
			// TODO: 活动按钮暂时隐藏，后面需重新考虑流程
			// activityBtn.setVisibility(View.GONE);
		}
		int isLive = vpd.livePlay;
		// VodProgramData.current.livePlay;
		if (isLive == 1) {
			liveBtn.setImageResource(R.drawable.pd_live_btn);
		} else {
			liveBtn.setVisibility(View.GONE);
		}
		ArrayList<User> temp = (ArrayList<User>) vpd.getSignUser();
		// VodProgramData.current
		// .getSignUser();
		if (temp != null) {

			if (temp.size() > 7) {
				users.clear();
				for (int i = 0; i < 7; ++i) {
					users.add(temp.get(i));
				}
			} else {
				users = temp;
			}
			signUserGallery.setAdapter(new SignUSerGalleryAdapter(users));
			signUserGallery.setSelection(users.size() / 2);
			signUserGallery.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					if (UserNow.current().userID == 0) {
						openLoginActivity();
					} else {
						int otherUserId = users.get(position).userId;
						openOtherUserCenterActivity(otherUserId);
					}
				}
			});
		}
		// VodProgramData.current.signCount

		int sc = vpd.signCount;

		if (sc < 1000)
			signUserCount.setText(vpd.signCount + "\n" + "来过");
		else if (2000 >= sc && sc >= 1000)
			signUserCount.setText("1K+" + "\n" + "来过");
		else if (3000 >= sc && sc >= 2000)
			signUserCount.setText("2K+" + "\n" + "来过");
		else if (4000 >= sc && sc >= 3000)
			signUserCount.setText("3K+" + "\n" + "来过");
		else if (5000 >= sc && sc >= 4000)
			signUserCount.setText("4K+" + "\n" + "来过");
		else if (6000 >= sc && sc >= 5000)
			signUserCount.setText("5K+" + "\n" + "来过");
		else if (7000 >= sc && sc >= 6000)
			signUserCount.setText("6K+" + "\n" + "来过");
		else if (8000 >= sc && sc >= 7000)
			signUserCount.setText("7K+" + "\n" + "来过");
		else if (9000 >= sc && sc >= 8000)
			signUserCount.setText("8K+" + "\n" + "来过");
		else if (10000 > sc && sc >= 9000)
			signUserCount.setText("9K+" + "\n" + "来过");
		else if (20000 > sc && sc >= 10000)
			signUserCount.setText("1W+" + "\n" + "来过");
		else if (30000 > sc && sc >= 20000)
			signUserCount.setText("2W+" + "\n" + "来过");
		else if (40000 > sc && sc >= 30000)
			signUserCount.setText("3W+" + "\n" + "来过");
		else if (50000 > sc && sc >= 40000)
			signUserCount.setText("4W+" + "\n" + "来过");
		else if (60000 > sc && sc >= 50000)
			signUserCount.setText("5W+" + "\n" + "来过");
		else if (70000 > sc && sc >= 60000)
			signUserCount.setText("6W+" + "\n" + "来过");
		else if (80000 > sc && sc >= 70000)
			signUserCount.setText("7W+" + "\n" + "来过");
		else if (90000 > sc && sc >= 80000)
			signUserCount.setText("8W+" + "\n" + "来过");
		else if (100000 > sc && sc >= 90000)
			signUserCount.setText("9W+" + "\n" + "来过");
		else if (sc >= 100000)
			signUserCount.setText("10W+" + "\n" + "来过");
	}

	private void openLoginActivity() {
		Intent intent = new Intent(ProgramNewActivity.this, LoginActivity.class);
		startActivity(intent);
	}

	ArrayList<User> users = new ArrayList<User>();
	private GridView signUserGallery;
	private TextView signUserCount;

	private void openOtherUserCenterActivity(int id) {
		Intent intent = new Intent(this, OtherUserCenterActivity.class);
		intent.putExtra("id", id);
		startActivity(intent);
	}

	private class SignUSerGalleryAdapter extends BaseAdapter {

		private final ArrayList<User> list;

		public SignUSerGalleryAdapter(ArrayList<User> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list != null) {
				return list.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			ViewHolder viewHolder;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ProgramNewActivity.this
						.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
				rowView = inflater
						.inflate(R.layout.pd_signuser_list_item, null);
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) rowView
						.findViewById(R.id.p_gallery_img_small);
				rowView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) rowView.getTag();
			}
			String url = list.get(position).iconURL;
			viewHolder.imageView.setTag(url);
			try {
				loadListImage(viewHolder.imageView, url,
						R.drawable.list_star_default);
			} catch (NullPointerException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			return rowView;
		}

		private class ViewHolder {
			public ImageView imageView;
		}
	}

	private GetCommentTask getCommentTask;

	private void getComment() {
		if (getCommentTask == null) {
			int topicId = 0;
			try {
				topicId = Integer.parseInt(vpd.topicId);
			} catch (Exception e) {

			}
			if (topicId != 0) {
				getCommentTask = new GetCommentTask();
				getCommentTask.execute(this, this, new TalkListRequest(cpId,
						topicId), new TalkListNewParser());
				if (comments.size() == 0) {
					commentProgressBar.setVisibility(View.VISIBLE);
				}
				commentErr.setVisibility(View.GONE);
			}
		}
	}

	private void getComment(int start, int count) {
		if (getCommentTask == null) {
			int topicId = 0;
			try {
				topicId = Integer.parseInt(vpd.topicId);
			} catch (Exception e) {

			}
			if (topicId != 0) {
				getCommentTask = new GetCommentTask();
				OtherCacheData.current().offset = start;
				OtherCacheData.current().pageCount = count;
				getCommentTask.execute(this, this, new TalkListRequest(cpId,
						topicId), new TalkListNewParser());
				if (comments.size() == 0) {
					commentProgressBar.setVisibility(View.VISIBLE);
				}
				commentErr.setVisibility(View.GONE);
			}
		}
	}

	private ProgramCommentListView commentListView;
	private TextView commentErr;
	private ProgressBar commentProgressBar;
	private ArrayList<CommentData> comments = new ArrayList<CommentData>();
	private ImageView screenShot;
	private ImageView sendComment;
	private ImageView sendCommentLong;
	private LinearLayout btnLayout;

	private void initCommentView(View view) {
		commentListView = (ProgramCommentListView) view
				.findViewById(R.id.listView);
		commentErr = (TextView) view.findViewById(R.id.err_text);
		commentProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		sendComment = (ImageView) view.findViewById(R.id.sendcomment);
		sendCommentLong = (ImageView) view.findViewById(R.id.sendcomment1);
		sendComment.setOnClickListener(commentBtnClicked);
		sendCommentLong.setOnClickListener(commentBtnClicked);
		screenShot = (ImageView) view.findViewById(R.id.screenshot);
		btnLayout = (LinearLayout) view.findViewById(R.id.pd_btn_layout);
	}

	private OnClickListener commentBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO 节目类型

			MobclickAgent.onEvent(ProgramNewActivity.this, "zhuanxie");

			MobclickAgent.onEvent(ProgramNewActivity.this, "comment", vpd.name);
			// VodProgramData.current.name);
			openSendCommentActivity();
		}
	};

	public void updateCommentListView(VodProgramData p) {
		// ArrayList<CommentData> temp = (ArrayList<CommentData>)
		// vpd.getComment();
		ArrayList<CommentData> temp = (ArrayList<CommentData>) p.getComment();
		// VodProgramData.current
		// .getComment();
		if (temp != null) {
			comments = temp;
			int size = comments.size();
			if (size == 0) {
				commentErr.setVisibility(View.VISIBLE);
				commentErr.setText("快来抢沙发");
			} else {
				CommentAdapter adapter = new CommentAdapter(comments);
				commentListView.setAdapter(adapter);
				commentListView
						.setOnItemClickListener(commentOnItemClickListener);
			}
		} else {
			commentErr.setVisibility(View.VISIBLE);
		}
		commentProgressBar.setVisibility(View.GONE);
		// if (VodProgramData.current.canShot != 0) {
		if (vpd.canShot != 0) {
			// sendCommentLong.setVisibility(View.GONE);
			// btnLayout.setVisibility(View.VISIBLE);
			// sendComment.setVisibility(View.VISIBLE);
			// sendComment.setImageResource(R.drawable.pd_send_comment);
			// screenShot.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// openScreenShotActivity();
			// }
			// });

			handler.sendEmptyMessage(SHOW_SHOT_BTN);
		} else {
			// btnLayout.setVisibility(View.GONE);
			// sendComment.setVisibility(View.GONE);
			// sendCommentLong.setVisibility(View.VISIBLE);

			handler.sendEmptyMessage(HIDE_SHOT_BTN);
		}
	}

	private class CommentAdapter extends BaseAdapter {
		private final ArrayList<CommentData> comments;

		public CommentAdapter(ArrayList<CommentData> list) {
			this.comments = list;
		}

		@Override
		public int getCount() {
			if (comments != null) {
				return comments.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return comments.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(ProgramNewActivity.this);
				convertView = inflater.inflate(R.layout.comment_list_item_new,
						null);
				viewHolder.nameTextView = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.headpicImageView = (ImageView) convertView
						.findViewById(R.id.headpic);
				viewHolder.contentTextView = (TextView) convertView
						.findViewById(R.id.content);
				viewHolder.picImageView = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.rootLayout = (RelativeLayout) convertView
						.findViewById(R.id.root_layout);
				viewHolder.rootTextView = (TextView) convertView
						.findViewById(R.id.root_content);
				viewHolder.rootPicImageView = (ImageView) convertView
						.findViewById(R.id.root_pic);
				viewHolder.replayCountView = (TextView) convertView
						.findViewById(R.id.replay);
				viewHolder.zhuanfaCountView = (TextView) convertView
						.findViewById(R.id.zhuanfa);
				viewHolder.from = (TextView) convertView
						.findViewById(R.id.from);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);
				// 语音评论
				viewHolder.audioFrame = (RelativeLayout) convertView
						.findViewById(R.id.comment_audio_btn);
				viewHolder.audioBtn = (ImageView) convertView
						.findViewById(R.id.comment_item_content_audio_pic);
				viewHolder.audioPb = (ProgressBar) convertView
						.findViewById(R.id.comment_item_progressBar);

				// 语音评论
				viewHolder.rootAudioFrame = (RelativeLayout) convertView
						.findViewById(R.id.comment_audio_btn_root);
				viewHolder.rootAudioBtn = (ImageView) convertView
						.findViewById(R.id.comment_item_content_audio_pic_root);
				viewHolder.rootAudioPb = (ProgressBar) convertView
						.findViewById(R.id.comment_item_progressBar_root);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final CommentData temp = comments.get(position);
			String userName = temp.userName;
			if (userName != null) {
				viewHolder.nameTextView.setText(userName);
			}

			String fromString = temp.source;
			if (fromString != null) {
				viewHolder.from.setText(fromString);
			}
			String time = temp.commentTime;
			if (time != null) {
				viewHolder.time.setText(time);
			}

			String replayCount = String.valueOf(temp.replyCount);
			viewHolder.replayCountView.setText(replayCount);

			String zhuanfaCount = String.valueOf(temp.forwardCount);
			viewHolder.zhuanfaCountView.setText(zhuanfaCount);

			String headPicUrl = temp.userURL;
			viewHolder.headpicImageView.setTag(headPicUrl);
			loadListImage(viewHolder.headpicImageView, headPicUrl,
					R.drawable.list_headpic_default);
			if (temp.talkType != 4) {
				viewHolder.audioFrame.setVisibility(View.GONE);
				String content = temp.content;
				if (content != null) {
					viewHolder.contentTextView.setText(content);
					SpannableString contentString = Txt2Image.text2Emotion(
							ProgramNewActivity.this, content);
					viewHolder.contentTextView.setText(contentString);
				}

				if (temp.talkType == 1) {
					String picUrl = temp.contentURL;
					viewHolder.picImageView.setTag(picUrl);
					loadListImage(viewHolder.picImageView, picUrl,
							R.drawable.list_comment_default);
					viewHolder.picImageView.setVisibility(View.VISIBLE);
				} else {
					viewHolder.picImageView.setVisibility(View.GONE);
				}

			} else {
				if (OtherCacheData.current().isDebugMode)
					Log.e("Program-audio-url", "url" + temp.audioURL);

				if (!temp.audioURL.equals("")) {
					viewHolder.audioFrame.setVisibility(View.VISIBLE);
					viewHolder.picImageView.setVisibility(View.GONE);
					viewHolder.audioBtn.setVisibility(View.VISIBLE);
					viewHolder.audioBtn
							.setImageResource(R.drawable.pc_switch2audio_big_normal);
					final ProgressBar progressBar = viewHolder.audioPb;
					viewHolder.audioBtn
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									playVoice(temp.audioURL,
											viewHolder.audioBtn, progressBar);
								}
							});
				} else {
					viewHolder.audioFrame.setVisibility(View.GONE);
				}
			}
			if (temp.hasRootTalk) {
				viewHolder.rootLayout.setVisibility(View.VISIBLE);

				if (temp.rootTalk.talkType == 1) {
					String rootheadPicUrl = temp.rootTalk.contentURL;
					viewHolder.rootPicImageView.setTag(rootheadPicUrl);
					loadListImage(viewHolder.rootPicImageView, rootheadPicUrl,
							R.drawable.list_comment_default);
					viewHolder.rootPicImageView.setVisibility(View.VISIBLE);
					String rootContent = temp.rootTalk.content;
					if (rootContent != null) {
						viewHolder.rootTextView.setText(rootContent);
						SpannableString contentString = Txt2Image.text2Emotion(
								ProgramNewActivity.this, rootContent);
						viewHolder.rootTextView.setText(contentString);
					}
				} else if (temp.rootTalk.talkType == 4) {

					if (!temp.rootTalk.audioURL.equals("")) {
						viewHolder.rootAudioFrame.setVisibility(View.VISIBLE);
						viewHolder.rootPicImageView.setVisibility(View.GONE);
						viewHolder.rootAudioBtn.setVisibility(View.VISIBLE);
						viewHolder.rootAudioBtn
								.setImageResource(R.drawable.pc_switch2audio_big_normal);
						final ProgressBar progressBar = viewHolder.rootAudioPb;
						viewHolder.rootAudioBtn
								.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										playVoice(temp.rootTalk.audioURL,
												viewHolder.rootAudioBtn,
												progressBar);
									}
								});
					} else {
						viewHolder.rootAudioFrame.setVisibility(View.GONE);
					}
				} else {
					viewHolder.rootAudioFrame.setVisibility(View.VISIBLE);
					viewHolder.rootAudioBtn.setVisibility(View.GONE);
					viewHolder.rootPicImageView.setVisibility(View.GONE);
					String rootContent = temp.rootTalk.content;
					if (rootContent != null) {
						viewHolder.rootTextView.setText(rootContent);
						SpannableString contentString = Txt2Image.text2Emotion(
								ProgramNewActivity.this, rootContent);
						viewHolder.rootTextView.setText(contentString);
					}
				}
			} else {
				viewHolder.rootLayout.setVisibility(View.GONE);
			}

			return convertView;
		}

		private class ViewHolder {
			public TextView nameTextView;
			public ImageView headpicImageView;
			public TextView contentTextView;
			public ImageView picImageView;
			public RelativeLayout rootLayout;
			public TextView rootTextView;
			public ImageView rootPicImageView;

			// 语音评论框
			public RelativeLayout rootAudioFrame;
			// 语音评论按钮
			public ImageView rootAudioBtn;
			// 语音播放时等待
			public ProgressBar rootAudioPb;

			public TextView replayCountView;
			public TextView zhuanfaCountView;
			public TextView from;
			public TextView time;
			// 语音评论框
			public RelativeLayout audioFrame;
			// 语音评论按钮
			public ImageView audioBtn;
			// 语音播放时等待
			public ProgressBar audioPb;
		}
	}

	private String currentUrl;
	private MediaPlayer mediaPlayer;
	private ProgressBar currentProgressBar;
	private ImageView currentImageButton;

	private void playVoice(String voiceUrl, ImageView button,
			ProgressBar progressBar) {
		if (voiceUrl.equals(currentUrl)) {
			if (mediaPlayer != null) {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
				}
				mediaPlayer.release();
				progressBar.setVisibility(View.GONE);
				button.setImageResource(R.drawable.pc_switch2audio_big_normal);
				currentUrl = null;
				mediaPlayer = null;
			}
		} else {
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.release();
				currentProgressBar.setVisibility(View.GONE);
				currentImageButton
						.setImageResource(R.drawable.pc_switch2audio_big_normal);
			}
			currentProgressBar = progressBar;
			currentImageButton = button;
			currentUrl = voiceUrl;
			mediaPlayer = new MediaPlayer();
			currentProgressBar.setVisibility(View.VISIBLE);

			try {
				mediaPlayer.setDataSource(voiceUrl);
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						currentImageButton
								.setImageResource(R.drawable.pc_switch2audio_big_pressed);
						currentProgressBar.setVisibility(View.GONE);
						mediaPlayer.start();

					}
				});
				mediaPlayer.setOnErrorListener(new OnErrorListener() {
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						Toast.makeText(ProgramNewActivity.this, "播放失败",
								Toast.LENGTH_SHORT).show();
						return false;
					}
				});
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						currentImageButton
								.setImageResource(R.drawable.pc_switch2audio_big_normal);
						currentProgressBar.setVisibility(View.GONE);
						mediaPlayer.release();
						mediaPlayer = null;
						currentUrl = null;
					}
				});
				mediaPlayer.prepareAsync();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private final OnItemClickListener commentOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position == comments.size()) {
				getComment(0, position + 10);
				return;
			}
			CommentData temp = comments.get(position);
			CommentData.current().talkId = temp.talkId;
			CommentData.current().userName = temp.userName;
			CommentData.current().content = temp.content;
			CommentData.current().source = temp.source;
			CommentData.current().replyCount = temp.replyCount;
			CommentData.current().forwardCount = temp.forwardCount;
			CommentData.current().commentTime = temp.commentTime;
			CommentData.current().rootTalk = temp.rootTalk;
			CommentData.current().hasRootTalk = temp.hasRootTalk;
			CommentData.current().userId = temp.userId;
			CommentData.current().talkType = temp.talkType;
			CommentData.current().contentURL = temp.contentURL;
			CommentData.current().userURL = temp.userURL;
			CommentData.current().audioURL = temp.audioURL;
			MobclickAgent.onEvent(ProgramNewActivity.this, "commentone");
			openCommentDetailActivity();
		}
	};

	private void openCommentDetailActivity() {
		Intent intent = new Intent(this, CommentDetailNewActivity.class);
		intent.putExtra("programId", vpd.id);
		startActivity(intent);
	}

	private final OnClickListener errListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch ((Integer) v.getTag()) {
			case 0:
				getComment();
				break;
			case 1:
				getProgramVideo();
				break;
			case 2:
				getProgramDetail();
				break;
			default:
				break;
			}
		}

	};

	private TextView videoErr;
	private ProgressBar videoProgressBar;

	private boolean hasVideo;

	private TextView updateInfoText;
	private MyScrollView videoScrollView;
	private GridView jishuGridView;
	private ListView channelList;
	private ChannelAdapter ca;

	// 以下台也在播放的列表位置
	private int channelPosition = 0;

	private class ChannelAdapter extends BaseAdapter {
		private List<ChannelNewData> lc;

		public ChannelAdapter(List<ChannelNewData> lc) {
			this.lc = lc;
		}

		@Override
		public int getCount() {
			return lc.size();
		}

		@Override
		public Object getItem(int position) {
			return lc.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			View rowView = convertView;
			ViewHolder viewHolder;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ProgramNewActivity.this
						.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.nowplaytvlist_item, null);
				viewHolder = new ViewHolder();
				viewHolder.text = (TextView) rowView
						.findViewById(R.id.nowplaytvlist_item_name);
				viewHolder.btn = (ImageButton) rowView
						.findViewById(R.id.nowplaytvlist_item_btn);
				rowView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) rowView.getTag();
			}
			viewHolder.text.setText(lc.get(position).name
					+ lc.get(position).now.startTime + "-"
					+ lc.get(position).now.endTime);
			if (lc.get(position).now.isPlaying == 1) {
				viewHolder.btn.setImageResource(R.drawable.liveplaybtn);
				viewHolder.btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// Intent intent = new Intent(ProgramNewActivity.this,
						// NewLivePlayerActivity.class);

						CpData p = lc.get(position).now;
						// String path = lc.get(position).now.playUrl;
						// intent.putExtra("path", path);
						// intent.putExtra("playType", 2);
						// String title = lc.get(position).now.name;
						// intent.putExtra("title", title);
						//
						// intent.putExtra("topicId", p.topicId);
						// intent.putExtra("id", p.id);
						// intent.putExtra("nameHolder", p.name);
						// intent.putExtra("updateName", "");
						ArrayList<NetPlayData> netPlayDatas = lc.get(position).netPlayDatas;
						if (netPlayDatas != null) {
							if (netPlayDatas.size() == 1) {
								NetPlayData tempData = netPlayDatas.get(0);
								String url = tempData.url;
								String videoPath = tempData.videoPath;
								if (tempData.videoPath != null) {
									Intent intent = new Intent(
											ProgramNewActivity.this,
											NewLivePlayerActivity.class);
									intent.putExtra("path", videoPath);
									intent.putExtra("playType", 1);
									String title = p.name;
									intent.putExtra("title", title);
									startActivity(intent);
								} else if (tempData.url != null) {
									openNetLiveActivity(url, videoPath, 1,
											p.name);
								}
							} else {
								isLive = 1;
								liveThroughNet(lc.get(position).netPlayDatas);
							}
						} else {
							DialogUtil.alertToast(getApplicationContext(),
									"暂时无法播放");
							liveBtn.setVisibility(View.GONE);
						}
					}
				});
			} else {
				if (lc.get(position).now.order == 0) {
					viewHolder.btn.setImageResource(R.drawable.book);
					viewHolder.btn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (UserNow.current().userID != 0) {
								channelPosition = position;

								RemindAddTask remindAddTask = new RemindAddTask(
										ProgramNewActivity.this);
								remindAddTask.execute(ProgramNewActivity.this,
										new RemindAddRequest(
												lc.get(position).now.id),
										new ResultParser());
							} else {
								openLogInActivity();
							}
						}
					});
				} else {
					viewHolder.btn.setImageResource(R.drawable.book_cancel);
					viewHolder.btn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (UserNow.current().userID != 0) {
								channelPosition = position;

								RemindDeleteTask remindDeleteTask = new RemindDeleteTask(
										ProgramNewActivity.this);
								remindDeleteTask.execute(
										ProgramNewActivity.this,
										new RemindDeleteRequest(lc
												.get(position).now.id),
										new ResultParser());
							} else {
								openLogInActivity();
							}
						}
					});
				}
			}
			return rowView;
		}

		private class ViewHolder {
			public TextView text;
			public ImageButton btn;
		}
	}

	private GetProgramVideoTask getProgramVideoTask;

	private void getProgramVideo() {
		if (getProgramVideoTask == null) {
			// VodProgramData.current.id = programId;
			getProgramVideoTask = new GetProgramVideoTask();
			getProgramVideoTask.execute(this, this,
					new ProgramVideoListRequest(Integer.parseInt(programId)),
					new ProgramVideoListParser());
		}
	}

	private void initVideoView(View view) {
		videoErr = (TextView) view.findViewById(R.id.err_text);
		videoProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		updateInfoText = (TextView) view.findViewById(R.id.intro);
		updateInfoText.setOnClickListener(this);
		videoScrollView = (MyScrollView) view.findViewById(R.id.scollView);
		videoScrollView.setTag(0);
		videoScrollView.setOnScrollTopListener(this);
		jishuGridView = (GridView) view.findViewById(R.id.jishu);
		jishuGridView.setSelector(R.drawable.transparent_background);
		jishuGridView.setOnItemClickListener(episodeGridClickListener);
		channelList = (ListView) view.findViewById(R.id.pd_video_tvlist);

		otherStationLine = (ImageView) view
				.findViewById(R.id.pd_video_tvstaion_line);
		otherStationTips = (TextView) view.findViewById(R.id.pd_video_tvstaion);
		vodTips = (TextView) view.findViewById(R.id.pd_video_vod_tips);
	}

	private void updateVidoeView() {
		videoProgressBar.setVisibility(View.GONE);
		videoErr.setVisibility(View.GONE);
		videoScrollView.setVisibility(View.VISIBLE);
		updateVideoView();
	}

	private ArrayList<ParentVideoData> parentVideos;

	private void updateVideoView() {
		List<ChannelNewData> lc = vpd.getChannel();
		if (lc != null && lc.size() > 0) {
			ca = new ChannelAdapter(lc);
			channelList.setAdapter(ca);

			int count = (lc.size());
			int height = CommonUtils.dip2px(this, count * 60);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, height);
			channelList.setLayoutParams(params);

			otherStationLine.setVisibility(View.VISIBLE);
			otherStationTips.setVisibility(View.VISIBLE);
			channelList.setVisibility(View.VISIBLE);

		} else {

			otherStationLine.setVisibility(View.GONE);
			otherStationTips.setVisibility(View.GONE);
			channelList.setVisibility(View.GONE);
		}

		ArrayList<ParentVideoData> temp = (ArrayList<ParentVideoData>) vpd
				.getVideo();
		if (temp != null) {
			if (temp.size() == 1) {

				if (vpd.showPattern == 1) {// 剧集
					ArrayList<VideoData> tempVideoDatas = temp.get(0)
							.getVideos();
					jishuAdapter = new JishuGridViewAdapter(tempVideoDatas);
					jishuGridView.setAdapter(jishuAdapter);

					// TODO: 2013-1-18 先关闭，因为个别节目正续返回集数
					// vodTips.setText("在线观看(已更新到" + tempVideoDatas.get(0).name
					// + ")");

					if (tempVideoDatas != null && tempVideoDatas.size() > 0) {
						jishuGridView.setVisibility(View.VISIBLE);
						vodTips.setVisibility(View.VISIBLE);
						int count = (tempVideoDatas.size() / 4 + 1);
						int height = CommonUtils.dip2px(this, count * 45);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
								LayoutParams.WRAP_CONTENT, height);
						jishuGridView.setLayoutParams(params);
					} else {
						otherStationLine.setVisibility(View.GONE);
					}
				} else {
					// TODO:非剧集界面效果还需调整
					ArrayList<VideoData> tempVideoDatas = temp.get(0)
							.getVideos();
					zongyiadapter = new ZongYiGridViewAdapter(tempVideoDatas);
					jishuGridView.setAdapter(zongyiadapter);
					if (tempVideoDatas != null && tempVideoDatas.size() > 0) {
						jishuGridView.setVisibility(View.VISIBLE);
						vodTips.setVisibility(View.VISIBLE);
						int count = tempVideoDatas.size() + 1;
						int height = CommonUtils.dip2px(this, count * 45);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
								LayoutParams.WRAP_CONTENT, height);
						jishuGridView.setLayoutParams(params);
						jishuGridView.setNumColumns(1);
					} else {
						otherStationLine.setVisibility(View.GONE);
					}
				}
			} else if (temp.size() > 1) {

				// TODO:分季

			}
		}

	}

	ZongYiGridViewAdapter zongyiadapter;
	JishuGridViewAdapter jishuAdapter;
	private int currentEpisodePosition = 0;

	private final OnItemClickListener episodeGridClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			ArrayList<ParentVideoData> temp = (ArrayList<ParentVideoData>) vpd
					.getVideo();

			ArrayList<VideoData> tempVideoDatas = temp.get(0).getVideos();

			// VideoData vd = tempVideoDatas.get(arg2);
			// VodProgramData.current.playUrl = vd.url;
			// VodProgramData.current.nameHolder = VodProgramData.current.name
			// + vd.name;
			// MobclickAgent.onEvent(ProgramNewActivity.this, "videoone");
			// openLiveActivity();
			currentEpisodePosition = arg2;
			VideoData vd = null;
			try {
				vd = tempVideoDatas.get(arg2);
			} catch (IndexOutOfBoundsException e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			if (vd != null) {
				vpd.playUrl = vd.url;
				vpd.fromString = vd.fromString;
				if (!vpd.name.equals(vd.name)) {
					if (vpd.showPattern == 1) {
						vpd.nameHolder = vpd.name + " " + vd.name;
					} else
						vpd.nameHolder = vd.name;
				} else
					vpd.nameHolder = vpd.name;
				MobclickAgent.onEvent(ProgramNewActivity.this, "videoone");
				switch (vd.playType) {
				// 网页播放
				case 2:
					Intent intentWeb = new Intent(ProgramNewActivity.this,
							WebBrowserActivity.class);
					intentWeb.putExtra("url", vd.url);
					String titleWeb = vpd.name;
					intentWeb.putExtra("title", titleWeb);
					if (vd.url != null && !vd.url.equals("")) {
						MobclickAgent.onEvent(ProgramNewActivity.this,
								"epgplaytv", titleWeb);
						startActivity(intentWeb);
					} else {
						DialogUtil
								.alertToast(getApplicationContext(), "暂时无法播放");
					}
					break;
				// 直接播放
				case 1:
				default:
					if (vpd.playUrl != null && !vpd.playUrl.equals(""))
						openLiveActivity(2);
					else {
						isLive = 2;
						liveThroughNet(vd);
					}
					// liveThroughNet(vd);
					break;
				}
			}
			if (jishuAdapter != null) {
				jishuAdapter.notifyDataSetChanged();
			}
			if (zongyiadapter != null) {
				zongyiadapter.notifyDataSetChanged();
			}
		}
	};

	private class JishuGridViewAdapter extends BaseAdapter {
		private ArrayList<VideoData> list;

		public JishuGridViewAdapter(ArrayList<VideoData> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list != null) {
				return list.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			ViewHolder viewHolder;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ProgramNewActivity.this
						.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.program_video_grid_item,
						null);
				viewHolder = new ViewHolder();
				viewHolder.textView = (TextView) rowView
						.findViewById(R.id.textView);
				rowView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) rowView.getTag();
			}
			String temp = list.get(position).name;
			if (temp != null) {
				viewHolder.textView.setText(temp);
			}
			if (position == currentEpisodePosition) {
				viewHolder.textView
						.setBackgroundResource(R.drawable.pd_video_item_focused);
				viewHolder.textView.setTextColor(whiteColor);
			} else {
				viewHolder.textView
						.setBackgroundResource(R.drawable.pd_video_grid_item);
				viewHolder.textView.setTextColor(pdVideoDefaultColor);
			}
			return rowView;
		}

		private class ViewHolder {
			public TextView textView;
		}
	}

	private class ZongYiGridViewAdapter extends BaseAdapter {
		private ArrayList<VideoData> list;

		public ZongYiGridViewAdapter(ArrayList<VideoData> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list != null) {
				return list.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			ViewHolder viewHolder;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ProgramNewActivity.this
						.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.program_zongyi_grid_item,
						null);
				viewHolder = new ViewHolder();
				viewHolder.textView = (TextView) rowView
						.findViewById(R.id.textView);
				rowView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) rowView.getTag();
			}
			String temp = list.get(position).name;
			if (temp != null) {
				viewHolder.textView.setText(temp);
			}
			if (position == currentEpisodePosition) {
				viewHolder.textView
						.setBackgroundResource(R.drawable.pd_video_item_focused);
				;
				viewHolder.textView.setTextColor(whiteColor);
			} else {
				viewHolder.textView
						.setBackgroundResource(R.drawable.pd_video_grid_item);
				viewHolder.textView.setTextColor(pdVideoDefaultColor);
			}
			return rowView;
		}

		private class ViewHolder {
			public TextView textView;
		}
	}

	private TextView detailErr;
	private ProgressBar detailProgressBar;

	private boolean hasDetail;

	private TextView intro;
	private Gallery starGallery;
	private Gallery titBitGallery;
	private GridView programAroundListView;
	private MyScrollView scrollView;
	private RatingBar ratingBar;
	private TextView score;
	private TextView from;
	private boolean isIntroClose = true;
	// 明星
	private ImageView starLine;
	private TextView starTips;
	private LinearLayout starLayout;
	// 剧照
	private ImageView titBitLine;
	private TextView titBitTips;
	private LinearLayout titBitLayout;
	// 周边
	private ImageView aroundLine;
	private TextView aroundTips;
	// 其他台在播
	private ImageView otherStationLine;
	private TextView otherStationTips;
	private TextView vodTips;

	private void initDetailView(View view) {
		detailErr = (TextView) view.findViewById(R.id.err_text);
		detailProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

		scrollView = (MyScrollView) view.findViewById(R.id.scollView);
		scrollView.setTag(1);
		scrollView.setOnScrollTopListener(this);

		intro = (TextView) view.findViewById(R.id.detail);
		starGallery = (Gallery) view.findViewById(R.id.starGallery);
		titBitGallery = (Gallery) view.findViewById(R.id.titbitGallery);
		score = (TextView) view.findViewById(R.id.score);
		from = (TextView) view.findViewById(R.id.from);
		ratingBar = (RatingBar) view.findViewById(R.id.pd_ratingbar);
		ratingBar.setOnRatingBarChangeListener(this);
		programAroundListView = (GridView) view
				.findViewById(R.id.pd_detail_listview);

		starLine = (ImageView) view.findViewById(R.id.pd_detail_star_line);
		starTips = (TextView) view.findViewById(R.id.pd_detail_star);
		titBitLine = (ImageView) view.findViewById(R.id.pd_detail_juzhao_line);
		titBitTips = (TextView) view.findViewById(R.id.pd_detail_juzhao);
		aroundLine = (ImageView) view.findViewById(R.id.pd_detail_related_line);
		aroundTips = (TextView) view.findViewById(R.id.pd_detail_related);

		starLayout = (LinearLayout) view
				.findViewById(R.id.pd_detail_star_layout);
		titBitLayout = (LinearLayout) view
				.findViewById(R.id.pd_detail_juzhao_layout);
	}

	private GetProgramDetailTask getProgramDetailTask;

	private void getProgramDetail() {
		if (getProgramDetailTask == null) {
			// VodProgramData.current.id = programId;
			getProgramDetailTask = new GetProgramDetailTask();
			getProgramDetailTask
					.execute(
							this,
							this,
							new ProgramDetailNewNewRequest(Integer
									.parseInt(programId)),
							new ProgramDetailNewNewParser());
		}
	}

	private void updateProgramDetail() {
		detailProgressBar.setVisibility(View.GONE);
		detailErr.setVisibility(View.GONE);
		scrollView.setVisibility(View.VISIBLE);
		String introText = vpd.intro;
		if (introText != null) {
			intro.setText(introText);
			intro.setMaxLines(100);
			intro.setEllipsize(null);
			// intro.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// if (isIntroClose) {
			//
			// } else {
			// scrollView.scrollTo(0, 0);
			// intro.setMaxLines(3);
			// intro.setEllipsize(TextUtils.TruncateAt.END);
			// }
			// isIntroClose = !isIntroClose;
			// }
			// });
		}
		String point = vpd.point;
		if (point != null) {
			try {
				float floatPoint = Float.valueOf(point);
				if (floatPoint <= 1.0) {
					ratingBar.setVisibility(View.GONE);
					score.setVisibility(View.GONE);
					from.setVisibility(View.GONE);
				} else {
					if (point.length() > 3) {
						point = point.substring(0, 3);
					}
					score.setText(point);
					float score = Float.parseFloat(point) / 2;
					ratingBar.setRating(score);
					lastProgress = score;
				}
			} catch (Exception e) {
				ratingBar.setRating(0);
				lastProgress = 0;
				ratingBar.setVisibility(View.GONE);
				score.setVisibility(View.GONE);
				from.setVisibility(View.GONE);
			}
		}

		ArrayList<StarData> stars = (ArrayList<StarData>) vpd.getStar();
		if (stars != null) {
			starGallery.setAdapter(new GalleryAdapter(stars));
			int c = stars.size();
			if (c > 3) {
				starGallery.setSelection(3);
			} else {
				switch (c) {
				case 2:
					starGallery.setSelection(1);
					break;
				case 3:
					starGallery.setSelection(2);
					break;
				default:
					break;
				}
			}
			starGallery.setOnItemClickListener(starClickListener);
		} else {
			// 无明星，隐藏布局
			starLine.setVisibility(View.GONE);
			starTips.setVisibility(View.GONE);
			starLayout.setVisibility(View.GONE);
		}

		String[] urls = vpd.photos;
		if (urls != null) {
			titBitGallery.setAdapter(new TitBitGalleryAdapter(urls));
			int c = urls.length;
			if (c > 3) {
				titBitGallery.setSelection(3);
			} else {
				switch (c) {
				case 2:
					titBitGallery.setSelection(1);
					break;
				case 3:
					titBitGallery.setSelection(2);
					break;
				default:
					break;
				}
			}
			titBitGallery.setOnItemClickListener(titOnItemClickListener);
		} else {
			// 无剧照，隐藏布局
			titBitLine.setVisibility(View.GONE);
			titBitTips.setVisibility(View.GONE);
			titBitLayout.setVisibility(View.GONE);
		}
		ArrayList<ProgramAroundData> arounds = (ArrayList<ProgramAroundData>) vpd
				.getAround();
		if (arounds != null && arounds.size() > 0) {
			programAroundListView.setAdapter(new ProgramAroundAdapter(arounds));
			/*
			 * programAroundListView
			 * .setOnItemClickListener(programAroundClickListener); int count =
			 * arounds.size(); int height = CommonUtils.dip2px(this, count * 80)
			 * + 15; LinearLayout.LayoutParams params = new
			 * LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, height);
			 * programAroundListView.setLayoutParams(params);
			 */
			int count = arounds.size() + 1;
			int height = CommonUtils.dip2px(this, count * 80);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, height);
			programAroundListView.setLayoutParams(params);
			programAroundListView.setNumColumns(1);
			programAroundListView
					.setOnItemClickListener(programAroundClickListener);
		} else {
			// 无周边，隐藏布局
			aroundLine.setVisibility(View.GONE);
			aroundTips.setVisibility(View.GONE);
		}
	}

	OnItemClickListener titOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			MobclickAgent.onEvent(ProgramNewActivity.this, "juzhao");

			String[] temp = vpd.photos;
			String url = temp[position];
			url = url.replace("s.", "b.");
			OtherCacheData.current().bigPics = temp;
			OtherCacheData.current().bigPicPosition = position;
			openShowImageActivity(url);
		}
	};

	private void openShowImageActivity(String url) {
		Intent intent = new Intent(this, ShowImageActivity.class);
		intent.putExtra("url", url);
		startActivity(intent);
	}

	private class GalleryAdapter extends BaseAdapter {

		private final ArrayList<StarData> list;

		public GalleryAdapter(ArrayList<StarData> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list != null) {
				return list.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			ViewHolder viewHolder;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ProgramNewActivity.this
						.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.programdetail_gallery_item,
						null);
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) rowView
						.findViewById(R.id.p_gallery_img_small);
				rowView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) rowView.getTag();
			}
			String url = list.get(position).photoBig_V;
			viewHolder.imageView.setTag(url);
			try {
				loadListImage(viewHolder.imageView, url,
						R.drawable.list_star_default);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			return rowView;
		}

		private class ViewHolder {
			public ImageView imageView;
		}
	}

	private OnItemClickListener starClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			MobclickAgent.onEvent(ProgramNewActivity.this, "mingxing");

			ArrayList<StarData> stars = (ArrayList<StarData>) vpd.getStar();
			int starId = stars.get(position).stagerID;
			openStarActivity(starId);
		}
	};

	private void openStarActivity(int id) {
		Intent intent = new Intent(this, StarDetailActivity.class);
		intent.putExtra("starId", id);
		startActivity(intent);
	}

	private class TitBitGalleryAdapter extends BaseAdapter {
		private final String[] urls;

		public TitBitGalleryAdapter(String[] urls) {
			this.urls = urls;
		}

		@Override
		public int getCount() {
			if (urls != null) {
				return urls.length;
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return urls[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			ViewHolder viewHolder;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ProgramNewActivity.this
						.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.programdetail_gallery_item,
						null);
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) rowView
						.findViewById(R.id.p_gallery_img_small);
				rowView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) rowView.getTag();
			}
			String url = urls[position];
			viewHolder.imageView.setTag(url);
			try {
				loadListImage(viewHolder.imageView, url,
						R.drawable.list_star_default);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			return rowView;
		}

		private class ViewHolder {
			public ImageView imageView;
		}
	}

	private class ProgramAroundAdapter extends BaseAdapter {

		private final ArrayList<ProgramAroundData> list;

		public ProgramAroundAdapter(ArrayList<ProgramAroundData> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list != null) {
				return list.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			ViewHolder viewHolder;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) ProgramNewActivity.this
						.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.program_around_list_item,
						null);
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) rowView
						.findViewById(R.id.pic);
				viewHolder.introTextView = (TextView) rowView
						.findViewById(R.id.intro);
				viewHolder.timeTextView = (TextView) rowView
						.findViewById(R.id.time);
				rowView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) rowView.getTag();
			}
			ProgramAroundData temp = list.get(position);

			String url = temp.url;

			// if (OtherCacheData.current().isDebugMode)
			// Log.e("ProgramAround", url);

			viewHolder.imageView.setTag(url);
			loadListImage(viewHolder.imageView, url,
					R.drawable.rcmd_list_item_pic_default);
			String intro = temp.title;
			if (intro != null) {
				viewHolder.introTextView.setText(intro);
			}
			String time = temp.time;
			if (time != null) {
				viewHolder.timeTextView.setText(time);
			}
			return rowView;
		}

		private class ViewHolder {
			public ImageView imageView;
			public TextView introTextView;
			public TextView timeTextView;
		}
	}

	private OnItemClickListener programAroundClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			MobclickAgent.onEvent(ProgramNewActivity.this, "news");

			ArrayList<ProgramAroundData> arounds = (ArrayList<ProgramAroundData>) vpd
					.getAround();
			int progamAroundId = arounds.get(position).id;
			openProgramAroundActivity(progamAroundId);
		}
	};

	private void openProgramAroundActivity(int id) {
		Intent intent = new Intent(this, ProgramAroundActivity.class);
		intent.putExtra("id", id);
		startActivity(intent);
	}

	private ChaseProgramTask chaseProgramTask;

	private void chaseProgram() {
		if (chaseProgramTask == null) {
			chaseProgramTask = new ChaseProgramTask(this);
			chaseProgramTask.execute(this,
					new ChaseAddRequest(Integer.parseInt(programId)),
					new ResultParser());

		}
	}

	private void onZhuijuSuccess() {
		vpd.isChased = 1;
		zhuiBtn.setImageResource(R.drawable.pd_yizhui_pressed);

	}

	private void onCancleZhuijuSuccess() {
		vpd.isChased = 0;
		zhuiBtn.setImageResource(R.drawable.pd_zhuiju_btn);

	}

	private SignProgramTask signProgramTask;

	private void signProgram() {
		if (signProgramTask == null) {
			signProgramTask = new SignProgramTask(this);
			signProgramTask.execute(this,
					new SignProgramRequest(Integer.parseInt(programId)),
					new SignProgramParser());

		}
	}

	private void onSignProgramSuccess() {
		vpd.isSigned = 1;
		signBtn.setImageResource(R.drawable.pd_yisigned_ressed);

	}

	private void openLogInActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}

	private void openLiveActivity(int isLive) {
		ShareData.text = Constants.url + "/share/weixinVideo.action?playType="
				+ vpd.playType + "&programId=" + vpd.id + "&playUrl="
				+ vpd.playUrl;

		ShareData.shareWeiboText = Constants.url
				+ "/share/sinaWeiboVideo.action?params=" + vpd.playUrl + "--"
				+ +vpd.playType + "--" + vpd.id;

		Intent intent = new Intent(this, NewLivePlayerActivity.class);
		String path = vpd.playUrl;
		intent.putExtra("path", path);
		intent.putExtra("playType", isLive);// 点播
		intent.putExtra("topicId", vpd.topicId);
		intent.putExtra("id", vpd.id);
		String title = vpd.nameHolder;
		if (title != null) {
			intent.putExtra("title", title);
			intent.putExtra("nameHolder", title);
		}
		intent.putExtra("updateName", vpd.updateName);
		if (vpd.pic != null) {
			intent.putExtra("programPic", vpd.pic);
		}
		if (vpd.fromString != null) {
			intent.putExtra("fromString", vpd.fromString);
		}
		if (vpd.playVideoActivityId != 0) {
			intent.putExtra("playVideoActivityId", vpd.playVideoActivityId);
		}
		if (path != null) {
			startActivity(intent);
		} else {
			DialogUtil.alertToast(getApplicationContext(), "暂时无法播放");
		}
	}

	private void openScreenShotActivity() {
		MobclickAgent.onEvent(ProgramNewActivity.this, "jietu");

		Intent intent = new Intent(this, TVScreenShotActivity.class);
		startActivity(intent);

	}

	private void openSendCommentActivity() {
		Intent intent = new Intent(this, SendCommentActivity.class);
		intent.putExtra("topicId", topicId);
		intent.putExtra("programId", programId);
		// TODO:programName需在progranHeader后赋值
		intent.putExtra("programName", vpd.name);

		startActivityForResult(intent, SendCommentActivity.NORMAL);

	}

	private RelativeLayout connectBg;

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	@Override
	public void onNetBegin(String method) {
		if ("chaseAdd".equals(method)) {
			showpb();
		} else if ("programSign".equals(method)) {
			showpb();
		}
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("programHead".equals(method)) {
			if (msg != null) {
				String s = new ProgramHeadParser().parse(msg, vpd);
				if (s != null && s.equals("")) {
					upateProgramHeader();
				}
			} else {
				errText.setVisibility(View.VISIBLE);
				programProgressBar.setVisibility(View.GONE);
				refreshProgressBar.setVisibility(View.GONE);
				refreshBtn.setVisibility(View.VISIBLE);
			}
			programHeaderTask = null;
		} else if ("talkList".equals(method)) {
			if (msg != null) {
				String result = new TalkListNewParser().parse(msg, vpd);
				if (result.equals("")) {
					updateCommentListView(vpd);
				} else {
					DialogUtil.alertToast(getApplicationContext(), result);
				}

			} else {
				commentErr.setVisibility(View.VISIBLE);
				commentProgressBar.setVisibility(View.GONE);
			}
			getCommentTask = null;
		} else if ("programDetail".equals(method)) {
			if (msg != null) {
				String s = new ProgramDetailNewNewParser().parse(msg, vpd);
				if (s != null && s.equals("")) {
					hasDetail = true;
					updateProgramDetail();
				} else {
					DialogUtil.alertToast(getApplicationContext(), s);
				}
			} else {
				hasDetail = false;
				detailErr.setVisibility(View.VISIBLE);
				detailProgressBar.setVisibility(View.GONE);
			}
			getProgramDetailTask = null;
		} else if ("programVideoList".equals(method)) {
			if (msg != null) {
				hasVideo = true;
				String s = new ProgramVideoListParser().parse(msg, vpd);
				if (s != null && s.equals("")) {
					try {
						updateVidoeView();
					} catch (IndexOutOfBoundsException e) {
						// TODO: handle exception
						e.printStackTrace();
						finish();
					}
				} else {
					DialogUtil.alertToast(getApplicationContext(), s);
					hasVideo = false;
					videoErr.setVisibility(View.VISIBLE);
					videoProgressBar.setVisibility(View.GONE);
				}
			} else {
				hasVideo = false;
				videoErr.setVisibility(View.VISIBLE);
				videoProgressBar.setVisibility(View.GONE);
			}
			getProgramVideoTask = null;
		} else if ("chaseAdd".equals(method)) {
			hidepb();
			if (msg != null && msg.equals("")) {
				onZhuijuSuccess();

				saveUserBookCount(true);

				DialogUtil.alertToast(getApplicationContext(), "追剧成功!");
			} else {
				DialogUtil.alertToast(getApplicationContext(), "追剧失败!");
			}
			chaseProgramTask = null;
		} else if ("chaseDelete".equals(method)) {
			hidepb();
			if (msg != null && msg.equals("")) {
				saveUserBookCount(false);
				onCancleZhuijuSuccess();
				DialogUtil.alertToast(getApplicationContext(), "取消追剧成功!");
			} else {
				DialogUtil.alertToast(getApplicationContext(), "取消追剧失败!");
			}
			chaseDeleTask = null;
		}

		else if ("programSign".equals(method)) {
			hidepb();
			if (msg != null && msg.equals("")) {
				onSignProgramSuccess();
				DialogUtil.alertToast(getApplicationContext(), "签到成功!");
			} else {
				DialogUtil.alertToast(getApplicationContext(), "签到失败!");
			}
			signProgramTask = null;
		} else if ("remindAdd".equals(method)) {
			hidepb();
			if (msg != null && msg.equals("")) {
				// onSignProgramSuccess();
				vpd.getChannel().get(channelPosition).now.order = 1;
				ca.notifyDataSetChanged();
				DialogUtil.alertToast(getApplicationContext(), "预约成功!");
				UserNow.current().remindCount += 1;
			} else {

				if (msg.equals("已经预约该节目，不能重复预约")) {
					vpd.getChannel().get(channelPosition).now.order = 1;
					ca.notifyDataSetChanged();
					DialogUtil.alertToast(getApplicationContext(), "预约成功!");
				} else {
					DialogUtil.alertToast(getApplicationContext(), "预约失败!");
				}
			}
			signProgramTask = null;
		} else if ("remindDelete".equals(method)) {
			hidepb();
			if (msg != null && msg.equals("")) {
				// onSignProgramSuccess();
				vpd.getChannel().get(channelPosition).now.order = 0;
				ca.notifyDataSetChanged();
				DialogUtil.alertToast(getApplicationContext(), "取消预约成功!");
			} else {
				DialogUtil.alertToast(getApplicationContext(), "取消预约失败!");
			}
			signProgramTask = null;
		}
		if (UserNow.current().getNewBadge() != null) {
			for (int i = 0; i < UserNow.current().getNewBadge().size(); i++) {
				String name = UserNow.current().getNewBadge().get(i).name;
				if (name != null) {
					DialogUtil.showBadgeAddToast(ProgramNewActivity.this, name);
				}
			}
			UserNow.current().setNewBadge(null);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SendCommentActivity.NORMAL:
			if (resultCode == Activity.RESULT_OK) {
				getComment();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		if ("programHead".equals(method)) {
			programHeaderTask = null;
		} else if ("talkList".equals(method)) {
			getCommentTask = null;
		} else if ("programDetail".equals(method)) {
			getProgramDetailTask = null;
		} else if ("programVideoList".equals(method)) {
			getProgramVideoTask = null;
		}
		Log.e(TAG, method);
	}

	/**
	 * 加载图片
	 */
	private void loadImage(final ImageView imageView, String url, int defaultPic) {
		final ImageView local = imageView;
		Drawable drawable = imageLoader.loadDrawable(url, new ImageCallback() {
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				local.setImageDrawable(imageDrawable);
			}
		});
		if (drawable != null) {
			local.setImageDrawable(drawable);
		} else {
			local.setImageResource(defaultPic);
		}
	}

	private void loadListImage(final ImageView imageView, String url, int resId) {
		if (url != null) {
			Drawable bitmap = imageLoader.loadDrawable(url,
					new AsyncImageLoader.ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable,
								String imageUrl) {
							String selfUrl = (String) imageView.getTag();
							if (selfUrl != null && selfUrl.equals(imageUrl)) {
								imageView.setImageDrawable(imageDrawable);
							}
						}
					});
			if (bitmap != null) {
				imageView.setImageDrawable(bitmap);
			} else {
				imageView.setImageResource(resId);
			}
		}
	}

	private int commentFirstVisibleItem;

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (commentFirstVisibleItem == 0) {
			ProgramViewEventDispatchedController.listViewNeedEvent = false;
			commentListView.setSelection(0);
		} else {
			ProgramViewEventDispatchedController.listViewNeedEvent = true;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		commentFirstVisibleItem = firstVisibleItem;
	}

	@Override
	public void onTop(MyScrollView view) {
		int which = (Integer) view.getTag();
		if (which == 1) {
			ProgramViewEventDispatchedController.detaiViewNeedEvent = false;
			Log.e("TAG", "onTop detail");
		} else if (which == 0) {
			ProgramViewEventDispatchedController.videoViewNeedEvent = false;
			Log.e("TAG", "onTop video");
		}
	}

	@Override
	public void onOther(MyScrollView view) {
		int which = (Integer) view.getTag();
		if (which == 1) {
			ProgramViewEventDispatchedController.detaiViewNeedEvent = true;
			Log.e("TAG", "onOther detail");
		} else if (which == 0) {
			ProgramViewEventDispatchedController.videoViewNeedEvent = true;
			Log.e("TAG", "onOther video");
		}
	}

	private static final String TAG = "ProgramNewActivity";

	private void close() {
		if (programHeaderTask != null) {
			programHeaderTask.cancel(true);
		} else if (getCommentTask != null) {
			getCommentTask.cancel(true);
		} else if (getProgramDetailTask != null) {
			getProgramDetailTask.cancel(true);
		} else if (getProgramVideoTask != null) {
			getProgramVideoTask.cancel(true);
		}

		viewPager = null;
		starGallery = null;
		titBitGallery = null;
		programAroundListView = null;
		scrollView = null;
		videoScrollView = null;
		jishuGridView = null;
		channelList = null;
		imageLoader = null;

		System.gc();

		// if (imageLoader != null) {
		// try {
		// imageLoader.recyle();
		// } catch (RuntimeException e) {
		// e.printStackTrace();
		// }
		// }
		finish();
	}

	@Override
	public void onBackPressed() {
		close();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);

	}

	private void saveUserBookCount(boolean isAdd) {
		int c = 0;
		if (isAdd) {
			c = UserNow.current().chaseCount + 1;

		} else {
			c = UserNow.current().chaseCount - 1;
		}

		if (c >= 0) {
			UserNow.current().chaseCount = c;
			SharedPreferences sp = getSharedPreferences("userInfo", 0);
			Editor spEd = sp.edit();
			spEd.putInt("chaseCount", c);
			spEd.commit();
		} else {
			UserNow.current().chaseCount = 0;
		}

	}

	private ChaseDeleteTask chaseDeleTask;

	private void cancelChase() {
		if (chaseDeleTask == null) {
			chaseDeleTask = new ChaseDeleteTask(this);
			chaseDeleTask.execute(this, new ChaseDeleteRequest(),
					new ResultParser());
		}
	}

	// 处理评论与电视截图按钮
	private final int SHOW_SHOT_BTN = 1;
	private final int HIDE_SHOT_BTN = 2;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_SHOT_BTN:
				sendCommentLong.setVisibility(View.GONE);
				btnLayout.setVisibility(View.VISIBLE);
				sendComment.setVisibility(View.VISIBLE);
				sendComment.setImageResource(R.drawable.pd_send_comment);
				screenShot.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						openScreenShotActivity();
					}
				});
				break;
			case HIDE_SHOT_BTN:
				btnLayout.setVisibility(View.GONE);
				sendComment.setVisibility(View.GONE);
				sendCommentLong.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		};
	};

	private float lastProgress;

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {
		if (fromUser) {
			ratingBar.setRating(lastProgress);
		}
	}

	private void liveThroughNet(VideoData vd) {
		ArrayList<NetPlayData> netPlayDatas = vd.netPlayDatas;
		if (netPlayDatas != null) {
			if (netPlayDatas.size() == 1) {
				NetPlayData tempData = netPlayDatas.get(0);
				String url = tempData.url;
				String videoPath = tempData.videoPath;
				if (videoPath != null && !videoPath.equals("")) {
					openLiveActivity(2);
				} else if (url != null) {
					openNetLiveActivity(url, videoPath, 2, vpd.nameHolder);
				}
			} else {
				netLiveLayout.setVisibility(View.VISIBLE);
				netPlayDataListAdapter = new NetPlayDataListAdapter(
						ProgramNewActivity.this, netPlayDatas);
				netLiveListView.setAdapter(netPlayDataListAdapter);
			}
		} else {
			DialogUtil.alertToast(getApplicationContext(), "暂时无法播放");
		}
	}

	private void liveThroughNet(ArrayList<NetPlayData> netPlayDatas) {
		if (netPlayDatas != null) {
			if (netPlayDatas.size() == 1) {
				NetPlayData tempData = netPlayDatas.get(0);
				String url = tempData.url;
				String videoPath = tempData.videoPath;
				if (videoPath != null && !videoPath.equals("")) {
					Intent intent = new Intent(this,
							NewLivePlayerActivity.class);
					intent.putExtra("path", videoPath);
					intent.putExtra("playType", 2);
					String title = vpd.name;
					intent.putExtra("title", title);
					startActivity(intent);
				} else if (url != null) {
					openNetLiveActivity(url, videoPath, 1, vpd.name);
				}
			} else {
				netLiveLayout.setVisibility(View.VISIBLE);
				netPlayDataListAdapter = new NetPlayDataListAdapter(
						ProgramNewActivity.this, netPlayDatas);
				netLiveListView.setAdapter(netPlayDataListAdapter);
			}
		} else {
			DialogUtil.alertToast(getApplicationContext(), "暂时无法播放");
		}
	}

	RelativeLayout netLiveLayout;
	ListView netLiveListView;
	private NetPlayDataListAdapter netPlayDataListAdapter;
	private ImageButton netLiveCancel;

	private void initNetLiveLayout() {
		netLiveLayout = (RelativeLayout) findViewById(R.id.netlive_layout);
		netLiveListView = (ListView) findViewById(R.id.nettvListView);
		netLiveListView.setOnItemClickListener(netLiveItemClickListener);
		netLiveCancel = (ImageButton) findViewById(R.id.cancelnetTv);
		netLiveCancel.setOnClickListener(cancelNetLiveListener);
		netLiveLayout.setOnClickListener(cancelNetLiveListener);
	}

	OnClickListener cancelNetLiveListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			netLiveLayout.setVisibility(View.GONE);
		}
	};

	OnItemClickListener netLiveItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			netLiveLayout.setVisibility(View.GONE);
			String url = netPlayDataListAdapter.getItem(position).url;
			String videoPath = netPlayDataListAdapter.getItem(position).videoPath;
			if (videoPath != null && !videoPath.equals("")) {
				Intent intent = new Intent(ProgramNewActivity.this,
						NewLivePlayerActivity.class);
				intent.putExtra("path", videoPath);
				intent.putExtra("playType", 2);
				String title = vpd.name;
				intent.putExtra("title", title);
				startActivity(intent);
			} else {
				openNetLiveActivity(url, videoPath, 1, vpd.name);
			}
		}
	};

	private void openNetLiveActivity(String url, String videoPath, int isLive,
			String title) {
		// Intent intent = new Intent(this, WebPlayActivity.class);

		startActivity(new Intent(this, MainWebPlayBlockWaitActivity.class));
		Intent intent = new Intent(this, MainWebPlayActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("videoPath", videoPath);
		intent.putExtra("playType", isLive);
		intent.putExtra("title", title);
		startActivity(intent);
	}

	// 表示网页播放的是直播还是点播
	private int isLive;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (netLiveLayout.isShown()) {
				netLiveLayout.setVisibility(View.GONE);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
