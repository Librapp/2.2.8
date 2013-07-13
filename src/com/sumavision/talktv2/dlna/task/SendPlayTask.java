package com.sumavision.talktv2.dlna.task;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ArgumentList;

import android.os.AsyncTask;

import com.sumavision.talktv2.dlna.common.DlNAConstants;
import com.sumavision.tvfanmultiscreen.data.DLNAData;

public class SendPlayTask extends AsyncTask<Object, Integer, Integer> {
	private static final int method = DlNAConstants.play;
	private DLNANetListener listener;

	public SendPlayTask(DLNANetListener listener, int method) {
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (listener != null)
			listener.onNetStart(method);
	}

	@Override
	protected Integer doInBackground(Object... params) {
		// DLNAData.current().DLNA_post_type = PLAY;
		Action play = DLNAData.current().getPlay();

		ArgumentList pauseal = play.getArgumentList();
		pauseal.getArgument("InstanceID").setValue("0");
		pauseal.getArgument("Speed").setValue("1");
		play.setInArgumentValues(pauseal);
		int result = 0;
		if (play.postControlAction()) {
			DLNAData.current().hasPlayingOnTV = true;
			DLNAData.current().hasSetTransportURI = true;
			result = 1;
		} else {
			result = 0;
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		boolean isSucces = result == 1 ? true : false;
		if (listener != null)
			listener.onNetEnd(method, isSucces);
	}
}
