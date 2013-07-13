package com.sumavision.talktv2.activity;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.AsyncImageLoader.ImageCallback;
import com.sumavision.talktv2.data.BindOpenAPIData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.net.BindUserParser;
import com.sumavision.talktv2.net.BindUserRequest;
import com.sumavision.talktv2.net.LoginNewParser;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.RegisterRequest;
import com.sumavision.talktv2.task.BindAccountTask;
import com.sumavision.talktv2.task.RegisterTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.AppUtil;
import com.umeng.analytics.MobclickAgent;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.net.RequestListener;

/**
 * 
 * @author 郭鹏
 * @version 2.2
 * @createTime 2012-5-31
 * @description 注册界面
 * @changeLog 修改为2.2版本 by 李梦思 2013-1-11
 * 
 */
public class RegisterActivity extends Activity implements OnClickListener,
		NetConnectionListener {

	private Context c;
	private Button btnOk;
	private Button btnBack;
	private EditText name;
	private EditText passwd;
	private EditText passwd1;
	private EditText eMail;
	private ImageView headpic;
	private LinearLayout bindOldAccount;
	private RelativeLayout clientLayout;
	private TextView readP;
	private String userName;
	private String passWord;
	private String passWord1;
	private String userEMail;
	private FrameLayout main;
	private Animation a;
	// private Animation b;
	// private Animation close;
	private CheckBox cb;

	private final int MSG_CLOSE_ACTIVITY = 3;
	private FrameLayout all;

	public static final int BIND = 1;
	public static final int NORMAL = 0;
	private int type = NORMAL;
	// SINA微博信息
	private final int SINA = 1;
	private final int SINA_OK = 0;
	private final int SINA_ERROR = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		type = getIntent().getIntExtra("type", NORMAL);
		c = getApplicationContext();

		all = (FrameLayout) findViewById(R.id.userregister_main);

		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);

		cb = (CheckBox) findViewById(R.id.reg_read_p);
		a = AnimationUtils.loadAnimation(this, R.anim.leftright);
		// b = AnimationUtils.loadAnimation(this, R.anim.activity_open2up);
		// close = AnimationUtils.loadAnimation(getApplicationContext(),
		// R.anim.activity_close2bottom);
		readP = (TextView) findViewById(R.id.reg_p_text_txt);
		/*
		 * SpannableString sp = new SpannableString("我已阅读并接受 电视粉使用协议"); // 设置超链接
		 * sp.setSpan(new ForegroundColorSpan(Color.RED), 7, 15,
		 * Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		 */
		readP.setText(Html.fromHtml("<u>"
				+ getResources().getString(R.string.dianshifenxieyi) + "<u>"));
		// readP.setText(sp);

		readP.setOnClickListener(this);
		btnOk = (Button) findViewById(R.id.register_btn_ok);
		btnOk.setOnClickListener(this);
		btnBack = (Button) findViewById(R.id.reg_detail_back);
		btnBack.setOnClickListener(this);
		name = (EditText) findViewById(R.id.register_edit_name);
		passwd = (EditText) findViewById(R.id.register_edit_passwd);
		passwd1 = (EditText) findViewById(R.id.register_edit_passwd1);
		eMail = (EditText) findViewById(R.id.register_edit_email);
		main = (FrameLayout) findViewById(R.id.userregister_main);
		// main.startAnimation(b);
		headpic = (ImageView) findViewById(R.id.register_client_icon);
		bindOldAccount = (LinearLayout) findViewById(R.id.register_bindoldaccount);
		clientLayout = (RelativeLayout) findViewById(R.id.register_client_layout);

		if (type == BIND) {
			headpic.setVisibility(View.VISIBLE);
			eMail.setVisibility(View.GONE);
			bindOldAccount.setVisibility(View.VISIBLE);
			clientLayout.setVisibility(View.VISIBLE);
			bindOldAccount.setOnClickListener(this);
			initOthers();
			showpb();
			UsersAPI usersAPI = new UsersAPI(SinaData.weibo().accessToken);
			if (OtherCacheData.current().isDebugMode) {
				Log.e("sina微博获取用户ID", SinaData.id);
			}
			usersAPI.show(Long.parseLong(SinaData.id), new RequestListener() {

				@Override
				public void onIOException(IOException arg0) {
					if (OtherCacheData.current().isDebugMode) {
						Log.e("sina微博账号信息", arg0.getMessage());
					}
					Message msg = new Message();
					msg.what = SINA;
					msg.arg1 = SINA_ERROR;
					msg.obj = arg0.getMessage();
					handler.sendMessage(msg);
				}

				@Override
				public void onError(WeiboException arg0) {
					if (OtherCacheData.current().isDebugMode) {
						Log.e("sina微博账号信息", arg0.getMessage());
					}
					Message msg = new Message();
					msg.what = SINA;
					msg.arg1 = SINA_ERROR;
					msg.obj = arg0.getMessage();
					handler.sendMessage(msg);
				}

				@Override
				public void onComplete(String arg0) {
					if (OtherCacheData.current().isDebugMode) {
						Log.e("sina微博账号信息", arg0);
					}
					try {
						JSONObject sinaInfo = new JSONObject(arg0);
						SinaData.name = sinaInfo.getString("name");
						SinaData.icon = sinaInfo.getString("profile_image_url");
						if ("m".equals(sinaInfo.getString("gender")))
							SinaData.gender = 1;
						else
							SinaData.gender = 2;
						SinaData.description = sinaInfo
								.getString("description");
						Message msg = new Message();
						msg.what = SINA;
						msg.arg1 = SINA_OK;
						handler.sendMessage(msg);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	private AsyncImageLoader imageLoader;

	private void initOthers() {
		imageLoader = new AsyncImageLoader();
	}

	/**
	 * 加载图片
	 */
	private void loadImage(final ImageView imageView, String url) {
		final ImageView local = imageView;
		Drawable drawable = imageLoader.loadDrawable(url, new ImageCallback() {
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				local.setImageDrawable(imageDrawable);
			}
		});
		if (drawable != null && local.getTag().equals(url)) {
			local.setImageDrawable(drawable);
		}
	}

	private final Handler handler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case SINA:
				hidepb();
				switch (msg.arg1) {
				case SINA_OK:
					name.setText(SinaData.name);
					loadImage(headpic, SinaData.icon);
					break;
				default:
					Toast.makeText(RegisterActivity.this, "获取SINA微博用户信息失败",
							Toast.LENGTH_SHORT).show();
					break;
				}
				break;
			case MSG_CLOSE_ACTIVITY:
				finish();
				break;
			default:
				break;
			}
			return false;
		}
	});

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
		passWord1 = passwd1.getText().toString().trim();
		userEMail = eMail.getText().toString().trim();

		switch (v.getId()) {
		case R.id.reg_p_text_txt:
			Intent i = new Intent(c, WebBrowserActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// i.putExtra("url", "http://www.tvfan.com.cn/app/agreement.jsp");
			i.putExtra("url", "file:///android_asset/tvfan_agreement.htm");
			i.putExtra("title", "电视粉使用协议");
			c.startActivity(i);
			break;
		case R.id.register_btn_ok:
			if (type == NORMAL) {
				if (userName.equals("")) {
					Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
					name.startAnimation(a);
				}

				else if (userName.length() > 15) {
					Toast.makeText(this, "请输入正确的用户名，少于15个字符",
							Toast.LENGTH_SHORT).show();
					name.startAnimation(a);
				}

				else if (userEMail.equals("")) {
					Toast.makeText(this, "请输入邮箱地址", Toast.LENGTH_SHORT).show();
					eMail.startAnimation(a);
				}

				else if (!AppUtil.isEmail(userEMail)) {
					Toast.makeText(this, "请输入正确的邮箱", Toast.LENGTH_SHORT).show();
					eMail.startAnimation(a);
				} else if (passWord.equals("")) {
					Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
					passwd.startAnimation(a);
				} else if (passWord1.equals("")) {
					Toast.makeText(this, "请再次输入密码", Toast.LENGTH_SHORT).show();
					passwd1.startAnimation(a);
				} else if (!passWord.equals(passWord1)) {
					Toast.makeText(this, "两次输入密码不一致，请重新输入", Toast.LENGTH_SHORT)
							.show();
					passwd.startAnimation(a);
					passwd1.startAnimation(a);
				} else if (!cb.isChecked()) {
					Toast.makeText(this, "请选择是否接受注册协议", Toast.LENGTH_SHORT)
							.show();
					cb.startAnimation(a);
				} else if (!userName.equals("") && !passWord.equals("")
						&& passWord.equals(passWord1) && !userEMail.equals("")) {
					hideSoftPad();

					UserNow.current().name = userName;
					UserNow.current().passwd = passWord;
					UserNow.current().eMail = userEMail;
					// 启动注册网络通信
					register();
				}
			} else {
				if (userName.equals("")) {
					Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
					name.startAnimation(a);
				} else if (passWord.equals("")) {
					Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
					passwd.startAnimation(a);
				} else if (passWord1.equals("")) {
					Toast.makeText(this, "请再次输入密码", Toast.LENGTH_SHORT).show();
					passwd1.startAnimation(a);
				} else if (!passWord.equals(passWord1)) {
					Toast.makeText(this, "两次输入密码不一致，请重新输入", Toast.LENGTH_SHORT)
							.show();
					passwd.startAnimation(a);
					passwd1.startAnimation(a);
				} else if (!cb.isChecked()) {
					Toast.makeText(this, "请选择是否接受注册协议", Toast.LENGTH_SHORT)
							.show();
					cb.startAnimation(a);
				} else if (!userName.equals("") && !passWord.equals("")
						&& passWord.equals(passWord1)) {
					hideSoftPad();
					UserNow.current().name = userName;
					UserNow.current().passwd = passWord;
					UserNow.current().thirdType = 1;
					UserNow.current().thirdToken = SinaData.accessToken;
					UserNow.current().userType = 1;
					bindRegister();
				}
			}
			break;
		case R.id.register_bindoldaccount:
			startActivityForResult(new Intent(RegisterActivity.this,
					BindAccountActivity.class), LoginActivity.REQUESTCODE_REG);
			break;
		case R.id.reg_detail_back:
			// all.startAnimation(close);
			handler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

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

	// 注册
	private void register() {
		RegisterTask registerTask = new RegisterTask(this);
		registerTask.execute(this, new RegisterRequest(), new LoginNewParser());
	}

	// 绑定注册
	private void bindRegister() {
		UserNow.current().errorCode = -1;
		BindAccountTask bindAccountTask = new BindAccountTask(this);
		bindAccountTask.execute(this, new BindUserRequest(),
				new BindUserParser());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (connectBg.isShown()) {
				hidepb();
				return true;
			} else {
				// all.startAnimation(close);
				handler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void hideSoftPad() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(RegisterActivity.this
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
			// String point = "0";
			// if (UserNow.current().getPoint > 0) {
			// point += "TV币 +" + UserNow.current().getPoint + "\n";
			// UserNow.current().getPoint = 0;
			// if (OtherCacheData.current().isShowExp)
			// if (UserNow.current().getExp > 0) {
			// point += "经验值 +" + UserNow.current().getExp + "\n";
			// UserNow.current().getExp = 0;
			// }
			// DialogUtil.showScoreAddToast(RegisterActivity.this, point);
			// }
			MobclickAgent.onEvent(getApplicationContext(), "zhuce");
			setResult(RESULT_OK);
			SaveUserData(true);
			// all.startAnimation(close);
			handler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 300);
		} else {
			Toast.makeText(RegisterActivity.this, UserNow.current().errMsg,
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			setResult(RESULT_OK);
			finish();
		}
	}

	// 将来应该删除
	private void SaveUserData(boolean b) {
		SharedPreferences sp = getSharedPreferences("userInfo", 0);
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

}