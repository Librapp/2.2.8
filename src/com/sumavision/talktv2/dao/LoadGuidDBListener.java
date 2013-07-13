package com.sumavision.talktv2.dao;
/**
 * 
 * @author 郭鹏
 *
 */
public interface LoadGuidDBListener {

	public void onLoadDBStart();

	public void onLoadDBOver(String errorMessage);
}
