package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.adapter.SharetoWeixinAdapter;
import com.sumavision.talktv2.data.ShareData;
import com.sumavision.talktv2.utils.BitmapUtils;
import com.sumavision.talktv2.utils.ShareWeixinBitmapUtil;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXVideoObject;
import com.umeng.analytics.MobclickAgent;

/**
 * @author jianghao
 * @version 2.2
 * @createTime 2013-4-8
 * @description 分享到微信
 * @changLog
 */

public class ShareToWeixinActivity extends Activity implements OnClickListener,
		OnItemClickListener, IWXAPIEventHandler {
	// 微信分享的内容 （从其他页面带来）
	private String titleName;
	private String picUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sharetoweixin);
		regToWX();
		getExtra();
		initViews();
		setListeners();
	}

	private void getExtra() {
		Intent intent = getIntent();
		if (intent.hasExtra("titleName")) {
			titleName = intent.getStringExtra("titleName");
		}
		if (intent.hasExtra("picUrl")) {
			picUrl = intent.getStringExtra("picUrl");
		}
	}

	private ListView listView;

	private void initViews() {
		listView = (ListView) findViewById(R.id.listView);
		ArrayList<String> list = new ArrayList<String>();
		list.add("分享给好友");
		list.add("分享到朋友圈");
		listView.setAdapter(new SharetoWeixinAdapter(
				ShareToWeixinActivity.this, list));

	}

	private void setListeners() {
		findViewById(R.id.share_layout).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.share_layout:
		case R.id.cancel:
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		switch (arg2) {
		case 0:
			shareToWeixin(true);
			break;
		case 1:
			shareToWeixin(false);
			break;
		default:
			break;
		}
	}

	// 微信
	private final String WEIXIN_APP_ID = "wxcfaa020ee248a2f2";
	private IWXAPI mWeixin;

	private void shareToWeixin(boolean toFriend) {

		sendWeixinMessage(toFriend);
	}

	/**
	 * 注册到微信
	 */
	private void regToWX() {

		mWeixin = WXAPIFactory.createWXAPI(this, WEIXIN_APP_ID, true);
		mWeixin.registerApp(WEIXIN_APP_ID);
		mWeixin.handleIntent(getIntent(), this);
	}

	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

	public boolean ISWXAppInstalled() {
		boolean b = mWeixin.isWXAppInstalled();
		return b;
	}

	Bitmap bitmap;

	/**
	 * 
	 * @param toFriend
	 *            分享到朋友圈还是给朋友
	 */
	private void sendWeixinMessage(boolean toFriend) {
		// mWeixin = WXAPIFactory.createWXAPI(this, WEIXIN_APP_ID, true);
		// mWeixin.registerApp(WEIXIN_APP_ID);
		// mWeixin.handleIntent(getIntent(), this);

		if (!ISWXAppInstalled()) {
			Toast.makeText(this, "没有微信客户端不能分享到微信!", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!toFriend) {
			if (mWeixin.getWXAppSupportAPI() < TIMELINE_SUPPORTED_VERSION) {
				Toast.makeText(this, "分享失败，只有微信4.2版本以上才支持分享到朋友圈哦",
						Toast.LENGTH_SHORT).show();
				return;
			}
		}
		WXVideoObject video = new WXVideoObject();
		video.videoUrl = ShareData.text;
		WXMediaMessage mediaMessage = new WXMediaMessage();
		mediaMessage.mediaObject = video;
		if (toFriend) {
			mediaMessage.title = "电视粉分享";
		} else {
			mediaMessage.title = titleName == null ? "电视粉分享" : titleName;
		}
		mediaMessage.description = titleName == null ? "电视粉分享" : titleName;

		Drawable d = getPicFile(picUrl);
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
		if (d != null) {
			try {
				bitmap = BitmapUtils.drawableToBitmap(d);
			} catch (Exception e) {
				bitmap = BitmapFactory.decodeResource(getResources(),
						R.drawable.icon);
			}
		} else {
			bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.icon);
		}
		mediaMessage.thumbData = ShareWeixinBitmapUtil.bmpToByteArray(
				ShareWeixinBitmapUtil.imageZoom(bitmap, 30.00), true);
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("video");
		req.message = mediaMessage;
		req.scene = toFriend ? SendMessageToWX.Req.WXSceneSession
				: SendMessageToWX.Req.WXSceneTimeline;
		if (mWeixin.sendReq(req)) {
			MobclickAgent.onEvent(getApplicationContext(), "weixin");
			if (!toFriend)
				Toast.makeText(this, "发送到微信朋友圈", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "发送到微信", Toast.LENGTH_SHORT).show();
			finish();
		} else {
			Toast.makeText(this, "分享失败，只有微信4.0版本以上才支持分享哦", Toast.LENGTH_SHORT)
					.show();
			finish();
		}
	}

	private String buildTransaction(final String type) {

		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}

	@Override
	public void onReq(BaseReq arg0) {
		Toast.makeText(getApplicationContext(), "开始发送", Toast.LENGTH_SHORT)
				.show();
		Log.e("@ShareToWeixinActivity", "开始发送");
	}

	@Override
	public void onResp(BaseResp arg0) {
		Toast.makeText(getApplicationContext(),
				arg0.errStr + "errcode=" + arg0.errCode, Toast.LENGTH_SHORT)
				.show();

		switch (arg0.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			Log.e("ShareToWeixinActivity", arg0.errStr + "errcode="
					+ arg0.errCode);
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			Log.e("ShareToWeixinActivity", arg0.errStr + "errcode="
					+ arg0.errCode);
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			Log.e("ShareToWeixinActivity", arg0.errStr + "errcode="
					+ arg0.errCode);
			break;
		default:
			Log.e("ShareToWeixinActivity", arg0.errStr + "errcode="
					+ arg0.errCode);
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.e("ShareToWeixinActivity", "onNewIntent");
		setIntent(intent);
		mWeixin.handleIntent(intent, this);
	}

	private Drawable getPicFile(String url) {
		Drawable d = BitmapUtils.getSdCardFromDrawable(url);
		return d;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
	}
}
