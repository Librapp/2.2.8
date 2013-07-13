package com.sumavision.talktv2.dao;

/**
 * 
 * @author 郭鹏
 *
 */
public interface LoadCustomDataListener {

	public void onLoadDBStart();

	public void onLoadDBOver(String errorMessage);
}
