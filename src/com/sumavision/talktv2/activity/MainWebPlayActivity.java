/*
 * Zirco Browser for Android
 * 
 * Copyright (C) 2010 - 2012 J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.sumavision.talktv2.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.components.CustomWebView;
import com.sumavision.talktv2.components.CustomWebViewClient;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.UpdateData;
import com.sumavision.talktv2.utils.ExeptionHandler;

/**
 * The application main activity.
 */
public class MainWebPlayActivity extends Activity implements OnTouchListener,
		OnClickListener
// , UncaughtExceptionHandler
{

	// public static MainWebPlayActivity INSTANCE = null;

	private static final int FLIP_PIXEL_THRESHOLD = 200;
	private static final int FLIP_TIME_THRESHOLD = 400;

	protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT);

	protected LayoutInflater mInflater = null;
	private ProgressBar mProgressBar;
	private CustomWebView mCurrentWebView;
	private List<CustomWebView> mWebViews;
	private Drawable mCircularProgress;
	private boolean mFindDialogVisible = false;

	// private ViewFlipper mViewFlipper;
	private GestureDetector mGestureDetector;
	private View mCustomView;
	private Bitmap mDefaultVideoPoster = null;
	private View mVideoProgressView = null;

	private FrameLayout mFullscreenContainer;
	private final int CLOSE_ME_DELAY = 1500;
	private WebChromeClient.CustomViewCallback mCustomViewCallback;

	// private enum SwitchTabsMethod {
	// BUTTONS, FLING, BOTH
	// }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= 11) {
			ExeptionHandler crashHandler = ExeptionHandler.getInstance();
			crashHandler.init(this);
		}

		// Thread.setDefaultUncaughtExceptionHandler(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setProgressBarVisibility(true);

		setContentView(R.layout.mainwebplay);
		getExtra();
		initViews();
		mCircularProgress = getResources().getDrawable(
				R.drawable.base_loading_small_icon);
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		buildComponents();

		// mViewFlipper.removeAllViews();
		addTab(false);
		init();
		setUA();
		mCurrentWebView.loadUrl(url);
		mCurrentWebView.setPlayUrl(videoPath);
		mCurrentWebView.setTitle(title);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (OtherCacheData.current().isDebugMode)
			Log.e("onKeyDown", "step - 4");

		// if (mCurrentWebView.isHardwareAccelerated())
		// mCurrentWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		processQuit();
		handler.sendEmptyMessageDelayed(DELAY_FINISH, 1500);
	}

	/**
	 * Handle url request from external apps.
	 * 
	 * @param intent
	 *            The intent.
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.getData() != null) {
			addTab(false);
		}

		setIntent(intent);

		super.onNewIntent(intent);
	}

	/**
	 * Restart the application.
	 */
	public void restartApplication() {
		// PendingIntent intent =
		// PendingIntent.getActivity(this.getBaseContext(),
		// 0, new Intent(getIntent()), getIntent().getFlags());
		// AlarmManager mgr = (AlarmManager)
		// getSystemService(Context.ALARM_SERVICE);
		// mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, intent);
		// System.exit(2);
	}

	/**
	 * Create main UI.
	 */
	private void buildComponents() {
		// mViewFlipper = (ViewFlipper) findViewById(R.id.ViewFlipper);
		mGestureDetector = new GestureDetector(this, new GestureListener());
		mWebViews = new ArrayList<CustomWebView>();
		mProgressBar = (ProgressBar) findViewById(R.id.WebViewProgress);
		mProgressBar.setMax(100);

	}

	/**
	 * Apply preferences to the current UI objects.
	 */
	public void applyPreferences() {

		for (CustomWebView view : mWebViews) {
			view.initializeOptions();
		}
	}

	// private void setStatusBarVisibility(boolean visible) {
	// int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
	// getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	// }

	/**
	 * Initialize a newly created WebView.
	 */
	private void initializeCurrentWebView() {

		mCurrentWebView.setWebViewClient(new CustomWebViewClient(
				MainWebPlayActivity.this, url));
		mCurrentWebView.setOnTouchListener(this);
		mCurrentWebView.setDownloadListener(new DownloadListener() {

			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				// doDownloadStart(url, userAgent, contentDisposition, mimetype,
				// contentLength);
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(url);
				intent.setData(content_url);
				startActivity(intent);
			}

		});

		final Activity activity = this;
		mCurrentWebView.setWebChromeClient(new WebChromeClient() {

			@SuppressWarnings("unused")
			// This is an undocumented method, it _is_ used, whatever Eclipse
			// may think :)
			// Used to show a file chooser dialog.
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				// Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				// i.addCategory(Intent.CATEGORY_OPENABLE);
				// i.setType("*/*");
			}

			@Override
			public Bitmap getDefaultVideoPoster() {
				if (mDefaultVideoPoster == null) {
					mDefaultVideoPoster = BitmapFactory.decodeResource(
							MainWebPlayActivity.this.getResources(),
							R.drawable.netlive_icon);
				}

				return mDefaultVideoPoster;
			}

			@Override
			public View getVideoLoadingProgressView() {
				if (mVideoProgressView == null) {
					LayoutInflater inflater = LayoutInflater
							.from(MainWebPlayActivity.this);
					mVideoProgressView = inflater.inflate(
							R.layout.video_loading_progress, null);
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

			// @Override
			// public void onShowCustomView(View view, CustomViewCallback
			// callback) {
			// super.onShowCustomView(view, callback);
			//
			// if (view instanceof FrameLayout) {
			// mCustomViewContainer = (FrameLayout) view;
			// mCustomViewCallback = callback;
			//
			// mContentView = (LinearLayout) findViewById(R.id.MainContainer);
			//
			// if (mCustomViewContainer.getFocusedChild() instanceof VideoView)
			// {
			// mCustomVideoView = (VideoView) mCustomViewContainer
			// .getFocusedChild();
			// // frame.removeView(video);
			// mContentView.setVisibility(View.GONE);
			// mCustomViewContainer.setVisibility(View.VISIBLE);
			//
			// setContentView(mCustomViewContainer);
			// // mCustomViewContainer.bringToFront();
			//
			// mCustomVideoView
			// .setOnCompletionListener(new OnCompletionListener() {
			// @Override
			// public void onCompletion(MediaPlayer mp) {
			// mp.stop();
			// onHideCustomView();
			// }
			// });
			//
			// mCustomVideoView
			// .setOnErrorListener(new OnErrorListener() {
			// @Override
			// public boolean onError(MediaPlayer mp,
			// int what, int extra) {
			// onHideCustomView();
			// return true;
			// }
			// });
			//
			// mCustomVideoView.start();
			// }
			//
			// }
			// }
			//
			// @Override
			// public void onHideCustomView() {
			// super.onHideCustomView();
			//
			// if (mCustomVideoView == null) {
			// return;
			// }
			//
			// mCustomVideoView.setVisibility(View.GONE);
			// mCustomViewContainer.removeView(mCustomVideoView);
			// mCustomVideoView = null;
			//
			// mCustomViewContainer.setVisibility(View.GONE);
			// mCustomViewCallback.onCustomViewHidden();
			//
			// mContentView.setVisibility(View.VISIBLE);
			// setContentView(mContentView);
			// }

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				try {
					((CustomWebView) view).setProgress(newProgress);
					mProgressBar.setProgress(mCurrentWebView.getProgress());
					if (newProgress == 100)
						handler.sendEmptyMessage(LOAD_OVER);
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onReceivedIcon(WebView view, Bitmap icon) {
				// new Thread(new FaviconUpdaterRunnable(MainActivity.this, view
				// .getUrl(), view.getOriginalUrl(), icon)).start();
				// updateFavIcon();

				super.onReceivedIcon(view, icon);
			}

			// @Override
			// public boolean onCreateWindow(WebView view, final boolean dialog,
			// final boolean userGesture, final Message resultMsg) {
			//
			// WebView.WebViewTransport transport = (WebView.WebViewTransport)
			// resultMsg.obj;
			//
			// // addTab(false, mViewFlipper.getDisplayedChild());
			//
			// transport.setWebView(mCurrentWebView);
			// resultMsg.sendToTarget();
			//
			// return true;
			// }

			@Override
			public void onReceivedTitle(WebView view, String title) {
				setTitle(String.format(
						getResources().getString(R.string.ApplicationNameUrl),
						title));

				// startHistoryUpdaterRunnable(title, mCurrentWebView.getUrl(),
				// mCurrentWebView.getOriginalUrl());

				super.onReceivedTitle(view, title);
			}

			// @Override
			// public boolean onJsAlert(WebView view, String url, String
			// message,
			// final JsResult result) {
			// new AlertDialog.Builder(activity)
			// .setTitle(R.string.Commons_JavaScriptDialog)
			// .setMessage(message)
			// .setPositiveButton(android.R.string.ok,
			// new AlertDialog.OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog,
			// int which) {
			// result.confirm();
			// }
			// }).setCancelable(false).create().show();
			//
			// return true;
			// }
			//
			// @Override
			// public boolean onJsConfirm(WebView view, String url,
			// String message, final JsResult result) {
			// new AlertDialog.Builder(MainWebPlayActivity.this)
			// .setTitle(R.string.Commons_JavaScriptDialog)
			// .setMessage(message)
			// .setPositiveButton(android.R.string.ok,
			// new DialogInterface.OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog,
			// int which) {
			// result.confirm();
			// }
			// })
			// .setNegativeButton(android.R.string.cancel,
			// new DialogInterface.OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog,
			// int which) {
			// result.cancel();
			// }
			// }).create().show();
			//
			// return true;
			// }
			//
			// @Override
			// public boolean onJsPrompt(WebView view, String url, String
			// message,
			// String defaultValue, final JsPromptResult result) {
			//
			// final LayoutInflater factory = LayoutInflater
			// .from(MainWebPlayActivity.this);
			// final View v = factory.inflate(
			// R.layout.javascript_prompt_dialog, null);
			// ((TextView) v.findViewById(R.id.JavaScriptPromptMessage))
			// .setText(message);
			// ((EditText) v.findViewById(R.id.JavaScriptPromptInput))
			// .setText(defaultValue);
			//
			// new AlertDialog.Builder(MainWebPlayActivity.this)
			// .setTitle(R.string.Commons_JavaScriptDialog)
			// .setView(v)
			// .setPositiveButton(android.R.string.ok,
			// new DialogInterface.OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			// String value = ((EditText) v
			// .findViewById(R.id.JavaScriptPromptInput))
			// .getText().toString();
			// result.confirm(value);
			// }
			// })
			// .setNegativeButton(android.R.string.cancel,
			// new DialogInterface.OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			// result.cancel();
			// }
			// })
			// .setOnCancelListener(
			// new DialogInterface.OnCancelListener() {
			// @Override
			// public void onCancel(DialogInterface dialog) {
			// result.cancel();
			// }
			// }).show();
			//
			// return true;
			//
			// }

		});
	}

	/**
	 * Select Text in the webview and automatically sends the selected text to
	 * the clipboard.
	 */
	public void swithToSelectAndCopyTextMode() {
		try {
			KeyEvent shiftPressEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
			shiftPressEvent.dispatch(mCurrentWebView);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Add a new tab.
	 * 
	 * @param navigateToHome
	 *            If True, will load the user home page.
	 */
	private void addTab(boolean navigateToHome) {
		addTab(navigateToHome, -1);
	}

	/**
	 * Add a new tab.
	 * 
	 * @param navigateToHome
	 *            If True, will load the user home page.
	 * @param parentIndex
	 *            The index of the new tab.
	 */
	private void addTab(boolean navigateToHome, int parentIndex) {
		if (mFindDialogVisible) {
			closeFindDialog();
		}

		// RelativeLayout view = (RelativeLayout) mInflater.inflate(
		// R.layout.webview, mViewFlipper, false);

		mCurrentWebView = (CustomWebView) // view.
		findViewById(R.id.webview);

		initializeCurrentWebView();

		// synchronized (mViewFlipper) {
		// if (parentIndex != -1) {
		// mWebViews.add(parentIndex + 1, mCurrentWebView);
		// mViewFlipper.addView(view, parentIndex + 1);
		// } else {
		// mWebViews.add(mCurrentWebView);
		// mViewFlipper.addView(view);
		// }
		// mViewFlipper.setDisplayedChild(mViewFlipper.indexOfChild(view));
		// }

		updateUI();
		updatePreviousNextTabViewsVisibility();
	}

	private void closeFindDialog() {
		hideKeyboardFromFindDialog();
		mCurrentWebView.doNotifyFindDialogDismissed();
		setFindBarVisibility(false);
	}

	private void setFindBarVisibility(boolean visible) {
		if (visible) {
			mFindDialogVisible = true;
		} else {
			mFindDialogVisible = false;
		}
	}

	private void hideKeyboardFromFindDialog() {
		// InputMethodManager imm = (InputMethodManager)
		// getSystemService(Context.INPUT_METHOD_SERVICE);
		// imm.hideSoftInputFromWindow(mFindText.getWindowToken(), 0);
	}

	/**
	 * Hide the keyboard.
	 * 
	 * @param delayedHideToolbars
	 *            If True, will start a runnable to delay tool bars hiding. If
	 *            False, tool bars are hidden immediatly.
	 */
	private void hideKeyboard(boolean delayedHideToolbars) {

	}

	/**
	 * Start a runnable to update history.
	 * 
	 * @param title
	 *            The page title.
	 * @param url
	 *            The page url.
	 */
	private void startHistoryUpdaterRunnable(String title, String url,
			String originalUrl) {
		// if ((url != null) && (url.length() > 0)) {
		// new Thread(new HistoryUpdater(this, title, url, originalUrl))
		// .start();
		// }
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (OtherCacheData.current().isDebugMode)
				Log.e("onKeyDown", "step - 3");
			processQuit();
			handler.sendEmptyMessageDelayed(DELAY_FINISH, CLOSE_ME_DELAY);

			return true;
		default:
			return super.onKeyLongPress(keyCode, event);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		switch (keyCode) {
		// TODO: guop
		case KeyEvent.KEYCODE_MENU:
			return true;
		case KeyEvent.KEYCODE_SEARCH:
			return true;
		case KeyEvent.KEYCODE_BACK:
			return true;
		default:
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mCurrentWebView.canGoBack()) {
				mCurrentWebView.goBack();
				return true;
			} else {
				if (OtherCacheData.current().isDebugMode)
					Log.e("onKeyDown", "step - 1");
				processQuit();
				handler.sendEmptyMessageDelayed(DELAY_FINISH, CLOSE_ME_DELAY);
				return true;
			}
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * Set the application title to default.
	 */
	private void clearTitle() {
		this.setTitle(getResources().getString(R.string.ApplicationName));
	}

	/**
	 * Update the application title.
	 */
	private void updateTitle() {
		String value = mCurrentWebView.getTitle();

		if ((value != null) && (value.length() > 0)) {
			this.setTitle(String.format(
					getResources().getString(R.string.ApplicationNameUrl),
					value));
		} else {
			clearTitle();
		}
	}

	/**
	 * Update the "Go" button image.
	 */
	private void updateGoButton() {
	}

	/**
	 * Update the fav icon display.
	 */
	private void updateFavIcon() {
	}

	/**
	 * Update the UI: Url edit text, previous/next button state,...
	 */
	private void updateUI() {
		try {
			mProgressBar.setProgress(mCurrentWebView.getProgress());
			updateGoButton();
			updateTitle();
			updateFavIcon();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private boolean isSwitchTabsByFlingEnabled() {
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		if (resultCode == RESULT_OK) {
			finish();
		} else
			super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	protected void onPause() {
		mCurrentWebView.doOnPause();
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onPause();
		mCurrentWebView.doOnPause();
		processQuit();
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!UpdateData.current().isNeedCloseWebPlay)
			mCurrentWebView.doOnResume();
		else {

			mCurrentWebView.doOnPause();
			finish();
			UpdateData.current().isNeedCloseWebPlay = false;
		}
	}

	/**
	 * Show a toast alert on tab switch.
	 */
	private void showToastOnTabSwitch() {
	}

	private void updatePreviousNextTabViewsVisibility() {
	}

	/**
	 * Show the previous tab, if any.
	 */
	private void showPreviousTab(boolean resetToolbarsRunnable) {

		// if (mViewFlipper.getChildCount() > 1) {
		//
		// if (mFindDialogVisible) {
		// closeFindDialog();
		// }
		//
		// mCurrentWebView.doOnPause();
		//
		// mViewFlipper.showPrevious();
		//
		// mCurrentWebView = mWebViews.get(mViewFlipper.getDisplayedChild());
		//
		// mCurrentWebView.doOnResume();
		//
		// showToastOnTabSwitch();
		//
		// updatePreviousNextTabViewsVisibility();
		//
		// updateUI();
		// }
	}

	private void showCustomView(View view,
			WebChromeClient.CustomViewCallback callback) {
		// if a view already exists then immediately terminate the new one
		if (mCustomView != null) {
			callback.onCustomViewHidden();
			return;
		}

		MainWebPlayActivity.this.getWindow().getDecorView();

		FrameLayout decor = (FrameLayout) getWindow().getDecorView();
		mFullscreenContainer = new FullscreenHolder(MainWebPlayActivity.this);
		mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
		decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
		mCustomView = view;
		// setStatusBarVisibility(false);
		mCustomViewCallback = callback;
	}

	private void hideCustomView() {
		// Toast.makeText(getApplicationContext(), "hideView",
		// Toast.LENGTH_SHORT)
		// .show();
		if (mCustomView == null)
			return;
		try {
			// setStatusBarVisibility(true);
			FrameLayout decor = (FrameLayout) getWindow().getDecorView();
			decor.removeView(mFullscreenContainer);
			mFullscreenContainer = null;
			mCustomView = null;
			mCustomViewCallback.onCustomViewHidden();
		} catch (NullPointerException e) {
			Toast.makeText(getApplicationContext(), "catch null exception",
					Toast.LENGTH_SHORT).show();
			finish();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "catch exception",
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	/**
	 * Show the next tab, if any.
	 */
	private void showNextTab(boolean resetToolbarsRunnable) {

		// if (mViewFlipper.getChildCount() > 1) {
		//
		// if (mFindDialogVisible) {
		// closeFindDialog();
		// }
		//
		// mCurrentWebView.doOnPause();
		//
		// mViewFlipper.showNext();
		//
		// mCurrentWebView = mWebViews.get(mViewFlipper.getDisplayedChild());
		//
		// mCurrentWebView.doOnResume();
		//
		// showToastOnTabSwitch();
		//
		// updatePreviousNextTabViewsVisibility();
		//
		// updateUI();
		// }
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		hideKeyboard(false);

		return mGestureDetector.onTouchEvent(event);
	}

	public void onPageFinished(String url) {
		updateUI();

	}

	public void onPageStarted(String url) {
		if (mFindDialogVisible) {
			closeFindDialog();
		}

		updateGoButton();
	}

	public void onUrlLoading(String url) {
		// setToolbarsVisibility(true);
		mCurrentWebView.loadUrl(url);
		progressBar.setVisibility(View.VISIBLE);
		errLayout.setVisibility(View.VISIBLE);
	}

	public void onMailTo(String url) {
		Intent sendMail = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(sendMail);
	}

	public void onExternalApplicationUrl(String url) {
		try {

			if (url.startsWith("http://") && url.contains(".mp4"))
				url = "tvfanplayurl" + url.substring(4);

			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(i);

		} catch (Exception e) {

			// Notify user that the vnd url cannot be viewed.
			new AlertDialog.Builder(this)
					.setTitle(R.string.Main_VndErrorTitle)
					.setMessage(
							String.format(
									getString(R.string.Main_VndErrorMessage),
									url))
					.setPositiveButton(android.R.string.ok,
							new AlertDialog.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).setCancelable(true).create().show();
		}
	}

	public void setHttpAuthUsernamePassword(String host, String realm,
			String username, String password) {
		mCurrentWebView.setHttpAuthUsernamePassword(host, realm, username,
				password);
	}

	/**
	 * Gesture listener implementation.
	 */
	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			mCurrentWebView.zoomIn();
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (isSwitchTabsByFlingEnabled()) {
				if (e2.getEventTime() - e1.getEventTime() <= FLIP_TIME_THRESHOLD) {
					if (e2.getX() > (e1.getX() + FLIP_PIXEL_THRESHOLD)) {

						showPreviousTab(false);
						return false;
					}

					// going forwards: pushing stuff to the left
					if (e2.getX() < (e1.getX() - FLIP_PIXEL_THRESHOLD)) {

						showNextTab(false);
						return false;
					}
				}
			}

			return super.onFling(e1, e2, velocityX, velocityY);
		}

	}

	static class FullscreenHolder extends FrameLayout {

		public FullscreenHolder(Context ctx) {
			super(ctx);
			setBackgroundColor(ctx.getResources().getColor(
					android.R.color.black));
		}

		@Override
		public boolean onTouchEvent(MotionEvent evt) {
			return true;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			if (OtherCacheData.current().isDebugMode)
				Log.e("onKeyDown", "step - 2");
			processQuit();
			handler.sendEmptyMessageDelayed(DELAY_FINISH, 1500);
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
	private static final int BACKING = 3;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case BACKING:
				progressBar.setVisibility(View.GONE);
				errLayout.setVisibility(View.VISIBLE);
				errText.setText("正在返回，请稍候...");
				errText.setVisibility(View.VISIBLE);
				errText.setTextSize(18);
				errText.setTextColor(Color.BLACK);
				// handler.sendEmptyMessageDelayed(BACKING, 200);
				break;
			case PROCESS_CLOSE:
				mCurrentWebView.destroy();
				mCurrentWebView = null;
				System.gc();
				break;
			case MSG_SHOW:
				progressBar.setVisibility(View.VISIBLE);
				errLayout.setVisibility(View.VISIBLE);
				break;
			case MSG_DISMISS:
				finish();
				break;
			case LOAD_OVER:
				if (!Build.MODEL.equals("MI 2")) {
					progressBar.setVisibility(View.GONE);
					errLayout.setVisibility(View.GONE);
				} else if (Build.MODEL.equals("MI 2") && url.contains("sohu")) {
					progressBar.setVisibility(View.GONE);
				} else {
					progressBar.setVisibility(View.GONE);
					errLayout.setVisibility(View.GONE);
				}

				// progressBar.setVisibility(View.GONE);
				// errLayout.setVisibility(View.GONE);

				if (videoPath != null && !videoPath.equals("")) {
					findViewById(R.id.play).setOnClickListener(
							MainWebPlayActivity.this);
					findViewById(R.id.play).setVisibility(View.VISIBLE);
				}
				break;
			case LOAD_ERROR:
				showErrorLayout();
				break;
			case PAGE_ERR:
				new AlertDialog.Builder(MainWebPlayActivity.this)
						.setIcon(R.drawable.icon_small)
						.setTitle("电视粉温馨提示")
						.setCancelable(false)
						.setMessage("当前网页暂时无法播放，请尝试更换其他网页地址播放或稍后重试")
						.setNeutralButton("知道了",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// CloseMeTask tt = new CloseMeTask();
										// tt.execute();
										MainWebPlayActivity.this.finish();
										// progressBar.setVisibility(View.VISIBLE);
										// errLayout.setVisibility(View.VISIBLE);
										// handler.sendEmptyMessageDelayed(
										// DELAY_FINISH, 5000);
									}

								}).create().show();
				break;
			case DELAY_FINISH:
				// mCurrentWebView.clearView();
				// mCurrentWebView.stopLoading();
				// mCurrentWebView.clearFormData();
				// mCurrentWebView.clearHistory();
				// mCurrentWebView.clearCache(true);
				// mCurrentWebView.destroyDrawingCache();
				// mCurrentWebView.freeMemory();
				// mCurrentWebView = null;
				// System.gc();
				finish();
			default:
				break;
			}
		};
	};
	private TextView errText;
	private RelativeLayout errLayout;
	private ProgressBar progressBar;

	private void showErrorLayout() {
		progressBar.setVisibility(View.GONE);
		errText.setVisibility(View.VISIBLE);
	}

	private void loadData() {
		if (url != null) {
			mCurrentWebView.loadUrl(url);
			progressBar.setVisibility(View.VISIBLE);
			errLayout.setVisibility(View.VISIBLE);
		} else {
			showErrorLayout();
		}
	}

	private String url;
	private String title;
	private int isLive;
	private String videoPath;

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

		if (OtherCacheData.current().isDebugMode)
			Log.e("MainWebPlay", url);
	}

	private void initViews() {
		findViewById(R.id.back).setOnClickListener(this);

		if (title != null) {
			((TextView) findViewById(R.id.title)).setText(title);
		}
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		errText = (TextView) findViewById(R.id.err_text);
		errLayout = (RelativeLayout) findViewById(R.id.errLayout);
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

	private void init() {
		if (Integer.parseInt(VERSION.SDK) >= 14
		// && !Build.MODEL.contains("N7100")
		// && !Build.MODEL.contains("N7102")
		// && !Build.MODEL.contains("N7108")
		// && !Build.MODEL.contains("N719")
				|| Build.MODEL.contains("I9") || Build.MODEL.contains("XT8")) {

			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
			// mCurrentWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}

	}

	public static final int PAGE_ERR = 33;
	public static final int MSG_SHOW = 34;
	public static final int MSG_DISMISS = 35;
	public static final int PROCESS_CLOSE = 36;

	public static final int DELAY_FINISH = 37;

	// @Override
	// public void uncaughtException(Thread thread, Throwable ex) {
	// handler.sendEmptyMessage(PAGE_ERR);
	// }

	class CloseMeTask extends AsyncTask<Integer, Integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			handler.sendEmptyMessage(MSG_SHOW);
			Log.e("AsyncTask", "step - 1");
		}

		@Override
		protected String doInBackground(Integer... params) {
			Log.e("AsyncTask", "step - 2");
			handler.sendEmptyMessage(PROCESS_CLOSE);
			return "执行完毕";
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
		}

		@Override
		protected void onPostExecute(String result) {
			Log.e("AsyncTask", "step - 3");
			super.onPostExecute(result);
			handler.sendEmptyMessage(MSG_DISMISS);
		}

	}

	private void setUA() {
		// 优酷
		if (url.contains("youku")) {
			// mCurrentWebView.getSettings().setSupportZoom(true);
		}
		// 搜狐
		else if (url.contains("sohu")) {
			if (Build.MODEL.equals("N7100") || Build.MODEL.contains("N7102")
					|| Build.MODEL.contains("N7108")
					|| Build.MODEL.contains("N719")) {

			} else {
				// 可以实现按钮跳转
				// mCurrentWebView
				// .getSettings()
				// .setUserAgentString(
				// "Mozilla/5.0 (Linux; U; Android 2.1.0; zh-cn; GT-I9000 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");

				// 有广告，点击自动退出网页播放
				// mCurrentWebView
				// .getSettings()
				// .setUserAgentString(
				// "Mozilla/5.0 (iPhone; CPU OS 4_3 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko ) Version/3.1 Mobile Safari/534.48.3");

				mCurrentWebView.getSettings().setSupportZoom(true);
			}
			// mCurrentWebView
			// .getSettings()
			// .setUserAgentString(
			// "Mozilla/5.0 (iPhone; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko ) Version/5.1 Mobile/9B176 Safari/7534.48.3");
		}
		// CNTV
		else if (url.contains("cntv")) {
			mCurrentWebView
					.getSettings()
					.setUserAgentString(
							"Mozilla/5.0 (iPhone; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko ) Version/5.1 Mobile/9B176 Safari/7534.48.3");
			mCurrentWebView.getSettings().setSupportZoom(true);
		}
		// 乐视
		else if (url.contains("letv")) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			// mCurrentWebView
			// .getSettings()
			// .setUserAgentString(
			// "Mozilla/5.0 (iPhone; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko ) Version/5.1 Mobile/9B176 Safari/7534.48.3");
			mCurrentWebView.getSettings().setSupportZoom(true);
		}
		// 其他网页
		else {
			mCurrentWebView
					.getSettings()
					.setUserAgentString(
							"Mozilla/5.0 (Linux; U; Android 2.1.0; zh-cn; GT-I9000 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
			mCurrentWebView.getSettings().setSupportZoom(true);
		}

	}

	private void processQuit() {
		mCurrentWebView.loadUrl("file:///android_asset/white_for_web.jpg");
		handler.sendEmptyMessageDelayed(BACKING, 100);
	}

}
