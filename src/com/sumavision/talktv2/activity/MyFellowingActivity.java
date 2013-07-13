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
import com.sumavision.talktv2.net.FriendParser;
import com.sumavision.talktv2.net.FriendRequest;
import com.sumavision.talktv2.net.GuanzhuCancelRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.ResultParser;
import com.sumavision.talktv2.task.DeleteGuanzhuTask;
import com.sumavision.talktv2.task.GetMyFellowingTask;
import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @author 我的关注页面 2012 12-18
 * 
 */
public class MyFellowingActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnRefreshListener, OnItemClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myfellowing);
		initOthers();
		initViews();
		setListeners();
		getMyFellowingData();
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
		myFellowingListView.setOnRefreshListener(this);
		myFellowingListView.setOnItemClickListener(this);
	}

	private TextView errText;
	private ProgressBar progressBar;
	private MyListView myFellowingListView;
	private ArrayList<User> list = new ArrayList<User>();

	private void initViews() {
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		myFellowingListView = (MyListView) findViewById(R.id.listView);
	}

	private GetMyFellowingTask getMyFellowingTask;

	private void getMyFellowingData() {
		if (getMyFellowingTask == null) {
			getMyFellowingTask = new GetMyFellowingTask();
			getMyFellowingTask.execute(this, this, new FriendRequest(),
					new FriendParser(this));
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getMyFellowingData(int start, int count) {
		if (getMyFellowingTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getMyFellowingTask = new GetMyFellowingTask();
			getMyFellowingTask.execute(this, this, new FriendRequest(),
					new FriendParser(this));
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void updateUI() {
		ArrayList<User> temp = (ArrayList<User>) UserNow.current().getFriend();
		if (temp != null) {
			list = temp;
			if (list.size() == 0) {
				errText.setText("你还没有粉过任何人");
				errText.setVisibility(View.VISIBLE);
			} else {
				errText.setVisibility(View.GONE);
				MyFellowingAdapter adapter = new MyFellowingAdapter(list);
				myFellowingListView.setAdapter(adapter);
			}
		} else {
			errText.setVisibility(View.VISIBLE);
		}
		progressBar.setVisibility(View.GONE);
	}

	private int deletePosition;

	private void OnDeleteOver() {
		ArrayList<User> temp = (ArrayList<User>) UserNow.current().getFriend();
		if (temp != null) {
			if (deletePosition >= 0 && deletePosition < temp.size()) {
				temp.remove(deletePosition);

				list = temp;
				if (list.size() == 0) {
					errText.setText("你还没有粉过任何人");
				} else {
					errText.setVisibility(View.GONE);
					MyFellowingAdapter adapter = new MyFellowingAdapter(list);
					myFellowingListView.setAdapter(adapter);
				}
			} else {
				errText.setVisibility(View.VISIBLE);
			}
			progressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getMyFellowingData();
			break;
		case R.id.back:
			close();
			break;
		default:
			break;
		}
	}

	@Override
	public void onRefresh() {
		getMyFellowingData();
	}

	private class MyFellowingAdapter extends BaseAdapter {
		private ArrayList<User> list;

		public MyFellowingAdapter(ArrayList<User> list) {
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
						.from(MyFellowingActivity.this);
				convertView = inflater.inflate(R.layout.myfellowing_list_item,
						null);
				viewHolder.headpic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.cancelFellowTextView = (ImageView) convertView
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
			if (intro != null && !intro.equals("")) {
				viewHolder.introTxt.setText(intro);
			} else {
				viewHolder.introTxt.setText("这个家伙神马也木有留下");
			}
			String signature = temp.signature;
			if (signature != null) {
				viewHolder.introTxt.setText(signature);
			}
			String url = temp.iconURL;
			imageLoaderHelper.loadImage(viewHolder.headpic, url,
					R.drawable.list_headpic_default);
			final int id = temp.userId;
			final int mPosition = position;
			viewHolder.sendMsgTextView
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// UserOther.current().userID = id;
							// UserOther.current().name = temp.name;
							openSendPrivateMsg(id, temp.name);
						}
					});
			viewHolder.cancelFellowTextView
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							deletePosition = mPosition;
							deleteGuanzhu(id);
						}
					});

			return convertView;
		}

		public class ViewHolder {
			public ImageView headpic;
			public TextView nameTxt;
			public ImageView cancelFellowTextView;
			public TextView introTxt;
			public ImageView sendMsgTextView;
		}

	}

	private DeleteGuanzhuTask deleteGuanzhuTask;

	private void deleteGuanzhu(int id) {
		if (deleteGuanzhuTask == null) {
			// UserOther.current().userID = id;
			deleteGuanzhuTask = new DeleteGuanzhuTask(this);
			deleteGuanzhuTask.execute(this, new GuanzhuCancelRequest(id),
					new ResultParser());
		}
	}

	private void openSendPrivateMsg(int id, String name) {
		Intent intent = new Intent(this, UserMailActivity.class);
		intent.putExtra("otherUserName", name);
		intent.putExtra("otherUserId", id);
		startActivity(intent);
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
		if ("guanZhuCancel".equals(method)) {
			showpb();
		}
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("guanzhuList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateUI();
			} else {
				progressBar.setVisibility(View.GONE);
				if (list.size() == 0) {
					errText.setVisibility(View.VISIBLE);
				}
				myFellowingListView.onLoadError();
			}
			getMyFellowingTask = null;
		} else if ("guanZhuCancel".equals(method)) {
			hidepb();
			if (msg != null && "".equals(msg)) {
				DialogUtil.alertToast(getApplication(), "取消关注成功");
				OnDeleteOver();
			} else {
				DialogUtil.alertToast(getApplication(), msg);
			}
			deleteGuanzhuTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		getMyFellowingTask = null;
		Log.e(TAG, "onCancel callback");
	}

	private void close() {
		if (getMyFellowingTask != null) {
			getMyFellowingTask.cancel(true);
			Log.e(TAG, " call cancel");
		}
		finish();
	}

	@Override
	public void onLoadingMore() {
		// TODO
		int start = 0;
		int count = list.size() + 10;
		getMyFellowingData(start, count);
	}

	private static final String TAG = "MyFellowingActivity";

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void openOtherUserCenterActivity(int id) {
		Intent intent = new Intent(this, OtherUserCenterActivity.class);
		intent.putExtra("id", id);
		startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position - 1 == list.size() || position - 1 < 0)
			return;
		int otherUserId = list.get(position - 1).userId;
		openOtherUserCenterActivity(otherUserId);
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
