package com.sumavision.talktv2.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.AsyncImageLoader.ImageCallback;
import com.sumavision.talktv2.data.BindOpenAPIData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.UserUpdateParser;
import com.sumavision.talktv2.net.UserUpdateRequest;
import com.sumavision.talktv2.task.UserUpdateTask;
import com.sumavision.talktv2.user.UserModify;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Base64;
import com.sumavision.talktv2.utils.BitmapUtils;
import com.sumavision.talktv2.utils.DialogUtil;
import com.sumavision.talktv2.utils.FileInfoUtils;
import com.sumavision.talktv2.utils.InfomationHelper;
import com.sumavision.talktv2.utils.MediaInfoUtils;
import com.sumavision.talktv2.utils.PicUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @author jianghao
 * @version 2.0
 * @description 我的编辑
 * @createTime 2012-6-14
 * @changeLog 2013-1-7
 */
public class UserInfoEditActivity extends Activity implements OnClickListener,
		NetConnectionListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userinfoedit);
		initOthers();
		initView();
		setListeners();
		setOriginalValue();
	}

	private AsyncImageLoader imageLoader;

	private void initOthers() {
		pu = new PicUtils(this, this);
		imageLoader = new AsyncImageLoader();
	}

	private TextView nameText;
	private EditText introText;
	private EditText passwordText;
	private EditText newPasswordText;
	private EditText confirmPasswordText;
	private ImageView iconImageView;

	private ImageButton maleImageView;
	private ImageButton femaleImageView;
	private boolean isMale;

	private void initView() {
		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
		nameText = (TextView) findViewById(R.id.name);
		introText = (EditText) findViewById(R.id.signnature);
		passwordText = (EditText) findViewById(R.id.ue_password);
		newPasswordText = (EditText) findViewById(R.id.ue_new_password);
		confirmPasswordText = (EditText) findViewById(R.id.ue_confirm_password);
		iconImageView = (ImageView) findViewById(R.id.head_pic);
		maleImageView = (ImageButton) findViewById(R.id.ue_male);
		femaleImageView = (ImageButton) findViewById(R.id.ue_female);
	}

	private void setOriginalValue() {
		String name = UserNow.current().name;
		if (name != null) {
			nameText.setText(name);
		}
		String intro = UserNow.current().signature;
		if (intro != null) {
			introText.setText(intro);
		}
		if (UserNow.current().gender == 1) {
			maleImageView.setImageResource(R.drawable.uc_sex_male);
			femaleImageView.setImageResource(R.drawable.uc_female_deselected);
			isMale = true;
		} else {
			maleImageView.setImageResource(R.drawable.uc_male_deselected);
			femaleImageView.setImageResource(R.drawable.uc_sex_female);
			isMale = false;
		}
		String url = UserNow.current().iconURL;
		if (url != null) {
			loadImage(iconImageView, url);
		}
	}

	private void setListeners() {
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.commit).setOnClickListener(this);
		maleImageView.setOnClickListener(this);
		femaleImageView.setOnClickListener(this);
		iconImageView.setOnClickListener(this);

	}

	private void hideSoftPad() {
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(UserInfoEditActivity.this
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.commit:
			hideSoftPad();
			commit();
			break;
		case R.id.ue_male:
			if (!isMale) {
				maleImageView.setImageResource(R.drawable.uc_sex_male);
				femaleImageView
						.setImageResource(R.drawable.uc_female_deselected);
				isMale = true;
			}
			break;
		case R.id.ue_female:
			if (isMale) {
				maleImageView.setImageResource(R.drawable.uc_male_deselected);
				femaleImageView.setImageResource(R.drawable.uc_sex_female);
				isMale = false;
			}
			break;
		case R.id.head_pic:
			CharSequence[] items = { "相册", "拍照" };
			ChooseImage(items);
			break;
		default:
			break;
		}
	}

	private UserUpdateTask userUpdateTask;

	private void sendUserUpdateInfo() {
		if (userUpdateTask == null) {
			userUpdateTask = new UserUpdateTask(this);
			userUpdateTask.execute(this, new UserUpdateRequest(),
					new UserUpdateParser());
		}
	}

	private void commit() {
		String oldPasswd = passwordText.getText().toString().trim();
		String tempPasswrod = newPasswordText.getText().toString();
		String tempConfirmPassword = confirmPasswordText.getText().toString();

		if (oldPasswd == null || oldPasswd.equals("")) {

			if (!tempPasswrod.equals("") || !tempConfirmPassword.equals("")) {
				DialogUtil.alertToast(getApplicationContext(), "请先输入旧密码");
				return;
			}
		} else if (oldPasswd.equals(UserNow.current().passwd)) {

		} else {
			DialogUtil.alertToast(getApplicationContext(), "旧密码输入错误");
			return;
		}

		if (tempPasswrod != null && !tempPasswrod.equals("")) {
			if (tempPasswrod.equals(tempConfirmPassword)) {
				UserModify.current().passwdNew = tempPasswrod;
				UserModify.current().passwdNewFlag = 1;
			} else {
				DialogUtil.alertToast(getApplicationContext(), "两次密码不一致");
				return;
			}
		}

		if (oldPasswd != null && !oldPasswd.equals("") && tempPasswrod != null
				&& !tempPasswrod.equals("") && tempConfirmPassword != null
				&& !tempConfirmPassword.equals("")) {
			UserModify.current().passwdOld = oldPasswd;
			UserModify.current().passwdNewFlag = 1;
		} else {
			UserModify.current().passwdNewFlag = 0;
		}
		String tempIntro = introText.getText().toString();
		if (tempIntro != null) {
			if (!tempIntro.equals(UserNow.current().signature)) {
				UserModify.current().sign = tempIntro;
				UserModify.current().signFlag = 1;
			}
		}
		if (isMale) {
			UserModify.current().gender = 1;
		} else {
			UserModify.current().gender = 2;
		}
		UserModify.current().genderFlag = 1;
		if (lastChangePic) {
			UserModify.current().picFlag = 1;
		}
		sendUserUpdateInfo();
	}

	private boolean lastChangePic;

	private Bitmap result;
	private PicUtils pu;

	private static final int REQUEST_CODE_GETIMAGE_BYSDCARD = 1;
	private static final int REQUEST_CODE_GETIMAGE_BYCAMERA = 2;
	public static final int PHOTORESOULT = 4;
	public static final String IMAGE_UNSPECIFIED = "image/*";
	private String theLarge = null;

	public void ChooseImage(CharSequence[] items) {
		AlertDialog imageDialog = new AlertDialog.Builder(
				UserInfoEditActivity.this).setTitle("选择图片")
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {

						if (item == 0) {
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);
							intent.setType("image/*");
							startActivityForResult(intent,
									REQUEST_CODE_GETIMAGE_BYSDCARD);
						} else if (item == 1) {
							Intent intent = new Intent(
									"android.media.action.IMAGE_CAPTURE");

							String camerName = InfomationHelper.getFileName();
							String fileName = "TalkTV" + camerName + ".tmp";

							File fileDir = new File(
									JSONMessageType.USER_PIC_SDCARD_FOLDER);
							if (!fileDir.exists()) {
								fileDir.mkdir();
							}

							File camerFile = new File(
									JSONMessageType.USER_PIC_SDCARD_FOLDER,
									fileName);
							UserNow.current().picPath = camerFile
									.getAbsolutePath();

							Uri originalUri = Uri.fromFile(camerFile);
							intent.putExtra(MediaStore.EXTRA_OUTPUT,
									originalUri);
							startActivityForResult(intent,
									REQUEST_CODE_GETIMAGE_BYCAMERA);
						}
					}
				}).create();

		imageDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PHOTORESOULT:
				Bundle extras = data.getExtras();
				if (extras != null) {

					result = extras.getParcelable("data");
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					result.compress(Bitmap.CompressFormat.JPEG, 100, stream);

					String str = new String(Base64.encode(
							BitmapUtils.bitmapToBytes(result), 0,
							BitmapUtils.bitmapToBytes(result).length));
					UserModify.current().pic_Base64 = str;
					iconImageView.setImageDrawable(new BitmapDrawable(result));
					lastChangePic = true;
				}
				break;
			case REQUEST_CODE_GETIMAGE_BYCAMERA:
				Uri uri = Uri.fromFile(new File(UserNow.current().picPath));
				startPhotoZoom(uri);
				break;
			case REQUEST_CODE_GETIMAGE_BYSDCARD:
				if (data == null)
					return;
				Uri thisUri = data.getData();
				String thePath = InfomationHelper
						.getAbsolutePathFromNoStandardUri(thisUri);

				if (com.sumavision.talktv2.utils.StringUtils.isBlank(thePath)) {
					theLarge = pu.getAbsoluteImagePath(thisUri);
				} else {
					theLarge = thePath;
				}

				String attFormat = FileInfoUtils.getFileFormat(theLarge);
				if (!"photo".equals(MediaInfoUtils.getContentType(attFormat))) {
					Toast.makeText(UserInfoEditActivity.this, "请选择图片文件！",
							Toast.LENGTH_SHORT).show();
					return;
				}

				Uri uri1 = Uri.fromFile(new File(theLarge));
				startPhotoZoom(uri1);
				break;
			default:
				break;
			}
		}
	}

	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", JSONMessageType.PIC_SIZE_LIMITE_H);
		intent.putExtra("outputY", JSONMessageType.PIC_SIZE_LIMITE_W);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, PHOTORESOULT);
	}

	private RelativeLayout connectBg;

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	@Override
	public void onNetBegin(String method) {
		if ("userUpdate".equals(method)) {
			showpb();
		}
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("userUpdate".equals(method)) {
			hidepb();
			if (msg != null && "".equals(msg)) {
				DialogUtil.alertToast(getApplicationContext(), "修改成功");
				finish();
			} else {
				DialogUtil.alertToast(getApplicationContext(), "修改失败");
			}
			userUpdateTask = null;

			if (UserModify.current().passwdNewFlag == 1) {
				UserNow.current().passwd = UserModify.current().passwdNew;
			}

			clearStatus();
			SaveUserData(true);
		}
		if (UserNow.current().getNewBadge() != null) {
			for (int i = 0; i < UserNow.current().getNewBadge().size(); i++) {
				String name = UserNow.current().getNewBadge().get(i).name;
				if (name != null) {
					DialogUtil.showBadgeAddToast(UserInfoEditActivity.this,
							name);
				}
			}
			UserNow.current().setNewBadge(null);
		}
	}

	private void clearStatus() {
		UserModify.current().passwdNewFlag = 0;
		UserModify.current().nameNewFlag = 0;
		UserModify.current().signFlag = 0;
		UserModify.current().genderFlag = 0;
		UserModify.current().picFlag = 0;
	}

	private void SaveUserData(boolean b) {
		SharedPreferences sp = getSharedPreferences("userInfo", 0);
		Editor spEd = sp.edit();

		if (b) {
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
	public void onNetEnd(String msg, String method, int type) {
	}

	@Override
	public void onCancel(String method) {

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
		if (drawable != null) {
			local.setImageDrawable(drawable);
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
