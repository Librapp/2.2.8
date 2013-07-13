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
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.RemindDeleteRequest;
import com.sumavision.talktv2.net.RemindParser;
import com.sumavision.talktv2.net.RemindRequest;
import com.sumavision.talktv2.net.ResultParser;
import com.sumavision.talktv2.task.GetMyBookTask;
import com.sumavision.talktv2.task.RemindDeleteTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class MyBookActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnRefreshListener, OnItemClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mybook);
		initOthers();
		initViews();
		setListeners();
		getMyBookData();
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
		myBookListView.setOnRefreshListener(this);
		myBookListView.setOnItemClickListener(this);
	}

	private TextView errText;
	private ProgressBar progressBar;
	private MyListView myBookListView;
	private ArrayList<VodProgramData> list = new ArrayList<VodProgramData>();

	private void initViews() {
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		myBookListView = (MyListView) findViewById(R.id.listView);
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
	}

	private GetMyBookTask getMyBookTask;

	private void getMyBookData() {
		if (getMyBookTask == null) {
			getMyBookTask = new GetMyBookTask();
			getMyBookTask.execute(this, this, new RemindRequest(),
					new RemindParser(this));
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getMyBookData(int start, int count) {
		if (getMyBookTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getMyBookTask = new GetMyBookTask();
			getMyBookTask.execute(this, this, new RemindRequest(),
					new RemindParser(this));
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private MyBookAdapter adapter;

	private void updateUI() {
		ArrayList<VodProgramData> temp = (ArrayList<VodProgramData>) UserNow
				.current().getRemind();
		if (temp != null) {
			list = temp;
			if (list.size() == 0) {
				errText.setText("您还没有预约");
				errText.setVisibility(View.VISIBLE);
			} else {
				errText.setVisibility(View.GONE);
				adapter = new MyBookAdapter(list);
				myBookListView.setAdapter(adapter);
			}
		} else {
			errText.setVisibility(View.VISIBLE);
		}
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void onRefresh() {
		getMyBookData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getMyBookData();
			break;
		case R.id.back:
			close();
			break;
		default:
			break;
		}
	}

	private class MyBookAdapter extends BaseAdapter {
		private ArrayList<VodProgramData> list;

		public MyBookAdapter(ArrayList<VodProgramData> list) {
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
						.from(MyBookActivity.this);
				convertView = inflater.inflate(R.layout.mybook_list_item, null);
				viewHolder.pic = (ImageView) convertView.findViewById(R.id.pic);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.tvNameTextView = (TextView) convertView
						.findViewById(R.id.tvName);
				viewHolder.cancelTextView = (ImageView) convertView
						.findViewById(R.id.cancel);
				viewHolder.timeTextView = (TextView) convertView
						.findViewById(R.id.time);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			VodProgramData temp = list.get(position);
			String name = temp.cpName;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String intro = temp.channelName;
			if (intro != null) {
				viewHolder.tvNameTextView.setText(intro);
			}
			String startTime = temp.startTime;
			String endTime = temp.endTime;
			if (startTime != null && endTime != null) {
				viewHolder.timeTextView.setText(startTime + "-" + endTime);
			}
			final int mPosition = position;
			final long cpId = temp.cpId;
			viewHolder.cancelTextView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					deleteIndex = mPosition;
					// VodProgramData.current.cpId = cpId;
					cancelBook(cpId);
				}
			});
			String url = temp.pic;
			imageLoaderHelper.loadImage(viewHolder.pic, url,
					R.drawable.rcmd_list_item_pic_default);
			return convertView;
		}

	}

	static class ViewHolder {
		public ImageView pic;
		public TextView nameTxt;
		public ImageView cancelTextView;
		public TextView tvNameTextView;
		public TextView timeTextView;
	}

	private RemindDeleteTask remindDeleteTask;

	private void cancelBook(long cpId) {
		if (remindDeleteTask == null) {
			remindDeleteTask = new RemindDeleteTask(this);
			remindDeleteTask.execute(this, new RemindDeleteRequest(cpId),
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
		if ("remindList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateUI();
			} else {
				progressBar.setVisibility(View.GONE);
				if (list.size() == 0) {
					errText.setVisibility(View.VISIBLE);
				}
				myBookListView.onLoadError();
			}
			getMyBookTask = null;
		} else if ("remindDelete".equals(method)) {
			hidepb();
			if (msg != null && "".equals(msg)) {
				DialogUtil.alertToast(getApplicationContext(), "取消成功");
				onRemindDeleteOver();
				saveUserBookCount();
			} else {
				DialogUtil.alertToast(getApplicationContext(), msg);
			}
			remindDeleteTask = null;
		}
	}

	private void saveUserBookCount() {

		int c = UserNow.current().remindCount - 1;
		if (c >= 0) {
			UserNow.current().remindCount = c;
			SharedPreferences sp = getSharedPreferences("userInfo", 0);
			Editor spEd = sp.edit();
			spEd.putInt("remindCount", c);
			spEd.commit();
		} else {
			UserNow.current().remindCount = 0;
		}

	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		getMyBookTask = null;
		Log.e(TAG, "onCancel callback");
	}

	private void close() {
		if (getMyBookTask != null) {
			getMyBookTask.cancel(true);
			Log.e(TAG, " call cancel");
		}
		finish();
	}

	@Override
	public void onLoadingMore() {
		// TODO
		int start = 0;
		int count = list.size() + 10;
		getMyBookData(start, count);
	}

	private static final String TAG = "MyBookActivity";

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (arg2 - 1 == list.size() || arg2 - 1 < 0)
			return;
		String tvId = list.get(arg2 - 1).id + "";
		String tvTopicId = list.get(arg2 - 1).topicId + "";
		// VodProgramData.current.cpId = 0;
		openProgramDetailActivity(tvId, tvTopicId);
	}

	private void openProgramDetailActivity(String id, String topicId) {
		Intent intent = new Intent(this, ProgramNewActivity.class);
		intent.putExtra("programId", id);
		intent.putExtra("topicId", topicId);
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
