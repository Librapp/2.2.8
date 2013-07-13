package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.BadgeData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.BadgeParser;
import com.sumavision.talktv2.net.BadgeRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.task.GetMyBadgeTask;
import com.sumavision.talktv2.user.UserNow;
import com.umeng.analytics.MobclickAgent;

public class MyBadgeActivity extends Activity implements NetConnectionListener,
		OnItemClickListener, OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activities_viewpager_my);
		initOthers();
		initMyPage();
		getMyBadgeData();
	}

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

	private ImageLoaderHelper imageLoaderHelper;

	private void initOthers() {
		OtherCacheData.current().offset = 0;
		OtherCacheData.current().pageCount = 10;
		imageLoaderHelper = new ImageLoaderHelper();
	}

	private MyListView myListView;
	private TextView myErrText;
	private ProgressBar myProgressBar;
	private ArrayList<MyMedalListItem> myList = new ArrayList<MyMedalListItem>();

	private void initMyPage() {
		myListView = (MyListView) findViewById(R.id.listView);
		myErrText = (TextView) findViewById(R.id.err_text);
		myProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		myListView.setOnRefreshListener(myListRefreshListener);
		myListView.setOnItemClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
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
						.from(MyBadgeActivity.this);

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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (position - 1 == myList.size() || position - 1 < 0)
			return;
		BadgeData medaldata = myList.get(position - 1).medalData;
		if (medaldata != null) {
			openBadgeDetailActivity(medaldata.badgeId);
		}

	}

	private void openBadgeDetailActivity(long badgeId) {
		Intent intent = new Intent(this, BadgeDetailActivity.class);
		intent.putExtra("badgeId", (int) badgeId);
		startActivity(intent);
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("badgeList".equals(method)) {
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		default:
			break;
		}
	}

}
