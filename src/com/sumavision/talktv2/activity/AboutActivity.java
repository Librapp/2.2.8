package com.sumavision.talktv2.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.utils.AppUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @author 郭鹏
 * @description 程序关于界面
 * 
 */
public class AboutActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		findViewById(R.id.back).setOnClickListener(this);
		String versionName = "Ver" + AppUtil.getAppVersionId(this);
		((TextView) findViewById(R.id.version_text)).setText(versionName);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		default:
			break;
		}

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
