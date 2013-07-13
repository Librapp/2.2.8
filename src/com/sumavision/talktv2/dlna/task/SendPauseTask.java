package com.sumavision.talktv2.dlna.task;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ArgumentList;

import android.os.AsyncTask;

import com.sumavision.talktv2.dlna.common.DlNAConstants;
import com.sumavision.tvfanmultiscreen.data.DLNAData;

public class SendPauseTask extends AsyncTask<Object, Integer, Integer> {
	private static final int method = DlNAConstants.pause;
	private DLNANetListener listener;

	public SendPauseTask(DLNANetListener listener, int method) {
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
		Action pause = DLNAData.current().getPause();

		ArgumentList pauseal = pause.getArgumentList();
		pauseal.getArgument("InstanceID").setValue("0");
		pause.setInArgumentValues(pauseal);
		int result = 0;
		if (pause.postControlAction()) {
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
