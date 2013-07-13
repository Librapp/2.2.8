package com.sumavision.talktv2.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.sumavision.talktv2.activity.ImageLoaderHelper;
import com.sumavision.talktv2.activity.MyListView;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.activity.OtherUserCenterActivity;
import com.sumavision.talktv2.data.EventData;
import com.sumavision.talktv2.data.MainPageData;
import com.sumavision.talktv2.net.EventRoomParser;
import com.sumavision.talktv2.net.EventRoomRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.task.GetEventRoomTask;

public class FriendAllFragment extends Fragment implements
		NetConnectionListener, OnItemClickListener, OnClickListener {
	private ImageLoaderHelper imageLoaderHelper;

	private void initUtils() {
		imageLoaderHelper = new ImageLoaderHelper();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUtils();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.friend_viewpager_all, null);
			initAllPager(rootView);
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
			getAllUserList();
			needLoadData = false;
		}
	}

	private void initAllPager(View view) {
		myAllListView = (MyListView) view.findViewById(R.id.listView);
		allErrText = (TextView) view.findViewById(R.id.err_text);
		allProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		myAllListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				getAllUserList();
			}

			@Override
			public void onLoadingMore() {
				// TODO
				int start = 0;
				int count = allList.size() + 10;
				getAllUserList(start, count);
			}
		});
		myAllListView.setOnItemClickListener(this);
		allErrText.setOnClickListener(this);
	}

	private TextView allErrText;
	private ProgressBar allProgressBar;
	private MyListView myAllListView;
	private ArrayList<EventData> allList = new ArrayList<EventData>();

	private GetEventRoomTask getEventRoomTask;

	private void getAllUserList() {
		if (getEventRoomTask == null) {
			getEventRoomTask = new GetEventRoomTask(this);
			getEventRoomTask.execute(getActivity(), new EventRoomRequest(),
					new EventRoomParser());

			if (allList.size() == 0) {
				allErrText.setVisibility(View.GONE);
				allProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getAllUserList(int start, int count) {
		if (getEventRoomTask == null) {
			getEventRoomTask = new GetEventRoomTask(this);
			getEventRoomTask.execute(getActivity(), new EventRoomRequest(),
					new EventRoomParser());

			if (allList.size() == 0) {
				allErrText.setVisibility(View.GONE);
				allProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void updateAllListView() {
		ArrayList<EventData> temp = MainPageData.current().eventDatas;
		if (temp != null) {
			allList = temp;
			if (allList.size() == 0) {
				allErrText.setText("暂无信息");
				allErrText.setVisibility(View.VISIBLE);
			} else {
				allErrText.setVisibility(View.GONE);
				AllAdapter adapter = new AllAdapter(allList, getActivity());
				myAllListView.setAdapter(adapter);
			}
		} else {
			allErrText.setVisibility(View.VISIBLE);
		}
		allProgressBar.setVisibility(View.GONE);
	}

	private class AllAdapter extends BaseAdapter {
		private ArrayList<EventData> list;
		private Context context;

		public AllAdapter(ArrayList<EventData> list, Context context) {
			this.list = list;
			this.context = context;
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
				convertView = inflater.inflate(R.layout.friend_all__list_item,
						null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.intro = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.headPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.timeText = (TextView) convertView
						.findViewById(R.id.time);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			EventData temp = list.get(position);
			String name = temp.userName;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String intro = temp.preMsg;
			if (intro != null) {
				viewHolder.intro.setText(intro);
			}
			String time = temp.createTime;
			if (time != null) {
				viewHolder.timeText.setText(time);
			}
			String url = temp.userPicUrl;
			imageLoaderHelper.loadImage(viewHolder.headPic, url,
					R.drawable.list_headpic_default);
			return convertView;
		}

	}

	static class ViewHolder {
		public TextView nameTxt;
		public TextView intro;
		public ImageView headPic;
		public TextView timeText;
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("eventRoomList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateAllListView();
			} else {
				allProgressBar.setVisibility(View.GONE);
				if (allList.size() == 0) {
					allErrText.setVisibility(View.VISIBLE);
				}
				myAllListView.onLoadError();
			}
			getEventRoomTask = null;
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
		if (position - 1 == allList.size() || position - 1 < 0) {
			return;
		}
		int uId = allList.get(position - 1).userId;
		openOtherUserCenterActivity(uId, allList.get(position - 1).userPicUrl);
	}

	private void openOtherUserCenterActivity(int id, String iconURL) {
		Intent intent = new Intent(getActivity(), OtherUserCenterActivity.class);
		intent.putExtra("id", id);
		intent.putExtra("iconURL", iconURL);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getAllUserList();
			break;
		default:
			break;
		}
	}
}
