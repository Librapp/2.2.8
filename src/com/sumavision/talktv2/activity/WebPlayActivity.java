package com.sumavision.talktv2.activity;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.sumavision.talktv2.R;

public class WebPlayActivity extends Activity implements OnClickListener,
		OnTouchListener, UncaughtExceptionHandler {

	private String url;
	private String title;
	private int isLive;
	private String videoPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		setContentView(R.layout.webplay);
		getExtra();
		initView();
		loadData();
	}

	private boolean isHighLevel;

	private void init() {
		if (Integer.parseInt(VERSION.SDK) >= 14) {
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
			isHighLevel = true;
		}
		mGestureDetector = new GestureDetector(this, new GestureListener());
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	private void getExtra() {
		Intent intent = getIntent();
		if (intent.hasExtra("url"))
			url = intent.getStringExtra("url");
		if (intent.hasExtra("title"))
			title = intent.getStringExtra("title");
		if (intent.hasExtra("videoPath"))
			videoPath = intent.getStringExtra("videoPath");
		if (intent.hasExtra("playType"))
			isLive = intent.getIntExtra("playType", 2);
	}

	private WebView webView;
	private Bitmap mDefaultVideoPoster = null;
	private View mVideoProgressView = null;

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
				if (mDefaultVideoPoster == null) {
					mDefaultVideoPoster = BitmapFactory.decodeResource(
							WebPlayActivity.this.getResources(),
							R.drawable.netlive_icon);
				}
				return mDefaultVideoPoster;
			}

			@Override
			public View getVideoLoadingProgressView() {
				if (mVideoProgressView == null) {
					LayoutInflater inflater = LayoutInflater
							.from(WebPlayActivity.this);
					mVideoProgressView = inflater.inflate(
							R.layout.netlive_loading_view, null);
				}

				return mVideoProgressView;
			}

			@Override
			public void onShowCustomView(View view,
					WebChromeClient.CustomViewCallback callback) {
				showCustomView(view, callback);
			}

			@Override
			public void onHideCustomView() {
				hideCustomView();
			}

			@Override
			public void onCloseWindow(WebView window) {
				super.onCloseWindow(window);
			}

			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

				return super.onConsoleMessage(consoleMessage);
			}

			@Override
			public void onShowCustomView(View view, int requestedOrientation,
					CustomViewCallback callback) {
				super.onShowCustomView(view, requestedOrientation, callback);
			}

		});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				handler.sendEmptyMessage(LOAD_ERROR);
			}

			@Override
			public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
				// DialogUtil.alertToast(getApplicationContext(), "keyevent="
				// + event.getAction());
				return super.shouldOverrideKeyEvent(view, event);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				if (url.equals(WebPlayActivity.this.url)) {
					// DialogUtil.alertToast(getApplicationContext(),
					// "same url");
					return false;
				}
				if (url.startsWith("http://") && url.contains(".mp4")) {
					url = "tvfanplayurl" + url.substring(4);
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					WebPlayActivity.this.startActivity(i);
					return true;
				} else {
					// DialogUtil.alertToast(getApplicationContext(),
					// "not mp4  "
					// + url);
				}

				return super.shouldOverrideUrlLoading(view, url);
			}

		});

		findViewById(R.id.back).setOnClickListener(this);

		if (title != null) {
			((TextView) findViewById(R.id.title)).setText(title);
		}

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		errText = (TextView) findViewById(R.id.err_text);
		errLayout = (RelativeLayout) findViewById(R.id.errLayout);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.play:
			openLiveActivity(isLive, videoPath, title);
			finish();
			break;
		case R.id.err_text:
			loadData();
			break;
		default:
			break;
		}
	}

	private static final int LOAD_OVER = 1;
	private static final int LOAD_ERROR = 2;
	private static final int VIDEO_CLICK = 3;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOAD_OVER:
				progressBar.setVisibility(View.GONE);
				errLayout.setVisibility(View.GONE);
				if (videoPath != null && !videoPath.equals("")) {
					findViewById(R.id.play).setOnClickListener(
							WebPlayActivity.this);
					findViewById(R.id.play).setVisibility(View.VISIBLE);
				}
				break;
			case LOAD_ERROR:
				showErrorLayout();
				break;
			case VIDEO_CLICK:
				// Toast.makeText(getApplicationContext(), "video click",
				// Toast.LENGTH_SHORT).show();
				break;
			case PAGE_ERR:
				Log.e("WebPlay", "error");
				new AlertDialog.Builder(WebPlayActivity.this)
						.setIcon(R.drawable.icon_small)
						.setTitle("电视粉温馨提示")
						.setCancelable(false)
						.setMessage("当前网页暂时无法播放，请尝试更换其他网页地址播放或稍后重试")
						.setNeutralButton("知道了",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										WebPlayActivity.this.finish();
									}

								}).create().show();
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (webView != null) {
			webView.loadUrl("www.baidu.com");
			if (webView.canGoBack()) {
				webView.goBack();
			}
		}

	};

	@Override
	protected void onPause() {
		super.onPause();
		if (isHighLevel) {
			// webView.onPause();
		}
	}

	private void openLiveActivity(int isLive, String url, String title) {
		Intent intent = new Intent(this, NewLivePlayerActivity.class);
		intent.putExtra("path", url);
		intent.putExtra("playType", isLive);// 点播
		if (title != null) {
			intent.putExtra("title", title);
			intent.putExtra("nameHolder", title);
		}
		startActivity(intent);
	}

	private TextView errText;
	private RelativeLayout errLayout;
	private ProgressBar progressBar;

	private void showErrorLayout() {
		progressBar.setVisibility(View.GONE);
		errText.setVisibility(View.VISIBLE);
	}

	private void loadData() {
		if (url != null) {
			webView.loadUrl(url);
			progressBar.setVisibility(View.VISIBLE);
			errLayout.setVisibility(View.VISIBLE);
		} else {
			showErrorLayout();
		}
	}

	public void initializeOptions() {
		WebSettings settings = webView.getSettings();

		// User settings
		settings.setJavaScriptEnabled(true);
		settings.setLoadsImagesAutomatically(true);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);// todo
		settings.setSaveFormData(true);
		settings.setSavePassword(true);

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
		settings.setAllowFileAccess(false);
		settings.setUseWideViewPort(true);
		// settings.setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.1.0; zh-cn; GT-I9000 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
		// settings.setUserAgentString(IPHONE_USERAGENT);

		// settings.setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.2; en-gb; Nexus One Build/FRF50) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
		// settings.setUserAgentString("Mozilla/5.0 (iPhone; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko ) Version/5.1 Mobile/9B176 Safari/7534.48.3");
	}

	private static final String DESKTOP_USERAGENT = "Mozilla/5.0 (Macintosh; "
			+ "U; Intel Mac OS X 10_5_7; en-us) AppleWebKit/530.17 (KHTML, "
			+ "like Gecko) Version/4.0 Safari/530.17";
	private static final String IPHONE_USERAGENT = "Mozilla/5.0 (iPhone; U; "
			+ "CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 "
			+ "(KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16";
	private FrameLayout mFullscreenContainer;

	static class FullscreenHolder extends FrameLayout {

		public FullscreenHolder(Context ctx) {
			super(ctx);
			setBackgroundColor(ctx.getResources().getColor(R.color.black));
		}

		@Override
		public boolean onTouchEvent(MotionEvent evt) {
			return true;
		}

	}

	private void setStatusBarVisibility(boolean visible) {
		int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT,
			ViewGroup.LayoutParams.FILL_PARENT);

	private View mCustomView;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;

	private void showCustomView(View view,
			WebChromeClient.CustomViewCallback callback) {
		if (mCustomView != null) {
			callback.onCustomViewHidden();
			// Toast.makeText(getApplicationContext(), "mCustom view return",
			// Toast.LENGTH_SHORT).show();
			return;
		}

		FrameLayout decor = (FrameLayout) getWindow().getDecorView();
		mFullscreenContainer = new FullscreenHolder(this);
		mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
		decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);

		mCustomView = view;
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setStatusBarVisibility(false);
		mCustomViewCallback = callback;
		if (view instanceof FrameLayout) {
			if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
				// Toast.makeText(getApplicationContext(), "video view",
				// Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void hideCustomView() {
		if (mCustomView == null)
			return;
		try {
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setStatusBarVisibility(true);
			FrameLayout decor = (FrameLayout) getWindow().getDecorView();
			decor.removeView(mFullscreenContainer);
			mFullscreenContainer = null;
			mCustomView = null;
			mCustomViewCallback.onCustomViewHidden();
		} catch (NullPointerException e) {
			// Toast.makeText(getApplicationContext(), "catch null exception",
			// Toast.LENGTH_SHORT).show();
			finish();
		} catch (Exception e) {
			// Toast.makeText(getApplicationContext(), "catch exception",
			// Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	private GestureDetector mGestureDetector;

	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			webView.zoomIn();
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}

	public static final int PAGE_ERR = 33;

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		handler.sendEmptyMessage(PAGE_ERR);
		// FrameLayout decor = (FrameLayout) getWindow().getDecorView();
		// if (mFullscreenContainer != null)
		// decor.removeView(mFullscreenContainer);
		//
		// new AlertDialog.Builder(WebPlayActivity.this)
		// .setIcon(R.drawable.icon_small).setTitle("电视粉温馨提示")
		// .setCancelable(false)
		// .setMessage("当前网页暂时无法播放，请尝试更换其他网页地址播放或稍后重试")
		// .setNeutralButton("知道了", new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// WebPlayActivity.this.finish();
		// }
		//
		// }).create().show();
	}

}
