package com.sumavision.talktv2.dlna.task;

public interface DLNANetListener {

	public void onNetEnd(int method, boolean isSuccess);

	public void onNetStart(int method);
}
