package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.EventData;
import com.sumavision.talktv2.net.GuanzhuAddRequest;
import com.sumavision.talktv2.net.GuanzhuCancelRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.OtherSpaceParser;
import com.sumavision.talktv2.net.OtherSpaceRequest;
import com.sumavision.talktv2.net.ResultParser;
import com.sumavision.talktv2.task.AddGuanzhuTask;
import com.sumavision.talktv2.task.DeleteGuanzhuTask;
import com.sumavision.talktv2.task.GetOtherUserSpaceTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.user.UserOther;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @author jianghao 其他用户中心 2012 12 -30
 * 
 */
public class OtherUserCenterActivity extends Activity implements
		OnClickListener, NetConnectionListener, OnItemClickListener {
	// 通信框
	private RelativeLayout connectBg;

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	private int id;
	private String otherUserName;

	private int from;

	// 其他页面传过来的头像
	private String iconURL;

	private UserOther userOther;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.otherusercenter);
		userOther = new UserOther();
		Intent i = getIntent();
		id = i.getIntExtra("id", 0);
		if (i.hasExtra("iconURL"))
			iconURL = i.getStringExtra("iconURL");
		if (i.hasExtra("from")) {
			from = i.getIntExtra("from", 0);
		}
		initUtils();
		initView();
		setListeners();
		getUserInfo();
	}

	private AsyncImageLoader imageLoader;

	private void initUtils() {
		imageLoader = new AsyncImageLoader();
	}

	private void setListeners() {
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.privatemsg).setOnClickListener(this);
		guanzhu.setOnClickListener(this);

		findViewById(R.id.book_layout).setOnClickListener(this);
		findViewById(R.id.zhuiju_layout).setOnClickListener(this);
		findViewById(R.id.comment_layout).setOnClickListener(this);
		eventListView.setOnItemClickListener(this);
	}

	private ImageView headPic;
	private TextView userNameTextView;
	private TextView userLevelTextView;
	private TextView signatureTextView;
	private ImageView gender;
	private ListView eventListView;

	private TextView zhuijuTextView;
	private TextView bookTextView;
	private TextView commentTextView;

	private ImageView guanzhu;

	private TextView errText;
	private ProgressBar progressBar;
	private ScrollView scrollView;

	private void initView() {
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);

		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		scrollView = (ScrollView) findViewById(R.id.scollView);
		headPic = (ImageView) findViewById(R.id.head_pic);
		userNameTextView = (TextView) findViewById(R.id.name);
		userLevelTextView = (TextView) findViewById(R.id.level);
		gender = (ImageView) findViewById(R.id.gender);
		signatureTextView = (TextView) findViewById(R.id.ou_signnature);
		eventListView = (ListView) findViewById(R.id.listView);

		zhuijuTextView = (TextView) findViewById(R.id.zhuiju);
		bookTextView = (TextView) findViewById(R.id.book);
		commentTextView = (TextView) findViewById(R.id.comment);

		guanzhu = (ImageView) findViewById(R.id.guanzhu);

	}

	private GetOtherUserSpaceTask getOtherUserSpaceTask;
	private boolean hasData;

	private void getUserInfo() {
		if (getOtherUserSpaceTask == null) {
			getOtherUserSpaceTask = new GetOtherUserSpaceTask(this);
			getOtherUserSpaceTask.execute(this, new OtherSpaceRequest(id),
					new OtherSpaceParser());
			if (!hasData) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void updateUI() {
		String name = userOther.name;
		if (name != null) {
			userNameTextView.setText(name);
			otherUserName = name;
		}
		String lvl = userOther.level;
		if (lvl != null) {
			userLevelTextView.setText("Lv " + lvl);
		}
		String signature = userOther.signature;
		if (signature != null && !signature.equals("")) {
			signatureTextView.setText(signature);
		} else {
			signatureTextView.setText("这个家伙什么也木有留下");
		}

		if (userOther.gender == 2) {
			gender.setImageResource(R.drawable.uc_sex_female);
		}
		String chaseCount = String.valueOf(userOther.chaseCount);
		zhuijuTextView.setText(chaseCount);
		String remindsCount = String.valueOf(userOther.remindCount);
		bookTextView.setText(remindsCount);
		String comments = String.valueOf(userOther.talkCount);
		commentTextView.setText(comments);
		String url = userOther.iconURL;
		headPic.setTag(url);
		if (url != null) {
			loadListImage(headPic, url);
		}
		if (userOther.isGuanzhu == 0) {// 未关注
			guanzhu.setImageResource(R.drawable.friend_fellow_btn);
		}
		ArrayList<EventData> temp = (ArrayList<EventData>) userOther.getEvent();
		if (temp != null) {

			eventListView.setAdapter(new EventListAdapter(temp));
			int count = temp.size();
			int height = CommonUtils.dip2px(this, count * 110) + 20;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, height);
			eventListView.setLayoutParams(params);

		}
	}

	private class EventListAdapter extends BaseAdapter {

		private final ArrayList<EventData> list;

		public EventListAdapter(ArrayList<EventData> list) {
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
						.from(OtherUserCenterActivity.this);
				convertView = inflater.inflate(
						R.layout.otheruser_event_list_item, null);
				viewHolder.picImageView = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.contentText = (TextView) convertView
						.findViewById(R.id.content);
				viewHolder.timeText = (TextView) convertView
						.findViewById(R.id.time);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			EventData temp = list.get(position);
			String intro = temp.preMsg;
			if (intro != null) {
				viewHolder.contentText.setText(intro);
			}
			String time = temp.createTime;
			if (time != null) {
				viewHolder.timeText.setText(time);
			}
			String url = temp.toObjectPicUrl;
			viewHolder.picImageView.setTag(url);
			loadListImage(viewHolder.picImageView, url);
			return convertView;
		}

		public class ViewHolder {
			public ImageView picImageView;
			public TextView contentText;
			public TextView timeText;
		}

	}

	private AddGuanzhuTask addGuanzhuTask;

	private void addGuanzhu() {
		if (addGuanzhuTask == null) {
			// UserOther.current().userID = id;
			addGuanzhuTask = new AddGuanzhuTask(this);
			addGuanzhuTask.execute(this, new GuanzhuAddRequest(id),
					new ResultParser());
		}
	}

	private void onFellowOver() {
		userOther.isGuanzhu = 1;
		guanzhu.setImageResource(R.drawable.delete_guanzhu);
	}

	private DeleteGuanzhuTask deleteGuanzhuTask;

	private void deleteGuanzhu() {
		if (deleteGuanzhuTask == null) {
			deleteGuanzhuTask = new DeleteGuanzhuTask(this);
			deleteGuanzhuTask.execute(this, new GuanzhuCancelRequest(id),
					new ResultParser());
		}
	}

	private void OnDeleteOver() {
		userOther.isGuanzhu = 0;
		guanzhu.setImageResource(R.drawable.friend_fellow_btn);
	}

	@Override
	public void onNetBegin(String method) {
		if ("guanZhuAdd".equals(method)) {
			showpb();
		} else if ("guanZhuCancel".equals(method)) {
			showpb();
		}

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("userSpace".equals(method)) {
			if (msg != null) {
				String s = new OtherSpaceParser().parse(msg, userOther);
				if (s != null && s.equals("")) {
					progressBar.setVisibility(View.GONE);
					errText.setVisibility(View.GONE);
					scrollView.setVisibility(View.VISIBLE);
					updateUI();
					hasData = true;
				} else {
					hasData = false;
					progressBar.setVisibility(View.GONE);
					errText.setVisibility(View.VISIBLE);
					DialogUtil.alertToast(getApplicationContext(), s);
				}
			} else {
				DialogUtil.alertToast(getApplicationContext(), "网络不给力");
				progressBar.setVisibility(View.GONE);
				errText.setVisibility(View.VISIBLE);
			}
			getOtherUserSpaceTask = null;
		} else if ("guanZhuCancel".equals(method)) {
			hidepb();
			if (msg != null && msg.equals("")) {
				DialogUtil.alertToast(getApplication(), "取消关注成功");
				OnDeleteOver();
			} else {
				DialogUtil.alertToast(getApplication(), msg);
			}
			deleteGuanzhuTask = null;
		} else if ("guanZhuAdd".equals(method)) {
			hidepb();
			if (msg != null && "".equals(msg)) {
				DialogUtil.alertToast(getApplication(), "添加关注成功!");
				onFellowOver();
			} else {
				DialogUtil.alertToast(getApplication(), msg);
			}
			addGuanzhuTask = null;
		}
		if (UserNow.current().getNewBadge() != null) {
			for (int i = 0; i < UserNow.current().getNewBadge().size(); i++) {
				String name = UserNow.current().getNewBadge().get(i).name;
				if (name != null) {
					DialogUtil.showBadgeAddToast(OtherUserCenterActivity.this,
							name);
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
		if ("userSpace".equals(method)) {
			getOtherUserSpaceTask = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			close();
			break;
		case R.id.privatemsg:
			openPrivateMsgActivity(id, otherUserName);
			break;
		case R.id.guanzhu:
			if (userOther.isGuanzhu == 0) {
				addGuanzhu();
			} else {
				deleteGuanzhu();
			}
			break;
		case R.id.zhuiju_layout:
			openZhuijuActivity();
			break;
		case R.id.comment_layout:
			openCommentActivity();
			break;
		case R.id.book_layout:
			openBookActivity();
			break;
		default:
			break;
		}
	}

	private void openPrivateMsgActivity(int id, String name) {
		Intent intent = new Intent(this, UserMailActivity.class);
		intent.putExtra("otherUserId", id);
		intent.putExtra("otherUserName", name);
		startActivity(intent);
	}

	private void openBookActivity() {
		Intent intent = new Intent(this, OtherUserBookActivity.class);
		intent.putExtra("otherUserId", id);
		startActivity(intent);
	}

	private void openCommentActivity() {
		Intent intent = new Intent(this, OtherUserCommentActivity.class);
		intent.putExtra("otherUserId", id);
		startActivity(intent);
	}

	private void openZhuijuActivity() {
		Intent intent = new Intent(this, OtherUserZhuijuActivity.class);
		intent.putExtra("otherUserId", id);
		startActivity(intent);
	}

	private void close() {
		if (getOtherUserSpaceTask != null) {
			getOtherUserSpaceTask.cancel(true);
			getOtherUserSpaceTask = null;
		}
		if (from == MyFansActivity.MY_FANS) {
			setResult(RESULT_OK);
		}
		finish();
	}

	private void loadListImage(final ImageView imageView, String url) {
		if (url != null) {
			Drawable bitmap = imageLoader.loadDrawable(url,
					new AsyncImageLoader.ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable,
								String imageUrl) {
							String selfUrl = (String) imageView.getTag();
							if (selfUrl != null && selfUrl.equals(imageUrl)) {
								imageView.setImageDrawable(imageDrawable);
							}
						}
					});
			if (bitmap != null) {
				imageView.setImageDrawable(bitmap);
			} else {
				imageView
						.setImageResource(R.drawable.rcmd_list_item_pic_default);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		userOther.setEvent(null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ArrayList<EventData> events = (ArrayList<EventData>) userOther
				.getEvent();

		EventData temp = null;
		try {
			temp = events.get(position);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		if (temp != null) {
			switch (temp.toObjectType) {
			case 1: // program
				openProgramDetailActivity(String.valueOf(temp.toObjectId), "");
				break;
			case 9:
				openOtherUserCenterActivity(temp.toObjectId);
				break;
			default:
				break;
			}
		}
	}

	private void openProgramDetailActivity(String id, String topicId) {
		Intent intent = new Intent(this, ProgramNewActivity.class);
		intent.putExtra("programId", id);
		intent.putExtra("topicId", topicId);
		startActivity(intent);
	}

	private void openOtherUserCenterActivity(int id) {
		Intent intent = new Intent(this, OtherUserCenterActivity.class);
		intent.putExtra("id", id);
		startActivity(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);

		scrollView.smoothScrollTo(-2000, -2000);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
