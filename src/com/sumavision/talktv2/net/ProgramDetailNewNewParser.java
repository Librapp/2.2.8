package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.ProgramAroundData;
import com.sumavision.talktv2.data.StarData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version v2.2
 * @createTime 2012-12-14
 * @description 节目详情解析类
 * @changeLog
 */
public class ProgramDetailNewNewParser {

	public String parse(String s, VodProgramData p) {
		JSONObject jAData = null;
		JSONObject content = null;
		String msg = "";
		int errCode = -1;

		try {
			jAData = new JSONObject(s);
			if (jAData.has("code")) {
				errCode = jAData.getInt("code");
			} else if (jAData.has("errcode")) {
				errCode = jAData.getInt("errcode");
			} else if (jAData.has("errorCode")) {
				errCode = jAData.getInt("errorCode");
			}
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				content = jAData.getJSONObject("content");
				// content.programDesc object 节目相关描述对象
				// content.programDesc.doubanPoint float 豆瓣评分
				// content.programDesc.stagerName string 主演名称
				// content.programDesc.contentTypeName string 内容类型名称
				// content.programDesc.intro string 节目介绍
				// content.starCount int 节目相关明星总数量
				// content.star[] array 节目相关名称数组
				// content.star[].id long 明星id
				// content.star[].name string 明星名称
				// content.star[].pic string 明星图片绝对路径
				// content.programAroundCount int 周边新闻总数量
				// content.programAround[] array 周边信息数组
				// content.programAround[].id long 周边信息id
				// content.programAround[].title string 周边信息标题
				// content.programAround[].pubDate string 周边信息发布日期
				// content.stagePhotoCount int 剧照总数量
				// content.stagePhoto[] array 剧照数组
				// content.stagePhoto[].pic string 剧照图片绝对路径
				JSONObject programDesc = content.getJSONObject("programDesc");
				p.point = programDesc.getString("doubanPoint");
				p.stagerName = programDesc.getString("stagerName");
				p.contentTypeName = programDesc.getString("contentTypeName");
				p.intro = programDesc.getString("intro");

				if (content.has("starCount") && content.getInt("starCount") > 0) {
					p.starCount = content.getInt("starCount");
					List<StarData> ls = new ArrayList<StarData>();
					JSONArray stars = content.getJSONArray("star");
					for (int i = 0; i < stars.length(); ++i) {
						JSONObject star = stars.getJSONObject(i);
						StarData ss = new StarData();
						ss.stagerID = star.getInt("id");
						ss.name = star.getString("name");
						ss.photoBig_V = star.getString("pic");
						ls.add(ss);
					}
					p.setStar(ls);
				}

				List<ProgramAroundData> lpa = new ArrayList<ProgramAroundData>();
				if (content.has("programAroundCount")
						&& content.getInt("programAroundCount") > 0) {
					p.aroundCount = content.getInt("programAroundCount");
					JSONArray arounds = content.getJSONArray("programAround");
					for (int i = 0; i < arounds.length(); i++) {
						JSONObject item = arounds.getJSONObject(i);
						ProgramAroundData pa = new ProgramAroundData();
						pa.id = item.getInt("id");
						pa.title = item.getString("title");
						pa.time = item.getString("pubDate");
						pa.url = item.getString("listPhoto");
						lpa.add(pa);
					}
				}
				p.setAround(lpa);
				String lp[] = null;
				if (content.has("stagePhotoCount")
						&& content.getInt("stagePhotoCount") > 0) {
					p.photoCount = content.getInt("stagePhotoCount");
					lp = new String[p.photoCount];
					JSONArray photos = content.getJSONArray("stagePhoto");
					for (int i = 0; i < photos.length(); i++) {
						JSONObject photo = photos.getJSONObject(i);
						lp[i] = photo.getString("pic");
					}
				}
				p.photos = lp;
			} else {
				msg = jAData.getString("msg");
			}
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		return msg;
	}
}
