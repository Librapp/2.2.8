package com.sumavision.talktv2.activity;

import io.vov.utils.Log;

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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.dao.AccessProgram;
import com.sumavision.talktv2.data.HotPlayProgram;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.HotPlayParser;
import com.sumavision.talktv2.net.HotPlayRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.task.HotPlayTask;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class PlayHistoryActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnItemClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playhistory);
		initOthers();
		initViews();
		setListeners();
		setPlayHistoryList();
		getHotPlay();
	}

	private void initOthers() {
		accessProgramPlayPosition = new AccessProgram(this);
	}

	private ListView playHistoryListView;
	private GridView hotGridView;
	private Button edit;

	private void initViews() {
		playHistoryListView = (ListView) findViewById(R.id.listView);
		edit = (Button) findViewById(R.id.ph_edit);
		hotGridView = (GridView) findViewById(R.id.grid);
		hotGridView.setSelector(R.color.transparent);
		hotGridView.setOnItemClickListener(this);
	}

	private void setListeners() {
		findViewById(R.id.ph_back).setOnClickListener(this);
		edit.setOnClickListener(this);
		playHistoryListView.setOnItemClickListener(this);
	}

	private ArrayList<VodProgramData> list = new ArrayList<VodProgramData>();

	private AccessProgram accessProgramPlayPosition = null;

	private PlayHistoryAdapter adapter;

	private void setPlayHistoryList() {
		list = accessProgramPlayPosition.findAll();
		adapter = new PlayHistoryAdapter(list);
		playHistoryListView.setAdapter(adapter);
		int height = list.size() * CommonUtils.dip2px(this, 70);

		int totalHeight = 0;
		for (int i = 0; i < adapter.getCount(); i++) {
			View listItem = adapter.getView(i, null, playHistoryListView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		if (height == 0) {
			edit.setVisibility(View.GONE);
		}
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, totalHeight + 20);
		playHistoryListView.setLayoutParams(params);
	}

	private class PlayHistoryAdapter extends BaseAdapter {
		private final ArrayList<VodProgramData> list;

		public PlayHistoryAdapter(ArrayList<VodProgramData> list) {
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
						.from(PlayHistoryActivity.this);
				convertView = inflater.inflate(R.layout.playhistory_list_item,
						null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.playbtn = (ImageView) convertView
						.findViewById(R.id.viewercount);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			VodProgramData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
				if (OtherCacheData.current().isDebugMode)
					Log.e("PlayHistoryAdapter-name", name);
			}
			String intro = temp.updateName;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			final int mPosition = position;
			viewHolder.playbtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					VodProgramData tempV = list.get(mPosition);
					if (!canDelete) {
						MobclickAgent.onEvent(PlayHistoryActivity.this,
								"historypr ");
						continuePlay(tempV);
					} else {
						accessProgramPlayPosition.delete(tempV);
						list.remove(mPosition);
						adapter.notifyDataSetChanged();
						int height = list.size()
								* CommonUtils.dip2px(PlayHistoryActivity.this,
										100);
						if (height == 0) {
							edit.setVisibility(View.GONE);
						}
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
								RelativeLayout.LayoutParams.FILL_PARENT, height);
						playHistoryListView.setLayoutParams(params);
					}
				}
			});
			if (!canDelete) {
				viewHolder.playbtn
						.setImageResource(R.drawable.playhistory_play);
			} else {
				viewHolder.playbtn
						.setImageResource(R.drawable.playhistory_delete);
			}

			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public TextView introTxt;
			public ImageView playbtn;
		}
	}

	private void continuePlay(VodProgramData temp) {
		// VodProgramData.current.topicId = temp.topicId;
		// VodProgramData.current.id = temp.id;
		// VodProgramData.current.updateName = temp.updateName;

		openLiveActivity(temp.dbUrl, temp.name, temp.topicId, temp.id,
				temp.updateName);

	}

	private HotPlayTask hotPlayTask;

	private void getHotPlay() {
		if (hotPlayTask == null) {
			hotPlayTask = new HotPlayTask(this);
			hotPlayTask
					.execute(this, new HotPlayRequest(), new HotPlayParser());
		}
	}

	private ArrayList<VodProgramData> hotPlayList;

	private void updateHotPlayGrid() {
		ArrayList<VodProgramData> temp = HotPlayProgram.current().hotProgramList;
		if (temp != null) {
			hotPlayList = temp;
			hotGridView.setAdapter(new HotGridAdapter(temp));
			int height = (hotPlayList.size() / 2 + 1)
					* CommonUtils.dip2px(this, 30);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, height);
			hotGridView.setLayoutParams(params);
			hotGridView.setNumColumns(2);
			hotGridView.setColumnWidth(CommonUtils.dip2px(this, 80));
		}
	}

	private class HotGridAdapter extends BaseAdapter {
		private final ArrayList<VodProgramData> list;

		public HotGridAdapter(ArrayList<VodProgramData> list) {
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
						.from(PlayHistoryActivity.this);
				convertView = inflater.inflate(
						R.layout.playhistory_hot_list_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			VodProgramData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}

			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
		}
	}

	private boolean canDelete = false;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ph_edit:
			canDelete = !canDelete;
			if (canDelete) {
				edit.setText("完成");
			} else {
				edit.setText("编辑");
			}
			adapter.notifyDataSetChanged();
			break;
		case R.id.err_text:
			break;
		case R.id.ph_back:
			MobclickAgent.onEvent(this, "historyback");
			close();
			break;
		default:
			break;
		}
	}

	private void close() {
		finish();
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("hotPlay".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateHotPlayGrid();
			} else {
			}
			hotPlayTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.getId() == R.id.grid) {
			MobclickAgent.onEvent(this, "historyhot");
			VodProgramData temp = hotPlayList.get(position);
			// VodProgramData.current.cpId = 0;
			openProgramDetailActivity(temp.id, temp.topicId);
		} else {
			VodProgramData temp = list.get(position);
			// VodProgramData.current.cpId = 0;
			openProgramDetailActivity(temp.id, temp.topicId);
		}
	}

	private void openProgramDetailActivity(String id, String topicId) {
		Intent intent = new Intent(this, ProgramNewActivity.class);
		intent.putExtra("programId", id);
		intent.putExtra("topicId", topicId);
		intent.putExtra("cpId", 0);
		startActivity(intent);
	}

	private void openLiveActivity(String path, String title, String topicId,
			String id, String updateName) {
		Intent intent = new Intent(this, NewLivePlayerActivity.class);
		intent.putExtra("path", path);
		intent.putExtra("playType", 2);// 点播
		intent.putExtra("title", title);
		intent.putExtra("topicId", topicId);
		intent.putExtra("id", id);
		intent.putExtra("updateName", updateName);
		// intent.putExtra("nameHolder", updateName);

		if (path != null) {
			startActivity(intent);
		} else {
			DialogUtil.alertToast(getApplicationContext(), "暂时无法播放");
		}
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
