package com.sumavision.talktv2.dlna.task;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ArgumentList;

import android.os.AsyncTask;

import com.sumavision.talktv2.dlna.common.DlNAConstants;
import com.sumavision.tvfanmultiscreen.data.DLNAData;

public class SendStopPlayTask extends AsyncTask<Object, Integer, Integer> {
	private static final int method = DlNAConstants.stopPlay;
	private DLNANetListener listener;

	public SendStopPlayTask(DLNANetListener listener, int method) {
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
		Action stop = DLNAData.current().getStop();

		ArgumentList stopal = stop.getArgumentList();
		stopal.getArgument("InstanceID").setValue("0");
		stop.setInArgumentValues(stopal);
		int result = 0;
		if (stop.postControlAction()) {
			DLNAData.current().hasPlayingOnTV = false;
			DLNAData.current().hasSetTransportURI = false;
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
