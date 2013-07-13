package com.sumavision.talktv2.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;

public class ActivityWebShowActivity extends Activity implements
		OnClickListener {
	private String url;
	private int activityId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activitywebshow);
		getExtra();
		// 拼凑网页地址 action +用户ID+活动ID
		url = Constants.url + "/activity/webJoin.action" + "?" + "userId="
				+ UserNow.current().userID + "&" + "id=" + activityId;
		initView();
		loadData();
	}

	private void getExtra() {
		Intent intent = getIntent();
		activityId = intent.getIntExtra("activityId", 0);
	}

	private WebView webView;
	private TextView errText;
	private RelativeLayout errLayout;
	private ProgressBar progressBar;

	private void initView() {
		webView = (WebView) findViewById(R.id.webview);
		initializeOptions();

		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				if (progress == 100)
					handler.sendEmptyMessage(LOAD_OVER);
			}

			@Override
			public Bitmap getDefaultVideoPoster() {
				// TODO Auto-generated method stub
				return BitmapFactory.decodeResource(getResources(),
						R.drawable.icon);

			}

			@Override
			public View getVideoLoadingProgressView() {
				// TODO Auto-generated method stub
				return super.getVideoLoadingProgressView();
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				((TextView) findViewById(R.id.title)).setText(title);
				super.onReceivedTitle(view, title);
			}

		});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				handler.sendEmptyMessage(LOAD_ERROR);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}

		});

		findViewById(R.id.back).setOnClickListener(this);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		errText = (TextView) findViewById(R.id.err_text);
		errText.setOnClickListener(this);
		errLayout = (RelativeLayout) findViewById(R.id.errLayout);
	}

	private static final int LOAD_OVER = 1;
	private static final int LOAD_ERROR = 2;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOAD_OVER:
				progressBar.setVisibility(View.GONE);
				errLayout.setVisibility(View.GONE);
				break;
			case LOAD_ERROR:
				showErrorLayout();
				break;
			default:
				break;
			}
		};
	};

	private void showErrorLayout() {
		progressBar.setVisibility(View.GONE);
		errText.setVisibility(View.VISIBLE);
	}

	public void initializeOptions() {
		WebSettings settings = webView.getSettings();

		// User settings
		settings.setJavaScriptEnabled(true);
		settings.setLoadsImagesAutomatically(true);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(false);// todo
		settings.setSaveFormData(true);
		settings.setSavePassword(true);
		settings.setUserAgentString("");

		CookieManager.getInstance().setAcceptCookie(true);

		if (Build.VERSION.SDK_INT <= 7) {
			settings.setPluginsEnabled(true);
		} else {
			settings.setPluginState(PluginState.valueOf(PluginState.ON_DEMAND
					.toString()));
		}

		settings.setSupportZoom(true);
		settings.setSupportMultipleWindows(true);
		webView.setLongClickable(true);
		webView.setScrollbarFadingEnabled(true);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webView.setDrawingCacheEnabled(true);

		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);
	}

	private void loadData() {
		webView.loadUrl(url);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			close();
			break;
		case R.id.err_text:
			loadData();
			break;
		default:
			break;
		}
	}

	private void close() {
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			close();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
