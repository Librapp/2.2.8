package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.BadgeData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.PlayNewData;
import com.sumavision.talktv2.net.ActivityListParser;
import com.sumavision.talktv2.net.ActivityListRequest;
import com.sumavision.talktv2.net.BadgeParser;
import com.sumavision.talktv2.net.BadgeRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.task.GetActivityListTask;
import com.sumavision.talktv2.task.GetMyBadgeTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class ActivititesActivity extends Activity implements OnClickListener,
		OnPageChangeListener, NetConnectionListener, OnRefreshListener,
		OnItemClickListener {

	// 来自用户中心
	public static int fromMy = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activities);
		initOthers();
		initViews();
		setListeners();
	}

	private ImageLoaderHelper imageLoaderHelper;

	private void initOthers() {
		OtherCacheData.current().offset = 0;
		OtherCacheData.current().pageCount = 10;
		imageLoaderHelper = new ImageLoaderHelper();
	}

	private void setListeners() {
		allTagTextView.setOnClickListener(this);
		myTagTextView.setOnClickListener(this);
		allListView.setOnRefreshListener(this);
		viewPager.setOnPageChangeListener(this);
		myListView.setOnRefreshListener(myListRefreshListener);
		allListView.setTag(0);
		allListView.setOnItemClickListener(this);
		myListView.setTag(1);
		myListView.setOnItemClickListener(this);
	}

	private void initViews() {

		tagLayout = (LinearLayout) findViewById(R.id.tag_layout);
		allTagTextView = (TextView) findViewById(R.id.all_tag);
		myTagTextView = (TextView) findViewById(R.id.my_tag);
		allTagTextView.setTag(0);
		myTagTextView.setTag(1);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		initViewPager();

		getActivityListData();
	}

	/*
	 * 表示当前的活动页标签位置
	 */
	private int tagPosition;
	private LinearLayout tagLayout;
	private TextView allTagTextView;
	private TextView myTagTextView;

	// 标题

	/**
	 * 当标签切换时执行
	 */
	private void onTagSelected(int position, boolean fromViewPager) {
		if (tagPosition != position) {
			tagPosition = position;
			for (int i = 0; i < 2; ++i) {
				TextView textView = (TextView) tagLayout.findViewWithTag(i);
				if (tagPosition == i) {
					int whiteColor = getResources().getColor(R.color.white);
					textView.setTextColor(whiteColor);
					textView.setBackgroundResource(R.drawable.recommand_tag_bg);
				} else {
					textView.setTextColor(R.color.tag_default);
					textView.setBackgroundDrawable(null);
				}
			}
		}

		if (fromMy == 1) {
			viewPager.setCurrentItem(1);
		} else {

			if (!fromViewPager) {
				viewPager.setCurrentItem(position);
			}
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.all_tag:
			onTagSelected(0, false);
			break;
		case R.id.my_tag:
			onTagSelected(1, false);
			break;
		default:
			break;
		}
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
		switch (arg0) {
		case 0:
			MobclickAgent.onEvent(this, "allbadge");
			if (allList.size() == 0) {
				getActivityListData();
			}
			break;
		case 1:
			MobclickAgent.onEvent(this, "mybadge");
			if (UserNow.current().userID != 0) {
				if (myList.size() == 0) {
					getMyBadgeData();
				}
			} else {
				openLoginActivity(REQUEST_LOGIN_GETMY);
			}
			break;
		default:
			break;
		}
	}

	private void openLoginActivity(int requestCode) {
		Intent intent = new Intent(ActivititesActivity.this,
				LoginActivity.class);
		startActivityForResult(intent, requestCode);
	}

	private ViewPager viewPager;

	private void initViewPager() {
		ArrayList<View> views = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.activities_viewpager_all, null);
		initAllPage(view);

		View myView = inflater.inflate(R.layout.activities_viewpager_my, null);
		initMyPage(myView);
		views.add(view);
		views.add(myView);
		AwesomeAdapter adapter = new AwesomeAdapter(views);
		viewPager.setAdapter(adapter);
	}

	private void initAllPage(View view) {
		allListView = (MyListView) view.findViewById(R.id.listView);
		allErrText = (TextView) view.findViewById(R.id.err_text);
		allProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	}

	private MyListView allListView;
	private TextView allErrText;
	private ProgressBar allProgressBar;
	private ArrayList<PlayNewData> allList = new ArrayList<PlayNewData>();
	private final int REQUEST_LOGIN_GETMY = 1;
	private final int REQUEST_LOGIN_DETAIL = 2;

	private GetActivityListTask getActivityListTask;

	private void getActivityListData() {
		if (getActivityListTask == null) {
			getActivityListTask = new GetActivityListTask(this);
			getActivityListTask.execute(this, new ActivityListRequest(),
					new ActivityListParser());
			if (allList.size() == 0) {
				allErrText.setVisibility(View.GONE);
				allProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getActivityListData(int start, int count) {
		if (getActivityListTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getActivityListTask = new GetActivityListTask(this);
			getActivityListTask.execute(this, new ActivityListRequest());
		}
	}

	MedalListViewAdapter allAdapter;

	private void updateAllListView(ArrayList<PlayNewData> temp) {
		// ArrayList<PlayNewData> temp = (ArrayList<PlayNewData>)
		// VodProgramData.current
		// .getActivity();
		if (temp != null) {
			allList = temp;
			if (allList.size() == 0) {
				allErrText.setText("暂无活动");
				allErrText.setVisibility(View.VISIBLE);
			} else {
				allErrText.setVisibility(View.GONE);
				allAdapter = new MedalListViewAdapter(allList);
				allListView.setAdapter(allAdapter);
			}
		} else {
			allErrText.setVisibility(View.VISIBLE);
		}
		allProgressBar.setVisibility(View.GONE);
	}

	private MyListView myListView;
	private TextView myErrText;
	private ProgressBar myProgressBar;
	private ArrayList<MyMedalListItem> myList = new ArrayList<MyMedalListItem>();

	private void initMyPage(View view) {
		myListView = (MyListView) view.findViewById(R.id.listView);
		myErrText = (TextView) view.findViewById(R.id.err_text);
		myProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

	}

	private final OnRefreshListener myListRefreshListener = new OnRefreshListener() {

		@Override
		public void onRefresh() {
			getMyBadgeData();
		}

		@Override
		public void onLoadingMore() {
			// TODO
			int start = 0;
			int count = myList.size() + 10;
			getMyBadgeData(start, count);
		}
	};

	private GetMyBadgeTask getMyBadgeTask;

	private void getMyBadgeData() {
		if (getMyBadgeTask == null) {
			getMyBadgeTask = new GetMyBadgeTask(this);
			getMyBadgeTask.execute(this, new BadgeRequest(), new BadgeParser(
					this));
			if (myList.size() == 0) {
				myErrText.setVisibility(View.GONE);
				myProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getMyBadgeData(int start, int count) {
		if (getMyBadgeTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getMyBadgeTask = new GetMyBadgeTask(this);
			getMyBadgeTask.execute(this, new BadgeRequest(), new BadgeParser(
					this));
		}
	}

	private MyMedalListAdapter myAdapter;

	private void updateMyListView() {
		ArrayList<BadgeData> temp = (ArrayList<BadgeData>) UserNow.current()
				.getBadgesGained();
		ArrayList<MyMedalListItem> list = convertList(temp);
		if (list != null) {
			myList = list;
			myAdapter = new MyMedalListAdapter(list);
			myListView.setAdapter(myAdapter);
		}
	}

	private ArrayList<MyMedalListItem> convertList(ArrayList<BadgeData> temp) {
		if (temp == null) {
			return null;
		} else if (temp.size() == 0) {
			myErrText.setText("您还没有获得任何勋章");
			myErrText.setVisibility(View.VISIBLE);
			return null;
		} else {
			ArrayList<MyMedalListItem> list = new ArrayList<MyMedalListItem>();
			MyMedalListItem item4 = new MyMedalListItem();
			item4.type = 2;
			list.add(item4);
			MyMedalListItem item = new MyMedalListItem();
			item.titleName = "已获得的勋章";
			item.type = 0;
			list.add(item);
			for (int i = 0; i < temp.size(); i++) {
				BadgeData medalData = temp.get(i);
				MyMedalListItem item2 = new MyMedalListItem();
				item2.type = 1;
				item2.medalData = medalData;
				list.add(item2);
			}
			return list;
		}
	}

	private class MedalListViewAdapter extends BaseAdapter {

		private final ArrayList<PlayNewData> list;

		public MedalListViewAdapter(ArrayList<PlayNewData> list) {
			this.list = list;
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
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(ActivititesActivity.this);
				convertView = inflater.inflate(
						R.layout.activities_medal_list_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.medalPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.statusPic = (ImageView) convertView
						.findViewById(R.id.status);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			PlayNewData temp = list.get(position);

			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}

			String intro = temp.intro;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			String url = temp.pic;
			viewHolder.medalPic.setTag(url);
			imageLoaderHelper.loadImage(viewHolder.medalPic, url,
					R.drawable.medal_default);
			if (temp.state == 2) {
				viewHolder.statusPic
						.setImageResource(R.drawable.activity_doing);
				if (temp.joinStatus == 3)
					viewHolder.statusPic
							.setImageResource(R.drawable.activity_get);
			} else if (temp.state == 3) {
				viewHolder.statusPic
						.setImageResource(R.drawable.activity_finish);
			} else if (temp.state == 1) {
				viewHolder.statusPic
						.setImageResource(R.drawable.activity_notbegin);
			}
			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public TextView introTxt;
			public ImageView medalPic;
			public ImageView statusPic;
		}
	}

	private class MyMedalListAdapter extends BaseAdapter {

		private final ArrayList<MyMedalListItem> list;

		public MyMedalListAdapter(ArrayList<MyMedalListItem> list) {
			this.list = list;
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
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(ActivititesActivity.this);

				if (getItemViewType(position) == 0) {
					convertView = inflater.inflate(
							R.layout.activities_my_list_item_title, null);
					viewHolder.titleTxt = (TextView) convertView
							.findViewById(R.id.textView);
				} else if (getItemViewType(position) == 1) {
					convertView = inflater.inflate(
							R.layout.activities_my_list_item_medal, null);
					viewHolder.nameTxt = (TextView) convertView
							.findViewById(R.id.name);
					viewHolder.introTxt = (TextView) convertView
							.findViewById(R.id.intro);
					viewHolder.medalPic = (ImageView) convertView
							.findViewById(R.id.pic);
					// viewHolder.statusPic = (ImageView) convertView
					// .findViewById(R.id.status);
				} else {
					convertView = inflater.inflate(
							R.layout.activities_my_list_item_baseinfo, null);
					viewHolder.nameTxt = (TextView) convertView
							.findViewById(R.id.medal_number);
					viewHolder.introTxt = (TextView) convertView
							.findViewById(R.id.lingxian);
				}
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			if (getItemViewType(position) == 0) {
				viewHolder.titleTxt.setText(list.get(position).titleName);
			} else if (getItemViewType(position) == 1) {
				BadgeData temp = list.get(position).medalData;
				String name = temp.name;
				if (name != null) {
					viewHolder.nameTxt.setText(name);
				}
				String intro = temp.createTime;
				if (intro != null) {
					viewHolder.introTxt.setText(intro);
				}
				String url = temp.picPath;
				viewHolder.medalPic.setTag(url);
				imageLoaderHelper.loadImage(viewHolder.medalPic, url,
						R.drawable.medal_default);

			} else {
				String number = String.valueOf(UserNow.current().badgeCount);
				viewHolder.nameTxt.setText(number);
				String rate = UserNow.current().badgeRate;
				if (rate != null) {
					viewHolder.introTxt.setText("领先" + rate + "用户");
				}
			}

			return convertView;
		}

		@Override
		public int getViewTypeCount() {
			return 3;
		}

		@Override
		public int getItemViewType(int position) {
			return list.get(position).type;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public TextView introTxt;
			public ImageView medalPic;
			public TextView titleTxt;
		}

	}

	private class MyMedalListItem {
		public int type; // 0 title 1 medalData
		public String titleName;
		public BadgeData medalData;
	}

	@Override
	public void onRefresh() {
		getActivityListData();
	}

	@Override
	public void onLoadingMore() {
		// TODO
		int start = 0;
		int count = allList.size() + 10;
		getActivityListData(start, count);
	}

	private int allId = 0;
	private int state = 0;
	private String activityName = "活动详情";

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch ((Integer) parent.getTag()) {
		case 0:
			if (position - 1 == allList.size() || position - 1 < 0)
				break;
			allId = allList.get(position - 1).id;
			state = allList.get(position - 1).state;
			activityName = allList.get(position - 1).name;

			MobclickAgent.onEventBegin(this, "thebadge",
					allList.get(position - 1).name);
			if (UserNow.current().userID != 0)
				openActivityDetailActivity(allId, 0, state, activityName);
			else
				openLoginActivity(REQUEST_LOGIN_DETAIL);
			break;
		case 1:
			if (position - 1 == myList.size() || position - 1 < 0)
				return;
			BadgeData medaldata = myList.get(position - 1).medalData;
			if (medaldata != null) {
				openBadgeDetailActivity(medaldata.badgeId);
			}
			break;
		default:
			break;
		}
	}

	private void openActivityDetailActivity(int id, int from, int state,
			String name) {
		Intent intent = new Intent(this, ActivitiesDetailActivity.class);
		intent.putExtra("id", id);
		intent.putExtra("from", from);
		intent.putExtra("state", state);
		intent.putExtra("name", name);
		startActivity(intent);
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("activityList".equals(method)) {
			if (msg != null) {
				ArrayList<PlayNewData> temp = new ArrayList<PlayNewData>();
				String result = new ActivityListParser().parse(msg, temp);
				if (result != null && result.equals("")) {
					updateAllListView(temp);
				} else {
					DialogUtil.alertToast(getApplicationContext(), result);
				}
			} else {
				allProgressBar.setVisibility(View.GONE);
				if (allList.size() == 0) {
					allErrText.setVisibility(View.VISIBLE);
				}
				allListView.onLoadError();
			}
			getActivityListTask = null;
		} else if ("badgeList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateMyListView();
			} else {
				if (myList.size() == 0) {
					myErrText.setVisibility(View.VISIBLE);
				}
				myListView.onLoadError();
			}
			myProgressBar.setVisibility(View.GONE);
			getMyBadgeTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		if (OtherCacheData.isNeedUpdateActivityPageAll) {
			allList.clear();
			if (allAdapter != null)
				allAdapter.notifyDataSetChanged();
			getActivityListData();

			myList.clear();
			if (myAdapter != null)
				myAdapter.notifyDataSetChanged();
			getMyBadgeData();
			OtherCacheData.isNeedUpdateActivityPageAll = false;
		}
		if (fromMy == 1) {
			viewPager.setCurrentItem(1);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_LOGIN_GETMY:
				if (myList.size() == 0) {
					getMyBadgeData();
				}
				break;
			case REQUEST_LOGIN_DETAIL:
				openActivityDetailActivity(allId, 0, state, activityName);
				break;
			default:
				break;
			}
		} else {
			switch (requestCode) {
			case REQUEST_LOGIN_GETMY:
				viewPager.setCurrentItem(0);
				break;
			case REQUEST_LOGIN_DETAIL:

				break;
			default:
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
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
			if (fromMy == 1) {
				return super.onKeyDown(keyCode, event);
			} else {
				dialog();
				return true;
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void openBadgeDetailActivity(long badgeId) {
		Intent intent = new Intent(this, BadgeDetailActivity.class);
		intent.putExtra("badgeId", (int) badgeId);
		startActivity(intent);
	}
}
