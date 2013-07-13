package com.sumavision.talktv2.dlna.task;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ArgumentList;

import android.os.AsyncTask;
import android.util.Log;

import com.sumavision.talktv2.dlna.DLNAControllActivity;
import com.sumavision.talktv2.dlna.common.DeviceData;
import com.sumavision.talktv2.dlna.common.DlNAConstants;
import com.sumavision.tvfanmultiscreen.data.DLNAData;

public class SetTransportUrlTask extends AsyncTask<Object, Integer, Integer> {
	private static final int method = DlNAConstants.setTransportUrl;
	private DLNANetListener listener;

	public SetTransportUrlTask(DLNANetListener listener, int method) {
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
		DLNAData.current().DLNA_post_type = DLNAControllActivity.SETTRANSPORTURL;
		Action setTransportURL = DLNAData.current().getSetTransportURL();
		ArgumentList sal = setTransportURL.getArgumentList();
		sal.getArgument("InstanceID").setValue("0");
		if (DeviceData.getInstance().getSelectedDevice().getFriendlyName()
				.equals("Realtek Embedded UPnP Render()")) {

			sal.getArgument("CurrentURIMetaData")
					.setValue(
							"<DIDL-Lite "
									+ "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" "
									+ "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
									+ "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" "
									+ "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\""
									+ ">"
									+ "<item><dc:title>"
									+ "video"
									+ "</dc:title><upnp:class>object.item.videoItem</upnp:class><res protocolInfo=\"http-get:*:video/mp4:*\"></res></item></DIDL-Lite>");
		} else if (DeviceData.getInstance().getSelectedDevice()
				.getFriendlyName().equals("DaPingMu(Q-1000DF)")) {
			sal.getArgument("CurrentURIMetaData").setValue("");
		} else {
			Log.e("CtMobileToTV", "setTransportURL-default");
			sal.getArgument("CurrentURIMetaData")
					.setValue(
							"<DIDL-Lite "
									+ "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" "
									+ "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
									+ "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" "
									+ "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\""
									+ ">"
									+ "<item>"
									+ "<dc:title>"
									+ "video"
									+ "</dc:title>"
									+ "<upnp:class>object.item.videoItem</upnp:class>"
									+ "<res protocolInfo=\"http-get:*:video/mp4:*\"></res>"
									+ "</item></DIDL-Lite>");
		}

		sal.getArgument("CurrentURI").setValue(
				DLNAData.current().nowProgramLiveAddress);
		setTransportURL.setInArgumentValues(sal);
		int result = 0;
		if (setTransportURL.postControlAction()) {
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
