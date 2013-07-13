package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.sumavision.talktv2.activity.AsyncImageBitmapLoader.ImageCallback;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.AppData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.RecommendAppParser;
import com.sumavision.talktv2.net.RecommendAppRequest;
import com.sumavision.talktv2.task.GetRecommandAppTask;
import com.sumavision.talktv2.utils.AutoNetConnection;
import com.umeng.analytics.MobclickAgent;

public class RecommandAppActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnRefreshListener, OnItemClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recommandapp);
		initOthers();
		initViews();
		setListeners();
		getRecommandAppData();
	}

	private AsyncImageBitmapLoader imageLoader;

	private void initOthers() {
		imageLoader = new AsyncImageBitmapLoader();
	}

	private void setListeners() {
		errText.setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
		myAppListView.setOnRefreshListener(this);
		myAppListView.setOnItemClickListener(this);
	}

	private TextView errText;
	private ProgressBar progressBar;
	private MyListView myAppListView;
	private ArrayList<AppData> list = new ArrayList<AppData>();

	private void initViews() {
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		myAppListView = (MyListView) findViewById(R.id.listView);
	}

	private GetRecommandAppTask getRecommendAppTask;

	private void getRecommandAppData() {
		if (getRecommendAppTask == null) {
			getRecommendAppTask = new GetRecommandAppTask(this);
			getRecommendAppTask.execute(this, new RecommendAppRequest(),
					new RecommendAppParser());
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private RecommandAppdapter adapter;

	private void updateUI() {
		ArrayList<AppData> temp = (ArrayList<AppData>) OtherCacheData.current()
				.getApp();
		if (temp != null) {
			list = temp;
			if (list.size() == 0) {
				errText.setText("暂时没有推荐软件");
				errText.setVisibility(View.VISIBLE);
			} else {
				errText.setVisibility(View.GONE);
				adapter = new RecommandAppdapter(list);
				myAppListView.setAdapter(adapter);
			}
		} else {
			errText.setVisibility(View.VISIBLE);
		}
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void onRefresh() {
		getRecommandAppData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getRecommandAppData();
			break;
		case R.id.back:
			close();
			break;
		default:
			break;
		}
	}

	private class RecommandAppdapter extends BaseAdapter {
		private ArrayList<AppData> list;

		public RecommandAppdapter(ArrayList<AppData> list) {
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
						.from(RecommandAppActivity.this);
				convertView = inflater.inflate(R.layout.recommendapp_list_item,
						null);
				viewHolder.pic = (ImageView) convertView.findViewById(R.id.pic);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.intro = (TextView) convertView
						.findViewById(R.id.intro);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			AppData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String intro = temp.shortIntro;
			if (intro != null) {
				viewHolder.intro.setText(intro);
			}
			String url = temp.pic;
			viewHolder.pic.setTag(url);
			loadImage(viewHolder.pic, url);
			return convertView;
		}

		public class ViewHolder {
			public ImageView pic;
			public TextView nameTxt;
			public TextView intro;
		}

	}

	private void loadImage(final ImageView imageView, String url) {
		if (url != null) {
			Bitmap bitmap = imageLoader.loadDrawable(url, new ImageCallback() {
				@Override
				public void imageLoaded(Bitmap imageDrawable, String imageUrl) {
					String selfUrl = (String) imageView.getTag();
					if (selfUrl != null && selfUrl.equals(imageUrl)) {
						imageView.setImageBitmap(imageDrawable);
					}
				}
			});
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
				imageView
						.setImageResource(R.drawable.rcmd_list_item_pic_default);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position != 0) {
			String url = list.get(position - 1).url;
			String title = list.get(position - 1).name;
			MobclickAgent.onEvent(this, "tuijianapp", title);
			openWebViewActivity(url, title);
			AutoNetConnection.count = 0;
			AutoNetConnection.processPlayURL(url);
		}
	}

	private void openWebViewActivity(String url, String title) {
		// Intent intent = new Intent(this, WebViewActivity.class);
		Intent intent = new Intent(this, WebBrowserActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("title", title);
		startActivity(intent);
	}

	@Override
	public void onNetBegin(String method) {
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("recommendAppList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateUI();
			} else {
				progressBar.setVisibility(View.GONE);
				if (list.size() == 0) {
					errText.setVisibility(View.VISIBLE);
				}
				myAppListView.onLoadError();
			}
			getRecommendAppTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		getRecommendAppTask = null;
		Log.e(TAG, "onCancel callback");
	}

	private void close() {
		if (getRecommendAppTask != null) {
			getRecommendAppTask.cancel(true);
			Log.e(TAG, " call cancel");
		}
		finish();
	}

	private static final String TAG = "RecommandAppActivity";

	@Override
	public void onBackPressed() {
		close();
	}

	@Override
	public void onLoadingMore() {
		// TODO
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
