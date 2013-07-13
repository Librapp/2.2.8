package com.sumavision.talktv2.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.utils.AutoNetConnection;
import com.umeng.analytics.MobclickAgent;

/**
 * @author 郭鹏
 * @version 2.0
 * @createTime 2012-6-6
 * @description web浏览器界面
 * @changeLog
 */
public class WebBrowserActivity extends Activity implements OnClickListener {

	private WebView web;
	private Button back;
	private ProgressBar pb;
	private final int CLSOE_PROGRESSBAR = 1;
	private final int OPEN_PROGRESSBAR = 2;
	private final int MSG_CLOSE_ACTIVITY = 3;
	private String url = null;
	private String title = "电视粉V2.0";
	private TextView title_txt;

	private RelativeLayout all;
	// private Animation open2up;
	// private Animation close2bottom;
	private boolean isAgreement = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webbrowser);
		all = (RelativeLayout) findViewById(R.id.webbrowser_all);
		// open2up = AnimationUtils.loadAnimation(getApplicationContext(),
		// R.anim.activity_open2up);
		// close2bottom = AnimationUtils.loadAnimation(getApplicationContext(),
		// R.anim.activity_close2bottom);
		// all.startAnimation(open2up);
		if (getIntent().getStringExtra("url") != null) {
			url = getIntent().getStringExtra("url");
		}
		if (getIntent().getStringExtra("title") != null)
			title = getIntent().getStringExtra("title");
		initView();

		if (title.equals("电视粉使用协议")) {
			isAgreement = true;
		}

	}

	private boolean isBigDisplay() {

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels;

		if (width > 320) {
			return true;
		} else {
			return false;
		}

	}

	private void initView() {

		web = (WebView) findViewById(R.id.wbb);

		if (!isAgreement) {

			web.getSettings().setJavaScriptEnabled(true);
			web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
			web.getSettings().setPluginsEnabled(true);
			web.invokeZoomPicker();
			web.getSettings().setSupportZoom(true);
			web.getSettings().setBuiltInZoomControls(true);

			boolean b = isBigDisplay();
			if (b) {
				web.setInitialScale(50);
			} else {
				web.setInitialScale(32);
			}

			web.getSettings().setLoadWithOverviewMode(true);
			web.getSettings().setPluginsEnabled(true);
			web.getSettings().setAllowFileAccess(true);
			web.setDownloadListener(new FileDownLoadListener());

			// web.getSettings().setPluginState(PluginState.ON);

			web.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);
					return true;
				}

			});

			web.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int progress) {
					serverHandler.sendEmptyMessage(OPEN_PROGRESSBAR);
					if (progress == 100)
						serverHandler.sendEmptyMessage(CLSOE_PROGRESSBAR);
				}

			});

			int screenDensity = getResources().getDisplayMetrics().densityDpi;
			WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
			switch (screenDensity) {
			case DisplayMetrics.DENSITY_LOW:
				zoomDensity = WebSettings.ZoomDensity.CLOSE;
				break;
			case DisplayMetrics.DENSITY_MEDIUM:
				zoomDensity = WebSettings.ZoomDensity.MEDIUM;
				break;
			case DisplayMetrics.DENSITY_HIGH:
				zoomDensity = WebSettings.ZoomDensity.FAR;
				break;
			}

			web.getSettings().setDefaultZoom(zoomDensity);

			if (OtherCacheData.current().isDebugMode) {
				Log.e("DisplayMetrics", zoomDensity + "");
			}

			int width = getResources().getDisplayMetrics().widthPixels;
			int height = getResources().getDisplayMetrics().heightPixels;
			web.measure(width, height);

		} else {
			web.getSettings().setBuiltInZoomControls(false);
			web.getSettings().setSupportZoom(false);
		}

		// url =
		// "http://wan.sogou.com/nav.do?fl=sxd_fl_18&fid=3&tf=0&ab=0&source=0001000100001&gid=2&sid=48&pid=2125676224";
		// url = "http://www.cntv.cn/";
		// url = "http://www.youku.com/";
		if (url != null)
			web.loadUrl(url);
		else {
			Toast.makeText(getApplicationContext(), "页面加载失败!",
					Toast.LENGTH_SHORT).show();
			finish();
		}
		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);

		pb = (ProgressBar) findViewById(R.id.wbb_title_pb);
		title_txt = (TextView) findViewById(R.id.wbb_title_name);
		title_txt.setText(title);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.back:
			// all.startAnimation(close2bottom);
			serverHandler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_BACK) && web.canGoBack()) {
			web.goBack();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			// all.startAnimation(close2bottom);
			serverHandler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 400);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private Handler serverHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_CLOSE_ACTIVITY:
				finish();
				break;
			case OPEN_PROGRESSBAR:
				pb.setVisibility(View.VISIBLE);
				break;

			case CLSOE_PROGRESSBAR:
				pb.setVisibility(View.INVISIBLE);
				break;

			case JSONMessageType.NET_BEGIN:

				break;
			case JSONMessageType.NET_END:

				break;
			default:
				break;
			}

		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		if (User.current().needChangeBackground) {
			if (User.current().sdcardThemeDrawable != null) {
				all.setBackgroundDrawable(User.current().sdcardThemeDrawable);
			} else {
				all.setBackgroundResource(User.current().nowBg);
			}
		}

		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);

	}

	private class FileDownLoadListener implements DownloadListener {

		@Override
		public void onDownloadStart(String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength) {
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AutoNetConnection.closeconnectThread();
	}
}
