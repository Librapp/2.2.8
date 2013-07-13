package com.sumavision.talktv2.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.net.BindUserParser;
import com.sumavision.talktv2.net.BindUserRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.task.BindAccountTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-11
 * @description 绑定账号界面
 * @changeLog
 */
public class BindAccountActivity extends Activity implements OnClickListener,
		NetConnectionListener {
	private EditText name;
	private EditText passwd;
	private String userName;
	private String passWord;
	private Animation a;
	private Animation close;

	private final int MSG_CLOSE_ACTIVITY = 1;

	private FrameLayout all;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bindaccount);

		all = (FrameLayout) findViewById(R.id.bindaccount);
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
		connectBg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

		a = AnimationUtils.loadAnimation(this, R.anim.leftright);
		close = AnimationUtils.loadAnimation(this, R.anim.close2bottom);
		findViewById(R.id.bindaccount_back).setOnClickListener(this);
		findViewById(R.id.bindaccount_login).setOnClickListener(this);
		name = (EditText) findViewById(R.id.bindaccount_edit_email);
		passwd = (EditText) findViewById(R.id.bindaccount_edit_passwd);
	}

	// 通信框
	private RelativeLayout connectBg;

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		userName = name.getText().toString().trim();
		passWord = passwd.getText().toString().trim();
		switch (v.getId()) {
		case R.id.bindaccount_login:
			if (userName.equals("")) {
				Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
				name.startAnimation(a);
			} else if (passWord.equals("")) {
				Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
				passwd.startAnimation(a);
			} else if (!userName.equals("") && !passWord.equals("")) {
				hideSoftPad();
				UserNow.current().name = userName;
				UserNow.current().passwd = passWord;
				UserNow.current().thirdType = 1;
				UserNow.current().thirdToken = SinaData.accessToken;
				UserNow.current().userType = 2;
				bindRegister();
			}
			break;
		case R.id.bindaccount_back:
			finish();
			break;
		default:
			break;
		}
	}

	private Handler handler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CLOSE_ACTIVITY:
				finish();
				break;

			default:
				break;
			}
			return false;
		}
	});

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	// 绑定注册
	private void bindRegister() {
		BindAccountTask bindAccountTask = new BindAccountTask(this);
		bindAccountTask.execute(this, new BindUserRequest(),
				new BindUserParser());
	}

	private void hideSoftPad() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(BindAccountActivity.this
				.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onNetBegin(String method) {
		showpb();
	}

	@Override
	public void onNetEnd(String msg, String method) {
		hidepb();
		if (msg.equals("")) {
			String point = "0";
			if (UserNow.current().getPoint > 0) {
				point += "TV币 +" + UserNow.current().getPoint + "\n";
				UserNow.current().getPoint = 0;
				if (OtherCacheData.current().isShowExp)
					if (UserNow.current().getExp > 0) {
						point += "经验值 +" + UserNow.current().getExp + "\n";
						UserNow.current().getExp = 0;
					}
				DialogUtil.showScoreAddToast(BindAccountActivity.this, point);
			}
			setResult(RESULT_OK);
			all.startAnimation(close);
			handler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
		} else {
			Toast.makeText(BindAccountActivity.this, UserNow.current().errMsg,
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCancel(String method) {
		hidepb();
	}

}