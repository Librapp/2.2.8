package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.OtherUserRemindParser;
import com.sumavision.talktv2.net.OtherUserRemindRequest;
import com.sumavision.talktv2.task.GetOtherRemindTask;
import com.sumavision.talktv2.user.UserOther;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class OtherUserBookActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnRefreshListener {

	private int otherUserId;

	private UserOther userOther;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		otherUserId = intent.getIntExtra("otherUserId", 0);
		userOther = new UserOther();
		setContentView(R.layout.otheruser_book);
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

	}

	private TextView errText;
	private ProgressBar progressBar;
	private MyListView myBookListView;
	private ArrayList<VodProgramData> list = new ArrayList<VodProgramData>();

	private void initViews() {
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		myBookListView = (MyListView) findViewById(R.id.listView);
	}

	private GetOtherRemindTask getOtherRemindTask;

	private void getMyBookData() {
		if (getOtherRemindTask == null) {
			getOtherRemindTask = new GetOtherRemindTask(this);
			getOtherRemindTask.execute(this, new OtherUserRemindRequest(
					otherUserId), new OtherUserRemindParser());
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getMyBookData(int start, int count) {
		if (getOtherRemindTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getOtherRemindTask = new GetOtherRemindTask(this);
			getOtherRemindTask.execute(this, new OtherUserRemindRequest(
					otherUserId), new OtherUserRemindParser());
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private MyBookAdapter adapter;

	private void updateUI() {
		ArrayList<VodProgramData> temp = (ArrayList<VodProgramData>) userOther
				.getRemind();
		if (temp != null) {
			list = temp;
			if (list.size() == 0) {
				errText.setText("没有预约");
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
						.from(OtherUserBookActivity.this);
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
			viewHolder.cancelTextView.setVisibility(View.GONE);

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

	@Override
	public void onNetBegin(String method) {
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("remindList".equals(method)) {
			if (msg != null) {
				String s = new OtherUserRemindParser().parse(msg, userOther);
				if (s != null && s.equals("")) {
					updateUI();
				} else {
					DialogUtil.alertToast(getApplication(), s);
				}
			} else {
				progressBar.setVisibility(View.GONE);
				if (list.size() == 0) {
					errText.setVisibility(View.VISIBLE);
				}
			}
			getOtherRemindTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		getOtherRemindTask = null;
		Log.e(TAG, "onCancel callback");
	}

	private void close() {
		if (getOtherRemindTask != null) {
			getOtherRemindTask.cancel(true);
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
