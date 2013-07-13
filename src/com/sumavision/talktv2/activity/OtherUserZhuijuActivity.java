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
import com.sumavision.talktv2.data.ChaseData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.OtherUserChaseParser;
import com.sumavision.talktv2.net.OtherUserChaseRequest;
import com.sumavision.talktv2.task.GetOtherUserChaseTask;
import com.sumavision.talktv2.user.UserOther;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class OtherUserZhuijuActivity extends Activity implements
		OnClickListener, NetConnectionListener, OnRefreshListener {

	private int otherUserId;

	private UserOther userOther;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		otherUserId = intent.getIntExtra("otherUserId", 0);
		userOther = new UserOther();
		setContentView(R.layout.otheruser_zhuiju);
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
	}

	private TextView errText;
	private ProgressBar progressBar;
	private MyListView myZhuijuListView;
	private ArrayList<ChaseData> list = new ArrayList<ChaseData>();

	private void initViews() {
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		myZhuijuListView = (MyListView) findViewById(R.id.listView);
	}

	private GetOtherUserChaseTask getOtherUserChaseTask;

	private void getMyChaseData() {
		if (getOtherUserChaseTask == null) {
			getOtherUserChaseTask = new GetOtherUserChaseTask(this);
			getOtherUserChaseTask.execute(this, new OtherUserChaseRequest(
					otherUserId), new OtherUserChaseParser());
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getMyChaseData(int start, int count) {
		if (getOtherUserChaseTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getOtherUserChaseTask = new GetOtherUserChaseTask(this);
			getOtherUserChaseTask.execute(this, new OtherUserChaseRequest(
					otherUserId), new OtherUserChaseParser());

		}
	}

	private MyChaseAdapter adapter;

	private void updateUI() {
		ArrayList<ChaseData> temp = (ArrayList<ChaseData>) userOther.getChase();
		if (temp != null) {
			list = temp;
			if (list.size() == 0) {
				errText.setText("没有追剧");
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
						.from(OtherUserZhuijuActivity.this);
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
			viewHolder.cancelTextView.setVisibility(View.GONE);
			String url = temp.programPic;
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

	@Override
	public void onNetBegin(String method) {
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("chaseList".equals(method)) {

			if (msg != null) {
				String s = new OtherUserChaseParser().parse(msg, userOther);
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
			getOtherUserChaseTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		getOtherUserChaseTask = null;
		Log.e(TAG, "onCancel callback");
	}

	private void close() {
		if (getOtherUserChaseTask != null) {
			getOtherUserChaseTask.cancel(true);
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
