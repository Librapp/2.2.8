package com.sumavision.talktv2.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.sumavision.talktv2.R;

/**
 * 
 * @author guopeng
 * @description 网页播放等待空界面
 * 
 */
public class MainWebPlayBlockWaitActivity extends Activity {

	private final int DELAY = 500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainwebplayblock);

		findViewById(R.id.mainwebplayblock_all).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						handler.sendEmptyMessageDelayed(CLOSEME, DELAY);
					}
				});
		
		if(Build.MODEL.equals("MI 2")) {
			findViewById(R.id.progressBar).setVisibility(View.GONE);
		}
	}

	private final int CLOSEME = 1;
	private Handler handler = new Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case CLOSEME:
				finish();
				break;
			default:
				break;
			}
			return false;
		}
	});

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			handler.sendEmptyMessageDelayed(CLOSEME, DELAY);
			return true;
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeMessages(CLOSEME);
	}

	@Override
	protected void onResume() {
		super.onResume();
		handler.sendEmptyMessageDelayed(CLOSEME, DELAY);
	}
}
