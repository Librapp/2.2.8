package com.sumavision.talktv2.net;

import org.json.JSONObject;

/**
 * 
 * @author 李梦思
 * @version 1.0
 * @createTime 2013-1-16
 * @description JSON请求组装抽象类
 * 
 */
public abstract class JSONRequest {
	public String method;
	int first = 0;
	int count = 10;
	JSONObject holder = new JSONObject();

	public abstract String make();
}
