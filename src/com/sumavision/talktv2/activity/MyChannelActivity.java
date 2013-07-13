package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.adapter.NetPlayDataListAdapter;
import com.sumavision.talktv2.data.NetPlayData;
import com.sumavision.talktv2.data.ShortChannelData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.task.ChannelProgramUserTask;
import com.sumavision.talktv2.task.DeleteChannelTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class MyChannelActivity extends Activity implements
		NetConnectionListenerNew, OnItemClickListener, OnClickListener {
	private ImageLoaderHelper imageLoaderHelper;

	private void initUtils() {
		imageLoaderHelper = new ImageLoaderHelper();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUtils();
		setContentView(R.layout.custom_channel_my_list);
		initMyList();
		initNetLiveLayout();
		getDefaultChannelProgram();
	}

	public Button editBtn;

	private TextView myErrText;
	private ProgressBar myProgressBar;
	private MyListView myListView;

	private ArrayList<ShortChannelData> myList = new ArrayList<ShortChannelData>();
	private ArrayList<ShortChannelData> tempMyList = new ArrayList<ShortChannelData>();

	private void initMyList() {
		myListView = (MyListView) findViewById(R.id.listView);
		myErrText = (TextView) findViewById(R.id.err_text);
		myErrText.setOnClickListener(errOnClickListener);
		myErrText.setTag(1);
		myProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		myListView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				getChannelProgram(UserNow.current().userID, tempMyList, 0, 20,
						1, false);
			}

			@Override
			public void onLoadingMore() {
				int start = myList.size();
				int count = 20;
				getChannelProgram(UserNow.current().userID, tempMyList, start,
						count, 1, true);
			}
		});
		myListView.setOnItemClickListener(this);

		editBtn = (Button) findViewById(R.id.manage);
		editBtn.setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
	}

	private OnClickListener errOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			String text = ((TextView) v).getText().toString();
			if (Constants.noChannel.equals(text)) {
				DialogUtil.alertToast(getApplicationContext(),
						"你还未添加任何频道!\n请去\"全部频道\"\"编辑\"来定制自己的频道吧!");
			} else if (Constants.noLogIn.equals(text)) {
				openLoginActivity();
			} else {
				getDefaultChannelProgram();
			}

		}

	};
	// 获取我的频道节目列表的task
	private ChannelProgramUserTask channelProgramTask;

	private void getDefaultChannelProgram() {
		getChannelProgram(UserNow.current().userID, tempMyList, 0, 20, 1, false);
	}

	private void getChannelProgram(int userId,
			ArrayList<ShortChannelData> temp, int first, int count, int cctv,
			boolean isLoadMore) {
		if (channelProgramTask == null) {
			channelProgramTask = new ChannelProgramUserTask(this, isLoadMore);
			channelProgramTask.execute(this, temp, first, count, cctv);
		}
	}

	private void openLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivityForResult(intent, REQUEST_LOGIN);
	}

	private final int REQUEST_LOGIN = 1;

	private void hideMyErrorLayout() {
		myProgressBar.setVisibility(View.GONE);
		myErrText.setVisibility(View.GONE);
	}

	private void showMyErrorLayout() {
		myProgressBar.setVisibility(View.GONE);
		myErrText.setVisibility(View.VISIBLE);
		myErrText.setText(Constants.errText);
	}

	private void showMyLoadingLayout() {
		myProgressBar.setVisibility(View.VISIBLE);
		myErrText.setVisibility(View.GONE);
	}

	private RelativeLayout connectBg;

	private void hidepb() {
		if (connectBg.isShown())
			connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		if (!connectBg.isShown())
			connectBg.setVisibility(View.VISIBLE);
	}

	private MyChannelListAdapter myChannelListAdapter;

	private void updateMyChannelList(boolean isLoadMore) {
		if (tempMyList != null) {
			if (isLoadMore) {
				if (tempMyList.size() < 20) {
					myListView.setCanLoadMore(false);
					myListView.onLoadMoreOver();
				} else {
					myList.addAll(tempMyList);
					myChannelListAdapter.notifyDataSetChanged();
					myListView.onLoadMoreOver();
				}
			} else {
				myList.clear();
				myList.addAll(tempMyList);
				if (myList.size() == 0) {
					myErrText.setVisibility(View.VISIBLE);
					myErrText.setText(Constants.noChannel);
					myErrText.setGravity(Gravity.CENTER);
					myErrText.setTextColor(Color.BLACK);
				} else {
					myChannelListAdapter = new MyChannelListAdapter(myList);
					myListView.setAdapter(myChannelListAdapter);
					myListView.setCanLoadMore(true);
				}
			}
			tempMyList.clear();
		}
	}

	@Override
	public void onNetBegin(String method, boolean isLoadMore) {
		if (!isLoadMore) {
			if (Constants.channelProgramUser.equals(method)) {
				if (myList.size() == 0)
					showMyLoadingLayout();
			} else if (Constants.deleteChannelTask.equals(method)) {
				showpb();
			} else if (Constants.addChannelTask.equals(method)) {
				showpb();
			}
		}
	}

	@Override
	public void onNetEnd(String msg, String method) {

	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	@Override
	public void onNetEnd(int code, String msg, String method, boolean isLoadMore) {
		if (Constants.channelProgramUser.equals(method)) {
			switch (code) {
			case Constants.requestErr:
				// .alertToast(getApplicationContext(), "request net");
				showMyErrorLayout();
				break;
			case Constants.fail_no_net:
				// DialogUtil.alertToast(getApplicationContext(), "no net");
				showMyErrorLayout();
				break;
			case Constants.fail_server_err:
				// DialogUtil.alertToast(getApplicationContext(),
				// "server Error");
				showMyErrorLayout();
				break;
			case Constants.parseErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "parse Error");
				showMyErrorLayout();
				break;
			case Constants.sucess:
				// DialogUtil.alertToast(getApplicationContext(), "sucess");
				hideMyErrorLayout();
				updateMyChannelList(isLoadMore);
				break;
			default:
				break;
			}
			channelProgramTask = null;

		} else if (Constants.deleteChannelTask.equals(method)) {
			hidepb();
			switch (code) {
			case Constants.requestErr:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.fail_no_net:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.fail_server_err:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.parseErr:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.sucess:
				DialogUtil.alertToast(getApplicationContext(),
						Constants.errMsg_s_deleteChannel);
				onChannelDeleted(deleteList, deleteGroupPosition,
						deletePosition);
				break;
			default:
				break;
			}
			deleteChannelTask = null;
		}

	}

	private class MyChannelListAdapter extends BaseAdapter {
		private ArrayList<ShortChannelData> list;

		public MyChannelListAdapter(ArrayList<ShortChannelData> list) {
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
						.from(MyChannelActivity.this);
				convertView = inflater.inflate(
						R.layout.channel_custom_my_list_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.channelPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);
				viewHolder.liveBtn = (ImageView) convertView
						.findViewById(R.id.liveBtn);
				viewHolder.editLayout = (RelativeLayout) convertView
						.findViewById(R.id.edit_layout);
				viewHolder.editBtn = (ImageView) convertView
						.findViewById(R.id.edit_btn);
				viewHolder.tvNameView = (TextView) convertView
						.findViewById(R.id.tvName);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			ShortChannelData temp = list.get(position);
			String name = temp.programName;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			if (temp.livePlay) {
				viewHolder.liveBtn
						.setImageResource(R.drawable.channel_live_btn);
				viewHolder.liveBtn.setVisibility(View.VISIBLE);
			} else {
				viewHolder.liveBtn.setVisibility(View.GONE);
			}
			final int mPosition = position;
			viewHolder.liveBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onLiveBtnClick(-1, mPosition, LIST_MY);
				}
			});
			SpannableString time = temp.spannableTimeString;
			if (time != null) {
				viewHolder.time.setText(time);
				viewHolder.time.setVisibility(View.VISIBLE);
			} else {
				viewHolder.time.setVisibility(View.GONE);
			}
			String url = temp.channelPicUrl;
			imageLoaderHelper.loadImage(viewHolder.channelPic, url,
					R.drawable.channel_tv_logo_default);
			viewHolder.channelPic.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onTvLogoClick(0, mPosition, LIST_MY);
				}
			});
			viewHolder.editBtn.setImageResource(R.drawable.channel_delete);
			viewHolder.editBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onMyEditClick(mPosition);
				}
			});
			if (myEditState) {
				viewHolder.editLayout.setVisibility(View.VISIBLE);
			} else {
				viewHolder.editLayout.setVisibility(View.GONE);
			}
			if (temp.channelName != null)
				viewHolder.tvNameView.setText(temp.channelName);
			return convertView;
		}

	}

	public static class ViewHolder {
		public TextView nameTxt;
		public ImageView channelPic;
		public TextView time;
		// 直播或者预约按钮
		public ImageView liveBtn;
		public RelativeLayout editLayout;
		public ImageView editBtn;
		public TextView tvNameView;
	}

	private boolean myEditState = false;
	private int deleteList;
	private int deleteGroupPosition;
	private int deletePosition;
	private static final int LIST_MY = 0;

	private void onChannelDeleted(int deleteList, int groupPosition,
			int deletePosition) {
		myList.remove(deletePosition);
		myChannelListAdapter.notifyDataSetChanged();
	}

	private void onMyEditClick(int position) {
		ShortChannelData shortChannelData = (ShortChannelData) myChannelListAdapter
				.getItem(position);
		deleteList = LIST_MY;
		deletePosition = position;
		deleteChannel(UserNow.current().userID, shortChannelData.channelId);
	}

	private DeleteChannelTask deleteChannelTask;

	private void deleteChannel(int userId, int channelId) {
		if (deleteChannelTask == null) {
			deleteChannelTask = new DeleteChannelTask(this, false);
			deleteChannelTask.execute(this, userId, channelId);
		}
	}

	private void onTvLogoClick(int groupPosition, int position, int from) {
		ShortChannelData channelData1 = (ShortChannelData) myChannelListAdapter
				.getItem(position);
		if (myEditState) {
			onMyEditClick(position);
		} else {
			openChannel(channelData1);
		}

	}

	private void openChannel(ShortChannelData shortChannelData) {
		SharedPreferences sp = getSharedPreferences("channelId", 0);
		sp.edit().putInt("channelId", shortChannelData.channelId).commit();
		Intent i = new Intent(this, ChannelDetailActivity.class);
		i.putExtra("tvName", shortChannelData.channelName);
		i.putExtra("channelId", shortChannelData.channelId);
		startActivity(i);
	}

	private void onLiveBtnClick(int groupPosition, int position, int from) {
		ShortChannelData channelData = (ShortChannelData) myChannelListAdapter
				.getItem(position);
		if (channelData.livePlay) {
			liveThroughNet(channelData);
		}
	}

	private void liveThroughNet(ShortChannelData shortChannelData) {
		ArrayList<NetPlayData> netPlayDatas = shortChannelData.netPlayDatas;
		if (netPlayDatas != null) {
			if (netPlayDatas.size() == 1) {
				NetPlayData tempData = netPlayDatas.get(0);
				String url = tempData.url;
				String videoPath = tempData.videoPath;
				if (tempData.videoPath != null
						&& !tempData.videoPath.equals("")) {

					Intent intent = new Intent(this,
							NewLivePlayerActivity.class);
					intent.putExtra("path", videoPath);
					intent.putExtra("playType", 1);
					String title = shortChannelData.channelName;
					intent.putExtra("title", title);
					startActivity(intent);
				} else if (tempData.url != null) {
					openNetLiveActivity(url, videoPath, 1,
							shortChannelData.channelName);
				}
			} else {
				netLiveLayout.setVisibility(View.VISIBLE);
				netPlayDataListAdapter = new NetPlayDataListAdapter(this,
						netPlayDatas);
				netLiveListView.setAdapter(netPlayDataListAdapter);
			}
		} else {
			DialogUtil.alertToast(getApplicationContext(), "暂时无法播放");
		}
	}

	RelativeLayout netLiveLayout;
	ListView netLiveListView;
	private NetPlayDataListAdapter netPlayDataListAdapter;
	private ImageButton netLiveCancel;

	private void initNetLiveLayout() {
		netLiveLayout = (RelativeLayout) findViewById(R.id.netlive_layout);
		netLiveListView = (ListView) findViewById(R.id.nettvListView);
		netLiveListView.setOnItemClickListener(netLiveItemClickListener);
		netLiveCancel = (ImageButton) findViewById(R.id.cancelnetTv);
		netLiveCancel.setOnClickListener(cancelNetLiveListener);
		netLiveLayout.setOnClickListener(cancelNetLiveListener);
	}

	OnClickListener cancelNetLiveListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			netLiveLayout.setVisibility(View.GONE);
		}
	};

	OnItemClickListener netLiveItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			netLiveLayout.setVisibility(View.GONE);
			NetPlayData temp = netPlayDataListAdapter.getItem(position);
			String url = temp.url;
			String videoPath = temp.videoPath;
			String channelName = temp.channelName;
			if (videoPath != null && !videoPath.equals("")) {
				Intent intent = new Intent(MyChannelActivity.this,
						NewLivePlayerActivity.class);
				intent.putExtra("path", videoPath);
				intent.putExtra("playType", 1);
				intent.putExtra("title", channelName);
				startActivity(intent);
			} else {
				openNetLiveActivity(url, videoPath, 1, channelName);
			}
		}
	};

	private void openNetLiveActivity(String url, String videoPath, int isLive,
			String title) {
		// Intent intent = new Intent(this, WebPlayActivity.class);
		startActivity(new Intent(this, MainWebPlayBlockWaitActivity.class));
		Intent intent = new Intent(this, MainWebPlayActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("videoPath", videoPath);
		intent.putExtra("playType", isLive);
		intent.putExtra("title", title);
		startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg2 == 0) {
			return;
		}
		if (myEditState) {
			onMyEditClick(arg2 - 1);
		} else {
			ShortChannelData channelData = (ShortChannelData) myChannelListAdapter
					.getItem(arg2 - 1);
			VodProgramData vpd = new VodProgramData();
			vpd.cpId = channelData.channelId;
			vpd.id = String.valueOf(channelData.programId);
			vpd.topicId = channelData.topicId;
			vpd.name = channelData.programName;
			MobclickAgent.onEvent(this, "tvpr", vpd.name);
			openProgram(vpd);
		}
	}

	private void openProgram(VodProgramData vpd) {
		if (vpd.topicId == null || vpd.topicId.equals("")
				|| Integer.parseInt(vpd.topicId) == 0) {
			Toast.makeText(getApplicationContext(), "暂无此节目相关信息",
					Toast.LENGTH_SHORT).show();
		} else {

			Intent i = new Intent(this, ProgramNewActivity.class);
			i.putExtra("programId", vpd.id);
			i.putExtra("id", vpd.id);
			i.putExtra("cpId", vpd.cpId);
			i.putExtra("topicId", vpd.topicId);
			i.putExtra("nameHolder", vpd.nameHolder);
			i.putExtra("updateName", vpd.updateName);
			i.putExtra("from", 1);

			startActivity(i);

		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.manage:
			myEditState = !myEditState;
			if (myList.size() != 0) {
				myChannelListAdapter.notifyDataSetChanged();
			}
			boolean edit = myEditState;
			if (edit) {
				editBtn.setText(getResources().getString(
						R.string.navigator_channel_complete));
			} else {
				editBtn.setText(getResources().getString(
						R.string.navigator_channel_edit));
			}
			break;
		case R.id.back:
			finish();
			break;
		default:
			break;
		}
	}
}