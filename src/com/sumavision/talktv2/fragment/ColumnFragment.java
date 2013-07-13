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
import com.sumavision.talktv2.activity.ImageLoaderHelper;
import com.sumavision.talktv2.activity.MyListView;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.activity.ProgramNewActivity;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.task.ColumnVideoListTask;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.talktv2.utils.Constants;
import com.umeng.analytics.MobclickAgent;

public class ColumnFragment extends Fragment implements NetConnectionListener,
		OnRefreshListener, OnItemClickListener, OnClickListener {
	public ColumnFragment() {

	}

	public ColumnFragment(int columnId) {
		this.columnId = columnId;
	}

	private int columnId;
	private ImageLoaderHelper imageLoaderHelper;

	private void initUtils() {
		imageLoaderHelper = new ImageLoaderHelper();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constants.load_data:
				getDefaultData();
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
			rootView = inflater.inflate(R.layout.rcmd_program_viewpager_item,
					null);
			initViews(rootView);
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

	private MyListView listView;
	private TextView errTextView;
	private ProgressBar progressBar;

	private void initViews(View view) {
		listView = (MyListView) view.findViewById(R.id.listView);
		errTextView = (TextView) view.findViewById(R.id.err_text);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		listView.setOnRefreshListener(this);
		listView.setOnItemClickListener(this);
		errTextView.setOnClickListener(this);
	}

	private ColumnVideoListTask columnVideoListTask;
	private final ArrayList<VodProgramData> list = new ArrayList<VodProgramData>();
	private int invalid = -1;

	private void getDefaultData() {
		getData(columnId, 0, 20);
	}

	private void getData(int id, int offset, int count) {
		if (columnVideoListTask == null) {
			columnVideoListTask = new ColumnVideoListTask();
			columnVideoListTask.execute(getActivity(), this, list, id, offset,
					count, invalid);
			if (list.size() == 0) {
				progressBar.setVisibility(View.VISIBLE);
			}
			errTextView.setVisibility(View.GONE);
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
		if ("columnVideoList".equals(method)) {

			if (msg != null && msg.equals("")) {
				updateProgramList();
			} else {
				if (list.size() == 0) {
					errTextView.setVisibility(View.VISIBLE);
				}
				progressBar.setVisibility(View.GONE);
				listView.onLoadError();
			}
			columnVideoListTask = null;

		}
	}

	@Override
	public void onCancel(String method) {

	}

	private void updateProgramList() {
		if (list.size() != 0) {
			progressBar.setVisibility(View.GONE);
			VodProgramListViewAdapter adapter = new VodProgramListViewAdapter(
					list);
			listView.setAdapter(adapter);
		} else {
			errTextView.setVisibility(View.VISIBLE);
		}

	}

	private class VodProgramListViewAdapter extends BaseAdapter {

		private ArrayList<VodProgramData> list;

		public VodProgramListViewAdapter(ArrayList<VodProgramData> list) {
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
				convertView = inflater.inflate(R.layout.rcmd_list_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.updateTxt = (TextView) convertView
						.findViewById(R.id.update);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.viewerCount = (TextView) convertView
						.findViewById(R.id.viewercount);
				viewHolder.programPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.scoreView = (TextView) convertView
						.findViewById(R.id.score);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			VodProgramData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String updateText = temp.updateName;
			if (updateText != null) {
				viewHolder.updateTxt.setText(updateText);
			}
			int viewerCount = temp.playTimes;
			viewHolder.viewerCount.setText(CommonUtils
					.processPlayCount(viewerCount));
			String intro = temp.shortIntro;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			String score = temp.point;
			if (score != null) {
				if (score.length() > 3) {
					score = score.substring(0, 3);
				}
				viewHolder.scoreView.setText(score + "分");
				viewHolder.scoreView.setVisibility(View.VISIBLE);
			} else {
				viewHolder.scoreView.setVisibility(View.GONE);
			}
			String url = temp.pic;
			viewHolder.programPic.setTag(url);
			imageLoaderHelper.loadImage(viewHolder.programPic, url,
					R.drawable.recommend_default);
			return convertView;
		}

	}

	public static class ViewHolder {
		public TextView nameTxt;
		public TextView updateTxt;
		public TextView introTxt;
		public TextView viewerCount;
		public ImageView programPic;
		public TextView scoreView;
	}

	@Override
	public void onRefresh() {
		getDefaultData();
	}

	@Override
	public void onLoadingMore() {
		int size = list.size() + 20;
		getData(columnId, 0, size);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		int listPosition = position - 1;
		if (listPosition == list.size() || listPosition < 0) {
			return;
		}
		VodProgramData tempTv = list.get(listPosition);
		String tvId = tempTv.id;
		String tvTopicId = tempTv.topicId;
		MobclickAgent.onEvent(getActivity(), "jiemu", tempTv.name);
		openProgramDetailActivity(tvId, tvTopicId, tempTv.updateName, 0L);
	}

	private void openProgramDetailActivity(String id, String topicId,
			String updateName, long cpId) {
		Intent intent = new Intent(getActivity(), ProgramNewActivity.class);
		intent.putExtra("programId", id);
		intent.putExtra("topicId", topicId);
		intent.putExtra("cpId", cpId);
		intent.putExtra("updateName", updateName);

		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getDefaultData();
			break;
		default:
			break;
		}
	}
}
