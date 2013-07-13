package com.sumavision.talktv2.utils;

public class ShareUtil {
	public void ShareTxtToWeixin() {
		// Intent intent = new Intent();
		// intent.setAction(Intent.ACTION_SEND);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.setType("text/*");
		// PackageManager pm = this.getPackageManager();
		// List<ResolveInfo> lr = pm.queryIntentActivities(intent,
		// PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
		// for (ResolveInfo r : lr) {
		// Log.e("能分享文字的App", r.activityInfo.name);
		// }
		// // 要发送的内容
		// intent.putExtra(Intent.EXTRA_TEXT, JSONMessageType.SHARE_MESSAGE);
		// intent.setClassName("com.tencent.mm",
		// "com.tencent.mm.ui.tools.ShareImgUI");
		// this.startActivity(intent);
	}

	public void ShareTxtToWeibo() {
		// Intent iSina = new Intent();
		// iSina.setAction(Intent.ACTION_SEND);
		// iSina.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// iSina.setType("text/*");
		// // 要发送的内容
		// iSina.putExtra(Intent.EXTRA_TEXT, JSONMessageType.SHARE_MESSAGE);
		// iSina.putExtra(Intent.EXTRA_STREAM, "最好是图片链接");
		// iSina.setClassName("com.sina.weibo",
		// "com.sina.weibo.EditActivity");
		// try{
		// this.startActivity(iSina);
		// }catch (Exception e) {
		// // TODO: handle exception
		// }
	}

}
