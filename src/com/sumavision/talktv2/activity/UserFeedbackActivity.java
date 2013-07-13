package com.sumavision.talktv2.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.task.FeedBackTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * @author 郭鹏
 * @createTime
 * @description 用户反馈
 * @changeLog
 */
public class UserFeedbackActivity extends Activity implements OnClickListener,
		NetConnectionListenerNew {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		initView();
		setListener();
	}

	private EditText contentText;
	private EditText userText;
	// 通信框
	private RelativeLayout connectBg;

	private void initView() {
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
		connectBg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hidepb();
				if (feedBackTask != null) {
					feedBackTask.cancel(true);
					feedBackTask = null;
				}
			}
		});

		contentText = (EditText) findViewById(R.id.content_text);
		userText = (EditText) findViewById(R.id.user_text);
	}

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	private void setListener() {
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.commit).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.commit:
			commit();
			break;
		default:
			break;
		}
	}

	private void commit() {
		String content = contentText.getText().toString();
		String connect = userText.getText().toString();
		if (content.equals("")) {
			DialogUtil.alertToast(getApplicationContext(), "请写点内容再提交吧~");
			contentText.startAnimation(AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.leftright));
		} else if (connect.equals("")) {
			DialogUtil.alertToast(getApplicationContext(), "请留下你的联系费方式~");
			userText.startAnimation(AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.leftright));
		} else {
			hideSoftPad();
			sendFeedBackInfo(UserNow.current().userID, content, connect);
		}
	}

	private void hideSoftPad() {

		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(UserFeedbackActivity.this
				.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);

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

	private FeedBackTask feedBackTask;

	private void sendFeedBackInfo(int userId, String content, String contactNum) {
		if (feedBackTask == null) {
			feedBackTask = new FeedBackTask(this, false);
			feedBackTask.execute(this, userId, content, contactNum);
		}
	}

	@Override
	public void onNetBegin(String method, boolean isLoadMore) {
		if (Constants.feedBack.equals(method)) {
			showpb();
		}
	}

	@Override
	public void onNetEnd(String msg, String method) {

	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	@Override
	public void onNetEnd(int code, String msg, String method, boolean isLoadMore) {
		if (Constants.feedBack.equals(method)) {
			hidepb();
			switch (code) {
			case Constants.fail_server_err:
			case Constants.fail_no_net:
			case Constants.parseErr:
			case Constants.requestErr:
				DialogUtil.alertToast(getApplicationContext(), "反馈失败!");
				break;
			case Constants.sucess:
				DialogUtil.alertToast(getApplicationContext(),
						"已接收到您的反馈，我们会尽快处理，感谢您的参与!");
				String point = "";
				if (UserNow.current().getPoint > 0) {
					point += "TV币 +" + UserNow.current().getPoint + "\n";
					UserNow.current().getPoint = 0;
					if (OtherCacheData.current().isShowExp)
						if (UserNow.current().getExp > 0) {
							point += "经验值 +" + UserNow.current().getExp + "\n";
							UserNow.current().getExp = 0;
						}
					DialogUtil.showScoreAddToast(UserFeedbackActivity.this,
							point);
				}
				finish();
				break;
			default:
				break;
			}
			feedBackTask = null;
		}
	}
}
