package com.sumavision.talktv2.fragment;

import io.vov.utils.Log;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.ActivitiesDetailActivity;
import com.sumavision.talktv2.activity.FocusGallery;
import com.sumavision.talktv2.activity.GalleryAdapter;
import com.sumavision.talktv2.activity.ImageLoaderHelper;
import com.sumavision.talktv2.activity.ProgramNewActivity;
import com.sumavision.talktv2.activity.RecommandAppActivity;
import com.sumavision.talktv2.activity.RecommendListView;
import com.sumavision.talktv2.activity.RecommendListView.OnRefreshListener;
import com.sumavision.talktv2.activity.WebBrowserActivity;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.RecommendData;
import com.sumavision.talktv2.data.RecommendPageData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.RecommendPageParser;
import com.sumavision.talktv2.net.RecommendPageRequest;
import com.sumavision.talktv2.net.RecommendVodProgramListParser;
import com.sumavision.talktv2.net.RecommendVodProgramListRequest;
import com.sumavision.talktv2.task.RecommandDetailTask;
import com.sumavision.talktv2.task.RecommendVodProgramTask;
import com.sumavision.talktv2.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

public class RecommendFragment extends Fragment implements OnClickListener,
		NetConnectionListener, OnItemClickListener, OnRefreshListener {

	private TextView errTxtView;
	private ProgressBar progressBar;

	private RecommendListView hotListView;
	private FocusGallery picViewPager;
	private LinearLayout picStarsLayout;
	private TextView picTitleTextView;
	private int bigPicSize = 0;

	private ImageLoaderHelper imageLoaderHelper;

	private void initUtils() {
		imageLoaderHelper = new ImageLoaderHelper();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUtils();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(
					R.layout.rcmd_recommend_program_viewpager_item, null);
			errTxtView = (TextView) rootView.findViewById(R.id.err_text);
			errTxtView.setOnClickListener(this);
			progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
			hotListView = (RecommendListView) rootView
					.findViewById(R.id.listView);
			View headerView = inflater.inflate(R.layout.rcmd_focus_pic_layout,
					null);
			hotListView.addHeaderView(headerView);
			picViewPager = (FocusGallery) headerView
					.findViewById(R.id.pic_view);
			picStarsLayout = (LinearLayout) headerView
					.findViewById(R.id.pic_star);
			picTitleTextView = (TextView) headerView
					.findViewById(R.id.pic_title);
			hotListView.setOnItemClickListener(this);
			hotListView.setOnRefreshListener(this);
			picViewPager.setOnItemClickListener(focusItemClickListener);
			picViewPager.setOnItemSelectedListener(focusItemSelectedListener);
			picViewPager.setAnimationDuration(0);
			needLoadData = true;
		} else {
			((ViewGroup) rootView.getParent()).removeView(rootView);
		}
		return rootView;
	}

	private OnItemClickListener focusItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (OtherCacheData.current().isDebugMode)
				Log.e("focusItemClickListener", "item  clicked!");
			int position = arg2;
			int type = list.get(position).type;
			switch (type) {
			case 1: // program
				MobclickAgent.onEvent(getActivity(), "banner",
						list.get(position).name);
				String programId = String.valueOf(list.get(position).id);
				String topicId = list.get(position).topicId;
				// VodProgramData.current.cpId = 0;
				openProgramDetailActivity(programId, topicId,
						list.get(position).name, 0);
				break;
			case 2: // activity
				int acitivtyId = (int) list.get(position).id;
				openActivityDetailActivity(acitivtyId);
				break;
			case 3:// user
				break;
			case 4:// star
				break;
			case 12:
				Intent iAd = new Intent(getActivity(), WebBrowserActivity.class);
				RecommendData rcd = list.get(position);
				iAd.putExtra("url", rcd.url);
				iAd.putExtra("title", rcd.name);
				startActivity(iAd);
				break;
			case 13:
				Intent intent = new Intent(getActivity(),
						RecommandAppActivity.class);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};
	private OnItemSelectedListener focusItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			try {
				onPicSelected(arg2);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	};

	private void onPicSelected(int position) {
		int size = list.size();
		for (int i = 0; i < size; i++) {
			ImageView imageView = (ImageView) picStarsLayout.findViewWithTag(i)
					.findViewById(R.id.imageView);
			if (i == position) {
				imageView.setImageResource(R.drawable.rcmd_pic_star_focus);
			} else {
				imageView.setImageDrawable(null);
			}
		}
		picTitleTextView.setText(list.get(position).name);
	}

	private void openActivityDetailActivity(int id) {
		Intent intent = new Intent(getActivity(),
				ActivitiesDetailActivity.class);
		intent.putExtra("id", id);
		startActivity(intent);
	}

	private View rootView;
	boolean needLoadData;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (needLoadData) {
			getRecommandDetail();
			needLoadData = false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getRecommandDetail();
			break;
		default:
			break;
		}
	}

	private RecommandDetailTask recommandDetailTask;

	private void getRecommandDetail() {
		if (recommandDetailTask == null) {
			recommandDetailTask = new RecommandDetailTask(this);
			recommandDetailTask.execute(getActivity(),
					new RecommendPageRequest(), new RecommendPageParser());
		}
	}

	private void showErrorLayout() {
		hotListView.setVisibility(View.GONE);
		errTxtView.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
	}

	private void showLoadingLayout() {
		errTxtView.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		hotListView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onNetBegin(String method) {
		if ("recommendDetail".equals(method)) {
			showLoadingLayout();
		}
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("recommendDetail".equals(method)) {
			if (msg != null && msg.equals("")) {
				updateHotProgramList();
			} else {
				hotListView.onLoadError();
				showErrorLayout();
			}
			recommandDetailTask = null;
		} else if ("hotVodProgramList".equals(method)) {
			if (msg != null && msg.equals("")) {
				updateHotProgramList();
			} else {
				progressBar.setVisibility(View.GONE);
			}
			recommendVodTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	ArrayList<RecommendData> list = new ArrayList<RecommendData>();

	private ArrayList<VodProgramData> hotList;

	private void updateHotProgramList() {
		list = (ArrayList<RecommendData>) RecommendPageData.current()
				.getRecommend();
		if (list != null) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			GalleryAdapter adapter = new GalleryAdapter(getActivity(), list);
			picViewPager.setAdapter(adapter);
			bigPicSize = list.size();
			if (bigPicSize > 0) {
				picStarsLayout.removeAllViews();
			}
			for (int i = 0; i < bigPicSize; i++) {
				FrameLayout frame = (FrameLayout) inflater.inflate(
						R.layout.rcmd_pic_star, null);
				frame.setTag(i);
				if (i == 0) {
					((ImageView) frame.findViewById(R.id.imageView))
							.setImageResource(R.drawable.rcmd_pic_star_focus);
				}
				picStarsLayout.addView(frame);
			}
			picTitleTextView.setText(list.get(0).name);
			// picViewPager.setOnClickListener(recommendClick);
		}

		hotList = new ArrayList<VodProgramData>();
		ArrayList<VodProgramData> temp = (ArrayList<VodProgramData>) RecommendPageData
				.current().getLiveProgram();
		if (temp != null && temp.size() > 0) {
			hotList.addAll(temp);
		}
		temp = (ArrayList<VodProgramData>) RecommendPageData.current()
				.getVodProgram();
		if (temp != null && temp.size() > 0) {
			hotList.addAll(temp);
		}
		if (hotList.size() != 0) {
			HotProgramListAdapter adapter = new HotProgramListAdapter(hotList,
					getActivity());
			hotListView.setAdapter(adapter);
			errTxtView.setVisibility(View.GONE);
			// hotProgressHide = true;
		} else {
			errTxtView.setVisibility(View.VISIBLE);
			// hotProgressHide = false;
		}
		progressBar.setVisibility(View.GONE);
	}

	public class HotProgramListAdapter extends BaseAdapter {
		private ArrayList<VodProgramData> list;
		private Context context;

		public HotProgramListAdapter(ArrayList<VodProgramData> list,
				Context context) {
			this.list = list;
			this.context = context;
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
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				convertView = inflater.inflate(R.layout.rcmd_hot_list_item,
						null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.updateTxt = (TextView) convertView
						.findViewById(R.id.update);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.viewerCount = (TextView) convertView
						.findViewById(R.id.viewercount);
				viewHolder.programPic = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.status = (TextView) convertView
						.findViewById(R.id.status);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);
				viewHolder.scoreView = (TextView) convertView
						.findViewById(R.id.score);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			VodProgramData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			int viewerCount = temp.playTimes;
			viewHolder.viewerCount.setText(CommonUtils
					.processPlayCount(viewerCount));
			String intro = temp.shortIntro;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			if (temp.livePlay == 0) {
				String tvName = temp.channelName;
				if (tvName != null) {
					viewHolder.updateTxt.setText(tvName);
				}
				String startTime = temp.startTime;
				String endTime = temp.endTime;
				if (startTime != null && endTime != null) {
					viewHolder.time.setText(startTime + "-" + endTime);
					viewHolder.time.setVisibility(View.VISIBLE);
				} else {
					viewHolder.time.setVisibility(View.GONE);
				}
				if (temp.isPlaying == 0) {
					String playMinutes = temp.playMinutes;
					if (playMinutes != null) {
						viewHolder.status.setText(playMinutes);
						viewHolder.status.setBackgroundDrawable(null);
					}
				} else {
					viewHolder.status.setText("");
				}
				viewHolder.status.setVisibility(View.VISIBLE);
			} else {
				String updateText = temp.updateName;
				if (updateText != null) {
					viewHolder.updateTxt.setText(updateText);
				}
				viewHolder.status.setVisibility(View.GONE);
				viewHolder.time.setVisibility(View.GONE);
			}
			String score = temp.point;
			if (score != null) {
				try {
					float floatScore = Float.valueOf(score);
					if (floatScore <= 1) {
						viewHolder.scoreView.setVisibility(View.GONE);
					} else {
						if (score.length() > 3) {
							score = score.substring(0, 3);
						}
						viewHolder.scoreView.setText(score + "分");
						viewHolder.scoreView.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					viewHolder.scoreView.setVisibility(View.GONE);
				}

			} else {
				viewHolder.scoreView.setVisibility(View.GONE);
			}
			String url = temp.pic;
			viewHolder.programPic.setTag(url);
			imageLoaderHelper.loadImage(viewHolder.programPic, url,
					R.drawable.recommend_default);
			return convertView;
		}

	}

	static class ViewHolder {
		public TextView nameTxt;
		public TextView updateTxt;
		public TextView introTxt;
		public TextView viewerCount;
		public ImageView programPic;
		public TextView status;
		public TextView time;
		public TextView scoreView;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		int hotPosition = position - hotListView.getHeaderViewsCount();
		if (hotPosition == hotList.size() || hotPosition < 0) {
			return;
		}
		VodProgramData tempHot = hotList.get(hotPosition);
		MobclickAgent.onEvent(getActivity(), "jiemu", tempHot.name);
		String pid = tempHot.id;
		String topicId = tempHot.topicId;
		openProgramDetailActivity(pid, topicId, tempHot.updateName,
				tempHot.cpId);
	}

	private void openProgramDetailActivity(String id, String topicId,
			String updateName, long cpId) {
		Intent intent = new Intent(getActivity(), ProgramNewActivity.class);
		intent.putExtra("programId", id);
		intent.putExtra("topicId", topicId);
		intent.putExtra("cpId", cpId);
		intent.putExtra("updateName", updateName);

		startActivity(intent);
	}

	@Override
	public void onRefresh() {
		int start = 0;
		int count = +20;
		getHotList(start, count);
	}

	@Override
	public void onLoadingMore() {
		int start = 0;
		int count = hotList.size() + 20;
		getHotList(start, count);
	}

	private RecommendVodProgramTask recommendVodTask;

	private void getHotList(int start, int count) {
		if (recommendVodTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			recommendVodTask = new RecommendVodProgramTask();
			recommendVodTask.execute(getActivity(), this,
					new RecommendVodProgramListRequest(),
					new RecommendVodProgramListParser());
			errTxtView.setVisibility(View.GONE);
		}
	}

}
