package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
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
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.MailData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.MailBoxParser;
import com.sumavision.talktv2.net.MailBoxRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.services.NotificationService;
import com.sumavision.talktv2.task.GetMyPrivateMsgTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.umeng.analytics.MobclickAgent;

public class MyPrivateMsgActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnRefreshListener, OnItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_privatemsg);
		initOthers();
		initViews();
		setListeners();
		getMyPrivateMsgData();
		clearNotificationInfo();
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
		myPrivateMsgListView.setOnRefreshListener(this);
		myPrivateMsgListView.setOnItemClickListener(this);
	}

	private TextView errText;
	private ProgressBar progressBar;
	private MyListView myPrivateMsgListView;
	private ArrayList<MailData> list = new ArrayList<MailData>();

	private void initViews() {
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		myPrivateMsgListView = (MyListView) findViewById(R.id.listView);
	}

	private GetMyPrivateMsgTask getMyPrivateMsgTask;

	private void getMyPrivateMsgData() {
		if (getMyPrivateMsgTask == null) {
			getMyPrivateMsgTask = new GetMyPrivateMsgTask(this);
			getMyPrivateMsgTask.execute(this, new MailBoxRequest(),
					new MailBoxParser(this));
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getMyPrivateMsgData(int start, int count) {
		if (getMyPrivateMsgTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getMyPrivateMsgTask = new GetMyPrivateMsgTask(this);
			getMyPrivateMsgTask.execute(this, new MailBoxRequest(),
					new MailBoxParser(this));
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void updateUI() {
		ArrayList<MailData> temp = (ArrayList<MailData>) UserNow.current()
				.getMail();
		if (temp != null) {
			list = temp;
			if (list.size() == 0) {
				errText.setText("您还没有私信");
			} else {
				errText.setVisibility(View.GONE);
				adapter = new MyMsgsAdapter(list);
				myPrivateMsgListView.setAdapter(adapter);
			}
		} else {
			errText.setVisibility(View.VISIBLE);
		}
		progressBar.setVisibility(View.GONE);
	}

	private MyMsgsAdapter adapter;

	@Override
	public void onRefresh() {
		getMyPrivateMsgData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getMyPrivateMsgData();
			break;
		case R.id.back:
			close();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PRIVATE_MSG) {
			adapter.notifyDataSetChanged();
		}
	}

	public static final int PRIVATE_MSG = 1;

	private class MyMsgsAdapter extends BaseAdapter {
		private ArrayList<MailData> list;

		public MyMsgsAdapter(ArrayList<MailData> list) {
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
						.from(MyPrivateMsgActivity.this);
				convertView = inflater.inflate(
						R.layout.my_privatemsg_list_item, null);
				viewHolder.headpic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.lastmsg);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			MailData temp = list.get(position);
			String name = temp.sUserName;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String intro = temp.content;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			String timeValue = temp.timeStemp;
			if (timeValue != null) {
				viewHolder.time.setText(timeValue);
			}
			String url = temp.sUserPhoto;
			imageLoaderHelper.loadImage(viewHolder.headpic, url,
					R.drawable.list_headpic_default);
			return convertView;
		}

		public class ViewHolder {
			public ImageView headpic;
			public TextView nameTxt;
			public TextView introTxt;
			public TextView time;
		}

	}

	private void openPrivateMsgPage(int id, String name, String iconURL) {
		Intent intent = new Intent(this, UserMailActivity.class);
		intent.putExtra("otherUserId", id);
		intent.putExtra("otherUserName", name);
		intent.putExtra("otherUserIconURL", iconURL);
		intent.putExtra("from", PRIVATE_MSG);
		startActivityForResult(intent, PRIVATE_MSG);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position != 0) {
			if (position - 1 == list.size() || position - 1 < 0)
				return;
			int sid = list.get(position - 1).sid;
			// UserOther.current().name = list.get(position - 1).sUserName;
			// UserOther.current().userID = sid;
			openPrivateMsgPage(sid, list.get(position - 1).sUserName,
					list.get(position - 1).sUserPhoto);
		}
	}

	@Override
	public void onNetBegin(String method) {
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("mailUserList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateUI();
			} else {
				progressBar.setVisibility(View.GONE);
				if (list.size() == 0) {
					errText.setVisibility(View.VISIBLE);
				}
				myPrivateMsgListView.onLoadError();
			}
			getMyPrivateMsgTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		getMyPrivateMsgTask = null;
		Log.e(TAG, "onCancel callback");
	}

	private void close() {
		if (getMyPrivateMsgTask != null) {
			getMyPrivateMsgTask.cancel(true);
			Log.e(TAG, " call cancel");
		}
		finish();
	}

	@Override
	public void onLoadingMore() {
		// TODO
		int start = 0;
		int count = list.size() + 10;
		getMyPrivateMsgData(start, count);
	}

	private static final String TAG = "MyPrivateMsgActivity";

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		clearNotificationInfo();
		if (!myPrivateMsgListView.isGetData()) {
			getMyPrivateMsgData();
			if (list != null && list.size() != 0) {
				myPrivateMsgListView.setRefreshState();
			}
		}
	}

	/** 清除推送信息 */
	private void clearNotificationInfo() {
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(NotificationService.NOTIFICATION_ID_PRIVATE_MSG);
		SharedPreferences pushMsgPreferences = getSharedPreferences(
				Constants.pushMessage, 0);
		Editor pushMsgEditor = pushMsgPreferences.edit();
		pushMsgEditor.putBoolean(Constants.key_privateMsg, false);
		pushMsgEditor.putBoolean(Constants.key_msg_new, false);
		pushMsgEditor.commit();
	}

}
