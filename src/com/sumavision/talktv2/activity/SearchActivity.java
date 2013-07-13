package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.HotPlayProgram;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.HotPlayParser;
import com.sumavision.talktv2.net.HotPlayRequest;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.task.HotPlayTaskNew;
import com.sumavision.talktv2.task.SearchTask;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

public class SearchActivity extends Activity implements
		NetConnectionListenerNew, OnClickListener, OnItemClickListener,
		OnRefreshListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_new);
		initOthers();
		initViews();
		setListeners();
		getHotPlay();
	}

	ImageLoaderHelper imageLoaderHelper;

	private void initOthers() {
		imageLoaderHelper = new ImageLoaderHelper();
	}

	private EditText input;
	private MyListView searchResultListView;
	private ProgressBar myProgressBar;
	private TextView myErrText;

	private Button searchBtn;

	private void initViews() {
		input = (EditText) findViewById(R.id.ps_search_edit);
		input.clearFocus();
		gridSearchHistory = (GridView) findViewById(R.id.ps_seatch_history_grid);
		gridSearchHotKey = (GridView) findViewById(R.id.ps_hot_key_grid);
		// gridSearchHistory.setOnItemClickListener(this);
		gridSearchHotKey.setOnItemClickListener(this);
		layoutSearchHistory = (LinearLayout) findViewById(R.id.ps_search_history);
		layoutSearchHotKey = (LinearLayout) findViewById(R.id.ps_hot_key);
		searchResultListView = (MyListView) findViewById(R.id.listView);
		myProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		myErrText = (TextView) findViewById(R.id.err_text);
		searchBtn = (Button) findViewById(R.id.search);
	}

	private void setListeners() {
		findViewById(R.id.ps_back).setOnClickListener(this);
		findViewById(R.id.ps_search_delete).setOnClickListener(this);
		searchBtn.setOnClickListener(this);
		searchResultListView.setOnRefreshListener(this);
		searchResultListView.setPullToRefresh(false);
		searchResultListView.setOnItemClickListener(onItemClickListener);
		input.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE
						|| actionId == EditorInfo.IME_ACTION_SEARCH) {
					searchBtn.performClick();
				}
				if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					searchBtn.performClick();
					return true;
				}
				return false;
			}
		});
	}

	private void hidMErrorLayout() {
		myProgressBar.setVisibility(View.GONE);
		myErrText.setVisibility(View.GONE);
	}

	private void showErrorLayout() {
		myProgressBar.setVisibility(View.GONE);
		myErrText.setVisibility(View.VISIBLE);
		myErrText.setText(Constants.errText);
	}

	private void showLoadingLayout() {
		myProgressBar.setVisibility(View.VISIBLE);
		myErrText.setVisibility(View.GONE);
	}

	@Override
	public void onNetBegin(String method, boolean isLoadMore) {
		if (Constants.searchProgram.equals(method)) {
			if (!isLoadMore) {
				showLoadingLayout();
				showSearchResultLayout();
			}
		}
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("hotPlay".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateHotPlayGrid();
			}
			hotPlayTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	@Override
	public void onNetEnd(int code, String msg, String method, boolean isLoadMore) {
		if (Constants.searchProgram.equals(method)) {
			switch (code) {
			case Constants.requestErr:
			case Constants.fail_no_net:
			case Constants.fail_server_err:
			case Constants.parseErr:
				if (!isLoadMore) {
					hideSearchResltLayout();
				}
				DialogUtil.alertToast(getApplicationContext(), "搜索失败");
				break;
			case Constants.sucess:
				if (isLoadMore) {
					showSearchResultLayout();
				}
				hidMErrorLayout();
				updateSearchList(isLoadMore);
				break;
			default:
				break;
			}
			searchTask = null;
		}
	}

	private String mKeyWord;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ps_back:
			hideSoftPad();
			close();
			break;
		case R.id.ps_search_delete:
			input.setText("");
			break;
		case R.id.search:
			String s = input.getText().toString();
			boolean isValid = isKeyWordValid(s);
			if (isValid) {
				hideSoftPad();
				mKeyWord = s;
				searchList.clear();
				search(0, LIST_COUNT, s, tempList, false);
			} else {
				DialogUtil.alertToast(getApplicationContext(), "请先输入关键字");
			}
			break;

		default:
			break;
		}

	}

	private boolean isKeyWordValid(String s) {
		if (s == null || s.equals("")) {
			return false;
		}
		return true;
	}

	// 搜索历史网格
	private LinearLayout layoutSearchHistory;
	private GridView gridSearchHistory;
	// 热搜词网格
	private LinearLayout layoutSearchHotKey;
	private GridView gridSearchHotKey;
	private ArrayList<VodProgramData> hotPlayList;

	private HotPlayTaskNew hotPlayTask;

	private void getHotPlay() {
		if (hotPlayTask == null) {
			hotPlayTask = new HotPlayTaskNew(this);
			hotPlayTask
					.execute(this, new HotPlayRequest(), new HotPlayParser());
		}
	}

	private void updateHotPlayGrid() {
		ArrayList<VodProgramData> temp = HotPlayProgram.current().hotProgramList;
		if (temp != null) {
			hotPlayList = temp;
			gridSearchHotKey.setAdapter(new HotGridAdapter(temp));
			int height = (hotPlayList.size() / 2 + 1)
					* CommonUtils.dip2px(this, 30);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, height);
			gridSearchHotKey.setLayoutParams(params);
			gridSearchHotKey.setNumColumns(2);
			gridSearchHotKey.setColumnWidth(CommonUtils.dip2px(this, 80));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		VodProgramData temp = hotPlayList.get(position);
		// VodProgramData.current.cpId = 0;
		openProgramDetailActivity(temp.id, temp.topicId);
	}

	private class HotGridAdapter extends BaseAdapter {
		private ArrayList<VodProgramData> list;

		public HotGridAdapter(ArrayList<VodProgramData> list) {
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
			ViewHolderSearch viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolderSearch();
				LayoutInflater inflater = LayoutInflater
						.from(SearchActivity.this);
				convertView = inflater.inflate(
						R.layout.playhistory_hot_list_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolderSearch) convertView.getTag();
			}
			VodProgramData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			return convertView;
		}

	}

	static class ViewHolderSearch {
		public TextView nameTxt;
		public ImageView programPic;
	}

	private void openProgramDetailActivity(String id, String topicId) {
		Intent intent = new Intent(this, ProgramNewActivity.class);
		intent.putExtra("programId", id);
		intent.putExtra("topicId", topicId);
		intent.putExtra("cpId", 0);
		startActivity(intent);
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

	private class SearchResultAdapter extends BaseAdapter {
		private final ArrayList<VodProgramData> list;

		public SearchResultAdapter(ArrayList<VodProgramData> list) {
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
			ViewHolderSearch viewHolderSearch;

			if (convertView == null) {
				viewHolderSearch = new ViewHolderSearch();
				LayoutInflater inflater = LayoutInflater
						.from(SearchActivity.this);
				convertView = inflater.inflate(
						R.layout.search_result_list_item, null);
				viewHolderSearch.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolderSearch.programPic = (ImageView) convertView
						.findViewById(R.id.pic);
				convertView.setTag(viewHolderSearch);
			} else {
				viewHolderSearch = (ViewHolderSearch) convertView.getTag();
			}
			VodProgramData tempS = list.get(position);
			String nameP = tempS.name;
			if (nameP != null) {
				viewHolderSearch.nameTxt.setText(nameP);
			}
			String url = tempS.pic;
			viewHolderSearch.programPic.setTag(url);
			imageLoaderHelper.loadImage(viewHolderSearch.programPic, url,
					R.drawable.rcmd_list_item_pic_default);
			return convertView;
		}
	}

	SearchTask searchTask = null;
	private ArrayList<VodProgramData> searchList = new ArrayList<VodProgramData>();
	private ArrayList<VodProgramData> tempList = new ArrayList<VodProgramData>();
	private SearchResultAdapter searchListAdapter;

	private void search(int first, int count, String keyWord,
			ArrayList<VodProgramData> list, boolean isLoadMore) {
		if (searchTask == null) {
			searchTask = new SearchTask(this, isLoadMore);
			searchTask.execute(this, first, count, keyWord, list);
		}
	}

	private void updateSearchList(boolean isLoadMore) {
		if (isLoadMore) {
			if (tempList != null) {
				searchList.addAll(tempList);
				searchListAdapter.notifyDataSetChanged();
				if (tempList.size() < 20) {
					searchResultListView.setCanLoadMore(false);
				}
				searchResultListView.onLoadMoreOver();
				tempList.clear();
			}
		} else {
			if (tempList != null) {
				searchList.addAll(tempList);
				searchListAdapter = new SearchResultAdapter(searchList);
				searchResultListView.setAdapter(searchListAdapter);
				if (tempList.size() < 20) {
					searchResultListView.setCanLoadMore(false);
				} else {
					searchResultListView.setCanLoadMore(true);
				}
				if (tempList.size() == 0) {
					myErrText.setText("暂无结果");
					myErrText.setVisibility(View.VISIBLE);
				} else {
					myErrText.setVisibility(View.GONE);
				}
				tempList.clear();
			}
		}
	}

	private void hideSearchResltLayout() {
		layoutSearchHistory.setVisibility(View.GONE);
		layoutSearchHotKey.setVisibility(View.VISIBLE);
		searchResultListView.setVisibility(View.GONE);
	}

	private void showSearchResultLayout() {
		layoutSearchHistory.setVisibility(View.GONE);
		layoutSearchHotKey.setVisibility(View.GONE);
		searchResultListView.setVisibility(View.VISIBLE);
	}

	private void hideSoftPad() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadingMore() {
		int start = searchList.size();
		int count = LIST_COUNT;
		search(start, count, mKeyWord, tempList, true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			hideSoftPad();
			close();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void close() {
		if (searchTask != null) {
			searchTask.cancel(true);
			searchTask = null;
		}
		if (hotPlayTask != null) {
			hotPlayTask.cancel(true);
			hotPlayTask = null;
		}
		finish();
	}

	private static final int LIST_COUNT = 20;
	private OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			int position = arg2 - 1;
			if (position == searchList.size()) {
				return;
			}
			VodProgramData programData = searchList.get(position);
			openProgramDetailActivity(programData.id, programData.topicId);
		}
	};
}
