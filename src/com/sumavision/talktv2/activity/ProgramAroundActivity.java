package com.sumavision.talktv2.activity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.AsyncImageLoader.ImageCallback;
import com.sumavision.talktv2.data.ProgramAroundData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.ProgramAroundParser;
import com.sumavision.talktv2.net.ProgramAroundRequest;
import com.sumavision.talktv2.task.GetProgramAroundTask;
import com.umeng.analytics.MobclickAgent;

/**
 * @author 李梦思
 * @version 2.0
 * @createTime 2012-6-16
 * @description 节目周边信息页
 * @changeLog
 */
public class ProgramAroundActivity extends Activity implements OnClickListener,
		NetConnectionListener {
	private int id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.program_around);
		id = getIntent().getIntExtra("id", 0);
		initOthers();
		initViews();
		setListeners();
		getProgramAroundData();
	}

	private AsyncImageLoader imageLoader;

	private void initOthers() {
		imageLoader = new AsyncImageLoader();
	}

	private void setListeners() {
		errText.setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
	}

	private TextView errText;
	private ProgressBar progressBar;
	private ScrollView scrollView;
	private TextView sourceTextView;
	private TextView timeTextView;
	private TextView contentTextView;
	private TextView titleTextView;
	private ImageView imageView;

	private boolean hasData;

	private void initViews() {
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		scrollView = (ScrollView) findViewById(R.id.scollView);
		timeTextView = (TextView) findViewById(R.id.time);
		sourceTextView = (TextView) findViewById(R.id.source);
		contentTextView = (TextView) findViewById(R.id.detail);
		titleTextView = (TextView) findViewById(R.id.title);
		imageView = (ImageView) findViewById(R.id.imageView);
	}

	private void updateUI() {
		scrollView.setVisibility(View.VISIBLE);
		errText.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);

		String title = ProgramAroundData.current().title;
		if (title != null) {
			titleTextView.setText(title);
		}
		String time = ProgramAroundData.current().time;
		if (time != null) {
			timeTextView.setText(time);
		}
		String source = ProgramAroundData.current().source;
		if (source != null) {
			sourceTextView.setText(source);
		}
		String content = ProgramAroundData.current().summary;
		if (content != null) {
			contentTextView.setText(content);
		}
		String url = ProgramAroundData.current().photo;
		// Log.e("ProgramAround", url);
		loadImage(imageView, url);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getProgramAroundData();
			break;
		case R.id.back:
			close();
			break;
		default:
			break;
		}
	}

	private GetProgramAroundTask getProgramAroundTask;

	private void getProgramAroundData() {
		if (getProgramAroundTask == null) {
			ProgramAroundData.current().id = id;
			getProgramAroundTask = new GetProgramAroundTask(this);
			getProgramAroundTask.execute(this, new ProgramAroundRequest(),
					new ProgramAroundParser());
			if (!hasData) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("programAroundDetail".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateUI();
				hasData = true;
			} else {
				progressBar.setVisibility(View.GONE);
			}
			getProgramAroundTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	private void close() {
		if (getProgramAroundTask != null) {
			getProgramAroundTask.cancel(true);
		}
		if (imageLoader != null) {
			try {
				imageLoader.recyle();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		finish();
	}

	private void loadImage(final ImageView imageView, String url) {
		final ImageView local = imageView;
		Drawable drawable = imageLoader.loadDrawable(url, new ImageCallback() {
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				local.setImageDrawable(imageDrawable);
			}
		});
		if (drawable != null) {
			local.setImageDrawable(drawable);
		} else {
			local.setImageResource(R.drawable.programaround_pic_defalut);
		}
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
