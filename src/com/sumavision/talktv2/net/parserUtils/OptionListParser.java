package com.sumavision.talktv2.net.parserUtils;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.sumavision.talktv2.data.OptionData;

/**
 * 
 * @author 李梦思
 * @version 1.0
 * @createTime 2013-2-22
 * @description 选项列表解析类
 * 
 */
public class OptionListParser extends JSONArrayParser {

	public String parser(JSONArray array, List<OptionData> lc) {

		try {
			for (int i = 0; i < array.length(); i++) {
				OptionData c = new OptionData();

				item = array.getJSONObject(i);
				c.id = item.getLong("optionId");
				c.content = item.getString("content");
				c.countUser = item.getInt("countUser");
				c.isChosed = item.optInt("isGuessed") + item.optInt("isVoted");
				c.type = item.optInt("type");
				lc.add(c);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			msg = "解析回复列表出错";
		}
		return msg;
	}
}
