package com.sumavision.talktv2.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.BadgeDetailData;
import com.sumavision.talktv2.data.PlayNewData;
import com.sumavision.talktv2.net.BadgeDetailParser;
import com.sumavision.talktv2.net.BadgeDetailRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.task.GetBadgeDetailTask;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;

/**
 * @author 姜浩
 * @version 2.2.4
 * @createTime 2013-4-9
 * @description 徽章详情解析
 * @changeLog
 */
public class BadgeDetailActivity extends Activity implements
		NetConnectionListener, OnClickListener {
	private BadgeDetailData badgeDetailData;
	private BadgeDetailData tempBadgeDetailData;

	private int badgeId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.badge_detail);
		getExtras();
		initViews();
		setListeners();
		getData();
	}

	private void getExtras() {
		Intent intent = getIntent();
		badgeId = intent.getIntExtra("badgeId", 0);
	}

	/** 标题 */
	private TextView titleView;
	/** 徽章图片 */
	private ImageView picView;
	/** 徽章获取人数 */
	private TextView personCountView;
	/** 徽章详细描述 */
	private TextView descriptionView;
	/** 内容布局 */
	private RelativeLayout contentView;
	/** 加载和错误控件 */
	private TextView errText;
	private ProgressBar progressBar;

	private void initViews() {
		titleView = (TextView) findViewById(R.id.title);
		picView = (ImageView) findViewById(R.id.pic);
		personCountView = (TextView) findViewById(R.id.person_count);
		descriptionView = (TextView) findViewById(R.id.description);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		errText = (TextView) findViewById(R.id.err_text);
		contentView = (RelativeLayout) findViewById(R.id.content_Layout);
	}

	private void setListeners() {
		findViewById(R.id.back).setOnClickListener(this);
	}

	private void updateUI(BadgeDetailData badgeDetailData) {
		contentView.setVisibility(View.VISIBLE);
		String count = String.valueOf(badgeDetailData.getCount);
		titleView.setText(badgeDetailData.name);
		String countStr = "总共" + count + "人领取";
		int firstIndex = 2;
		int lastIndex = countStr.indexOf("人");
		SpannableString spannableString = CommonUtils.getSpannableString(
				countStr, firstIndex, lastIndex, new ForegroundColorSpan(
						Color.RED));
		personCountView.setText(spannableString);

		String intro = badgeDetailData.intro;
		if (intro != null) {
			descriptionView.setText(intro);
		}

		String url = PlayNewData.current.pic;
		if (url != null) {
			ImageLoaderHelper helper = new ImageLoaderHelper();
			helper.loadImage(picView, url,
					R.drawable.rcmd_list_item_pic_default);
		}
	}

	private void hideErrLayout() {
		errText.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
	}

	private void showErrLayout() {
		errText.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		contentView.setVisibility(View.GONE);
	}

	private void showLoadingLayout() {
		errText.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		contentView.setVisibility(View.GONE);
	}

	private void getData() {
		showLoadingLayout();
		tempBadgeDetailData = new BadgeDetailData();
		executeGetData(badgeId, tempBadgeDetailData);
	}

	GetBadgeDetailTask getBadgeDetailTask;

	/**
	 * 
	 * @param badgeId
	 *            徽章ID
	 * @param tempData
	 *            临时数据存贮
	 */
	private void executeGetData(int badgeId, BadgeDetailData tempData) {
		if (getBadgeDetailTask == null) {
			getBadgeDetailTask = new GetBadgeDetailTask(this,
					Constants.badgeDetail);
			getBadgeDetailTask.execute(this, new BadgeDetailRequest(badgeId),
					new BadgeDetailParser(), tempData);
		}
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if (Constants.badgeDetail.equals(method)) {
			if (msg != null) {
				if (msg.equals("")) {
					badgeDetailData = tempBadgeDetailData;
					hideErrLayout();
					updateUI(badgeDetailData);
				} else {
					showErrLayout();
					DialogUtil.alertToast(getApplicationContext(), msg);
				}
			} else {
				showErrLayout();
				DialogUtil.alertToast(getApplicationContext(), "网络不给力");
			}
			getBadgeDetailTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			close();
			break;
		default:
			break;
		}
	}

	private void close() {
		if (getBadgeDetailTask != null) {
			getBadgeDetailTask.cancel(true);
			getBadgeDetailTask = null;
		}
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
