package com.sumavision.talktv2.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.AccessTokenKeeper;
import com.sumavision.talktv2.data.BindOpenAPIData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListenerNew;
import com.sumavision.talktv2.task.BindLogInTask;
import com.sumavision.talktv2.task.LogInTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.AccountAPI;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

/**
 * @author 郭鹏
 * @createTime 2012-5-31
 * @description 登录界面
 * @changeLog
 */
public class LoginActivity extends Activity implements OnClickListener,
		NetConnectionListenerNew {

	private final String CONFIGFILE = "userInfo";
	private String sevedUserName;
	private String userPassword;
	private CheckBox savePassword;
	private CheckBox autoLogin;
	private Button btnOk;
	private Button btnReg;
	private Button btnBack;
	private EditText name;
	private EditText passwd;
	private String userName;
	private String passWord;
	public static final int REQUESTCODE_REG = 0;
	private final int MSG_CLOSE_ACTIVITY = 3;
	private Animation a;

	// 绑定按钮
	private Button bind_sina;

	private int funcFlag = 0;
	private final int LOGIN = 1;
	private final int BIND_LOGIN = 4;
	private final int REGISTER = 5;

	private final int SINA = 1;
	private final int SINA_GET_UID = 3;

	private SsoHandler ssh;

	// 通信框
	private RelativeLayout connectBg;

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		initView();
	}

	@Override
	public void onClick(View v) {

		UserNow.current().isTimeOut = true;

		userName = name.getText().toString().trim();
		passWord = passwd.getText().toString().trim();

		switch (v.getId()) {
		case R.id.login_btn_bottom_sina:
			showpb();
			funcFlag = SINA;
			ssh = new SsoHandler(this, SinaData.weibo());
			ssh.authorize(new WeiboAuthListener() {

				@Override
				public void onWeiboException(WeiboException arg0) {
					Message msg = new Message();
					msg.what = SINA;
					msg.obj = "sina微博授权失败";
					if (OtherCacheData.current().isDebugMode)
						Log.e("sina微博授权", arg0.getMessage());
					serverHandler.sendMessage(msg);
				}

				@Override
				public void onError(WeiboDialogError arg0) {
					Message msg = new Message();
					msg.what = SINA;
					msg.obj = "sina微博授权失败";
					if (OtherCacheData.current().isDebugMode)
						Log.e("sina微博授权", arg0.getMessage());
					serverHandler.sendMessage(msg);
				}

				@Override
				public void onComplete(Bundle arg0) {
					SinaData.accessToken = arg0.getString("access_token");
					SinaData.expires_in = arg0.getString("expires_in");
					SinaData.weibo().accessToken = new Oauth2AccessToken(
							SinaData.accessToken, SinaData.expires_in);
					AccessTokenKeeper.keepAccessToken(LoginActivity.this,
							SinaData.weibo().accessToken);
					SinaData.isSinaBind = true;
					Message msg = new Message();
					msg.what = SINA;
					msg.arg1 = SINA_GET_UID;
					serverHandler.sendMessage(msg);
				}

				@Override
				public void onCancel() {
					Message msg = new Message();
					msg.what = SINA;
					msg.obj = "sina授权取消";
					serverHandler.sendMessage(msg);
				}
			});
			break;
		case R.id.login_btn_ok:
			if (userName.equals("")) {
				Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
				name.startAnimation(a);
			} else if (passWord.equals("")) {
				Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
				passwd.startAnimation(a);
			} else if (!userName.equals("") && !passWord.equals("")) {
				UserNow.current().name = userName;
				UserNow.current().passwd = passWord;
				// 启动登录网络通信
				funcFlag = LOGIN;
				UserNow.current().errMsg = JSONMessageType.SERVER_NETFAIL;
				// server.addListener(this);
				// server.service(new LoginNewRequest(), new LoginNewParser());
				login();
				hideSoftPad();
			}
			break;
		case R.id.login_btn_reg:
			Intent iR = new Intent(this, RegisterActivity.class);
			iR.putExtra("type", RegisterActivity.NORMAL);
			startActivityForResult(iR, REQUESTCODE_REG);
			break;
		case R.id.login_detail_back:
			OtherCacheData.current().isFromMyActivityToLoginNotClose = true;
			// all.startAnimation(close);
			serverHandler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
			break;
		default:
			break;
		}
	}

	// 关闭键盘
	private void hideSoftPad() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(LoginActivity.this
				.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UserNow.current().errorCode = -1;
	}

	private void SaveUserData(boolean b) {
		SharedPreferences sp = getSharedPreferences(CONFIGFILE, 0);
		Editor spEd = sp.edit();
		if (b) {
			if (OtherCacheData.current().isDebugMode) {
				Log.e("LoginActivity", "step - 1");
			}
			spEd.putInt("openType", BindOpenAPIData.current().openType);
			spEd.putBoolean("isOpenTypeLogin",
					BindOpenAPIData.current().isOpenTypeLogin);
			spEd.putBoolean("login", true);
			spEd.putBoolean("autologin", true);
			spEd.putString("username", UserNow.current().name);
			spEd.putString("name", UserNow.current().name);
			spEd.putString("nickName", UserNow.current().name);
			if (!BindOpenAPIData.current().isOpenTypeLogin) {
				spEd.putString("password", UserNow.current().passwd);
			} else {
				spEd.putString("password", "");
			}
			spEd.putString("address", UserNow.current().eMail);
			spEd.putString("sessionID", UserNow.current().sessionID);

			spEd.putInt("checkInCount", UserNow.current().checkInCount);
			spEd.putInt("commentCount", UserNow.current().commentCount);
			spEd.putInt("messageCount",
					UserNow.current().privateMessageAllCount);
			spEd.putInt("messagePeopleCount",
					UserNow.current().privateMessageOnlyCount);
			spEd.putInt("fansCount", UserNow.current().fansCount);
			spEd.putInt("friendCount", UserNow.current().friendCount);

			spEd.putString("iconURL", UserNow.current().iconURL);
			spEd.putInt("userID", UserNow.current().userID);
			spEd.putLong("point", UserNow.current().point);
			spEd.putString("level", UserNow.current().level);
			spEd.putInt("gender", UserNow.current().gender);
			spEd.putLong("exp", UserNow.current().exp);
			spEd.putString("signature", UserNow.current().signature);

			spEd.putInt("commentCount", UserNow.current().commentCount);
			spEd.putInt("remindCount", UserNow.current().remindCount);
			spEd.putInt("chaseCount", UserNow.current().chaseCount);

			// 被@数量
			spEd.putInt("atMeCount", UserNow.current().atMeCount);
			// 被回复数量
			spEd.putInt("replyMeCount", UserNow.current().replyMeCount);
			spEd.putInt("badgesCount", UserNow.current().badgesCount);
		} else {
			spEd.putBoolean("isOpenTypeLogin", false);
			spEd.putBoolean("login", false);
			spEd.putBoolean("autologin", false);
			spEd.putString("username", "");
			spEd.putString("password", "");
			spEd.putInt("userID", 0);
		}
		spEd.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (funcFlag == SINA) {
			funcFlag = 0;
			if (ssh != null) {
				ssh.authorizeCallBack(requestCode, resultCode, data);
			}
		} else {
			if (resultCode == RESULT_OK) {
				switch (requestCode) {
				case REQUESTCODE_REG:
					if (resultCode == RESULT_OK) {
						funcFlag = REGISTER;
						SaveUserData(true);
						setResult(RESULT_OK);
						finish();
					}
					break;
				default:
					break;
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

	}

	private final Handler serverHandler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case SINA:
				switch (msg.arg1) {
				case SINA_GET_UID:
					AccountAPI accountAPI = new AccountAPI(Weibo.getInstance(
							Weibo.app_key, Weibo.redirecturl).accessToken);
					accountAPI.getUid(new RequestListener() {

						@Override
						public void onIOException(IOException arg0) {
							Message msg = new Message();
							msg.what = SINA;
							msg.obj = "sina微博获取用户ID失败";
							if (OtherCacheData.current().isDebugMode)
								Log.e("sina微博获取用户ID", arg0.getMessage());
							serverHandler.sendMessage(msg);
						}

						@Override
						public void onError(WeiboException arg0) {
							Message msg = new Message();
							msg.what = SINA;
							msg.obj = "sina微博获取用户ID失败";
							if (OtherCacheData.current().isDebugMode)
								Log.e("sina微博获取用户ID", arg0.getMessage());
							serverHandler.sendMessage(msg);
						}

						@Override
						public void onComplete(String arg0) {
							SinaData.id = arg0.substring(
									arg0.lastIndexOf(":") + 1,
									arg0.length() - 1);
							bindLogin();
						}
					});
					break;
				default:
					hidepb();
					Toast.makeText(LoginActivity.this, msg.obj.toString(),
							Toast.LENGTH_SHORT).show();
					break;
				}
				break;
			case MSG_CLOSE_ACTIVITY:
				finish();
				break;
			case JSONMessageType.NET_BEGIN:
				showpb();
				UserNow.current().errMsg = JSONMessageType.SERVER_NETFAIL;
				UserNow.current().errorCode = -1;
				break;
			case JSONMessageType.NET_END:
				if (UserNow.current().getNewBadge() != null) {
					for (int i = 0; i < UserNow.current().getNewBadge().size(); i++) {
						String name = UserNow.current().getNewBadge().get(i).name;
						if (name != null) {
							DialogUtil.showBadgeAddToast(LoginActivity.this,
									name);
						}
					}
					UserNow.current().setNewBadge(null);
				}
				hidepb();
				String point = "";
				if (msg.obj.equals("")) {
					switch (funcFlag) {
					case LOGIN:
						MobclickAgent
								.onEvent(getApplicationContext(), "denglu");
						if (UserNow.current().getPoint > 0) {
							point += "TV币 +" + UserNow.current().getPoint
									+ "\n";
							UserNow.current().getPoint = 0;
							if (OtherCacheData.current().isShowExp)
								if (UserNow.current().getExp > 0) {
									point += "经验值 +" + UserNow.current().getExp
											+ "\n";
									UserNow.current().getExp = 0;
								}
							DialogUtil.showScoreAddToast(LoginActivity.this,
									point);
						}
						SaveUserData(true);
						setResult(RESULT_OK);
						finish();
						break;
					case BIND_LOGIN:
						MobclickAgent.onEvent(getApplicationContext(),
								"weibodenglu");
						if (UserNow.current().getPoint > 0) {
							point += "TV币 +" + UserNow.current().getPoint
									+ "\n";
							UserNow.current().getPoint = 0;
							if (OtherCacheData.current().isShowExp)
								if (UserNow.current().getExp > 0) {
									point += "经验值 +" + UserNow.current().getExp
											+ "\n";
									UserNow.current().getExp = 0;
								}
							DialogUtil.showScoreAddToast(LoginActivity.this,
									point);
						}
						SaveUserData(true);
						setResult(RESULT_OK);
						finish();
						break;
					default:
						break;
					}
				} else {
					switch (funcFlag) {
					case LOGIN:
						Toast.makeText(LoginActivity.this,
								UserNow.current().errMsg, Toast.LENGTH_LONG)
								.show();
						break;
					case BIND_LOGIN:
						switch (UserNow.current().errorCode) {
						// 通信出错
						case 1:
							Toast.makeText(LoginActivity.this,
									UserNow.current().errMsg, Toast.LENGTH_LONG)
									.show();
							break;
						case 2:
							Intent iR = new Intent(LoginActivity.this,
									RegisterActivity.class);
							iR.putExtra("type", RegisterActivity.BIND);
							startActivityForResult(iR, REQUESTCODE_REG);
							break;
						default:
							Toast.makeText(LoginActivity.this,
									UserNow.current().errMsg, Toast.LENGTH_LONG)
									.show();
							break;
						}
						break;
					default:
						break;
					}
				}
				break;
			default:
				break;
			}
			return false;
		}
	});

	private void initView() {

		bind_sina = (Button) findViewById(R.id.login_btn_bottom_sina);
		bind_sina.setOnClickListener(this);

		btnOk = (Button) findViewById(R.id.login_btn_ok);
		btnOk.setOnClickListener(this);
		btnReg = (Button) findViewById(R.id.login_btn_reg);
		btnReg.setOnClickListener(this);
		btnBack = (Button) findViewById(R.id.login_detail_back);
		btnBack.setOnClickListener(this);
		name = (EditText) findViewById(R.id.login_edit_name);
		passwd = (EditText) findViewById(R.id.login_edit_passwd);
		passwd.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					btnOk.performClick();
				}

				return false;
			}
		});
		a = AnimationUtils.loadAnimation(this, R.anim.leftright);
		// b = AnimationUtils.loadAnimation(this, R.anim.activity_open2up);
		// close = AnimationUtils.loadAnimation(getApplicationContext(),
		// R.anim.activity_close2bottom);

		savePassword = (CheckBox) findViewById(R.id.login_save_password);
		autoLogin = (CheckBox) findViewById(R.id.login_audo_login);

		SharedPreferences sp = getSharedPreferences(CONFIGFILE, 0);
		sevedUserName = sp.getString("username", "");
		userPassword = sp.getString("password", "");

		if (!BindOpenAPIData.current().isOpenTypeLogin) {

			if (!sevedUserName.equals("") && !userPassword.equals("")) {
				name.setText(sevedUserName);
				passwd.setText(userPassword);
				savePassword.setChecked(true);
				if (sp.getBoolean("autologin", false)) {
					autoLogin.setChecked(true);
				}
			} else {
				name.setText("");
				passwd.setText("");
			}
		} else {
			name.setText("");
			passwd.setText("");
		}

		userName = name.getText().toString().trim();
		passWord = passwd.getText().toString().trim();
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);

		// main.startAnimation(b);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (UserNow.current().userID != 0) {
			SaveUserData(true);
			setResult(RESULT_OK);
			finish();
		}
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);

	}

	public void bindLogin() {
		funcFlag = BIND_LOGIN;
		UserNow.current().errorCode = -1;
		UserNow.current().errMsg = JSONMessageType.SERVER_NETFAIL;
		UserNow.current().thirdType = 1;
		UserNow.current().thirdToken = SinaData.accessToken;
		doBindLogIn();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (connectBg.isShown()) {
				hidepb();
				return true;
			} else {
				OtherCacheData.current().isFromMyActivityToLoginNotClose = true;
				// all.startAnimation(close);
				serverHandler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
				return true;
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private LogInTask loginTask;

	private void login() {
		if (loginTask == null) {
			loginTask = new LogInTask(this, false);
			loginTask.execute(this);
		}
	}

	@Override
	public void onNetBegin(String method, boolean isLoadMore) {
		if (Constants.logIn.equals(method)) {
			showpb();
		} else if (Constants.bindLogIn.equals(method)) {
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
		if (Constants.logIn.equals(method)) {
			hidepb();
			switch (code) {
			case Constants.fail_server_err:
			case Constants.fail_no_net:
			case Constants.parseErr:
			case Constants.requestErr:
				DialogUtil.alertToast(getApplicationContext(), "登录失败!");
				break;
			case Constants.sucess:
				MobclickAgent.onEvent(getApplicationContext(), "denglu");
				String point = "";
				if (UserNow.current().getPoint > 0) {
					point += "TV币 +" + UserNow.current().getPoint + "\n";
					UserNow.current().getPoint = 0;
					if (OtherCacheData.current().isShowExp)
						if (UserNow.current().getExp > 0) {
							point += "经验值 +" + UserNow.current().getExp + "\n";
							UserNow.current().getExp = 0;
						}
					DialogUtil.showScoreAddToast(LoginActivity.this, point);
				}
				SaveUserData(true);
				setResult(RESULT_OK);
				finish();
				break;
			default:
				break;
			}
			loginTask = null;
		} else if (Constants.bindLogIn.equals(method)) {
			hidepb();
			switch (code) {
			case Constants.fail_server_err:
			case Constants.fail_no_net:
			case Constants.parseErr:
			case Constants.requestErr:
				if (UserNow.current().errorCode == 2) {
					Intent iR = new Intent(LoginActivity.this,
							RegisterActivity.class);
					iR.putExtra("type", RegisterActivity.BIND);
					startActivityForResult(iR, REQUESTCODE_REG);
				} else {
					DialogUtil.alertToast(getApplicationContext(), "登录失败!");
				}
				break;
			case Constants.sucess:
				MobclickAgent.onEvent(getApplicationContext(), "weibodenglu");
				String point = "";
				if (UserNow.current().getPoint > 0) {
					point += "TV币 +" + UserNow.current().getPoint + "\n";
					UserNow.current().getPoint = 0;
					if (OtherCacheData.current().isShowExp)
						if (UserNow.current().getExp > 0) {
							point += "经验值 +" + UserNow.current().getExp + "\n";
							UserNow.current().getExp = 0;
						}
					DialogUtil.showScoreAddToast(LoginActivity.this, point);
				}
				SaveUserData(true);
				setResult(RESULT_OK);
				finish();
				break;
			default:
				break;
			}
			bindLogInTask = null;
		}

	}

	private BindLogInTask bindLogInTask;

	private void doBindLogIn() {
		if (bindLogInTask == null) {
			bindLogInTask = new BindLogInTask(this, false);
			bindLogInTask.execute(this);
		}
	}
}