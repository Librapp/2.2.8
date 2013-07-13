package com.sumavision.talktv2.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.ImageLoaderHelper;
import com.sumavision.talktv2.activity.MyListView;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.activity.OtherUserCenterActivity;
import com.sumavision.talktv2.activity.ProgramCommentListView;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.RecommendUserListParser;
import com.sumavision.talktv2.net.RecommendUserListRequest;
import com.sumavision.talktv2.net.SearchUserParser;
import com.sumavision.talktv2.net.SearchUserRequest;
import com.sumavision.talktv2.task.GetRecommendUserTask;
import com.sumavision.talktv2.task.SearchUserTask;
import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;

public class FriendSearchFragment extends Fragment implements
		NetConnectionListener, OnClickListener, OnItemClickListener {
	private ImageLoaderHelper imageLoaderHelper;

	private void initUtils() {
		imageLoaderHelper = new ImageLoaderHelper();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUtils();
	}

	private RelativeLayout recommendLayout;
	private Button calcel;
	private EditText searchInput;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.friend_viewpager_find, null);
			initRecommendUserPager(rootView);
			needLoadData = true;
		} else {
			((ViewGroup) rootView.getParent()).removeView(rootView);
		}
		return rootView;
	}

	private View rootView;
	boolean needLoadData;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (needLoadData) {
			getRecommendUserData();
			needLoadData = false;
		}
	}

	private void initRecommendUserPager(View view) {
		searchInput = (EditText) view.findViewById(R.id.search_edit);
		searchInput.setOnClickListener(this);
		calcel = (Button) view.findViewById(R.id.search_cancle);
		calcel.setOnClickListener(this);

		recommendLayout = (RelativeLayout) view
				.findViewById(R.id.recommend_list_layout);

		searchUserListView = (ProgramCommentListView) view
				.findViewById(R.id.searchListView);

		myRecommandListView = (MyListView) view.findViewById(R.id.listView);
		recommandErrText = (TextView) view.findViewById(R.id.err_text);
		recommendProgressBar = (ProgressBar) view
				.findViewById(R.id.progressBar);
		myRecommandListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				getRecommendUserData();
			}

			@Override
			public void onLoadingMore() {
				// TODO
				int start = 0;
				int count = recommandUserList.size() + 10;
				getRecommendUserData(start, count);
			}
		});
		myRecommandListView.setOnItemClickListener(this);
		recommandErrText.setOnClickListener(this);
	}

	private TextView recommandErrText;
	private ProgressBar recommendProgressBar;
	private MyListView myRecommandListView;
	private ArrayList<User> recommandUserList = new ArrayList<User>();

	private GetRecommendUserTask getRecommendUserTask;

	private void getRecommendUserData() {
		if (getRecommendUserTask == null) {
			getRecommendUserTask = new GetRecommendUserTask(this);
			getRecommendUserTask.execute(getActivity(),
					new RecommendUserListRequest(),
					new RecommendUserListParser());

			if (recommandUserList.size() == 0) {
				recommandErrText.setVisibility(View.GONE);
				recommendProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getRecommendUserData(int start, int count) {
		if (getRecommendUserTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getRecommendUserTask = new GetRecommendUserTask(this);
			getRecommendUserTask.execute(getActivity(),
					new RecommendUserListRequest(),
					new RecommendUserListParser());

		}
	}

	private ProgramCommentListView searchUserListView;

	private ArrayList<User> searchUserList = new ArrayList<User>();

	private SearchUserTask searchUserTask;

	private void getSearchUserData() {
		if (searchUserTask == null) {
			searchUserTask = new SearchUserTask(this);
			searchUserTask.execute(getActivity(), new SearchUserRequest(),
					new SearchUserParser());
		}
	}

	private void getSearchUserData(int start, int count) {
		if (searchUserTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			searchUserTask = new SearchUserTask(this);
			searchUserTask.execute(getActivity(), new SearchUserRequest(),
					new SearchUserParser());
		}
	}

	private void updateSearchListView() {
		ArrayList<User> temp = (ArrayList<User>) UserNow.current()
				.getSearchUser();
		if (temp != null) {
			searchUserList = temp;
			FindAdapter adapter = new FindAdapter(searchUserList, getActivity());
			searchUserListView.setAdapter(adapter);
			searchUserListView.setOnItemClickListener(searchListClicked);
			searchUserListView.setVisibility(View.VISIBLE);
		}
	}

	private OnItemClickListener searchListClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (arg2 == searchUserList.size()) {
				getSearchUserData(0, arg2 + 10);
				return;
			}
			int fid = searchUserList.get(arg2).userId;
			// UserOther.current().iconURL = searchUserList.get(arg2).iconURL;
			openOtherUserCenterActivity(fid, searchUserList.get(arg2).iconURL);
		}
	};

	private void updateFindListView() {
		searchUserListView.setVisibility(View.GONE);
		recommendLayout.setVisibility(View.VISIBLE);
		ArrayList<User> temp = (ArrayList<User>) UserNow.current().getFriend();
		if (temp != null) {
			recommandUserList = temp;
			if (recommandUserList.size() == 0) {
				recommandErrText.setText("暂无推荐用户");
				recommandErrText.setVisibility(View.VISIBLE);
			} else {
				recommandErrText.setVisibility(View.GONE);
				FindAdapter adapter = new FindAdapter(recommandUserList,
						getActivity());
				myRecommandListView.setAdapter(adapter);
			}
		} else {
			recommandErrText.setVisibility(View.VISIBLE);
		}
		recommendProgressBar.setVisibility(View.GONE);
	}

	private class FindAdapter extends BaseAdapter {
		private ArrayList<User> list;
		private Context context;

		public FindAdapter(ArrayList<User> list, Context context) {
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
			FindViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new FindViewHolder();
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				convertView = inflater.inflate(R.layout.friend_find_list_item,
						null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.introTxt = (TextView) convertView
						.findViewById(R.id.intro);
				viewHolder.headPic = (ImageView) convertView
						.findViewById(R.id.pic);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (FindViewHolder) convertView.getTag();

			}
			User temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String intro = temp.signature;
			if (intro != null) {
				viewHolder.introTxt.setText(intro);
			}
			String url = temp.iconURL;
			viewHolder.headPic.setTag(url);
			imageLoaderHelper.loadImage(viewHolder.headPic, url,
					R.drawable.list_headpic_default);
			return convertView;
		}

	}

	static class FindViewHolder {
		public TextView nameTxt;
		public TextView introTxt;
		public ImageView headPic;
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("recommendUserList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateFindListView();
			} else {
				recommendProgressBar.setVisibility(View.GONE);
				if (recommandUserList.size() == 0) {
					recommandErrText.setVisibility(View.VISIBLE);
				}
				myRecommandListView.onLoadError();
			}
			getRecommendUserTask = null;
		} else if (Constants.searchUser.equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateSearchListView();
			} else {
				DialogUtil.alertToast(getActivity(), "查询失败");
			}
			searchUserTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_cancle:
			calcel.setVisibility(View.GONE);
			recommendLayout.setVisibility(View.VISIBLE);
			searchUserListView.setVisibility(View.GONE);
			searchInput.setText("");
			hideSoftPad();
			break;
		case R.id.search_edit:
			if (!calcel.isShown()) {
				calcel.setVisibility(View.VISIBLE);
				recommendLayout.setVisibility(View.GONE);
				searchUserListView.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.err_text:
			getRecommendUserData();
			break;
		default:
			break;
		}
	}

	private void hideSoftPad() {
		InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(getActivity()
				.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (position - 1 == recommandUserList.size() || position - 1 < 0) {
			return;
		}

		int rId = recommandUserList.get(position - 1).userId;
		openOtherUserCenterActivity(rId,
				recommandUserList.get(position - 1).iconURL);
	}

	private void openOtherUserCenterActivity(int id, String iconURL) {
		Intent intent = new Intent(getActivity(), OtherUserCenterActivity.class);
		intent.putExtra("id", id);
		intent.putExtra("iconURL", iconURL);
		startActivity(intent);
	}

	public void onSearchSubmitClick() {
		String s = searchInput.getText().toString();
		boolean isValid = isKeyWordValid(s);
		if (isValid) {
			recommendLayout.setVisibility(View.GONE);
			User.current().name = s;
			if (searchUserTask != null) {
				searchUserTask.cancel(true);
				searchUserTask = null;
			}
			getSearchUserData();
		} else {
			DialogUtil.alertToast(getActivity(), "请先输入关键字");
		}
	}

	private boolean isKeyWordValid(String s) {
		if (s == null || s.equals("")) {
			return false;
		}
		return true;
	}
}
