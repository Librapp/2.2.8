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
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.task.ColumnVideoListTask;
import com.sumavision.talktv2.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

public class SpecicalActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnRefreshListener, OnItemClickListener {
	private int id;
	private String title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		id = getIntent().getIntExtra("id", 0);
		title = getIntent().getStringExtra("title");
		setContentView(R.layout.special);
		initOthers();
		initViews();
		setListeners();
		getSpecialData(id, 0, 10);
	}

	private ImageLoaderHelper imageLoader;

	private void initOthers() {
		imageLoader = new ImageLoaderHelper();
	}

	private void setListeners() {
		errText.setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
		mySpecialListView.setOnRefreshListener(this);
		mySpecialListView.setOnItemClickListener(this);
	}

	private TextView errText;
	private ProgressBar progressBar;
	private MyListView mySpecialListView;
	private ArrayList<VodProgramData> list = new ArrayList<VodProgramData>();

	private void initViews() {
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		mySpecialListView = (MyListView) findViewById(R.id.listView);
		if (title != null) {
			((TextView) findViewById(R.id.title)).setText(title);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getSpecialData(id, 0, 10);
			break;
		case R.id.back:
			close();
			break;
		default:
			break;
		}
	}

	private ColumnVideoListTask specialTask;

	private static final int INVIALID = 33;

	private void getSpecialData(int id, int offset, int count) {
		if (specialTask == null) {
			specialTask = new ColumnVideoListTask();
			specialTask.execute(this, this, list, id, offset, count, INVIALID);
			if (list.size() == 0) {
				progressBar.setVisibility(View.VISIBLE);
			}
			errText.setVisibility(View.GONE);
		}
	}

	private void updateSpecialList() {
		if (list.size() != 0) {
			progressBar.setVisibility(View.GONE);
			VodProgramListViewAdapter adapter = new VodProgramListViewAdapter(
					list);
			mySpecialListView.setAdapter(adapter);
		} else {
			errText.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onRefresh() {
		getSpecialData(id, 0, 10);

	}

	@Override
	public void onLoadingMore() {
		int start = 0;
		int count = list.size() + 10;
		getSpecialData(id, start, count);
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
				updateSpecialList();
			} else {
				errText.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				mySpecialListView.onLoadError();
			}
			specialTask = null;
		}
	}

	@Override
	public void onCancel(String method) {

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
				LayoutInflater inflater = LayoutInflater
						.from(SpecicalActivity.this);
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
			}
			String url = temp.pic;
			imageLoader.loadImage(viewHolder.programPic, url,
					R.drawable.rcmd_list_item_pic_default);
			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public TextView updateTxt;
			public TextView introTxt;
			public TextView viewerCount;
			public ImageView programPic;
			public TextView scoreView;
		}
	}

	private void close() {
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position - 1 == list.size() || position - 1 < 0) {
			return;
		}
		VodProgramData temp = list.get(position - 1);
		String programId = temp.id;
		String topicId = temp.topicId;
		// VodProgramData.current.cpId = 0;
		// VodProgramData.current.updateName = temp.updateName;
		openProgramDetailActivity(programId, topicId, temp.updateName);
	}

	private void openProgramDetailActivity(String id, String topicId,
			String updateName) {
		Intent intent = new Intent(this, ProgramNewActivity.class);
		intent.putExtra("programId", id);
		intent.putExtra("topicId", topicId);
		intent.putExtra("cpId", 0);
		intent.putExtra("updateName", updateName);
		startActivity(intent);

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
}
