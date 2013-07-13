/*
 * Zirco Browser for Android
 * 
 * Copyright (C) 2010 - 2012 J. Devauchelle and contributors.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 3 as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */

package com.sumavision.talktv2.components;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MainWebPlayActivity;
import com.sumavision.talktv2.activity.NewLivePlayerActivity;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.UpdateData;

/**
 * Convenient extension of WebViewClient.
 */
public class CustomWebViewClient extends WebViewClient {
	private String orignalUrl = "";
	private MainWebPlayActivity mMainWebPlayActivity;
	// 初次加载网页
	private boolean isFirst = true;

	public CustomWebViewClient(MainWebPlayActivity mainActivity) {
		super();
		mMainWebPlayActivity = mainActivity;
		isFirst = true;
	}

	// 传入原始url
	public CustomWebViewClient(MainWebPlayActivity mainActivity, String url) {
		super();
		mMainWebPlayActivity = mainActivity;
		orignalUrl = url;
		isFirst = true;
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		((CustomWebView) view).notifyPageFinished();
		mMainWebPlayActivity.onPageFinished(url);

		super.onPageFinished(view, url);
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {

		// Some magic here: when performing WebView.loadDataWithBaseURL, the url
		// is "file:///android_asset/startpage,
		// whereas when the doing a "previous" or "next", the url is
		// "about:start", and we need to perform the
		// loadDataWithBaseURL here, otherwise it won't load.
		// if (url.equals(Constants.URL_ABOUT_START)) {
		// view.loadDataWithBaseURL("file:///android_asset/startpage/",
		// ApplicationUtils.getStartPage(view.getContext()),
		// "text/html", "UTF-8", "about:start");
		// }

		((CustomWebView) view).notifyPageStarted();
		mMainWebPlayActivity.onPageStarted(url);

		super.onPageStarted(view, url, favicon);
	}

	@Override
	public void onReceivedSslError(WebView view, final SslErrorHandler handler,
			SslError error) {
		if (OtherCacheData.current().isDebugMode)
			Log.e("CustomWebView-onReceivedSslError", error + "");

		StringBuilder sb = new StringBuilder();

		sb.append(view.getResources().getString(
				R.string.Commons_SslWarningsHeader));
		sb.append("\n\n");

		if (error.hasError(SslError.SSL_UNTRUSTED)) {
			sb.append(" - ");
			sb.append(view.getResources().getString(
					R.string.Commons_SslUntrusted));
			sb.append("\n");
		}

		if (error.hasError(SslError.SSL_IDMISMATCH)) {
			sb.append(" - ");
			sb.append(view.getResources().getString(
					R.string.Commons_SslIDMismatch));
			sb.append("\n");
		}

		if (error.hasError(SslError.SSL_EXPIRED)) {
			sb.append(" - ");
			sb.append(view.getResources()
					.getString(R.string.Commons_SslExpired));
			sb.append("\n");
		}

		if (error.hasError(SslError.SSL_NOTYETVALID)) {
			sb.append(" - ");
			sb.append(view.getResources().getString(
					R.string.Commons_SslNotYetValid));
			sb.append("\n");
		}

		// ApplicationUtils.showContinueCancelDialog(view.getContext(),
		// android.R.drawable.ic_dialog_info, view.getResources()
		// .getString(R.string.Commons_SslWarning), sb.toString(),
		// new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// dialog.dismiss();
		// handler.proceed();
		// }
		//
		// }, new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// dialog.dismiss();
		// handler.cancel();
		// }
		// });
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if (OtherCacheData.current().isDebugMode)
			Log.e("CustomWebView-shouldOverrideUrlLoading", url + "\n"
					+ orignalUrl);

		// 解决部分手机无法正确退出浏览器
		if (!orignalUrl.equals("") && orignalUrl.equals(url)
		// 若当前地址包含原始地址去掉“http://”部分后的地址，也认为当前地址与原始地址相同
				|| url.contains(orignalUrl.substring(7)) && !isFirst) {

			mMainWebPlayActivity.finish();
			return true;
		} else {
			if (isFirst)
				isFirst = !isFirst;
			if (url.startsWith("http://") && url.contains(".mp4")) {
				url = "tvfanplayurl" + url.substring(4);

				Intent i = new Intent(mMainWebPlayActivity,
						NewLivePlayerActivity.class);
				i.setData(Uri.parse(url));
				mMainWebPlayActivity.startActivityForResult(i, 1);
				UpdateData.current().isNeedCloseWebPlay = true;
				return true;
			} else {

				try {

					if (isExternalApplicationUrl(url)) {
						mMainWebPlayActivity.onExternalApplicationUrl(url);
						return true;
					}

					// else if (view.getHitTestResult().getType() ==
					// HitTestResult.EMAIL_TYPE) {
					// mMainWebPlayActivity.onMailTo(url);
					// return true;
					//
					// }

					else {
						((CustomWebView) view).resetLoadedUrl();
						mMainWebPlayActivity.onUrlLoading(url);
						return false;
					}

				} catch (NullPointerException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
	}

	@Override
	public void onReceivedHttpAuthRequest(WebView view,
			final HttpAuthHandler handler, final String host, final String realm) {

		if (OtherCacheData.current().isDebugMode)
			Log.e("CustomWebView-onReceivedHttpAuthRequest", host + realm + "");

		String username = null;
		String password = null;

		boolean reuseHttpAuthUsernamePassword = handler
				.useHttpAuthUsernamePassword();

		if (reuseHttpAuthUsernamePassword && view != null) {
			String[] credentials = view
					.getHttpAuthUsernamePassword(host, realm);
			if (credentials != null && credentials.length == 2) {
				username = credentials[0];
				password = credentials[1];
			}
		}

		if (username != null && password != null) {
			handler.proceed(username, password);
		} else {
			LayoutInflater factory = LayoutInflater.from(mMainWebPlayActivity);
			final View v = factory.inflate(R.layout.http_authentication_dialog,
					null);

			if (username != null) {
				((EditText) v.findViewById(R.id.username_edit))
						.setText(username);
			}
			if (password != null) {
				((EditText) v.findViewById(R.id.password_edit))
						.setText(password);
			}

			AlertDialog dialog = new AlertDialog.Builder(mMainWebPlayActivity)
					.setTitle(
							String.format(
									mMainWebPlayActivity
											.getString(R.string.HttpAuthenticationDialog_DialogTitle),
									host, realm))
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setView(v)
					.setPositiveButton(R.string.Commons_Proceed,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String nm = ((EditText) v
											.findViewById(R.id.username_edit))
											.getText().toString();
									String pw = ((EditText) v
											.findViewById(R.id.password_edit))
											.getText().toString();
									mMainWebPlayActivity
											.setHttpAuthUsernamePassword(host,
													realm, nm, pw);
									handler.proceed(nm, pw);
								}
							})
					.setNegativeButton(R.string.Commons_Cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									handler.cancel();
								}
							})
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									handler.cancel();
								}
							}).create();

			dialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			dialog.show();

			v.findViewById(R.id.username_edit).requestFocus();
		}
	}

	private boolean isExternalApplicationUrl(String url) {
		return url.startsWith("vnd.") || url.startsWith("rtsp://")
				|| url.startsWith("itms://") || url.startsWith("itpc://");
	}

	@Override
	public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
		if (OtherCacheData.current().isDebugMode)
			Log.e("CustomWebView-shouldOverrideKeyEvent", event.getKeyCode()
					+ "");

		return super.shouldOverrideKeyEvent(view, event);
	}

	@Override
	public void onLoadResource(WebView view, String url) {
		// if (OtherCacheData.current().isDebugMode)
		// Log.e("CustomWebView-onLoadResource", url + "");
		super.onLoadResource(view, url);
	}
}
