package com.sumavision.talktv2.activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.sumavision.talktv2.R;
import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @author jianghao 软件推荐页面
 * 
 */
public class WebViewActivity extends Activity {
	String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getIntent().getStringExtra("url");
		setContentView(R.layout.app_webview);
		WebView webView = (WebView) findViewById(R.id.webview);
		webView.loadUrl(url);
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
