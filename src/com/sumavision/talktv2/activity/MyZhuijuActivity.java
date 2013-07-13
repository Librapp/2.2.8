package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.ChaseData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.ChaseDeleteRequest;
import com.sumavision.talktv2.net.ChaseParser;
import com.sumavision.talktv2.net.ChaseRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.ResultParser;
import com.sumavision.talktv2.task.ChaseDeleteTask;
import com.sumavision.talktv2.task.GetMyChaseProgramTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class MyZhuijuActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnRefreshListener, OnItemClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myzhuiju);
		initOthers();
		initViews();
		setListeners();
		getMyChaseData();
	}

	private ImageLoaderHelper imageLoaderHelper;

	private void initOthers() {
		OtherCacheData.current().offset = 0;
		OtherCacheData.current().pageCount = 10;
		imageLoaderHelper = new ImageLoaderHelper();
	}

	private void setListeners() {
		errText.setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
		myZhuijuListView.setOnRefreshListener(this);
		myZhuijuListView.setOnItemClickListener(this);
	}

	private TextView errText;
	private ProgressBar progressBar;
	private MyListView myZhuijuListView;
	private ArrayList<ChaseData> list = new ArrayList<ChaseData>();

	private void initViews() {
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		myZhuijuListView = (MyListView) findViewById(R.id.listView);
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
	}

	private GetMyChaseProgramTask getMyChaseTask;

	private void getMyChaseData() {
		if (getMyChaseTask == null) {
			getMyChaseTask = new GetMyChaseProgramTask(this);
			getMyChaseTask.execute(this, new ChaseRequest(), new ChaseParser(
					this));
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getMyChaseData(int start, int count) {
		if (getMyChaseTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getMyChaseTask = new GetMyChaseProgramTask(this);
			getMyChaseTask.execute(this, new ChaseRequest(), new ChaseParser(
					this));
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private MyChaseAdapter adapter;

	private void updateUI() {
		ArrayList<ChaseData> temp = (ArrayList<ChaseData>) UserNow.current()
				.getChase();
		if (temp != null) {
			list = temp;
			if (list.size() == 0) {
				errText.setText("您还没有追剧");
				errText.setVisibility(View.VISIBLE);
			} else {
				errText.setVisibility(View.GONE);
				adapter = new MyChaseAdapter(list);
				myZhuijuListView.setAdapter(adapter);
			}
		} else {
			errText.setVisibility(View.VISIBLE);
		}
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void onRefresh() {
		getMyChaseData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getMyChaseData();
			break;
		case R.id.back:
			close();
			break;
		default:
			break;
		}
	}

	private class MyChaseAdapter extends BaseAdapter {
		private ArrayList<ChaseData> list;

		public MyChaseAdapter(ArrayList<ChaseData> list) {
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
						.from(MyZhuijuActivity.this);
				convertView = inflater
						.inflate(R.layout.mychase_list_item, null);
				viewHolder.pic = (ImageView) convertView.findViewById(R.id.pic);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.introTextView = (TextView) convertView
						.findViewById(R.id.tvName);
				viewHolder.cancelTextView = (ImageView) convertView
						.findViewById(R.id.cancel);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			ChaseData temp = list.get(position);
			String name = temp.programName;
			if (name != null && !name.equals("")) {
				viewHolder.nameTxt.setText(name);
			}

			String intro = temp.latestSubName;
			if (intro != null) {
				viewHolder.introTextView.setText(intro);
			}
			final int mPosition = position;
			final long programId = temp.programId;
			viewHolder.cancelTextView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					deleteIndex = mPosition;
					ChaseData.current.id = programId;
					cancelChase();
				}
			});
			String url = temp.programPic;
			viewHolder.pic.setTag(url);
			imageLoaderHelper.loadImage(viewHolder.pic, url,
					R.drawable.rcmd_list_item_pic_default);
			return convertView;
		}

		public class ViewHolder {
			public ImageView pic;
			public TextView nameTxt;
			public ImageView cancelTextView;
			public TextView introTextView;
		}

	}

	private ChaseDeleteTask chaseDeleTask;

	private void cancelChase() {
		if (chaseDeleTask == null) {
			chaseDeleTask = new ChaseDeleteTask(this);
			chaseDeleTask.execute(this, new ChaseDeleteRequest(),
					new ResultParser());
		}
	}

	private int deleteIndex;

	private void onRemindDeleteOver() {
		list.remove(deleteIndex);
		adapter.notifyDataSetChanged();
	}

	private RelativeLayout connectBg;

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	@Override
	public void onNetBegin(String method) {
		if ("remindDelete".equals(method)) {
			showpb();
		}
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("chaseList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateUI();
			} else {
				progressBar.setVisibility(View.GONE);
				if (list.size() == 0) {
					errText.setVisibility(View.VISIBLE);
				}
				myZhuijuListView.onLoadError();
			}
			getMyChaseTask = null;
		} else if ("chaseDelete".equals(method)) {
			hidepb();
			if (msg != null && "".equals(msg)) {
				DialogUtil.alertToast(getApplicationContext(), "取消成功");
				onRemindDeleteOver();
				saveUserCheseCount();
			} else {
				DialogUtil.alertToast(getApplicationContext(), msg);
			}
			chaseDeleTask = null;
		}
	}

	private void saveUserCheseCount() {

		int c = UserNow.current().chaseCount - 1;
		if (c >= 0) {
			UserNow.current().chaseCount = c;
			SharedPreferences sp = getSharedPreferences("userInfo", 0);
			Editor spEd = sp.edit();
			spEd.putInt("chaseCount", c);
			spEd.commit();
		} else {
			UserNow.current().chaseCount = 0;
		}

	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		getMyChaseTask = null;
		Log.e(TAG, "onCancel callback");
	}

	private void close() {
		if (getMyChaseTask != null) {
			getMyChaseTask.cancel(true);
			Log.e(TAG, " call cancel");
		}
		finish();
	}

	@Override
	public void onLoadingMore() {
		// TODO
		int start = 0;
		int count = list.size() + 10;
		getMyChaseData(start, count);
	}

	private static final String TAG = "MyZhuijuActivity";

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (arg2 - 1 == list.size())
			return;
		String tvId = list.get(arg2 - 1).programId + "";
		String tvTopicId = list.get(arg2 - 1).topicId + "";
		// VodProgramData.current.cpId = 0;
		openProgramDetailActivity(tvId, tvTopicId);
	}

	private void openProgramDetailActivity(String id, String topicId) {
		Intent intent = new Intent(this, ProgramNewActivity.class);
		intent.putExtra("programId", id);
		intent.putExtra("topicId", topicId);
		intent.putExtra("cpId", 0);
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
