package com.sumavision.talktv2.dlna.common;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.control.ActionRequest;

import android.util.Log;

import com.sumavision.tvfanmultiscreen.data.DLNAData;

public class DLNASetTransportURl {
	public static DLNAApiListener listener;
	public static boolean OK = true;
	public static boolean ERROR = false;
	public static boolean TYPE;

	public static void setListener(DLNAApiListener l) {
		listener = l;
	}

	public static void removeListener() {
		listener = null;
	}

	public static void doSetTransportUrl() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				listener.onDLNAApiBegin();

				Action setTransportURL = DLNAData.current()
						.getSetTransportURL();
				ArgumentList sal = setTransportURL.getArgumentList();
				sal.getArgument("InstanceID").setValue("0");
				sal.getArgument("CurrentURIMetaData").setValue(
						"<DIDL-Lite><item><res protocolInfo=\"http-get:*:video/mp4:*\"></res>"
								+ "</item></DIDL-Lite>0");
				sal.getArgument("CurrentURI").setValue(
						DLNAData.current().nowProgramLiveAddress);
				Log.e("setTransportURL",
						DLNAData.current().nowProgramLiveAddress);
				setTransportURL.setInArgumentValues(sal);
				// if (CtUserInfo.remote) {
				ActionRequest ctrlReq = new ActionRequest();
				ctrlReq.setRequest(setTransportURL,
						setTransportURL.getInputArgumentList());

				if (setTransportURL.postControlAction()) {
					TYPE = true;
					listener.onDLNAApiResponse(null);
				} else {
					TYPE = false;
					listener.onDLNAApiResponse("初始化地址出错");
				}

			}
		}).start();
	}
}
