package com.sumavision.talktv2.activity;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.ScreenShotData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.ScreenShotParser;
import com.sumavision.talktv2.net.ScreenShotRequest;
import com.sumavision.talktv2.task.ScreenShotTask;
import com.sumavision.talktv2.user.User;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.BitmapUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-10-24
 * @description 电视截屏界面
 * @changLog 修改为2.2版本 by 李梦思 2013-1-5
 */
public class TVScreenShotActivity extends Activity implements OnClickListener,
		NetConnectionListener {

	private ImageView pic;
	private FrameLayout all;

	private Drawable drawable;

	private ExecutorService executorService = Executors.newFixedThreadPool(1);

	// 通信框
	private RelativeLayout connectBg;

	private ScreenShotTask screenShotTask;

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tvscreenshot);
		initViews();
		ScreenShotData.direction = 0;
		getScreenData();
	}

	private void setPicPath() {
		ScreenShotData.picPath = ScreenShotData.pic[ScreenShotData.current];
		loadNetImage(ScreenShotData.picPath, R.id.tss_pic);
		Log.e("TvScreenShotActivity", ScreenShotData.picPath);
	}

	private void initViews() {
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
		connectBg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		all = (FrameLayout) findViewById(R.id.tss_all);
		pic = (ImageView) findViewById(R.id.tss_pic);
		findViewById(R.id.tss_minusone).setOnClickListener(this);
		findViewById(R.id.tss_plusone).setOnClickListener(this);
		findViewById(R.id.tss_back).setOnClickListener(this);
		findViewById(R.id.tss_replay).setOnClickListener(this);
		findViewById(R.id.tss_next).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tss_minusone:
			if (0 == ScreenShotData.current) {
				ScreenShotData.direction = -1;
				getScreenData();
			} else {
				ScreenShotData.current--;
				setPicPath();
			}
			break;
		case R.id.tss_plusone:
			if (ScreenShotData.picCount - 2 < ScreenShotData.current) {
				ScreenShotData.direction = 1;
				getScreenData();
			} else {
				ScreenShotData.current++;
				setPicPath();
			}
			break;
		case R.id.tss_back:
			finish();
			break;
		case R.id.tss_replay:
			ScreenShotData.direction = 0;
			getScreenData();
			break;
		case R.id.tss_next:
			if (saveImage()) {
				Intent i = new Intent(this, SendCommentActivity.class);
				i.putExtra("fromWhere", SendCommentActivity.SCREENSHOT);
				startActivity(i);
				finish();
			} else {
				Toast.makeText(TVScreenShotActivity.this, "请等待图片加载完成",
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	private void getScreenData() {
		if (screenShotTask == null) {
			screenShotTask = new ScreenShotTask(this);
			screenShotTask.execute(this, new ScreenShotRequest(),
					new ScreenShotParser());
		}
	}

	private void loadNetImage(final String url, final int id) {
		final Handler handler = new Handler();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					drawable = Drawable.createFromStream(
							new URL(url).openStream(), "image.jpg");

					handler.post(new Runnable() {

						@Override
						public void run() {
							((ImageView) TVScreenShotActivity.this
									.findViewById(id))
									.setImageDrawable(drawable);
						}
					});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	private boolean saveImage() {
		String fileDir = JSONMessageType.USER_PIC_SDCARD_FOLDER;
		String filePath = fileDir + "/" + ScreenShotData.FILENAME;
		File dir = new File(fileDir);
		if (!dir.exists()) {
			dir.mkdir();
		}
		boolean sdExists = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (sdExists) {
			try {
				BitmapUtils.saveDrawableForTvShot(drawable,
						JSONMessageType.USER_PIC_SDCARD_FOLDER + "/",
						ScreenShotData.FILENAME);
				UserNow.current().picPath = filePath;
				if (OtherCacheData.current().isDebugMode) {
					Log.e("电视截图保存图片", "成功");
				}
				// if (InfomationHelper.getFinalScaleBitmapBigPic(this,
				// UserNow.current().picPath) != null)
				if (BitmapUtils.getLocalBitmap(UserNow.current().picPath) != null)
					return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);

		if (User.current().needChangeBackground) {
			if (User.current().sdcardThemeDrawable != null) {
				all.setBackgroundDrawable(User.current().sdcardThemeDrawable);
			} else {
				if (OtherCacheData.current().isDebugMode) {
					Log.e("UserCenter", User.current().nowBg + "");
				}
				all.setBackgroundResource(User.current().nowBg);
			}
		}
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (executorService != null) {
			executorService.shutdownNow();
			executorService = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (connectBg.isShown()) {
				hidepb();
				return true;
			}
			return super.onKeyDown(keyCode, event);
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onNetBegin(String method) {
		showpb();
	}

	@Override
	public void onNetEnd(String msg, String method) {
		hidepb();
		if ((null != msg) && (msg.equals(""))) {
			switch (ScreenShotData.direction) {
			case -1:

				break;
			case 0:
				ScreenShotData.current = (ScreenShotData.picCount + 1) / 2;
				break;
			case 1:

				break;
			default:
				break;
			}
			setPicPath();
			loadNetImage(ScreenShotData.picPath, pic.getId());
		} else {
			switch (ScreenShotData.direction) {
			case -1:

				break;
			case 0:

				break;
			case 1:

				break;
			default:
				break;
			}
			Toast.makeText(getApplicationContext(), UserNow.current().errMsg,
					Toast.LENGTH_SHORT).show();
		}
		screenShotTask = null;
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		// TODO Auto-generated method stub

	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
