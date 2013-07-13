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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.FansParser;
import com.sumavision.talktv2.net.FansRequest;
import com.sumavision.talktv2.net.GuanzhuAddRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.ResultParser;
import com.sumavision.talktv2.services.NotificationService;
import com.sumavision.talktv2.task.AddGuanzhuTask;
import com.sumavision.talktv2.task.GetMyFansTask;
import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class MyFansActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnRefreshListener, OnItemClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myfans);
		initOthers();
		initViews();
		setListeners();
		getMyFansData();
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
		myFansListView.setOnRefreshListener(this);
		myFansListView.setOnItemClickListener(this);
	}

	private TextView errText;
	private ProgressBar progressBar;
	private MyListView myFansListView;
	private ArrayList<User> list = new ArrayList<User>();

	private void initViews() {
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		myFansListView = (MyListView) findViewById(R.id.listView);
	}

	private GetMyFansTask getMyFansTask;

	private void getMyFansData() {
		if (getMyFansTask == null) {
			getMyFansTask = new GetMyFansTask(this);
			getMyFansTask
					.execute(this, new FansRequest(), new FansParser(this));
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getMyFansData(int start, int count) {
		if (getMyFansTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getMyFansTask = new GetMyFansTask(this);
			getMyFansTask
					.execute(this, new FansRequest(), new FansParser(this));
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private MyFansAdapter adapter;

	private void updateUI() {
		ArrayList<User> temp = (ArrayList<User>) UserNow.current()
				.getFansList();
		if (temp != null) {
			list = temp;
			if (list.size() == 0) {
				errText.setText("您还没有粉丝");
				errText.setVisibility(View.VISIBLE);
			} else {
				errText.setVisibility(View.GONE);
				adapter = new MyFansAdapter(list);
				myFansListView.setAdapter(adapter);
			}
		} else {
			errText.setVisibility(View.VISIBLE);
		}
		progressBar.setVisibility(View.GONE);
	}

	private int addPosition;

	private void onAddGuanzhuOver() {
		list.get(addPosition).isFriend = 1;
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onRefresh() {
		getMyFansData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getMyFansData();
			break;
		case R.id.back:
			close();
			break;
		default:
			break;
		}
	}

	private class MyFansAdapter extends BaseAdapter {
		private ArrayList<User> list;

		public MyFansAdapter(ArrayList<User> list) {
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
						.from(MyFansActivity.this);
				convertView = inflater
						.inflate(R.layout.my_fans_list_item, null);
				viewHolder.headpic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.fellowTextView = (ImageView) convertView
						.findViewById(R.id.guanzhu);
				viewHolder.sendMsgTextView = (ImageView) convertView
						.findViewById(R.id.privatemsg);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final User temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String intro = temp.intro;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			String signature = temp.signature;
			if (signature != null) {
				viewHolder.introTxt.setText(signature);
			}
			String url = temp.iconURL;
			imageLoaderHelper.loadImage(viewHolder.headpic, url,
					R.drawable.list_headpic_default);
			final int id = temp.userId;
			viewHolder.sendMsgTextView
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// UserOther.current().userID = id;
							// UserOther.current().name = temp.name;
							openSendPrivateMsg(id, temp.name);
						}
					});
			final int mPosition = position;
			if (temp.isFriend == 0) {
				viewHolder.fellowTextView
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								addPosition = mPosition;
								addGuanzhu(id);
							}
						});
			} else {
				viewHolder.fellowTextView.setVisibility(View.GONE);
			}
			return convertView;
		}

		public class ViewHolder {
			public ImageView headpic;
			public TextView nameTxt;
			public ImageView fellowTextView;
			public TextView introTxt;
			public ImageView sendMsgTextView;
		}

	}

	private AddGuanzhuTask addGuanzhuTask;

	private void addGuanzhu(int id) {
		if (addGuanzhuTask == null) {
			// UserOther.current().userID = id;
			addGuanzhuTask = new AddGuanzhuTask(this);
			addGuanzhuTask.execute(this, new GuanzhuAddRequest(id),
					new ResultParser());
		}
	}

	private RelativeLayout connectBg;

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	private void openSendPrivateMsg(int id, String name) {
		Intent intent = new Intent(this, UserMailActivity.class);
		intent.putExtra("otherUserName", name);
		intent.putExtra("otherUserId", id);
		startActivity(intent);
	}

	@Override
	public void onNetBegin(String method) {
		if ("guanZhuAdd".equals(method)) {
			showpb();
		}
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("fensiList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateUI();
			} else {
				progressBar.setVisibility(View.GONE);
				if (list.size() == 0) {
					errText.setVisibility(View.VISIBLE);
				}
				myFansListView.onLoadError();
			}
			getMyFansTask = null;
		} else if ("guanZhuAdd".equals(method)) {
			hidepb();
			if (msg != null && "".equals(msg)) {
				DialogUtil.alertToast(getApplication(), "添加关注成功!");
				onAddGuanzhuOver();
			} else {
				DialogUtil.alertToast(getApplication(), msg);
			}
			addGuanzhuTask = null;
		}
		if (UserNow.current().getNewBadge() != null) {
			for (int i = 0; i < UserNow.current().getNewBadge().size(); i++) {
				String name = UserNow.current().getNewBadge().get(i).name;
				if (name != null) {
					DialogUtil.showBadgeAddToast(MyFansActivity.this, name);
				}
			}
			UserNow.current().setNewBadge(null);
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		getMyFansTask = null;
		Log.e(TAG, "onCancel callback");
	}

	private void close() {
		if (getMyFansTask != null) {
			getMyFansTask.cancel(true);
			Log.e(TAG, " call cancel");
		}
		finish();
	}

	@Override
	public void onLoadingMore() {
		// TODO
		int start = 0;
		int end = list.size() + 10;
		getMyFansData(start, end);
	}

	public static final int MY_FANS = 1;

	private void openOtherUserCenterActivity(int id) {
		Intent intent = new Intent(this, OtherUserCenterActivity.class);
		intent.putExtra("id", id);
		intent.putExtra("from", MY_FANS);
		startActivityForResult(intent, MY_FANS);
	}

	private static final String TAG = "MyFansActivity";

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position - 1 == list.size() || position - 1 < 0)
			return;
		int otherUserId = list.get(position - 1).userId;
		openOtherUserCenterActivity(otherUserId);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		clearNotificationInfo();
		if (!myFansListView.isGetData()) {
			getMyFansData();
			if (list != null && list.size() != 0) {
				myFansListView.setRefreshState();
			}
		}
	}

	/** 清除推送信息 */
	private void clearNotificationInfo() {
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(NotificationService.NOTIFICATION_ID_FELLOW);
		SharedPreferences pushMsgPreferences = getSharedPreferences(
				Constants.pushMessage, 0);
		Editor pushMsgEditor = pushMsgPreferences.edit();
		pushMsgEditor.putBoolean(Constants.key_fans, false);
		pushMsgEditor.putBoolean(Constants.key_msg_new, false);
		pushMsgEditor.commit();
	}
}
