package com.sumavision.talktv2.dlna.task;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ArgumentList;

import android.os.AsyncTask;

import com.sumavision.talktv2.dlna.common.DlNAConstants;
import com.sumavision.tvfanmultiscreen.data.DLNAData;

public class SendSeekTask extends AsyncTask<Object, Integer, Integer> {
	private static final int method = DlNAConstants.seek;
	private DLNANetListener listener;

	public SendSeekTask(DLNANetListener listener, int method) {
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
		String seekTime = (String) params[0];
		Action seek = DLNAData.current().getSeek();
		ArgumentList seekal = seek.getArgumentList();
		seekal.getArgument("Unit").setValue("REL_TIME");
		seekal.getArgument("Target").setValue(seekTime);
		seekal.getArgument("InstanceID").setValue("0");
		seek.setInArgumentValues(seekal);
		int result = 0;
		if (seek.postControlAction()) {
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
