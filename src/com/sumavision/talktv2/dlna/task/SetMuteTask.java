package com.sumavision.talktv2.dlna.task;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ArgumentList;

import android.os.AsyncTask;

import com.sumavision.talktv2.dlna.common.DlNAConstants;
import com.sumavision.tvfanmultiscreen.data.DLNAData;

public class SetMuteTask extends AsyncTask<Object, Integer, Integer> {
	private static final int method = DlNAConstants.mute;
	private DLNANetListener listener;

	public SetMuteTask(DLNANetListener listener, int method) {
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

		Action setMute = DLNAData.current().getSetMute();
		ArgumentList setal = setMute.getArgumentList();
		// 暂时一直为0
		setal.getArgument("InstanceID").setValue(0);
		setal.getArgument("Channel").setValue("Master");

		int mute = (Integer) params[0];
		setal.getArgument("DesiredMute").setValue(mute);
		setMute.setInArgumentValues(setal);
		int result = 0;
		if (setMute.postControlAction()) {
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
