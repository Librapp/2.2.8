package com.sumavision.talktv2.net;

public interface NetConnectionListener {
	public void onNetBegin(String method);

	public void onNetEnd(String msg, String method);

	public void onNetEnd(String msg, String method, int type);

	public void onCancel(String method);
}
