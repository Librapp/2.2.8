package com.sumavision.talktv2.fragment;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import com.sumavision.talktv2.activity.ActivitiesDetailActivity;
import com.sumavision.talktv2.activity.ImageLoaderHelper;
import com.sumavision.talktv2.activity.LoginActivity;
import com.sumavision.talktv2.activity.MyListView;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.PlayNewData;
import com.sumavision.talktv2.net.ActivityListParser;
import com.sumavision.talktv2.net.ActivityListRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.task.GetActivityListTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class ActivityFragment extends Fragment implements
		NetConnectionListener, OnItemClickListener, OnClickListener,
		OnRefreshListener {
	private ImageLoaderHelper imageLoaderHelper;

	private void initUtils() {
		imageLoaderHelper = new ImageLoaderHelper();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constants.load_data:
				getActivityListData();
				needLoadData = false;
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUtils();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater
					.inflate(R.layout.activities_viewpager_all, null);
			initAllPage(rootView);
			needLoadData = true;
		} else {
			((ViewGroup) rootView.getParent()).removeView(rootView);
		}
		return rootView;
	}

	private View rootView;
	boolean needLoadData;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (needLoadData) {
			handler.sendEmptyMessageDelayed(Constants.load_data,
					Constants.animation_duration);
		}
	}

	private void initAllPage(View view) {
		allListView = (MyListView) view.findViewById(R.id.listView);
		allErrText = (TextView) view.findViewById(R.id.err_text);
		allProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		allListView.setOnItemClickListener(this);
		allListView.setOnRefreshListener(this);
		allErrText.setOnClickListener(this);
	}

	private MyListView allListView;
	private TextView allErrText;
	private ProgressBar allProgressBar;
	private ArrayList<PlayNewData> allList = new ArrayList<PlayNewData>();

	private GetActivityListTask getActivityListTask;

	private void getActivityListData() {
		if (getActivityListTask == null) {
			getActivityListTask = new GetActivityListTask(this);
			getActivityListTask.execute(getActivity(),
					new ActivityListRequest(), new ActivityListParser());
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
			getActivityListTask.execute(getActivity(),
					new ActivityListRequest());
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

	private class MedalListViewAdapter extends BaseAdapter {

		private ArrayList<PlayNewData> list;

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
				LayoutInflater inflater = LayoutInflater.from(getActivity());
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

	}

	static class ViewHolder {
		public TextView nameTxt;
		public TextView introTxt;
		public ImageView medalPic;
		public ImageView statusPic;
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
					DialogUtil.alertToast(getActivity(), result);
				}
			} else {
				allProgressBar.setVisibility(View.GONE);
				if (allList.size() == 0) {
					allErrText.setVisibility(View.VISIBLE);
				}
				allListView.onLoadError();
			}
			getActivityListTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (position - 1 == allList.size() || position - 1 < 0)
			return;
		int allId = allList.get(position - 1).id;
		int state = allList.get(position - 1).state;
		String activityName = allList.get(position - 1).name;

		MobclickAgent.onEventBegin(getActivity(), "thebadge",
				allList.get(position - 1).name);
		if (UserNow.current().userID != 0)
			openActivityDetailActivity(allId, 0, state, activityName);
		else
			openLoginActivity();

	}

	private void openActivityDetailActivity(int id, int from, int state,
			String name) {
		Intent intent = new Intent(getActivity(),
				ActivitiesDetailActivity.class);
		intent.putExtra("id", id);
		intent.putExtra("from", from);
		intent.putExtra("state", state);
		intent.putExtra("name", name);
		startActivity(intent);
	}

	private void openLoginActivity() {
		Intent intent = new Intent(getActivity(), LoginActivity.class);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getActivityListData();
			break;
		default:
			break;
		}
	}

	@Override
	public void onRefresh() {
		getActivityListData();
	}

	@Override
	public void onLoadingMore() {
		int start = 0;
		int count = allList.size() + 20;
		getActivityListData(start, count);
	}
}
