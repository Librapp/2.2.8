package com.sumavision.talktv2.net.parserUtils;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.sumavision.talktv2.data.BadgeData;

/**
 * 
 * @author 李梦思
 * @version 1.0
 * @createTime 2013-2-17
 * @description 回复列表解析类
 * 
 */
public class BadgeDataListParser extends JSONArrayParser {

	public String parser(JSONArray array, List<BadgeData> lc) {

		try {
			for (int i = 0; i < array.length(); i++) {
				BadgeData c = new BadgeData();

				item = array.getJSONObject(i);
				c.name = item.getString("name");
				c.picPath = item.getString("pic");
				lc.add(c);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			msg = "解析回复列表出错";
		}
		return msg;
	}
}
