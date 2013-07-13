package com.sumavision.talktv2.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.adapter.NetPlayDataListAdapter;
import com.sumavision.talktv2.data.ChannelData;
import com.sumavision.talktv2.data.ChannelNewData;
import com.sumavision.talktv2.data.CpData;
import com.sumavision.talktv2.data.NetPlayData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.task.AddRemindTask;
import com.sumavision.talktv2.task.DeleteRemindTask;
import com.sumavision.talktv2.task.GetChannelDetailTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;

public class ChannelDetailActivity extends Activity implements
		OnPageChangeListener, NetConnectionListenerNew, OnClickListener,
		OnItemClickListener {

	private int channelId;
	private String channelName;
	// 仅标记今天正在直播的位置
	private int todayNowPlayingPosition = ChannelNewData.current.nowPlayingItemPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_detail);
		getExtras();
		initViews();
		setListeners();
		channelData = new ChannelData();
		channelData.channelName = channelName;
		int value = setWeekText();
		today = value;
		viewPager.setCurrentItem(value);
		if (today == 0) {
			from = FROM_FIRST;
			getChannelData(UserNow.current().userID, channelId,
					getDate(FROM_FIRST), channelData, temp, getPlayType(from));
		}
	}

	private void getExtras() {
		Intent intent = getIntent();
		channelId = intent.getIntExtra("channelId", -1);
		if (intent.hasExtra("tvName")) {
			channelName = intent.getStringExtra("tvName");
		}
	}

	/*
	 * 表示当前的推荐标签位置
	 */
	private int tagPosition;
	private LinearLayout tagLayout;
	// 编辑按钮
	private TextView week1, week2, week3, week4, week5, week6, week7;

	private int setWeekText() {
		int returnValue = 0;
		switch (new Date().getDay()) {
		case 0:
			week1.setText(Constants.today);
			week2.setText(Constants.week1);
			week3.setText(Constants.week2);
			week4.setText(Constants.week3);
			week5.setText(Constants.week4);
			week6.setText(Constants.week5);
			week7.setText(Constants.week6);
			returnValue = 0;
			break;
		case 1:
			week1.setText(Constants.week7);
			week2.setText(Constants.today);
			week3.setText(Constants.week2);
			week4.setText(Constants.week3);
			week5.setText(Constants.week4);
			week6.setText(Constants.week5);
			week7.setText(Constants.week6);
			returnValue = 1;
			break;
		case 2:
			week1.setText(Constants.week7);
			week2.setText(Constants.week1);
			week3.setText(Constants.today);
			week4.setText(Constants.week3);
			week5.setText(Constants.week4);
			week6.setText(Constants.week5);
			week7.setText(Constants.week6);
			returnValue = 2;
			break;
		case 3:
			week1.setText(Constants.week7);
			week2.setText(Constants.week1);
			week3.setText(Constants.week2);
			week4.setText(Constants.today);
			week5.setText(Constants.week4);
			week6.setText(Constants.week5);
			week7.setText(Constants.week6);
			returnValue = 3;
			break;
		case 4:
			week1.setText(Constants.week7);
			week2.setText(Constants.week1);
			week3.setText(Constants.week2);
			week4.setText(Constants.week3);
			week5.setText(Constants.today);
			week6.setText(Constants.week5);
			week7.setText(Constants.week6);
			returnValue = 4;
			break;
		case 5:
			week1.setText(Constants.week7);
			week2.setText(Constants.week1);
			week3.setText(Constants.week2);
			week4.setText(Constants.week3);
			week5.setText(Constants.week4);
			week6.setText(Constants.today);
			week7.setText(Constants.week6);
			returnValue = 5;
			break;
		case 6:
			week1.setText(Constants.week7);
			week2.setText(Constants.week1);
			week3.setText(Constants.week2);
			week4.setText(Constants.week3);
			week5.setText(Constants.week4);
			week6.setText(Constants.week5);
			week7.setText(Constants.today);
			returnValue = 6;
			break;
		default:
			break;
		}
		return returnValue;
	}

	private String getDate(int position) {
		Date date = new Date();
		int dayOfWeek = date.getDay();
		Date dateNew = new Date(date.getTime() - (dayOfWeek - position) * 3600
				* 24 * 1000);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(dateNew);
	}

	private void initViews() {

		tagLayout = (LinearLayout) findViewById(R.id.tag_layout);
		week1 = (TextView) findViewById(R.id.tag1);
		week2 = (TextView) findViewById(R.id.tag2);
		week3 = (TextView) findViewById(R.id.tag3);
		week4 = (TextView) findViewById(R.id.tag4);
		week5 = (TextView) findViewById(R.id.tag5);
		week6 = (TextView) findViewById(R.id.tag6);
		week7 = (TextView) findViewById(R.id.tag7);
		week1.setTag(FROM_FIRST);
		week2.setTag(FROM_SECOND);
		week3.setTag(FROM_THIRD);
		week4.setTag(FROM_FOUTH);
		week5.setTag(FROM_FIVE);
		week6.setTag(FROM_SIX);
		week7.setTag(FROM_SEVEN);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setOnPageChangeListener(this);
		initViewPager();
		initNetLiveLayout();
		if (channelName != null)
			((TextView) findViewById(R.id.title)).setText(channelName);
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
	}

	private void setListeners() {
		week1.setOnClickListener(this);
		week2.setOnClickListener(this);
		week3.setOnClickListener(this);
		week4.setOnClickListener(this);
		week5.setOnClickListener(this);
		week6.setOnClickListener(this);
		week7.setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
	}

	private ViewPager viewPager;

	private void initViewPager() {
		ArrayList<View> views = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View firstView = inflater.inflate(
				R.layout.channel_detail_viewpager_item, null);

		View secondView = inflater.inflate(
				R.layout.channel_detail_viewpager_item, null);
		View thirdView = inflater.inflate(
				R.layout.channel_detail_viewpager_item, null);
		View fouthView = inflater.inflate(
				R.layout.channel_detail_viewpager_item, null);
		View fiveView = inflater.inflate(
				R.layout.channel_detail_viewpager_item, null);
		View sixView = inflater.inflate(R.layout.channel_detail_viewpager_item,
				null);
		View sevenView = inflater.inflate(
				R.layout.channel_detail_viewpager_item, null);
		init1List(firstView);
		init2List(secondView);
		init3List(thirdView);
		init4List(fouthView);
		init5List(fiveView);
		init6List(sixView);
		init7List(sevenView);
		views.add(firstView);
		views.add(secondView);
		views.add(thirdView);
		views.add(fouthView);
		views.add(fiveView);
		views.add(sixView);
		views.add(sevenView);

		AwesomeAdapter adapter = new AwesomeAdapter(views);
		viewPager.setAdapter(adapter);

	}

	private TextView firstErrText;
	private ProgressBar firstProgressBar;
	private ListView firstListView;

	private void init1List(View view) {
		firstListView = (ListView) view.findViewById(R.id.listView);
		firstListView.setTag(FROM_FIRST);
		firstListView.setOnItemClickListener(this);
		firstErrText = (TextView) view.findViewById(R.id.err_text);
		firstErrText.setOnClickListener(errOnClickListener);
		firstErrText.setTag(1);
		firstProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	}

	private TextView secondErrText;
	private ProgressBar secondProgressBar;
	private ListView secondListView;

	private void init2List(View view) {
		secondListView = (ListView) view.findViewById(R.id.listView);
		secondListView.setTag(FROM_SECOND);
		secondListView.setOnItemClickListener(this);
		secondErrText = (TextView) view.findViewById(R.id.err_text);
		secondErrText.setOnClickListener(errOnClickListener);
		secondErrText.setTag(1);
		secondProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	}

	private TextView thirdErrText;
	private ProgressBar thirdProgressBar;
	private ListView thirdListView;

	private void init3List(View view) {
		thirdListView = (ListView) view.findViewById(R.id.listView);
		thirdListView.setTag(FROM_THIRD);
		thirdListView.setOnItemClickListener(this);
		thirdErrText = (TextView) view.findViewById(R.id.err_text);
		thirdErrText.setOnClickListener(errOnClickListener);
		thirdErrText.setTag(1);
		thirdProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	}

	private TextView fouthErrText;
	private ProgressBar fouthProgressBar;
	private ListView fouthListView;

	private void init4List(View view) {
		fouthListView = (ListView) view.findViewById(R.id.listView);
		fouthListView.setTag(FROM_FOUTH);
		fouthListView.setOnItemClickListener(this);
		fouthErrText = (TextView) view.findViewById(R.id.err_text);
		fouthErrText.setOnClickListener(errOnClickListener);
		fouthErrText.setTag(1);
		fouthProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	}

	private TextView fiveErrText;
	private ProgressBar fiveProgressBar;
	private ListView fiveListView;

	private void init5List(View view) {
		fiveListView = (ListView) view.findViewById(R.id.listView);
		fiveListView.setTag(FROM_FIVE);
		fiveListView.setOnItemClickListener(this);
		fiveErrText = (TextView) view.findViewById(R.id.err_text);
		fiveErrText.setOnClickListener(errOnClickListener);
		fiveErrText.setTag(1);
		fiveProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	}

	private TextView sixErrText;
	private ProgressBar sixProgressBar;
	private ListView sixListView;

	private void init6List(View view) {
		sixListView = (ListView) view.findViewById(R.id.listView);
		sixListView.setTag(FROM_SIX);
		sixListView.setOnItemClickListener(this);
		sixErrText = (TextView) view.findViewById(R.id.err_text);
		sixErrText.setOnClickListener(errOnClickListener);
		sixErrText.setTag(1);
		sixProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	}

	private TextView sevenErrText;
	private ProgressBar sevenProgressBar;
	private ListView sevenListView;

	private void init7List(View view) {
		sevenListView = (ListView) view.findViewById(R.id.listView);
		sevenListView.setTag(FROM_SEVEN);
		sevenListView.setOnItemClickListener(this);
		sevenErrText = (TextView) view.findViewById(R.id.err_text);
		sevenErrText.setOnClickListener(errOnClickListener);
		sevenErrText.setTag(1);
		sevenProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	}

	/**
	 * 当标签切换时执行
	 */
	private void onTagSelected(int position, boolean fromViewPager) {
		if (tagPosition != position) {
			tagPosition = position;
			for (int i = 0; i < 7; ++i) {
				TextView textView = (TextView) tagLayout.findViewWithTag(i);
				if (tagPosition == i) {

					textView.setTextColor(getResources()
							.getColor(R.color.white));
					textView.setBackgroundResource(R.drawable.recommand_tag_bg);
				} else {
					textView.setTextColor(getResources().getColor(
							R.color.tag_default));
					textView.setBackgroundDrawable(null);
				}
			}
		}
		if (!fromViewPager) {
			viewPager.setCurrentItem(position);
		}
	}

	private OnClickListener errOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch ((Integer) v.getTag()) {
			case 1:
				break;
			case 2:
				break;
			default:
				break;
			}
		}

	};

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
			NetPlayData temp = netPlayDataListAdapter.getItem(position);
			String url = temp.url;
			String videoPath = temp.videoPath;
			String channelName = temp.channelName;
			if (videoPath != null && !videoPath.equals("")) {
				Intent intent = new Intent(ChannelDetailActivity.this,
						NewLivePlayerActivity.class);
				intent.putExtra("path", videoPath);
				intent.putExtra("playType", 1);
				intent.putExtra("title", channelName);
				startActivity(intent);
			} else {
				openNetLiveActivity(url, videoPath, 1, channelName);
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

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		onTagSelected(arg0, true);
		processPageNet(arg0);

	}

	private ArrayList<CpData> firstList = new ArrayList<CpData>();
	private ArrayList<CpData> secondList = new ArrayList<CpData>();
	private ArrayList<CpData> thirdList = new ArrayList<CpData>();
	private ArrayList<CpData> fouthList = new ArrayList<CpData>();
	private ArrayList<CpData> fiveList = new ArrayList<CpData>();
	private ArrayList<CpData> sixList = new ArrayList<CpData>();
	private ArrayList<CpData> sevenList = new ArrayList<CpData>();
	private ArrayList<CpData> temp = new ArrayList<CpData>();
	private int today;

	private int getPlayType(int from) {
		if (from > today) {
			return 1;
		} else if (from == today) {
			return 0;
		} else {
			return -1;
		}
	}

	private void processPageNet(int arg0) {
		switch (arg0) {
		case FROM_FIRST:
			if (firstList.size() == 0) {
				from = FROM_FIRST;

				getChannelData(UserNow.current().userID, channelId,
						getDate(FROM_FIRST), channelData, temp,
						getPlayType(from));
			}
			break;
		case FROM_SECOND:
			if (secondList.size() == 0) {
				from = FROM_SECOND;
				getChannelData(UserNow.current().userID, channelId,
						getDate(FROM_SECOND), channelData, temp,
						getPlayType(from));
			}
			break;
		case FROM_THIRD:
			if (thirdList.size() == 0) {
				from = FROM_THIRD;
				getChannelData(UserNow.current().userID, channelId,
						getDate(FROM_THIRD), channelData, temp,
						getPlayType(from));
			}
			break;
		case FROM_FOUTH:
			if (fouthList.size() == 0) {
				from = FROM_FOUTH;
				getChannelData(UserNow.current().userID, channelId,
						getDate(FROM_FOUTH), channelData, temp,
						getPlayType(from));
			}
			break;
		case FROM_FIVE:
			if (fiveList.size() == 0) {
				from = FROM_FIVE;
				getChannelData(UserNow.current().userID, channelId,
						getDate(FROM_FIVE), channelData, temp,
						getPlayType(from));
			}
			break;
		case FROM_SIX:
			if (sixList.size() == 0) {
				from = FROM_SIX;
				getChannelData(UserNow.current().userID, channelId,
						getDate(FROM_SIX), channelData, temp, getPlayType(from));
			}
			break;
		case FROM_SEVEN:
			if (sevenList.size() == 0) {
				from = FROM_SEVEN;
				getChannelData(UserNow.current().userID, channelId,
						getDate(FROM_SEVEN), channelData, temp,
						getPlayType(from));
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onNetBegin(String method, boolean isLoadMore) {
		if (!isLoadMore) {
			if (Constants.remindAdd.equals(method)) {
				showpb();
			} else if (Constants.remindDelete.equals(method)) {
				showpb();
			}
		}
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

	@Override
	public void onNetEnd(int code, String msg, String method, boolean isLoadMore) {
		if (Constants.channelContent.equals(method)) {
			switch (code) {
			case Constants.requestErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "request net");
				showErrorLayout(from);
				break;
			case Constants.fail_no_net:
				// DialogUtil.alertToast(getApplicationContext(), "no net");
				showErrorLayout(from);
				break;
			case Constants.fail_server_err:
				// DialogUtil.alertToast(getApplicationContext(),
				// "server Error");
				showErrorLayout(from);
				break;
			case Constants.parseErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "parse Error");
				showErrorLayout(from);
				break;
			case Constants.sucess:
				// DialogUtil.alertToast(getApplicationContext(), "sucess");
				hideErrorLayout(from);
				updateList(from, isLoadMore);
				break;
			default:
				break;
			}
			// channelProgramTask = null;
			getChannelDetailTask = null;
		} else if (Constants.remindAdd.equals(method)) {
			hidepb();
			switch (code) {
			case Constants.requestErr:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_addRemind);
				break;
			case Constants.fail_no_net:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_addRemind);
				break;
			case Constants.fail_server_err:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_addRemind);
				break;
			case Constants.parseErr:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_addRemind);
				break;
			case Constants.sucess:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_s_addRemind);
				onRemindAdded(remindList, remindPosition);
				remindList = -1;
				remindPosition = -1;
				break;
			default:
				break;
			}
			remindAddTask = null;
		} else if (Constants.remindDelete.equals(method)) {
			hidepb();
			switch (code) {
			case Constants.requestErr:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteRemind);
				break;
			case Constants.fail_no_net:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteRemind);
				break;
			case Constants.fail_server_err:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteRemind);
				break;
			case Constants.parseErr:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteRemind);
				break;
			case Constants.sucess:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_s_deleteRemind);
				onRemindDeleted(remindList, remindPosition);
				remindList = -1;
				remindPosition = -1;
				break;
			default:
				break;
			}
			deleteRemindTask = null;
		}
	}

	// 表示发出预约请求的listView和项目所在位置
	private int remindList;

	private int remindPosition;
	private AddRemindTask remindAddTask;

	private void addRemind(int userId, int programId) {
		if (remindAddTask == null) {
			remindAddTask = new AddRemindTask(this, false);
			remindAddTask.execute(this, userId, programId);
		}
	}

	private void onRemindAdded(int remindList, int remindPosition) {
		switch (from) {
		case FROM_FIRST:
			CpData cpData = firstChannelListAdapter.getItem(remindPosition);
			cpData.order = 1;
			firstChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_SECOND:
			CpData cpData2 = secondChannelListAdapter.getItem(remindPosition);
			cpData2.order = 1;
			secondChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_THIRD:
			CpData cpData3 = thirdChannelListAdapter.getItem(remindPosition);
			cpData3.order = 1;
			thirdChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_FOUTH:
			CpData cpData4 = fouthChannelListAdapter.getItem(remindPosition);
			cpData4.order = 1;
			fouthChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_FIVE:
			CpData cpData5 = fiveChannelListAdapter.getItem(remindPosition);
			cpData5.order = 1;
			fiveChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_SIX:
			CpData cpData6 = sixChannelListAdapter.getItem(remindPosition);
			cpData6.order = 1;
			sixChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_SEVEN:
			CpData cpData7 = sevenChannelListAdapter.getItem(remindPosition);
			cpData7.order = 1;
			sevenChannelListAdapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
	}

	private DeleteRemindTask deleteRemindTask;

	private void deleteRemind(int userId, int cpId) {
		if (deleteRemindTask == null) {
			deleteRemindTask = new DeleteRemindTask(this, false);
			deleteRemindTask.execute(this, userId, cpId);
		}
	}

	private void onRemindDeleted(int remindList, int remindPosition) {
		switch (from) {
		case FROM_FIRST:
			CpData cpData = firstChannelListAdapter.getItem(remindPosition);
			cpData.order = 0;
			firstChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_SECOND:
			CpData cpData2 = secondChannelListAdapter.getItem(remindPosition);
			cpData2.order = 0;
			secondChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_THIRD:
			CpData cpData3 = thirdChannelListAdapter.getItem(remindPosition);
			cpData3.order = 0;
			thirdChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_FOUTH:
			CpData cpData4 = fouthChannelListAdapter.getItem(remindPosition);
			cpData4.order = 0;
			fouthChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_FIVE:
			CpData cpData5 = fiveChannelListAdapter.getItem(remindPosition);
			cpData5.order = 0;
			fiveChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_SIX:
			CpData cpData6 = sixChannelListAdapter.getItem(remindPosition);
			cpData6.order = 0;
			sixChannelListAdapter.notifyDataSetChanged();
			break;
		case FROM_SEVEN:
			CpData cpData7 = sevenChannelListAdapter.getItem(remindPosition);
			cpData7.order = 0;
			sevenChannelListAdapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
	}

	private RelativeLayout connectBg;

	private void hidepb() {
		if (connectBg.isShown())
			connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		if (!connectBg.isShown())
			connectBg.setVisibility(View.VISIBLE);
	}

	private int from;
	private static final int FROM_FIRST = 0;
	private static final int FROM_SECOND = 1;
	private static final int FROM_THIRD = 2;
	private static final int FROM_FOUTH = 3;
	private static final int FROM_FIVE = 4;
	private static final int FROM_SIX = 5;
	private static final int FROM_SEVEN = 6;

	private void hideErrorLayout(int from) {
		switch (from) {
		case FROM_FIRST:
			firstProgressBar.setVisibility(View.GONE);
			firstErrText.setVisibility(View.GONE);
			break;
		case FROM_SECOND:
			secondProgressBar.setVisibility(View.GONE);
			secondErrText.setVisibility(View.GONE);
			break;
		case FROM_THIRD:
			thirdProgressBar.setVisibility(View.GONE);
			thirdErrText.setVisibility(View.GONE);
			break;
		case FROM_FOUTH:
			fouthProgressBar.setVisibility(View.GONE);
			fouthErrText.setVisibility(View.GONE);
			break;
		case FROM_FIVE:
			fiveProgressBar.setVisibility(View.GONE);
			fiveErrText.setVisibility(View.GONE);
			break;
		case FROM_SIX:
			sixProgressBar.setVisibility(View.GONE);
			sixErrText.setVisibility(View.GONE);
			break;
		case FROM_SEVEN:
			sevenProgressBar.setVisibility(View.GONE);
			sevenErrText.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	private void showErrorLayout(int from) {
		switch (from) {
		case FROM_FIRST:
			firstProgressBar.setVisibility(View.GONE);
			firstErrText.setVisibility(View.VISIBLE);
			firstErrText.setText(Constants.errText);
			break;
		case FROM_SECOND:
			secondProgressBar.setVisibility(View.GONE);
			secondErrText.setVisibility(View.VISIBLE);
			secondErrText.setText(Constants.errText);
			break;
		case FROM_THIRD:
			thirdProgressBar.setVisibility(View.GONE);
			thirdErrText.setVisibility(View.VISIBLE);
			thirdErrText.setText(Constants.errText);
			break;
		case FROM_FOUTH:
			fouthProgressBar.setVisibility(View.GONE);
			fouthErrText.setVisibility(View.VISIBLE);
			fouthErrText.setText(Constants.errText);
			break;
		case FROM_FIVE:
			fiveProgressBar.setVisibility(View.GONE);
			fiveErrText.setVisibility(View.VISIBLE);
			fiveErrText.setText(Constants.errText);
			break;
		case FROM_SIX:
			sixProgressBar.setVisibility(View.GONE);
			sixErrText.setVisibility(View.VISIBLE);
			sixErrText.setText(Constants.errText);
			break;
		case FROM_SEVEN:
			sevenProgressBar.setVisibility(View.GONE);
			sevenErrText.setVisibility(View.VISIBLE);
			sevenErrText.setText(Constants.errText);
			break;
		default:
			break;
		}
	}

	private void showLoadingLayout(int from) {
		switch (from) {
		case FROM_FIRST:
			firstProgressBar.setVisibility(View.VISIBLE);
			firstErrText.setVisibility(View.GONE);
			break;
		case FROM_SECOND:
			secondProgressBar.setVisibility(View.VISIBLE);
			secondErrText.setVisibility(View.GONE);
			break;
		case FROM_THIRD:
			thirdProgressBar.setVisibility(View.VISIBLE);
			thirdErrText.setVisibility(View.GONE);
			break;
		case FROM_FOUTH:
			fouthProgressBar.setVisibility(View.VISIBLE);
			fouthErrText.setVisibility(View.GONE);
			break;
		case FROM_FIVE:
			fiveProgressBar.setVisibility(View.VISIBLE);
			fiveErrText.setVisibility(View.GONE);
			break;
		case FROM_SIX:
			sixProgressBar.setVisibility(View.VISIBLE);
			sixErrText.setVisibility(View.GONE);
			break;
		case FROM_SEVEN:
			sevenProgressBar.setVisibility(View.GONE);
			sevenErrText.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tag1:
			onTagSelected(0, false);
			break;
		case R.id.tag2:
			onTagSelected(1, false);
			break;
		case R.id.tag3:
			onTagSelected(2, false);
			break;
		case R.id.tag4:
			onTagSelected(3, false);
			break;
		case R.id.tag5:
			onTagSelected(4, false);
			break;
		case R.id.tag6:
			onTagSelected(5, false);
			break;
		case R.id.tag7:
			onTagSelected(6, false);
			break;
		case R.id.back:
			finish();
			break;
		default:
			break;
		}
	}

	private ChannelData channelData;
	private GetChannelDetailTask getChannelDetailTask;

	private void getChannelData(int userId, int channelId, String date,
			ChannelData channelData, ArrayList<CpData> cpData, int isToday) {
		if (getChannelDetailTask == null) {
			getChannelDetailTask = new GetChannelDetailTask(this, false);
			getChannelDetailTask.execute(this, userId, channelId, date,
					channelData, cpData, isToday);
		}
	}

	private void updateList(int from, boolean isLoadMore) {
		if (OtherCacheData.current().isDebugMode)
			Log.e("updateList-today", today + "\n" + todayNowPlayingPosition);

		switch (from) {
		case FROM_FIRST:
			if (temp != null && temp.size() > 0) {
				firstList.addAll(temp);
				firstChannelListAdapter = new ChannelListAdapter(firstList,
						FROM_FIRST);
				temp.clear();
				firstListView.setAdapter(firstChannelListAdapter);
				if (today == FROM_FIRST)
					firstListView.setSelection(todayNowPlayingPosition);
			}
			break;
		case FROM_SECOND:
			if (temp != null && temp.size() > 0) {
				secondList.addAll(temp);
				secondChannelListAdapter = new ChannelListAdapter(secondList,
						FROM_SECOND);
				temp.clear();
				secondListView.setAdapter(secondChannelListAdapter);
				if (today == FROM_SECOND)
					secondListView.setSelection(todayNowPlayingPosition);
			}
			break;
		case FROM_THIRD:
			if (temp != null && temp.size() > 0) {
				thirdList.addAll(temp);
				thirdChannelListAdapter = new ChannelListAdapter(thirdList,
						FROM_THIRD);
				temp.clear();
				thirdListView.setAdapter(thirdChannelListAdapter);
				if (today == FROM_THIRD)
					thirdListView.setSelection(todayNowPlayingPosition);
			}
			break;
		case FROM_FOUTH:
			if (temp != null && temp.size() > 0) {
				fouthList.addAll(temp);
				fouthChannelListAdapter = new ChannelListAdapter(fouthList,
						FROM_FOUTH);
				temp.clear();
				fouthListView.setAdapter(fouthChannelListAdapter);
				if (today == FROM_FOUTH)
					fouthListView.setSelection(todayNowPlayingPosition);
			}
			break;
		case FROM_FIVE:
			if (temp != null && temp.size() > 0) {
				fiveList.addAll(temp);
				fiveChannelListAdapter = new ChannelListAdapter(fiveList,
						FROM_FIVE);
				temp.clear();
				fiveListView.setAdapter(fiveChannelListAdapter);
				if (today == FROM_FIVE)
					fiveListView.setSelection(todayNowPlayingPosition);
			}
			break;
		case FROM_SIX:
			if (temp != null && temp.size() > 0) {
				sixList.addAll(temp);
				sixChannelListAdapter = new ChannelListAdapter(sixList,
						FROM_SIX);
				temp.clear();
				sixListView.setAdapter(sixChannelListAdapter);
				if (today == FROM_SIX)
					sixListView.setSelection(todayNowPlayingPosition);
			}
			break;
		case FROM_SEVEN:
			if (temp != null && temp.size() > 0) {
				sevenList.addAll(temp);
				sevenChannelListAdapter = new ChannelListAdapter(sevenList,
						FROM_SEVEN);
				temp.clear();
				sevenListView.setAdapter(sevenChannelListAdapter);
				if (today == FROM_SEVEN)
					sevenListView.setSelection(todayNowPlayingPosition);
			}
			break;
		default:
			break;
		}
	}

	private class ChannelListAdapter extends BaseAdapter {
		private ArrayList<CpData> list;
		private int flag;

		public ChannelListAdapter(ArrayList<CpData> list, int flag) {
			this.list = list;
			this.flag = flag;
		}

		@Override
		public int getCount() {
			if (list == null) {
				return 0;
			} else {
				return list.size();
			}
		}

		@Override
		public CpData getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(ChannelDetailActivity.this);
				convertView = inflater.inflate(
						R.layout.channel_detail_list_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);
				viewHolder.liveBtn = (ImageView) convertView
						.findViewById(R.id.liveBtn);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			CpData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}

			String startTime = temp.startTime;
			String endTime = temp.endTime;
			if (startTime != null && endTime != null) {
				viewHolder.time.setVisibility(View.VISIBLE);
			} else {
				viewHolder.time.setVisibility(View.GONE);
			}

			boolean live = false;
			switch (temp.isPlaying) {
			case 0:
				viewHolder.liveBtn
						.setImageResource(R.drawable.channel_live_btn);
				viewHolder.liveBtn.setVisibility(View.VISIBLE);
				SpannableString sp2 = new SpannableString(startTime + "-"
						+ endTime);
				sp2.setSpan(
						new ForegroundColorSpan(Color.argb(0xff, 0xc2, 0x4d,
								0x37)), 0, 5,
						Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				viewHolder.time.setText(sp2);
				viewHolder.nameTxt.setTextColor(Color.BLACK);
				live = true;
				break;
			case 1:
				viewHolder.liveBtn.setVisibility(View.GONE);
				viewHolder.nameTxt.setTextColor(Color.argb(0xff, 0x99, 0x99,
						0x99));
				SpannableString sp1 = new SpannableString(startTime + "-"
						+ endTime);
				sp1.setSpan(
						new ForegroundColorSpan(Color.argb(0xff, 0x99, 0x99,
								0x99)), 0, sp1.length(),
						Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				viewHolder.time.setText(sp1);
				break;
			case 2:
				SpannableString sp = new SpannableString(startTime + "-"
						+ endTime);
				sp.setSpan(
						new ForegroundColorSpan(Color.argb(0xff, 0xc2, 0x4d,
								0x37)), 0, 5,
						Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				viewHolder.time.setText(sp);
				viewHolder.nameTxt.setTextColor(Color.BLACK);
				if (temp.programId != null && !temp.programId.equals("0")) {
					viewHolder.liveBtn.setVisibility(View.VISIBLE);
				} else {
					viewHolder.liveBtn.setVisibility(View.GONE);
				}
				if (temp.order == 1) {
					viewHolder.liveBtn
							.setImageResource(R.drawable.channel_cancel_book);
				} else {
					viewHolder.liveBtn
							.setImageResource(R.drawable.channel_book_btn);
				}
				break;
			}
			final boolean isLive = live;
			final int mPosition = position;
			viewHolder.liveBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onLiveBtnClick(mPosition, flag, isLive);
				}
			});

			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public TextView time;
			// 直播或者预约按钮
			public ImageView liveBtn;
		}

	}

	private ChannelListAdapter firstChannelListAdapter;
	private ChannelListAdapter secondChannelListAdapter;
	private ChannelListAdapter thirdChannelListAdapter;
	private ChannelListAdapter fouthChannelListAdapter;
	private ChannelListAdapter fiveChannelListAdapter;
	private ChannelListAdapter sixChannelListAdapter;
	private ChannelListAdapter sevenChannelListAdapter;

	/**
	 * 
	 * @param position
	 *            listPosition
	 * @param from
	 *            表示来自哪个listView
	 */
	private void onLiveBtnClick(int position, int from, boolean liveFlag) {
		this.from = from;
		switch (from) {
		case FROM_FIRST:
			CpData cpData = firstChannelListAdapter.getItem(position);
			if (liveFlag) {
				liveThroughNet(channelData);
			} else {
				// 预约或者取消预约
				if (UserNow.current().userID == 0) {
					openLogInActivity();
				} else {
					if (cpData.order == 1) {
						remindPosition = position;
						deleteRemind(UserNow.current().userID, cpData.id);
					} else {
						remindPosition = position;
						addRemind(UserNow.current().userID, cpData.id);
					}
				}
			}
			break;
		case FROM_SECOND:
			CpData cpData2 = secondChannelListAdapter.getItem(position);
			if (liveFlag) {
				liveThroughNet(channelData);
			} else {
				// 预约或者取消预约
				if (UserNow.current().userID == 0) {
					openLogInActivity();
				} else {
					if (cpData2.order == 1) {
						remindPosition = position;
						deleteRemind(UserNow.current().userID, cpData2.id);
					} else {
						remindPosition = position;
						addRemind(UserNow.current().userID, cpData2.id);
					}
				}
			}
			break;
		case FROM_THIRD:
			CpData cpData3 = thirdChannelListAdapter.getItem(position);
			if (liveFlag) {
				liveThroughNet(channelData);
			} else {
				// 预约或者取消预约
				if (UserNow.current().userID == 0) {
					openLogInActivity();
				} else {
					if (cpData3.order == 1) {
						remindList = 0;
						remindPosition = position;
						deleteRemind(UserNow.current().userID, cpData3.id);
					} else {
						remindList = 0;
						remindPosition = position;
						addRemind(UserNow.current().userID, cpData3.id);
					}
				}
			}
			break;
		case FROM_FOUTH:
			CpData cpData4 = fouthChannelListAdapter.getItem(position);
			if (liveFlag) {
				liveThroughNet(channelData);
			} else {
				if (UserNow.current().userID == 0) {
					openLogInActivity();
				} else {
					// 预约或者取消预约
					if (cpData4.order == 1) {
						remindList = 0;
						remindPosition = position;
						deleteRemind(UserNow.current().userID, cpData4.id);
					} else {
						remindList = 0;
						remindPosition = position;
						addRemind(UserNow.current().userID, cpData4.id);
					}
				}
			}
			break;
		case FROM_FIVE:
			CpData cpData5 = fiveChannelListAdapter.getItem(position);
			if (liveFlag) {
				liveThroughNet(channelData);
			} else {
				if (UserNow.current().userID == 0) {
					openLogInActivity();
				} else {
					// 预约或者取消预约
					if (cpData5.order == 1) {
						remindList = 0;
						remindPosition = position;
						deleteRemind(UserNow.current().userID, cpData5.id);
					} else {
						remindList = 0;
						remindPosition = position;
						addRemind(UserNow.current().userID, cpData5.id);
					}
				}
			}
			break;
		case FROM_SIX:
			CpData cpData6 = sixChannelListAdapter.getItem(position);
			if (liveFlag) {
				liveThroughNet(channelData);
			} else {
				if (UserNow.current().userID == 0) {
					openLogInActivity();
				} else {
					// 预约或者取消预约
					if (cpData6.order == 1) {
						remindList = 0;
						remindPosition = position;
						deleteRemind(UserNow.current().userID, cpData6.id);
					} else {
						remindList = 0;
						remindPosition = position;
						addRemind(UserNow.current().userID, cpData6.id);
					}
				}
			}
			break;
		case FROM_SEVEN:
			CpData cpData7 = sevenChannelListAdapter.getItem(position);
			if (liveFlag) {
				liveThroughNet(channelData);
			} else {
				if (UserNow.current().userID == 0) {
					openLogInActivity();
				} else {
					// 预约或者取消预约
					if (cpData7.order == 1) {
						remindList = 0;
						remindPosition = position;
						deleteRemind(UserNow.current().userID, cpData7.id);
					} else {
						remindList = 0;
						remindPosition = position;
						addRemind(UserNow.current().userID, cpData7.id);
					}
				}
			}
			break;
		default:
			break;
		}
	}

	private void liveThroughNet(ChannelData shortChannelData) {
		ArrayList<NetPlayData> netPlayDatas = shortChannelData.netPlayDatas;
		if (netPlayDatas != null) {
			if (netPlayDatas.size() == 1) {
				NetPlayData tempData = netPlayDatas.get(0);
				String url = tempData.url;
				String videoPath = tempData.videoPath;
				if (tempData.videoPath != null
						&& !tempData.videoPath.equals("")) {

					Intent intent = new Intent(this,
							NewLivePlayerActivity.class);
					intent.putExtra("path", videoPath);
					intent.putExtra("playType", 1);
					String title = shortChannelData.channelName;
					intent.putExtra("title", title);
					startActivity(intent);
				} else if (tempData.url != null) {
					openNetLiveActivity(url, videoPath, 1,
							shortChannelData.channelName);
				}
			} else {
				netLiveLayout.setVisibility(View.VISIBLE);
				netPlayDataListAdapter = new NetPlayDataListAdapter(this,
						netPlayDatas);
				netLiveListView.setAdapter(netPlayDataListAdapter);
			}
		} else {
			DialogUtil.alertToast(getApplicationContext(), "暂时无法播放");
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		int from = (Integer) arg0.getTag();
		CpData cp;
		switch (from) {
		case FROM_FIRST:
			cp = firstChannelListAdapter.getItem(arg2);
			break;
		case FROM_SECOND:
			cp = secondChannelListAdapter.getItem(arg2);
			break;
		case FROM_THIRD:
			cp = thirdChannelListAdapter.getItem(arg2);
			break;
		case FROM_FOUTH:
			cp = fouthChannelListAdapter.getItem(arg2);
			break;
		case FROM_FIVE:
			cp = fiveChannelListAdapter.getItem(arg2);
			break;
		case FROM_SIX:
			cp = sixChannelListAdapter.getItem(arg2);
			break;
		case FROM_SEVEN:
			cp = sevenChannelListAdapter.getItem(arg2);
			break;
		default:
			cp = new CpData();
			break;
		}
		if (cp.id == 0 || cp.topicId == null || cp.topicId.equals("")
				|| Integer.parseInt(cp.topicId) == 0) {
			Toast.makeText(getApplicationContext(), "暂无此节目相关信息",
					Toast.LENGTH_SHORT).show();
		} else {

			Intent intent = new Intent(this, ProgramNewActivity.class);
			intent.putExtra("programId", cp.programId);
			intent.putExtra("topicId", cp.topicId);
			intent.putExtra("cpId", Long.parseLong(cp.id + ""));
			startActivity(intent);

		}
	}

	private void openLogInActivity() {
		startActivity(new Intent(this, LoginActivity.class));
	}

}
