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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.ChannelDetailActivity;
import com.sumavision.talktv2.activity.ImageLoaderHelper;
import com.sumavision.talktv2.activity.ProgramNewActivity;
import com.sumavision.talktv2.data.ShortChannelData;
import com.sumavision.talktv2.data.TypeChannelData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.task.AddChannelTask;
import com.sumavision.talktv2.task.ChannelProgramRankingTask;
import com.sumavision.talktv2.task.DeleteChannelTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class ChannelRankingFragment extends Fragment implements
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
				getDefaultRankingChannelProgram();
				needReloadData = false;
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

	View rankingView;
	boolean needReloadData = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rankingView == null) {
			rankingView = inflater.inflate(
					R.layout.custom_channel_ranking_list, null);
			initRankingList(rankingView);
			needReloadData = true;
		} else {
			((ViewGroup) rankingView.getParent()).removeView(rankingView);
		}
		return rankingView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (needReloadData) {
			handler.sendEmptyMessageDelayed(Constants.load_data,
					Constants.animation_duration);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

	}

	// 获取排行榜频道节目列表的task
	private ChannelProgramRankingTask channelProgramRankingTask;
	private ArrayList<TypeChannelData> tempRankingTypeChannelData = new ArrayList<TypeChannelData>();

	private void getDefaultRankingChannelProgram() {
		getRankingChannelProgram(UserNow.current().userID,
				tempRankingTypeChannelData, 0, 0, 1, false);
	}

	private void getRankingChannelProgram(int userId,
			ArrayList<TypeChannelData> temp, int first, int count, int cctv,
			boolean isLoadMore) {
		if (channelProgramRankingTask == null) {
			channelProgramRankingTask = new ChannelProgramRankingTask(this,
					isLoadMore);
			channelProgramRankingTask.execute(getActivity(), temp, first,
					count, cctv);
		}
	}

	RankingExpandableListViewAdapter rankingAdapter;
	private ArrayList<TypeChannelData> rankingTypeChannelData = new ArrayList<TypeChannelData>();

	private void hideRankingErrorLayout() {
		rankingProgressBar.setVisibility(View.GONE);
		rankingErrText.setVisibility(View.GONE);
	}

	private void showRankingErrorLayout() {
		rankingProgressBar.setVisibility(View.GONE);
		rankingErrText.setVisibility(View.VISIBLE);
		rankingErrText.setText(Constants.errText);
	}

	private void showRankingLoadingLayout() {
		rankingProgressBar.setVisibility(View.VISIBLE);
		rankingErrText.setVisibility(View.GONE);
	}

	private TextView rankingErrText;
	private ProgressBar rankingProgressBar;
	private ExpandableListView rankingListView;

	private void initRankingList(View view) {
		rankingListView = (ExpandableListView) view.findViewById(R.id.listView);
		rankingListView.setOnChildClickListener(this);
		rankingErrText = (TextView) view.findViewById(R.id.err_text);
		rankingErrText.setOnClickListener(this);
		rankingProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		connectBg = (RelativeLayout) view.findViewById(R.id.communication_bg);
	}

	private void updateRankingChannelList(boolean isLoadMore) {
		if (tempRankingTypeChannelData != null) {
			rankingTypeChannelData.addAll(tempRankingTypeChannelData);
			if (rankingTypeChannelData.size() == 0) {
				rankingErrText.setText(Constants.noRankingChannel);
				rankingErrText.setVisibility(View.VISIBLE);
			} else {
				rankingAdapter = new RankingExpandableListViewAdapter(
						rankingTypeChannelData);
				rankingListView.setAdapter(rankingAdapter);
				// 展开第一栏
				rankingListView.expandGroup(0);
				rankingListView.setGroupIndicator(null);
				tempRankingTypeChannelData.clear();
			}
		}
	}

	// adapter 主页下半部分的列表项目
	private class RankingExpandableListViewAdapter extends
			BaseExpandableListAdapter {

		private ArrayList<TypeChannelData> list;

		public RankingExpandableListViewAdapter(ArrayList<TypeChannelData> list) {
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

			if (list == null || list.size() == 0) {
				return 0;
			}
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

			RankingViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new RankingViewHolder();
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				convertView = inflater.inflate(
						R.layout.channel_ranking_children_item, null);
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
				viewHolder.rankInfoView = (TextView) convertView
						.findViewById(R.id.rankingInfo);
				viewHolder.tvNameView = (TextView) convertView
						.findViewById(R.id.tvName);
				viewHolder.hintTextView = (TextView) convertView
						.findViewById(R.id.hint_text);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (RankingViewHolder) convertView.getTag();
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
			SpannableString timeInfo = temp.spannableTimeInfo;
			if (timeInfo != null && timeInfo.length() > 4) {
				viewHolder.time.setText(timeInfo);
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
					// onEditClick(mGroupPosition, mPosition, LIST_RANKING,
					// !isMy);
				}
			});
			if (myRankingEditeState) {
				viewHolder.editLayout.setVisibility(View.VISIBLE);
			} else {
				viewHolder.editLayout.setVisibility(View.GONE);
			}
			String channelName = temp.channelName;
			if (channelName != null) {
				viewHolder.tvNameView.setText(channelName);
			}
			String rankingInfo = temp.programInfo;
			if (rankingInfo != null) {
				viewHolder.rankInfoView.setText("(" + rankingInfo + ")");
			}
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {

			return true;
		}
	}

	static class RankingViewHolder {

		public TextView nameTxt;
		public ImageView channelPic;
		public TextView time;
		// 直播或者预约按钮
		public ImageView liveBtn;
		public RelativeLayout infoLayout;
		public RelativeLayout editLayout;
		public ImageView editBtn;
		public TextView rankInfoView;
		public TextView tvNameView;
		public TextView hintTextView;
	}

	static class GroupViewHolder {

		public TextView textView;
		public ImageView imageView;
		public RelativeLayout layout;
	}

	private void onLiveBtnClick(int groupPosition, int position) {
		ShortChannelData tempRankingChannelData = rankingAdapter.getChild(
				groupPosition, position);
		if (tempRankingChannelData.livePlay) {
			// liveThroughNet(tempRankingChannelData);
		}
	}

	private boolean myRankingEditeState = false;

	@Override
	public void onNetBegin(String method, boolean isLoadMore) {
		if (Constants.channelProgramRanking.equals(method)) {
			if (rankingTypeChannelData.size() == 0)
				showRankingLoadingLayout();
		} else if (Constants.deleteChannelTask.equals(method)) {
			showpb();
		} else if (Constants.addChannelTask.equals(method)) {
			showpb();
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
		if (Constants.channelProgramRanking.equals(method)) {
			switch (code) {
			case Constants.requestErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "request net");
				showRankingErrorLayout();
				break;
			case Constants.fail_no_net:
				// DialogUtil.alertToast(getApplicationContext(), "no net");
				showRankingErrorLayout();
				break;
			case Constants.fail_server_err:
				// DialogUtil.alertToast(getApplicationContext(),
				// "server Error");
				showRankingErrorLayout();
				break;
			case Constants.parseErr:
				// DialogUtil.alertToast(getApplicationContext(),
				// "parse Error");
				showRankingErrorLayout();
				break;
			case Constants.sucess:
				// DialogUtil.alertToast(getApplicationContext(), "sucess");

				hideRankingErrorLayout();
				updateRankingChannelList(isLoadMore);
				// if (connectBg.isShown()) {
				// hidepb();
				// }
				break;
			default:
				break;
			}
			channelProgramRankingTask = null;
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

		ShortChannelData data3 = rankingTypeChannelData.get(groupPosition).typeChannelData
				.get(deletePosition);
		data3.flagMyChannel = false;
		rankingAdapter.notifyDataSetChanged();
	}

	private void onChannelAdded(int addList, int groupPosition, int addPosition) {

		ShortChannelData data3 = rankingTypeChannelData.get(groupPosition).typeChannelData
				.get(addPosition);
		data3.flagMyChannel = true;
		rankingAdapter.notifyDataSetChanged();
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

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		ShortChannelData channelData = rankingAdapter.getChild(groupPosition,
				childPosition);
		if (myRankingEditeState) {
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

		ShortChannelData shortChannelData = rankingAdapter.getChild(
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
		ShortChannelData channelData3 = rankingAdapter.getChild(groupPosition,
				position);
		if (myRankingEditeState) {
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

	public boolean getEditState() {
		return myRankingEditeState;
	};

	public void changeEditState() {
		if (rankingTypeChannelData.size() != 0) {
			myRankingEditeState = !myRankingEditeState;
			rankingAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getDefaultRankingChannelProgram();
			break;
		default:
			break;
		}
	}
}
