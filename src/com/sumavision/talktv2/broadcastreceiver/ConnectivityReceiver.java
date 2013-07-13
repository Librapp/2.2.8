package com.sumavision.talktv2.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;

public class ConnectivityReceiver extends BroadcastReceiver {

	public static final String netAction = "android.net.conn.CONNECTIVITY_CHANGE";
	public static final String startAction = "com.sumavison.app_boot_start";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action != null
				&& (action.equals(netAction) || action.equals(startAction))) {
			ConnectivityManager connManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = connManager.getActiveNetworkInfo();
			if (null != netInfo
					&& ConnectivityManager.TYPE_WIFI == netInfo.getType()) {
				DialogUtil.alertToast(context, Constants.environment_net_wifi);
			} else {
				DialogUtil.alertToast(context,
						Constants.environment_net_not_wifi);
			}
		}
	}
}
