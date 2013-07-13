package com.sumavision.talktv2.net;

public interface NetConnectionListenerNew {
	public void onNetBegin(String method, boolean isLoadMore);

	public void onNetEnd(String msg, String method);

	public void onNetEnd(String msg, String method, int type);

	public void onCancel(String method);

	/**
	 * 
	 * @param code
	 *            返回标志 包括 封装数据错误， 网络状况不好，解析错误，返回错误，网络不流畅
	 * @param msg
	 * @param method
	 */
	public void onNetEnd(int code, String msg, String method, boolean isLoadMore);
}
