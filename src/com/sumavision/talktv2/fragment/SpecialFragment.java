package com.sumavision.talktv2.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
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
import com.sumavision.talktv2.activity.SpecicalActivity;
import com.sumavision.talktv2.data.ColumnData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.SubColumnListRequest;
import com.sumavision.talktv2.task.GetSpecialDataTask;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.talktv2.utils.Constants;
import com.umeng.analytics.MobclickAgent;

public class SpecialFragment extends Fragment implements NetConnectionListener,
		OnRefreshListener, OnItemClickListener {

	private int columnId = Constants.column_id_special;
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
			getDefaultData();
			needLoadData = true;
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
	}

	private void getDefaultData() {
		getData(columnId, 0, 20);
	}

	private GetSpecialDataTask getSpecicalDataTask;
	ArrayList<ColumnData> tempSpeicalList = new ArrayList<ColumnData>();
	private ArrayList<ColumnData> specialList = new ArrayList<ColumnData>();

	private void getData(int id, int offset, int count) {
		if (getSpecicalDataTask == null) {
			getSpecicalDataTask = new GetSpecialDataTask(this);
			tempSpeicalList = new ArrayList<ColumnData>();
			getSpecicalDataTask.execute(getActivity(),
					new SubColumnListRequest(id, offset, count),
					tempSpeicalList);
			if (specialList.size() == 0) {
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
		if ("subColumnList".equals(method)) {
			if (msg != null && msg.equals("")) {
				// updateSpecialProgramList();
				updateSpecialProgramListNew();
			} else {
				if (specialList != null && specialList.size() == 0) {
					errTextView.setVisibility(View.VISIBLE);
				}
				progressBar.setVisibility(View.GONE);
				listView.onLoadError();
			}
			// recommandSpecialTask = null;
			getSpecicalDataTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	private void updateSpecialProgramListNew() {
		ArrayList<ColumnData> temp = tempSpeicalList;
		if (temp != null && temp.size() > 0) {
			specialList = temp;
			SpecialProgramListViewAdapter adapter = new SpecialProgramListViewAdapter(
					specialList, getActivity());
			listView.setAdapter(adapter);
		}
		if (specialList.size() == 0) {
			errTextView.setVisibility(View.VISIBLE);
		} else {
			errTextView.setVisibility(View.GONE);
		}
		progressBar.setVisibility(View.GONE);
	}

	private class SpecialProgramListViewAdapter extends BaseAdapter {

		private ArrayList<ColumnData> list;
		private Context context;

		public SpecialProgramListViewAdapter(ArrayList<ColumnData> list,
				Context context) {
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
				convertView = inflater.inflate(R.layout.rcmd_special_list_item,
						null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.programPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.viewCountText = (TextView) convertView
						.findViewById(R.id.viewercount);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			ColumnData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String intro = temp.intro;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			int viewerCount = temp.playTimes;
			viewHolder.viewCountText.setText(CommonUtils
					.processPlayCount(viewerCount));
			String url = temp.pic;
			viewHolder.programPic.setTag(url);
			/*
			 * loadListImage(viewHolder.programPic, url,
			 * R.drawable.recommend_default);
			 */
			imageLoaderHelper.loadImage(viewHolder.programPic, url,
					R.drawable.recommend_default);
			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public TextView introTxt;
			public ImageView programPic;
			public TextView viewCountText;
		}
	}

	@Override
	public void onRefresh() {
		getDefaultData();
	}

	@Override
	public void onLoadingMore() {
		int size = specialList.size() + 20;
		getData(columnId, 0, size);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		int listPosition = position - 1;
		if (listPosition == specialList.size() || listPosition < 0) {
			return;
		}
		MobclickAgent.onEvent(getActivity(), "jiemu",
				specialList.get(listPosition).name);
		int specialId = specialList.get(listPosition).id;
		String title = specialList.get(listPosition).name;
		openSepecicalActivity(specialId, title);
	}

	private void openSepecicalActivity(int id, String title) {
		Intent intent = new Intent(getActivity(), SpecicalActivity.class);
		intent.putExtra("id", id);
		intent.putExtra("title", title);
		startActivity(intent);
	}

}
