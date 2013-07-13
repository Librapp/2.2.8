package com.sumavision.talktv2.fragment;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.ChannelDetailActivity;
import com.sumavision.talktv2.activity.ImageLoaderHelper;
import com.sumavision.talktv2.activity.MainWebPlayActivity;
import com.sumavision.talktv2.activity.MainWebPlayBlockWaitActivity;
import com.sumavision.talktv2.activity.NewLivePlayerActivity;
import com.sumavision.talktv2.activity.ProgramNewActivity;
import com.sumavision.talktv2.adapter.NetPlayDataListAdapter;
import com.sumavision.talktv2.data.NetPlayData;
import com.sumavision.talktv2.data.ShortChannelData;
import com.sumavision.talktv2.data.TypeChannelData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.task.AddChannelTask;
import com.sumavision.talktv2.task.ChannelProgramWholeTask;
import com.sumavision.talktv2.task.DeleteChannelTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class ChannelAllFragment extends Fragment implements
		NetConnectionListenerNew, OnChildClickListener, OnClickListener {
	private ImageLoaderHelper imageLoaderHelper;

	private void initUtils() {
		imageLoaderHelper = new ImageLoaderHelper();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constants.load_data:
				getDefaultWholeChannelProgram();
				needLoadData = false;
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUtils();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (wholeView == null) {
			wholeView = inflater.inflate(R.layout.custom_channel_whole_list,
					null);
			initWholeList(wholeView);
			initNetLiveLayout(wholeView);
			needLoadData = true;
		} else {
			((ViewGroup) wholeView.getParent()).removeView(wholeView);
		}
		return wholeView;
	}

	View wholeView;
	boolean needLoadData;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (needLoadData) {
			handler.sendEmptyMessageDelayed(Constants.load_data,
					Constants.animation_duration);
		}
	}

	@Override
	public void onNetBegin(String method, boolean isLoadMore) {
		if (Constants.channelProgramWhole.equals(method)) {
			if (wholeList.size() == 0)
				showWholeLoadingLayout();
		} else if (Constants.deleteChannelTask.equals(method)) {
			showpb();
		} else if (Constants.addChannelTask.equals(method)) {
			showpb();
		}
	}

	@Override
	public void onNetEnd(String msg, String method) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNetEnd(String msg, String method, int type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCancel(String method) {
		// TODO Auto-generated method stub

	}

	private TextView wholeErrText;
	private ProgressBar wholeProgressBar;
	private ExpandableListView wholeListView;

	private ArrayList<ShortChannelData> wholeList = new ArrayList<ShortChannelData>();
	private ArrayList<ShortChannelData> tempWholeList = new ArrayList<ShortChannelData>();

	private void initWholeList(View view) {
		wholeListView = (ExpandableListView) view.findViewById(R.id.listView);
		// wholeListView.setTag(LIST_WHOLE);
		wholeListView.setOnChildClickListener(this);
		wholeErrText = (TextView) view.findViewById(R.id.err_text);
		// wholeErrText.setOnClickListener(errOnClickListener);
		wholeErrText.setTag(2);
		wholeProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		connectBg = (RelativeLayout) view.findViewById(R.id.communication_bg);
		wholeErrText.setOnClickListener(this);
	}

	// 获取全部频道节目列表的task
	private ChannelProgramWholeTask channelProgramWholeTask;

	private void getDefaultWholeChannelProgram() {
		getWholeChannelProgram(UserNow.current().userID, tempWholeList, 0, 0,
				1, false);
	}

	private void getWholeChannelProgram(int userId,
			ArrayList<ShortChannelData> temp, int first, int count, int cctv,
			boolean isLoadMore) {
		if (channelProgramWholeTask == null) {
			channelProgramWholeTask = new ChannelProgramWholeTask(this,
					isLoadMore);
			channelProgramWholeTask.execute(getActivity(), temp, first, count,
					cctv);
		}
	}

	private ArrayList<TypeChannelData> wholeTypeChannelData;

	/**
	 * 更新我的界面
	 * 
	 * @param isLoadMore
	 */
	private void updateWholeChannelList(boolean isLoadMore) {
		if (tempWholeList != null) {
			wholeList.addAll(tempWholeList);
			ArrayList<ShortChannelData> weishiChannels = new ArrayList<ShortChannelData>();
			ArrayList<ShortChannelData> yangshiChannels = new ArrayList<ShortChannelData>();
			for (ShortChannelData temp : tempWholeList) {
				if (temp.channelType == 0) {
					yangshiChannels.add(temp);
				} else {
					weishiChannels.add(temp);
				}
			}
			wholeTypeChannelData = new ArrayList<TypeChannelData>();
			TypeChannelData weishi = new TypeChannelData();
			weishi.channelTypeName = "卫视频道";
			weishi.typeChannelData = weishiChannels;
			TypeChannelData yangshi = new TypeChannelData();
			yangshi.channelTypeName = "央视频道";
			yangshi.typeChannelData = yangshiChannels;
			wholeTypeChannelData.add(weishi);
			// TODO: 机锋需要注释，机锋不显示央视
			wholeTypeChannelData.add(yangshi);
			wholeAdapter = new ExpandableListViewAdapter(wholeTypeChannelData);
			wholeListView.setAdapter(wholeAdapter);
			// 展开第一栏
			wholeListView.expandGroup(0);
			wholeListView.setGroupIndicator(null);
			// wholeListView.setOnChildClickListener(this);
			tempWholeList.clear();
		}
	}

	ExpandableListViewAdapter wholeAdapter;

	// adapter 主页下半部分的列表项目
	private class ExpandableListViewAdapter extends BaseExpandableListAdapter {

		private ArrayList<TypeChannelData> list;

		public ExpandableListViewAdapter(ArrayList<TypeChannelData> list) {
			this.list = list;
		}

		@Override
		public int getGroupCount() {

			if (list != null) {
				return list.size();
			}
			return 0;
		}

		@Override
		public int getChildrenCount(int groupPosition) {

			ArrayList<ShortChannelData> temp = list.get(groupPosition).typeChannelData;
			if (temp != null) {
				return temp.size();
			}
			return 0;
		}

		@Override
		public TypeChannelData getGroup(int groupPosition) {

			return list.get(groupPosition);
		}

		@Override
		public ShortChannelData getChild(int groupPosition, int childPosition) {

			ArrayList<ShortChannelData> temp = list.get(groupPosition).typeChannelData;
			return temp.get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {

			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {

			return childPosition;
		}

		@Override
		public boolean hasStableIds() {

			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {

			GroupViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new GroupViewHolder();
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				convertView = inflater.inflate(R.layout.channel_group_item,
						null);
				viewHolder.textView = (TextView) convertView
						.findViewById(R.id.textView);
				viewHolder.imageView = (ImageView) convertView
						.findViewById(R.id.imageView);
				viewHolder.layout = (RelativeLayout) convertView;

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (GroupViewHolder) convertView.getTag();
			}
			String title = getGroup(groupPosition).channelTypeName;
			if (title != null) {
				viewHolder.textView.setText(title);
			}
			if (isExpanded) {
				viewHolder.imageView
						.setImageResource(R.drawable.channel_indicator_up);
			} else {
				viewHolder.imageView
						.setImageResource(R.drawable.channel_indicator_down);
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			ChildrenViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ChildrenViewHolder();
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				convertView = inflater.inflate(R.layout.channel_children_item,
						null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.channelPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);
				viewHolder.liveBtn = (ImageView) convertView
						.findViewById(R.id.liveBtn);
				viewHolder.infoLayout = (RelativeLayout) convertView
						.findViewById(R.id.info_layout);
				viewHolder.editLayout = (RelativeLayout) convertView
						.findViewById(R.id.edit_layout);
				viewHolder.editBtn = (ImageView) convertView
						.findViewById(R.id.edit_btn);
				viewHolder.tvNameView = (TextView) convertView
						.findViewById(R.id.tvName);
				viewHolder.hintTextView = (TextView) convertView
						.findViewById(R.id.hint_text);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ChildrenViewHolder) convertView.getTag();
			}
			ShortChannelData temp = getChild(groupPosition, childPosition);
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
			final int mPosition = childPosition;
			final int mGroupPosition = groupPosition;
			viewHolder.liveBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onLiveBtnClick(mGroupPosition, mPosition);
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
					onTvLogoClick(mGroupPosition, mPosition);
				}
			});
			// viewHolder.editBtn.setImageResource(R.drawable.channel_delete);
			final boolean isMy = temp.flagMyChannel;
			Resources res = getResources();
			if (isMy) {
				viewHolder.editBtn.setImageResource(R.drawable.channel_delete);
				viewHolder.hintTextView.setText(res
						.getString(R.string.channel_edit_delete));
				viewHolder.hintTextView.setTextColor(res
						.getColor(R.color.channel_delete));
			} else {
				viewHolder.editBtn.setImageResource(R.drawable.channel_add);
				viewHolder.hintTextView.setText(res
						.getString(R.string.channel_edit_add));
				viewHolder.hintTextView.setTextColor(res
						.getColor(R.color.channel_add));
			}
			viewHolder.editBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onEditClick(mGroupPosition, mPosition, !isMy);
				}
			});
			if (myWholeEditeState) {
				viewHolder.editLayout.setVisibility(View.VISIBLE);
			} else {
				viewHolder.editLayout.setVisibility(View.GONE);
			}
			if (temp.channelName != null)
				viewHolder.tvNameView.setText(temp.channelName);
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {

			return true;
		}
	}

	static class GroupViewHolder {

		public TextView textView;
		public ImageView imageView;
		public RelativeLayout layout;
	}

	static class ChildrenViewHolder {

		public TextView nameTxt;
		public ImageView channelPic;
		public TextView time;
		// 直播或者预约按钮
		public ImageView liveBtn;
		public RelativeLayout infoLayout;
		public RelativeLayout editLayout;
		public ImageView editBtn;
		public TextView tvNameView;
		public TextView hintTextView;
	}

	private void hideWholeErrorLayout() {
		wholeProgressBar.setVisibility(View.GONE);
		wholeErrText.setVisibility(View.GONE);
	}

	private void showWholeErrorLayout() {
		wholeProgressBar.setVisibility(View.GONE);
		wholeErrText.setVisibility(View.VISIBLE);
		wholeErrText.setText(Constants.errText);
	}

	private void showWholeLoadingLayout() {
		wholeProgressBar.setVisibility(View.VISIBLE);
		wholeErrText.setVisibility(View.GONE);
	}

	@Override
	public void onNetEnd(int code, String msg, String method, boolean isLoadMore) {
		if (Constants.channelProgramWhole.equals(method)) {
			switch (code) {
			case Constants.requestErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "request net");
				showWholeErrorLayout();
				break;
			case Constants.fail_no_net:
				// DialogUtil.alertToast(getApplicationContext(), "no net");
				showWholeErrorLayout();
				break;
			case Constants.fail_server_err:
				// DialogUtil.alertToast(getApplicationContext(),
				// "server Error");
				showWholeErrorLayout();
				break;
			case Constants.parseErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "parse Error");
				showWholeErrorLayout();
				break;
			case Constants.sucess:
				// DialogUtil.alertToast(getApplicationContext(), "sucess");
				updateWholeChannelList(isLoadMore);
				hideWholeErrorLayout();
				// if (connectBg.isShown()) {
				// hidepb();
				// // myWholeEditeState = true;
				// // wholeAdapter.notifyDataSetChanged();
				// }
				break;
			default:
				break;
			}
			channelProgramWholeTask = null;
		} else if (Constants.deleteChannelTask.equals(method)) {
			hidepb();
			switch (code) {
			case Constants.requestErr:
				DialogUtil.alertToast(getActivity(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.fail_no_net:
				DialogUtil.alertToast(getActivity(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.fail_server_err:
				DialogUtil.alertToast(getActivity(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.parseErr:
				DialogUtil.alertToast(getActivity(),
						Constants.errMsg_f_deleteChannel);
				break;
			case Constants.sucess:
				DialogUtil.alertToast(getActivity(),
						Constants.errMsg_s_deleteChannel);
				onChannelDeleted(deleteList, deleteGroupPosition,
						deletePosition);
				break;
			default:
				break;
			}
			deleteChannelTask = null;
		} else if (Constants.addChannelTask.equals(method)) {
			hidepb();
			switch (code) {
			case Constants.requestErr:
				DialogUtil.alertToast(getActivity(),
						Constants.errMsg_f_addChannel);
				break;
			case Constants.fail_no_net:
				DialogUtil.alertToast(getActivity(),
						Constants.errMsg_f_addChannel);
				break;
			case Constants.fail_server_err:
				DialogUtil.alertToast(getActivity(),
						Constants.errMsg_f_addChannel);
				break;
			case Constants.parseErr:
				DialogUtil.alertToast(getActivity(),
						Constants.errMsg_f_addChannel);
				break;
			case Constants.sucess:
				DialogUtil.alertToast(getActivity(),
						Constants.errMsg_s_addChannel);
				onChannelAdded(addList, addGroupPosition, addPosition);
				break;
			default:
				break;
			}
			addChannelTask = null;
		}
	}

	private void onChannelDeleted(int deleteList, int groupPosition,
			int deletePosition) {

		ShortChannelData data3 = wholeTypeChannelData.get(groupPosition).typeChannelData
				.get(deletePosition);
		data3.flagMyChannel = false;
		wholeAdapter.notifyDataSetChanged();
	}

	private void onChannelAdded(int addList, int groupPosition, int addPosition) {

		ShortChannelData data3 = wholeTypeChannelData.get(groupPosition).typeChannelData
				.get(addPosition);
		data3.flagMyChannel = true;
		wholeAdapter.notifyDataSetChanged();
	}

	private boolean myWholeEditeState = false;

	private RelativeLayout connectBg;

	private void hidepb() {
		if (connectBg.isShown())
			connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		if (!connectBg.isShown())
			connectBg.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		ShortChannelData channelData = wholeAdapter.getChild(groupPosition,
				childPosition);
		if (myWholeEditeState) {
			boolean isAdding = true;
			if (channelData.flagMyChannel) {
				isAdding = false;
			}
			onEditClick(groupPosition, childPosition, isAdding);
		} else {
			VodProgramData vpd = new VodProgramData();
			vpd.cpId = channelData.channelId;
			vpd.id = String.valueOf(channelData.programId);
			vpd.topicId = channelData.topicId;
			vpd.name = channelData.programName;
			MobclickAgent.onEvent(getActivity(), "tvpr", vpd.name);
			openProgram(vpd);
		}
		return false;
	}

	private int deleteList;
	private int deleteGroupPosition;
	private int deletePosition;
	private int addList;
	private int addGroupPosition;
	private int addPosition;

	private void onEditClick(int groupPosition, int position, boolean isAdding) {

		ShortChannelData shortChannelData = wholeAdapter.getChild(
				groupPosition, position);
		if (isAdding) {
			addGroupPosition = groupPosition;
			addPosition = position;
			addChannel(UserNow.current().userID, shortChannelData.channelId);
		} else {
			deleteGroupPosition = groupPosition;
			deletePosition = position;
			deleteChannel(UserNow.current().userID, shortChannelData.channelId);
		}

	}

	private AddChannelTask addChannelTask;

	private void addChannel(int userId, int channelId) {
		if (addChannelTask == null) {
			addChannelTask = new AddChannelTask(this, false);
			addChannelTask.execute(getActivity(), userId, channelId);
		}
	}

	private DeleteChannelTask deleteChannelTask;

	private void deleteChannel(int userId, int channelId) {
		if (deleteChannelTask == null) {
			deleteChannelTask = new DeleteChannelTask(this, false);
			deleteChannelTask.execute(getActivity(), userId, channelId);
		}
	}

	private void openProgram(VodProgramData vpd) {
		if (vpd.topicId == null || vpd.topicId.equals("")
				|| Integer.parseInt(vpd.topicId) == 0) {
			Toast.makeText(getActivity(), "暂无此节目相关信息", Toast.LENGTH_SHORT)
					.show();
		} else {

			Intent i = new Intent(getActivity(), ProgramNewActivity.class);
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

	private void onTvLogoClick(int groupPosition, int position) {
		ShortChannelData channelData3 = wholeAdapter.getChild(groupPosition,
				position);
		if (myWholeEditeState) {
			boolean isAdding = true;
			if (channelData3.flagMyChannel) {
				isAdding = false;
			}
			onEditClick(groupPosition, position, isAdding);
		} else {
			openChannel(channelData3);
		}
	}

	private void openChannel(ShortChannelData shortChannelData) {
		SharedPreferences sp = getActivity().getSharedPreferences("channelId",
				0);
		sp.edit().putInt("channelId", shortChannelData.channelId).commit();
		Intent i = new Intent(getActivity(), ChannelDetailActivity.class);
		i.putExtra("tvName", shortChannelData.channelName);
		i.putExtra("channelId", shortChannelData.channelId);
		startActivity(i);
	}

	private void onLiveBtnClick(int groupPosition, int position) {
		ShortChannelData tempRankingChannelData = wholeAdapter.getChild(
				groupPosition, position);
		if (tempRankingChannelData.livePlay) {
			liveThroughNet(tempRankingChannelData);
		}
	}

	RelativeLayout netLiveLayout;
	ListView netLiveListView;
	private NetPlayDataListAdapter netPlayDataListAdapter;
	private ImageButton netLiveCancel;

	private void initNetLiveLayout(View view) {
		netLiveLayout = (RelativeLayout) view.findViewById(R.id.netlive_layout);
		netLiveListView = (ListView) view.findViewById(R.id.nettvListView);
		netLiveListView.setOnItemClickListener(netLiveItemClickListener);
		netLiveCancel = (ImageButton) view.findViewById(R.id.cancelnetTv);
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
				Intent intent = new Intent(getActivity(),
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
		Intent intent = new Intent(getActivity(),
				MainWebPlayBlockWaitActivity.class);
		startActivity(intent);
		Intent intent2 = new Intent(getActivity(), MainWebPlayActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("videoPath", videoPath);
		intent.putExtra("playType", isLive);
		intent.putExtra("title", title);
		startActivity(intent2);
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

					Intent intent = new Intent(getActivity(),
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
				netPlayDataListAdapter = new NetPlayDataListAdapter(
						getActivity(), netPlayDatas);
				netLiveListView.setAdapter(netPlayDataListAdapter);
			}
		} else {
			DialogUtil.alertToast(getActivity(), "暂时无法播放");
		}
	}

	public boolean getEditState() {
		return myWholeEditeState;
	};

	public void changeEditState() {
		if (wholeList.size() != 0) {
			myWholeEditeState = !myWholeEditeState;
			wholeAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getDefaultWholeChannelProgram();
			break;
		default:
			break;
		}
	}
}
