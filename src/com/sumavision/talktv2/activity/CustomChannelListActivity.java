package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.adapter.NetPlayDataListAdapter;
import com.sumavision.talktv2.data.NetPlayData;
import com.sumavision.talktv2.data.ShortChannelData;
import com.sumavision.talktv2.data.TypeChannelData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.task.AddChannelTask;
import com.sumavision.talktv2.task.ChannelProgramRankingTask;
import com.sumavision.talktv2.task.ChannelProgramUserTask;
import com.sumavision.talktv2.task.ChannelProgramWholeTask;
import com.sumavision.talktv2.task.DeleteChannelTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class CustomChannelListActivity extends Activity implements
		NetConnectionListenerNew, OnPageChangeListener, OnClickListener,
		OnItemClickListener, OnChildClickListener,
		OnSharedPreferenceChangeListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_channel_layout);
		initUtils();
		initViews();
		setListeners();
		registSharePreference();
		getDefaultPageData(getResumeLastPosition());
	}

	private ImageLoaderHelper imageLoaderHelper;

	private void initUtils() {
		imageLoaderHelper = new ImageLoaderHelper();
	}

	/*
	 * 表示当前的推荐标签位置
	 */
	private int tagPosition;
	private LinearLayout tagLayout;
	private TextView myTextView, allTextView, rankTextView;
	private final int REQUEST_LOGIN = 1;
	private int currentPosition;
	// 编辑按钮
	private Button editBtn;

	private void initViews() {

		tagLayout = (LinearLayout) findViewById(R.id.tag_layout);
		myTextView = (TextView) findViewById(R.id.my_tag);
		allTextView = (TextView) findViewById(R.id.all_tag);
		rankTextView = (TextView) findViewById(R.id.rank_tag);
		myTextView.setTag(0);
		allTextView.setTag(1);
		rankTextView.setTag(2);

		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setOnPageChangeListener(this);
		initViewPager();
		initNetLiveLayout();
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
		editBtn = (Button) findViewById(R.id.manage);

	}

	private void setListeners() {
		myTextView.setOnClickListener(this);
		allTextView.setOnClickListener(this);
		rankTextView.setOnClickListener(this);
		editBtn.setOnClickListener(this);
	}

	private ViewPager viewPager;

	private void initViewPager() {
		ArrayList<View> views = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View myView = inflater.inflate(R.layout.custom_channel_my_list, null);
		initMyList(myView);

		View wholeView = inflater.inflate(R.layout.custom_channel_whole_list,
				null);
		initWholeList(wholeView);
		View rankingView = inflater.inflate(
				R.layout.custom_channel_ranking_list, null);
		initRankingList(rankingView);
		views.add(myView);
		views.add(wholeView);
		views.add(rankingView);
		AwesomeAdapter adapter = new AwesomeAdapter(views);
		viewPager.setAdapter(adapter);

	}

	private TextView myErrText;
	private ProgressBar myProgressBar;
	private MyListView myListView;

	private ArrayList<ShortChannelData> myList = new ArrayList<ShortChannelData>();
	private ArrayList<ShortChannelData> tempMyList = new ArrayList<ShortChannelData>();

	private void initMyList(View view) {
		myListView = (MyListView) view.findViewById(R.id.listView);
		myErrText = (TextView) view.findViewById(R.id.err_text);
		myErrText.setOnClickListener(errOnClickListener);
		myErrText.setTag(1);
		myProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		myListView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				getChannelProgram(UserNow.current().userID, tempMyList, 0, 20,
						1, false);
			}

			@Override
			public void onLoadingMore() {
				int start = myList.size();
				int count = 20;
				getChannelProgram(UserNow.current().userID, tempMyList, start,
						count, 1, true);
			}
		});
		myListView.setOnItemClickListener(this);
	}

	// 获取我的频道节目列表的task
	private ChannelProgramUserTask channelProgramTask;

	private void getDefaultChannelProgram() {
		getChannelProgram(UserNow.current().userID, tempMyList, 0, 20, 1, false);
	}

	private void getChannelProgram(int userId,
			ArrayList<ShortChannelData> temp, int first, int count, int cctv,
			boolean isLoadMore) {
		if (channelProgramTask == null) {
			channelProgramTask = new ChannelProgramUserTask(this, isLoadMore);
			channelProgramTask.execute(this, temp, first, count, cctv);
		}
	}

	@Override
	public void onNetBegin(String method, boolean isLoadMore) {
		if (!isLoadMore) {
			if (Constants.channelProgramUser.equals(method)) {
				if (myList.size() == 0)
					showMyLoadingLayout();
			} else if (Constants.channelProgramWhole.equals(method)) {
				if (wholeList.size() == 0)
					showWholeLoadingLayout();
			} else if (Constants.channelProgramRanking.equals(method)) {
				if (rankingTypeChannelData.size() == 0)
					showRankingLoadingLayout();
			} else if (Constants.deleteChannelTask.equals(method)) {
				showpb();
			} else if (Constants.addChannelTask.equals(method)) {
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
		if (Constants.channelProgramUser.equals(method)) {
			switch (code) {
			case Constants.requestErr:
				// .alertToast(getApplicationContext(), "request net");
				showMyErrorLayout();
				break;
			case Constants.fail_no_net:
				// DialogUtil.alertToast(getApplicationContext(), "no net");
				showMyErrorLayout();
				break;
			case Constants.fail_server_err:
				// DialogUtil.alertToast(getApplicationContext(),
				// "server Error");
				showMyErrorLayout();
				break;
			case Constants.parseErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "parse Error");
				showMyErrorLayout();
				break;
			case Constants.sucess:
				// DialogUtil.alertToast(getApplicationContext(), "sucess");
				hideMyErrorLayout();
				updateMyChannelList(isLoadMore);
				break;
			default:
				break;
			}
			channelProgramTask = null;
		} else if (Constants.channelProgramWhole.equals(method)) {
			switch (code) {
			case Constants.requestErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "request net");
				showWholeErrorLayout();
				break;
			case Constants.fail_no_net:
				// DialogUtil.alertToast(getApplicationContext(), "no net");
				showWholeErrorLayout();
				break;
			case Constants.fail_server_err:
				// DialogUtil.alertToast(getApplicationContext(),
				// "server Error");
				showWholeErrorLayout();
				break;
			case Constants.parseErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "parse Error");
				showWholeErrorLayout();
				break;
			case Constants.sucess:
				// DialogUtil.alertToast(getApplicationContext(), "sucess");
				updateWholeChannelList(isLoadMore);
				hideWholeErrorLayout();
				if (connectBg.isShown()) {
					hidepb();
					// myWholeEditeState = true;
					// wholeAdapter.notifyDataSetChanged();
				}
				break;
			default:
				break;
			}
			channelProgramWholeTask = null;
		} else if (Constants.channelProgramRanking.equals(method)) {
			switch (code) {
			case Constants.requestErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "request net");
				showRankingErrorLayout();
				break;
			case Constants.fail_no_net:
				// DialogUtil.alertToast(getApplicationContext(), "no net");
				showRankingErrorLayout();
				break;
			case Constants.fail_server_err:
				// DialogUtil.alertToast(getApplicationContext(),
				// "server Error");
				showRankingErrorLayout();
				break;
			case Constants.parseErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "parse Error");
				showRankingErrorLayout();
				break;
			case Constants.sucess:
				// DialogUtil.alertToast(getApplicationContext(), "sucess");
				hideRankingErrorLayout();
				updateRankingChannelList(isLoadMore);
				if (connectBg.isShown()) {
					hidepb();
				}
				break;
			default:
				break;
			}
			channelProgramRankingTask = null;
		} else if (Constants.deleteChannelTask.equals(method)) {
			hidepb();
			switch (code) {
			case Constants.requestErr:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.fail_no_net:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.fail_server_err:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.parseErr:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.sucess:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_s_deleteChannel);
				onChannelDeleted(deleteList, deleteGroupPosition,
						deletePosition);
				break;
			default:
				break;
			}
			deleteChannelTask = null;
		} else if (Constants.addChannelTask.equals(method)) {
			hidepb();
			switch (code) {
			case Constants.requestErr:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_addChannel);
				break;
			case Constants.fail_no_net:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_addChannel);
				break;
			case Constants.fail_server_err:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_addChannel);
				break;
			case Constants.parseErr:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_addChannel);
				break;
			case Constants.sucess:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_s_addChannel);
				onChannelAdded(addList, addGroupPosition, addPosition);
				break;
			default:
				break;
			}
			addChannelTask = null;
		}

	}

	private void onChannelDeleted(int deleteList, int groupPosition,
			int deletePosition) {

		switch (deleteList) {
		case LIST_MY:
			ShortChannelData data = myList.get(deletePosition);
			myList.remove(deletePosition);
			notifyAdapterDataSetChanged(data, false, LIST_MY);
			myChannelListAdapter.notifyDataSetChanged();
			break;
		case LIST_WHOLE:
			ShortChannelData data2 = wholeTypeChannelData.get(groupPosition).typeChannelData
					.get(deletePosition);
			data2.flagMyChannel = false;
			notifyAdapterDataSetChanged(data2, false, LIST_WHOLE);
			wholeAdapter.notifyDataSetChanged();
			break;
		case LIST_RANKING:
			ShortChannelData data3 = rankingTypeChannelData.get(groupPosition).typeChannelData
					.get(deletePosition);
			data3.flagMyChannel = false;
			notifyAdapterDataSetChanged(data3, false, LIST_RANKING);
			rankingAdapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
	}

	private void onChannelAdded(int addList, int groupPosition, int addPosition) {
		switch (addList) {
		case LIST_WHOLE:
			ShortChannelData data2 = wholeTypeChannelData.get(groupPosition).typeChannelData
					.get(addPosition);
			data2.flagMyChannel = true;
			notifyAdapterDataSetChanged(data2, true, LIST_WHOLE);
			wholeAdapter.notifyDataSetChanged();
			break;
		case LIST_RANKING:

			ShortChannelData data3 = rankingTypeChannelData.get(groupPosition).typeChannelData
					.get(addPosition);
			data3.flagMyChannel = true;
			notifyAdapterDataSetChanged(data3, true, LIST_RANKING);
			rankingAdapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
	}

	private MyChannelListAdapter myChannelListAdapter;

	private void updateMyChannelList(boolean isLoadMore) {
		if (tempMyList != null) {
			if (isLoadMore) {
				if (tempMyList.size() < 20) {
					myListView.setCanLoadMore(false);
					myListView.onLoadMoreOver();
				} else {
					myList.addAll(tempMyList);
					myChannelListAdapter.notifyDataSetChanged();
					myListView.onLoadMoreOver();
				}
			} else {
				myList.clear();
				myList.addAll(tempMyList);
				if (myList.size() == 0) {
					myErrText.setVisibility(View.VISIBLE);
					myErrText.setText(Constants.noChannel);
					myErrText.setGravity(Gravity.CENTER);
					myErrText.setTextColor(Color.BLACK);
				} else {
					myChannelListAdapter = new MyChannelListAdapter(myList);
					myListView.setAdapter(myChannelListAdapter);
					myListView.setCanLoadMore(true);
				}
			}
			tempMyList.clear();
		}
	}

	private void hideMyErrorLayout() {
		myProgressBar.setVisibility(View.GONE);
		myErrText.setVisibility(View.GONE);
	}

	private void showMyErrorLayout() {
		myProgressBar.setVisibility(View.GONE);
		myErrText.setVisibility(View.VISIBLE);
		myErrText.setText(Constants.errText);
	}

	private void showMyLoadingLayout() {
		myProgressBar.setVisibility(View.VISIBLE);
		myErrText.setVisibility(View.GONE);
	}

	private OnClickListener errOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch ((Integer) v.getTag()) {
			case 1:
				String text = ((TextView) v).getText().toString();
				if (Constants.noChannel.equals(text)) {
					DialogUtil.alertToast(getApplicationContext(),
							"你还未添加任何频道!\n请去\"全部频道\"\"编辑\"来定制自己的频道吧!");
				} else if (Constants.noLogIn.equals(text)) {
					openLoginActivity();
				} else {
					getDefaultChannelProgram();
				}
				break;
			case 2:
				getDefaultWholeChannelProgram();
				break;
			default:
				break;
			}
		}

	};

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		currentPosition = arg0;
		onTagSelected(arg0, true);
		boolean edit;
		switch (arg0) {
		case 0:
			edit = myEditState;
			break;
		case 1:
			edit = myWholeEditeState;
			break;
		case 2:
			edit = myRankingEditeState;
			break;
		default:
			edit = false;
			break;
		}
		if (edit) {
			editBtn.setText(getResources().getString(
					R.string.navigator_channel_complete));
		} else {
			editBtn.setText(getResources().getString(
					R.string.navigator_channel_edit));
		}
		processPageNet(arg0);
	}

	private void processPageNet(int arg0) {
		switch (arg0) {
		case 0:
			if (UserNow.current().userID == 0) {
				myErrText.setText(Constants.noLogIn);
				myProgressBar.setVisibility(View.GONE);
				myErrText.setVisibility(View.VISIBLE);
				myList.clear();
				// 弹出登陆页面
				openLoginActivity();
			} else {
				if (myList.size() == 0)
					getDefaultChannelProgram();
			}
			break;
		case 1:
			if (wholeList.size() == 0)
				getDefaultWholeChannelProgram();
			break;
		case 2:
			if (rankingTypeChannelData.size() == 0) {
				getDefaultRankingChannelProgram();
			}
			break;
		default:
			break;
		}
	}

	private static final int WHOLE_REQUEST_LOGIN = 23;

	private static final int RANKING_REQUEST_LOGIN = 24;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.my_tag:
			onTagSelected(0, false);
			break;
		case R.id.all_tag:
			onTagSelected(1, false);
			break;
		case R.id.rank_tag:
			onTagSelected(2, false);
			break;
		case R.id.manage:
			boolean edit;
			switch (tagPosition) {
			case 0:
				myEditState = !myEditState;
				if (myList.size() != 0) {
					myChannelListAdapter.notifyDataSetChanged();
				}
				edit = myEditState;
				break;
			case 1:
				if (wholeList.size() != 0) {
					if (UserNow.current().userID == 0) {
						Intent intent = new Intent(this, LoginActivity.class);
						startActivityForResult(intent, WHOLE_REQUEST_LOGIN);
					} else {
						myWholeEditeState = !myWholeEditeState;
						wholeAdapter.notifyDataSetChanged();
						if (!myWholeEditeState) {
							viewPager.setCurrentItem(0);
						}
					}
				}
				edit = myWholeEditeState;
				break;
			case 2:
				if (rankingTypeChannelData.size() != 0) {
					if (UserNow.current().userID == 0) {
						Intent intent = new Intent(this, LoginActivity.class);
						startActivityForResult(intent, RANKING_REQUEST_LOGIN);
					} else {
						myRankingEditeState = !myRankingEditeState;
						rankingAdapter.notifyDataSetChanged();
						if (!myRankingEditeState) {
							viewPager.setCurrentItem(0);
						}
					}
				}
				edit = myRankingEditeState;
				break;
			default:
				edit = false;
				break;
			}
			if (edit) {
				editBtn.setText(getResources().getString(
						R.string.navigator_channel_complete));
			} else {
				editBtn.setText(getResources().getString(
						R.string.navigator_channel_edit));
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 当标签切换时执行
	 */
	private void onTagSelected(int position, boolean fromViewPager) {
		if (tagPosition != position) {
			tagPosition = position;
			for (int i = 0; i < 3; ++i) {
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

	private boolean myEditState = false;

	private class MyChannelListAdapter extends BaseAdapter {
		private ArrayList<ShortChannelData> list;

		public MyChannelListAdapter(ArrayList<ShortChannelData> list) {
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
						.from(CustomChannelListActivity.this);
				convertView = inflater.inflate(
						R.layout.channel_custom_my_list_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.channelPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);
				viewHolder.liveBtn = (ImageView) convertView
						.findViewById(R.id.liveBtn);
				viewHolder.editLayout = (RelativeLayout) convertView
						.findViewById(R.id.edit_layout);
				viewHolder.editBtn = (ImageView) convertView
						.findViewById(R.id.edit_btn);
				viewHolder.tvNameView = (TextView) convertView
						.findViewById(R.id.tvName);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			ShortChannelData temp = list.get(position);
			String name = temp.programName;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			if (temp.livePlay) {
				viewHolder.liveBtn
						.setImageResource(R.drawable.channel_live_btn);
				viewHolder.liveBtn.setVisibility(View.VISIBLE);
			} else {
				viewHolder.liveBtn.setVisibility(View.GONE);
			}
			final int mPosition = position;
			viewHolder.liveBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onLiveBtnClick(-1, mPosition, LIST_MY);
				}
			});
			SpannableString time = temp.spannableTimeString;
			if (time != null) {
				viewHolder.time.setText(time);
				viewHolder.time.setVisibility(View.VISIBLE);
			} else {
				viewHolder.time.setVisibility(View.GONE);
			}
			String url = temp.channelPicUrl;
			imageLoaderHelper.loadImage(viewHolder.channelPic, url,
					R.drawable.channel_tv_logo_default);
			viewHolder.channelPic.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onTvLogoClick(0, mPosition, LIST_MY);
				}
			});
			viewHolder.editBtn.setImageResource(R.drawable.channel_delete);
			viewHolder.editBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onMyEditClick(mPosition);
				}
			});
			if (myEditState) {
				viewHolder.editLayout.setVisibility(View.VISIBLE);
			} else {
				viewHolder.editLayout.setVisibility(View.GONE);
			}
			if (temp.channelName != null)
				viewHolder.tvNameView.setText(temp.channelName);
			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public ImageView channelPic;
			public TextView time;
			// 直播或者预约按钮
			public ImageView liveBtn;
			public RelativeLayout editLayout;
			public ImageView editBtn;
			public TextView tvNameView;
		}

	}

	private void onMyEditClick(int position) {
		ShortChannelData shortChannelData = (ShortChannelData) myChannelListAdapter
				.getItem(position);
		deleteList = LIST_MY;
		deletePosition = position;
		deleteChannel(UserNow.current().userID, shortChannelData.channelId);
	}

	private DeleteChannelTask deleteChannelTask;

	private void deleteChannel(int userId, int channelId) {
		if (deleteChannelTask == null) {
			deleteChannelTask = new DeleteChannelTask(this, false);
			deleteChannelTask.execute(this, userId, channelId);
		}
	}

	private AddChannelTask addChannelTask;

	private void addChannel(int userId, int channelId) {
		if (addChannelTask == null) {
			addChannelTask = new AddChannelTask(this, false);
			addChannelTask.execute(this, userId, channelId);
		}
	}

	private void onEditClick(int groupPosition, int position, int from,
			boolean isAdding) {
		if (from == LIST_WHOLE) {
			ShortChannelData shortChannelData = wholeAdapter.getChild(
					groupPosition, position);
			if (isAdding) {
				addList = LIST_WHOLE;
				addGroupPosition = groupPosition;
				addPosition = position;
				addChannel(UserNow.current().userID, shortChannelData.channelId);
			} else {
				deleteList = LIST_WHOLE;
				deleteGroupPosition = groupPosition;
				deletePosition = position;
				deleteChannel(UserNow.current().userID,
						shortChannelData.channelId);
			}
		} else {
			ShortChannelData shortChannelData = rankingAdapter.getChild(
					groupPosition, position);
			if (isAdding) {
				addList = LIST_RANKING;
				addGroupPosition = groupPosition;
				addPosition = position;
				addChannel(UserNow.current().userID, shortChannelData.channelId);
			} else {
				deleteList = LIST_RANKING;
				deleteGroupPosition = groupPosition;
				deletePosition = position;
				deleteChannel(UserNow.current().userID,
						shortChannelData.channelId);
			}
		}
	}

	/**
	 * 
	 * @param position
	 *            listPosition
	 * @param from
	 *            表示来自哪个listView 0我的频道 1全部2排行榜
	 */
	private void onTvLogoClick(int groupPosition, int position, int from) {
		switch (from) {
		case LIST_MY:
			ShortChannelData channelData1 = (ShortChannelData) myChannelListAdapter
					.getItem(position);

			if (myEditState) {
				onMyEditClick(position);
			} else {
				openChannel(channelData1);
			}
			break;
		case LIST_WHOLE:
			ShortChannelData channelData2 = wholeAdapter.getChild(
					groupPosition, position);
			if (myWholeEditeState) {
				boolean isAdding = true;
				if (channelData2.flagMyChannel) {
					isAdding = false;
				}
				onEditClick(groupPosition, position, LIST_WHOLE, isAdding);
			} else {
				openChannel(channelData2);
			}
			break;
		case LIST_RANKING:
			ShortChannelData channelData3 = rankingAdapter.getChild(
					groupPosition, position);
			if (myRankingEditeState) {
				boolean isAdding = true;
				if (channelData3.flagMyChannel) {
					isAdding = false;
				}
				onEditClick(groupPosition, position, LIST_RANKING, isAdding);
			} else {
				openChannel(channelData3);
			}
			break;

		default:
			break;
		}
	}

	private void openChannel(ShortChannelData shortChannelData) {
		SharedPreferences sp = getSharedPreferences("channelId", 0);
		sp.edit().putInt("channelId", shortChannelData.channelId).commit();
		Intent i = new Intent(this, ChannelDetailActivity.class);
		i.putExtra("tvName", shortChannelData.channelName);
		i.putExtra("channelId", shortChannelData.channelId);
		startActivity(i);
	}

	/**
	 * 
	 * @param position
	 *            listPosition
	 * @param from
	 *            表示来自哪个listView 0我的频道 1全部2排行榜
	 */
	private void onLiveBtnClick(int groupPosition, int position, int from) {
		switch (from) {
		case LIST_MY:
			ShortChannelData channelData = (ShortChannelData) myChannelListAdapter
					.getItem(position);
			if (channelData.livePlay) {
				liveThroughNet(channelData);
			}
			break;
		case LIST_WHOLE:
			ShortChannelData tempWholeChannelData = wholeAdapter.getChild(
					groupPosition, position);
			if (tempWholeChannelData.livePlay) {
				liveThroughNet(tempWholeChannelData);
			}
			break;
		case LIST_RANKING:
			ShortChannelData tempRankingChannelData = rankingAdapter.getChild(
					groupPosition, position);
			if (tempRankingChannelData.livePlay) {
				liveThroughNet(tempRankingChannelData);
			}
			break;
		default:
			break;
		}
	}

	private void openLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivityForResult(intent, REQUEST_LOGIN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_LOGIN:
				// 从我的频道登陆成功切换到全部频道
				viewPager.setCurrentItem(1);
				switch (currentPosition) {
				case 0:
					if (myList.size() == 0) {
						getDefaultChannelProgram();
					}
					break;
				default:
					break;
				}
				break;
			case WHOLE_REQUEST_LOGIN:
				showpb();
				getDefaultWholeChannelProgram();
				break;
			case RANKING_REQUEST_LOGIN:
				showpb();
				getDefaultRankingChannelProgram();
				break;
			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void liveThroughNet(ShortChannelData shortChannelData) {
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
				Intent intent = new Intent(CustomChannelListActivity.this,
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
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg2 == 0) {
			return;
		}
		if (myEditState) {
			onMyEditClick(arg2 - 1);
		} else {
			ShortChannelData channelData = (ShortChannelData) myChannelListAdapter
					.getItem(arg2 - 1);
			VodProgramData vpd = new VodProgramData();
			vpd.cpId = channelData.channelId;
			vpd.id = String.valueOf(channelData.programId);
			vpd.topicId = channelData.topicId;
			vpd.name = channelData.programName;
			MobclickAgent.onEvent(this, "tvpr", vpd.name);
			openProgram(vpd);
		}
	}

	private void openProgram(VodProgramData vpd) {
		if (vpd.topicId == null || vpd.topicId.equals("")
				|| Integer.parseInt(vpd.topicId) == 0) {
			Toast.makeText(getApplicationContext(), "暂无此节目相关信息",
					Toast.LENGTH_SHORT).show();
		} else {

			Intent i = new Intent(this, ProgramNewActivity.class);
			i.putExtra("programId", vpd.id);
			i.putExtra("id", vpd.id);
			i.putExtra("cpId", vpd.cpId);
			i.putExtra("topicId", vpd.topicId);
			i.putExtra("nameHolder", vpd.nameHolder);
			i.putExtra("updateName", vpd.updateName);
			i.putExtra("from", 1);

			startActivity(i);

		}
	}

	// 表示发出预约请求的listView和项目所在位置

	private int deleteList;
	private int deleteGroupPosition;
	private int deletePosition;
	private int addList;
	private int addGroupPosition;
	private int addPosition;
	private static final int LIST_MY = 0;
	private static final int LIST_WHOLE = 1;
	private static final int LIST_RANKING = 2;

	private RelativeLayout connectBg;

	private void hidepb() {
		if (connectBg.isShown())
			connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		if (!connectBg.isShown())
			connectBg.setVisibility(View.VISIBLE);
	}

	private TextView wholeErrText;
	private ProgressBar wholeProgressBar;
	private ExpandableListView wholeListView;

	private ArrayList<ShortChannelData> wholeList = new ArrayList<ShortChannelData>();
	private ArrayList<ShortChannelData> tempWholeList = new ArrayList<ShortChannelData>();

	private void initWholeList(View view) {
		wholeListView = (ExpandableListView) view.findViewById(R.id.listView);
		wholeListView.setTag(LIST_WHOLE);
		wholeListView.setOnChildClickListener(this);
		wholeErrText = (TextView) view.findViewById(R.id.err_text);
		wholeErrText.setOnClickListener(errOnClickListener);
		wholeErrText.setTag(2);
		wholeProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	}

	// 获取全部频道节目列表的task
	private ChannelProgramWholeTask channelProgramWholeTask;

	private void getDefaultWholeChannelProgram() {
		getWholeChannelProgram(UserNow.current().userID, tempWholeList, 0, 0,
				1, false);
	}

	private void getWholeChannelProgram(int userId,
			ArrayList<ShortChannelData> temp, int first, int count, int cctv,
			boolean isLoadMore) {
		if (channelProgramWholeTask == null) {
			channelProgramWholeTask = new ChannelProgramWholeTask(this,
					isLoadMore);
			channelProgramWholeTask.execute(this, temp, first, count, cctv);
		}
	}

	private ArrayList<TypeChannelData> wholeTypeChannelData;

	/**
	 * 更新我的界面
	 * 
	 * @param isLoadMore
	 */
	private void updateWholeChannelList(boolean isLoadMore) {
		if (tempWholeList != null) {
			wholeList.addAll(tempWholeList);
			ArrayList<ShortChannelData> weishiChannels = new ArrayList<ShortChannelData>();
			ArrayList<ShortChannelData> yangshiChannels = new ArrayList<ShortChannelData>();
			for (ShortChannelData temp : tempWholeList) {
				if (temp.channelType == 0) {
					yangshiChannels.add(temp);
				} else {
					weishiChannels.add(temp);
				}
			}
			wholeTypeChannelData = new ArrayList<TypeChannelData>();
			TypeChannelData weishi = new TypeChannelData();
			weishi.channelTypeName = "卫视频道";
			weishi.typeChannelData = weishiChannels;
			TypeChannelData yangshi = new TypeChannelData();
			yangshi.channelTypeName = "央视频道";
			yangshi.typeChannelData = yangshiChannels;
			wholeTypeChannelData.add(weishi);
			// TODO: 机锋需要注释，机锋不显示央视
			wholeTypeChannelData.add(yangshi);
			wholeAdapter = new ExpandableListViewAdapter(wholeTypeChannelData);
			wholeListView.setAdapter(wholeAdapter);
			// 展开第一栏
			wholeListView.expandGroup(0);
			wholeListView.setGroupIndicator(null);
			// wholeListView.setOnChildClickListener(this);
			tempWholeList.clear();
		}
	}

	ExpandableListViewAdapter wholeAdapter;

	// adapter 主页下半部分的列表项目
	private class ExpandableListViewAdapter extends BaseExpandableListAdapter {

		private ArrayList<TypeChannelData> list;

		public ExpandableListViewAdapter(ArrayList<TypeChannelData> list) {

			this.list = list;
		}

		@Override
		public int getGroupCount() {

			if (list != null) {
				return list.size();
			}
			return 0;
		}

		@Override
		public int getChildrenCount(int groupPosition) {

			ArrayList<ShortChannelData> temp = list.get(groupPosition).typeChannelData;
			if (temp != null) {
				return temp.size();
			}
			return 0;
		}

		@Override
		public TypeChannelData getGroup(int groupPosition) {

			return list.get(groupPosition);
		}

		@Override
		public ShortChannelData getChild(int groupPosition, int childPosition) {

			ArrayList<ShortChannelData> temp = list.get(groupPosition).typeChannelData;
			return temp.get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {

			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {

			return childPosition;
		}

		@Override
		public boolean hasStableIds() {

			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {

			GroupViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new GroupViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(CustomChannelListActivity.this);
				convertView = inflater.inflate(R.layout.channel_group_item,
						null);
				viewHolder.textView = (TextView) convertView
						.findViewById(R.id.textView);
				viewHolder.imageView = (ImageView) convertView
						.findViewById(R.id.imageView);
				viewHolder.layout = (RelativeLayout) convertView;

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (GroupViewHolder) convertView.getTag();
			}
			String title = getGroup(groupPosition).channelTypeName;
			if (title != null) {
				viewHolder.textView.setText(title);
			}
			if (isExpanded) {
				viewHolder.imageView
						.setImageResource(R.drawable.channel_indicator_up);
			} else {
				viewHolder.imageView
						.setImageResource(R.drawable.channel_indicator_down);
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			ChildrenViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ChildrenViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(CustomChannelListActivity.this);
				convertView = inflater.inflate(R.layout.channel_children_item,
						null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.channelPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);
				viewHolder.liveBtn = (ImageView) convertView
						.findViewById(R.id.liveBtn);
				viewHolder.infoLayout = (RelativeLayout) convertView
						.findViewById(R.id.info_layout);
				viewHolder.editLayout = (RelativeLayout) convertView
						.findViewById(R.id.edit_layout);
				viewHolder.editBtn = (ImageView) convertView
						.findViewById(R.id.edit_btn);
				viewHolder.tvNameView = (TextView) convertView
						.findViewById(R.id.tvName);
				viewHolder.hintTextView = (TextView) convertView
						.findViewById(R.id.hint_text);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ChildrenViewHolder) convertView.getTag();
			}
			ShortChannelData temp = getChild(groupPosition, childPosition);
			String name = temp.programName;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			if (temp.livePlay) {
				viewHolder.liveBtn
						.setImageResource(R.drawable.channel_live_btn);
				viewHolder.liveBtn.setVisibility(View.VISIBLE);
			} else {
				viewHolder.liveBtn.setVisibility(View.GONE);
			}
			final int mPosition = childPosition;
			final int mGroupPosition = groupPosition;
			viewHolder.liveBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onLiveBtnClick(mGroupPosition, mPosition, LIST_WHOLE);
				}
			});
			SpannableString time = temp.spannableTimeString;
			if (time != null) {
				viewHolder.time.setText(time);
				viewHolder.time.setVisibility(View.VISIBLE);
			} else {
				viewHolder.time.setVisibility(View.GONE);
			}
			String url = temp.channelPicUrl;
			imageLoaderHelper.loadImage(viewHolder.channelPic, url,
					R.drawable.channel_tv_logo_default);
			viewHolder.channelPic.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onTvLogoClick(mGroupPosition, mPosition, LIST_WHOLE);
				}
			});
			// viewHolder.editBtn.setImageResource(R.drawable.channel_delete);
			final boolean isMy = temp.flagMyChannel;
			Resources res = getResources();
			if (isMy) {
				viewHolder.editBtn.setImageResource(R.drawable.channel_delete);
				viewHolder.hintTextView.setText(res
						.getString(R.string.channel_edit_delete));
				viewHolder.hintTextView.setTextColor(res
						.getColor(R.color.channel_delete));
			} else {
				viewHolder.editBtn.setImageResource(R.drawable.channel_add);
				viewHolder.hintTextView.setText(res
						.getString(R.string.channel_edit_add));
				viewHolder.hintTextView.setTextColor(res
						.getColor(R.color.channel_add));
			}
			viewHolder.editBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onEditClick(mGroupPosition, mPosition, LIST_WHOLE, !isMy);
				}
			});
			if (myWholeEditeState) {
				viewHolder.editLayout.setVisibility(View.VISIBLE);
			} else {
				viewHolder.editLayout.setVisibility(View.GONE);
			}
			if (temp.channelName != null)
				viewHolder.tvNameView.setText(temp.channelName);
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {

			return true;
		}
	}

	static class GroupViewHolder {

		public TextView textView;
		public ImageView imageView;
		public RelativeLayout layout;
	}

	static class ChildrenViewHolder {

		public TextView nameTxt;
		public ImageView channelPic;
		public TextView time;
		// 直播或者预约按钮
		public ImageView liveBtn;
		public RelativeLayout infoLayout;
		public RelativeLayout editLayout;
		public ImageView editBtn;
		public TextView tvNameView;
		public TextView hintTextView;
	}

	private void hideWholeErrorLayout() {
		wholeProgressBar.setVisibility(View.GONE);
		wholeErrText.setVisibility(View.GONE);
	}

	private void showWholeErrorLayout() {
		wholeProgressBar.setVisibility(View.GONE);
		wholeErrText.setVisibility(View.VISIBLE);
		wholeErrText.setText(Constants.errText);
	}

	private void showWholeLoadingLayout() {
		wholeProgressBar.setVisibility(View.VISIBLE);
		wholeErrText.setVisibility(View.GONE);
	}

	// 获取排行榜频道节目列表的task
	private ChannelProgramRankingTask channelProgramRankingTask;
	private ArrayList<TypeChannelData> tempRankingTypeChannelData = new ArrayList<TypeChannelData>();

	private void getDefaultRankingChannelProgram() {
		getRankingChannelProgram(UserNow.current().userID,
				tempRankingTypeChannelData, 0, 0, 1, false);
	}

	private void getRankingChannelProgram(int userId,
			ArrayList<TypeChannelData> temp, int first, int count, int cctv,
			boolean isLoadMore) {
		if (channelProgramRankingTask == null) {
			channelProgramRankingTask = new ChannelProgramRankingTask(this,
					isLoadMore);
			channelProgramRankingTask.execute(this, temp, first, count, cctv);
		}
	}

	private ArrayList<TypeChannelData> rankingTypeChannelData = new ArrayList<TypeChannelData>();

	private void hideRankingErrorLayout() {
		rankingProgressBar.setVisibility(View.GONE);
		rankingErrText.setVisibility(View.GONE);
	}

	private void showRankingErrorLayout() {
		rankingProgressBar.setVisibility(View.GONE);
		rankingErrText.setVisibility(View.VISIBLE);
		rankingErrText.setText(Constants.errText);
	}

	private void showRankingLoadingLayout() {
		rankingProgressBar.setVisibility(View.VISIBLE);
		rankingErrText.setVisibility(View.GONE);
	}

	private TextView rankingErrText;
	private ProgressBar rankingProgressBar;
	private ExpandableListView rankingListView;

	private void initRankingList(View view) {
		rankingListView = (ExpandableListView) view.findViewById(R.id.listView);
		rankingListView.setTag(LIST_RANKING);
		rankingListView.setOnChildClickListener(this);
		rankingErrText = (TextView) view.findViewById(R.id.err_text);
		rankingErrText.setOnClickListener(errOnClickListener);
		rankingErrText.setTag(2);
		rankingProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
	}

	private void updateRankingChannelList(boolean isLoadMore) {
		if (tempRankingTypeChannelData != null) {
			rankingTypeChannelData.addAll(tempRankingTypeChannelData);
			if (rankingTypeChannelData.size() == 0) {
				rankingErrText.setText(Constants.noRankingChannel);
				rankingErrText.setVisibility(View.VISIBLE);
			} else {
				rankingAdapter = new RankingExpandableListViewAdapter(
						rankingTypeChannelData);
				rankingListView.setAdapter(rankingAdapter);
				// 展开第一栏
				rankingListView.expandGroup(0);
				rankingListView.setGroupIndicator(null);
				tempRankingTypeChannelData.clear();
			}
		}
	}

	RankingExpandableListViewAdapter rankingAdapter;

	// adapter 主页下半部分的列表项目
	private class RankingExpandableListViewAdapter extends
			BaseExpandableListAdapter {

		private ArrayList<TypeChannelData> list;

		public RankingExpandableListViewAdapter(ArrayList<TypeChannelData> list) {

			this.list = list;
		}

		@Override
		public int getGroupCount() {

			if (list != null) {
				return list.size();
			}
			return 0;
		}

		@Override
		public int getChildrenCount(int groupPosition) {

			if (list == null || list.size() == 0) {
				return 0;
			}
			ArrayList<ShortChannelData> temp = list.get(groupPosition).typeChannelData;
			if (temp != null) {
				return temp.size();
			}
			return 0;
		}

		@Override
		public TypeChannelData getGroup(int groupPosition) {

			return list.get(groupPosition);
		}

		@Override
		public ShortChannelData getChild(int groupPosition, int childPosition) {

			ArrayList<ShortChannelData> temp = list.get(groupPosition).typeChannelData;
			return temp.get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {

			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {

			return childPosition;
		}

		@Override
		public boolean hasStableIds() {

			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {

			GroupViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new GroupViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(CustomChannelListActivity.this);
				convertView = inflater.inflate(R.layout.channel_group_item,
						null);
				viewHolder.textView = (TextView) convertView
						.findViewById(R.id.textView);
				viewHolder.imageView = (ImageView) convertView
						.findViewById(R.id.imageView);
				viewHolder.layout = (RelativeLayout) convertView;

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (GroupViewHolder) convertView.getTag();
			}
			String title = getGroup(groupPosition).channelTypeName;
			if (title != null) {
				viewHolder.textView.setText(title);
			}
			if (isExpanded) {
				viewHolder.imageView
						.setImageResource(R.drawable.channel_indicator_up);
			} else {
				viewHolder.imageView
						.setImageResource(R.drawable.channel_indicator_down);
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			RankingViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new RankingViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(CustomChannelListActivity.this);
				convertView = inflater.inflate(
						R.layout.channel_ranking_children_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.channelPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);
				viewHolder.liveBtn = (ImageView) convertView
						.findViewById(R.id.liveBtn);
				viewHolder.infoLayout = (RelativeLayout) convertView
						.findViewById(R.id.info_layout);
				viewHolder.editLayout = (RelativeLayout) convertView
						.findViewById(R.id.edit_layout);
				viewHolder.editBtn = (ImageView) convertView
						.findViewById(R.id.edit_btn);
				viewHolder.rankInfoView = (TextView) convertView
						.findViewById(R.id.rankingInfo);
				viewHolder.tvNameView = (TextView) convertView
						.findViewById(R.id.tvName);
				viewHolder.hintTextView = (TextView) convertView
						.findViewById(R.id.hint_text);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (RankingViewHolder) convertView.getTag();
			}
			ShortChannelData temp = getChild(groupPosition, childPosition);
			String name = temp.programName;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			if (temp.livePlay) {
				viewHolder.liveBtn
						.setImageResource(R.drawable.channel_live_btn);
				viewHolder.liveBtn.setVisibility(View.VISIBLE);
			} else {
				viewHolder.liveBtn.setVisibility(View.GONE);
			}
			final int mPosition = childPosition;
			final int mGroupPosition = groupPosition;
			viewHolder.liveBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onLiveBtnClick(mGroupPosition, mPosition, LIST_RANKING);
				}
			});
			SpannableString timeInfo = temp.spannableTimeInfo;
			if (timeInfo != null && timeInfo.length() > 4) {
				viewHolder.time.setText(timeInfo);
				viewHolder.time.setVisibility(View.VISIBLE);
			} else {
				viewHolder.time.setVisibility(View.GONE);
			}
			String url = temp.channelPicUrl;
			imageLoaderHelper.loadImage(viewHolder.channelPic, url,
					R.drawable.channel_tv_logo_default);
			viewHolder.channelPic.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onTvLogoClick(mGroupPosition, mPosition, LIST_RANKING);
				}
			});

			final boolean isMy = temp.flagMyChannel;
			Resources res = getResources();
			if (isMy) {
				viewHolder.editBtn.setImageResource(R.drawable.channel_delete);
				viewHolder.hintTextView.setText(res
						.getString(R.string.channel_edit_delete));
				viewHolder.hintTextView.setTextColor(res
						.getColor(R.color.channel_delete));
			} else {
				viewHolder.editBtn.setImageResource(R.drawable.channel_add);
				viewHolder.hintTextView.setText(res
						.getString(R.string.channel_edit_add));
				viewHolder.hintTextView.setTextColor(res
						.getColor(R.color.channel_add));
			}
			viewHolder.editBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onEditClick(mGroupPosition, mPosition, LIST_RANKING, !isMy);
				}
			});
			if (myRankingEditeState) {
				viewHolder.editLayout.setVisibility(View.VISIBLE);
			} else {
				viewHolder.editLayout.setVisibility(View.GONE);
			}
			String channelName = temp.channelName;
			if (channelName != null) {
				viewHolder.tvNameView.setText(channelName);
			}
			String rankingInfo = temp.programInfo;
			if (rankingInfo != null) {
				viewHolder.rankInfoView.setText("(" + rankingInfo + ")");
			}
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {

			return true;
		}
	}

	static class RankingViewHolder {

		public TextView nameTxt;
		public ImageView channelPic;
		public TextView time;
		// 直播或者预约按钮
		public ImageView liveBtn;
		public RelativeLayout infoLayout;
		public RelativeLayout editLayout;
		public ImageView editBtn;
		public TextView rankInfoView;
		public TextView tvNameView;
		public TextView hintTextView;
	}

	private boolean myWholeEditeState = false;

	private boolean myRankingEditeState = false;

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		ShortChannelData channelData;
		if ((Integer) parent.getTag() == LIST_WHOLE) {
			channelData = wholeAdapter.getChild(groupPosition, childPosition);
			if (myWholeEditeState) {
				boolean isAdding = true;
				if (channelData.flagMyChannel) {
					isAdding = false;
				}
				onEditClick(groupPosition, childPosition, LIST_WHOLE, isAdding);
			} else {
				VodProgramData vpd = new VodProgramData();
				vpd.cpId = channelData.channelId;
				vpd.id = String.valueOf(channelData.programId);
				vpd.topicId = channelData.topicId;
				vpd.name = channelData.programName;
				MobclickAgent.onEvent(this, "tvpr", vpd.name);
				openProgram(vpd);
			}
		} else {
			channelData = rankingAdapter.getChild(groupPosition, childPosition);
			if (myRankingEditeState) {
				boolean isAdding = true;
				if (channelData.flagMyChannel) {
					isAdding = false;
				}
				onEditClick(groupPosition, childPosition, LIST_RANKING,
						isAdding);
			} else {
				VodProgramData vpd = new VodProgramData();
				vpd.cpId = channelData.channelId;
				vpd.id = String.valueOf(channelData.programId);
				vpd.topicId = channelData.topicId;
				vpd.name = channelData.programName;
				MobclickAgent.onEvent(this, "tvpr", vpd.name);
				openProgram(vpd);
			}
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (addChannelTask != null) {
				addChannelTask.cancel(true);
				addChannelTask = null;
				return true;
			} else if (deleteChannelTask != null) {
				deleteChannelTask.cancel(true);
				deleteChannelTask = null;
				return true;
			} else if (netLiveLayout.isShown()) {
				netLiveLayout.setVisibility(View.GONE);
				return true;
			} else {
				dialog();
			}
		}
		return super.onKeyDown(keyCode, event);
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
	protected void onResume() {
		super.onResume();
		if (needUpdate) {
			viewPager.setCurrentItem(1);
			clearEditStatus();
			myList.clear();
			if (myChannelListAdapter != null) {
				myChannelListAdapter.notifyDataSetChanged();
			}
			needUpdate = false;
		}
	}

	private void clearEditStatus() {
		editBtn.setText(getResources().getString(
				R.string.navigator_channel_edit));
		myWholeEditeState = false;
		myRankingEditeState = false;
		myEditState = false;
		if (wholeAdapter != null)
			wholeAdapter.notifyDataSetChanged();
		if (rankingAdapter != null)
			rankingAdapter.notifyDataSetChanged();
		if (myChannelListAdapter != null)
			myChannelListAdapter.notifyDataSetChanged();
	}

	/**
	 * 
	 */
	private void notifyAdapterDataSetChanged(ShortChannelData shortChannelData,
			boolean isAdded, int from) {
		if (from != LIST_MY) {
			boolean needNotifyMyAdapterChanged = false;
			if (isAdded) {
				if (myList.size() == 0) {
					myChannelListAdapter = new MyChannelListAdapter(myList);
					myListView.setAdapter(myChannelListAdapter);
					hideMyErrorLayout();
				}
				myList.add(0, shortChannelData);
				needNotifyMyAdapterChanged = true;
			} else {
				for (int i = 0; i < myList.size(); ++i) {
					ShortChannelData data = myList.get(i);
					if (data.channelId == shortChannelData.channelId) {
						myList.remove(i);
						needNotifyMyAdapterChanged = true;
						break;
					}
				}
			}
			if (needNotifyMyAdapterChanged) {
				myChannelListAdapter.notifyDataSetChanged();
			}
		}
		if (from != LIST_WHOLE) {
			boolean needNotifyWholeAdapter = false;
			for (TypeChannelData typeChannelData : wholeTypeChannelData) {
				ArrayList<ShortChannelData> list = typeChannelData.typeChannelData;
				if (list != null) {
					for (ShortChannelData data : list) {
						if (shortChannelData.channelId == data.channelId) {
							data.flagMyChannel = isAdded;
							needNotifyWholeAdapter = true;
							break;
						}
					}
				}
			}
			if (needNotifyWholeAdapter) {
				wholeAdapter.notifyDataSetChanged();
			}
		}
		boolean needNotifyRankingAdapter = false;
		for (TypeChannelData typeChannelData : rankingTypeChannelData) {
			ArrayList<ShortChannelData> list = typeChannelData.typeChannelData;
			if (list != null) {
				for (ShortChannelData data : list) {
					if (shortChannelData.channelId == data.channelId) {
						data.flagMyChannel = isAdded;
						needNotifyRankingAdapter = true;
					}
				}
			}
		}
		if (needNotifyRankingAdapter) {
			rankingAdapter.notifyDataSetChanged();
		}
	}

	private boolean needUpdate = false;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("userID")) {
			int userId = spUser.getInt("userID", 0);
			if (userId != myUserId) {
				needUpdate = true;
				myUserId = userId;

			} else {
				needUpdate = false;
			}
		}
	}

	private int myUserId;
	private SharedPreferences spUser;

	void registSharePreference() {
		myUserId = UserNow.current().userID;
		spUser = getSharedPreferences("userInfo", 0);
		spUser.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		savePosition();
	}

	private void savePosition() {
		Editor editor = spUser.edit();
		editor.putInt("channelPosition", currentPosition);
		editor.putInt("channelUserId", UserNow.current().userID);
		editor.commit();
	}

	private int getResumeLastPosition() {
		int position = spUser.getInt("channelPosition", LIST_WHOLE);
		int userId = spUser.getInt("channelUserId", 0);
		if (userId == 0 || userId != UserNow.current().userID) {
			return LIST_WHOLE;
		}
		return position;
	}

	private void getDefaultPageData(int position) {
		switch (position) {
		case LIST_MY:
			getDefaultChannelProgram();
			break;
		case LIST_WHOLE:
			viewPager.setCurrentItem(LIST_WHOLE);
			break;
		case LIST_RANKING:
			viewPager.setCurrentItem(LIST_RANKING);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.e("channel", "destroy");
	}
}
