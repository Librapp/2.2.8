package com.sumavision.talktv2.net;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sumavision.talktv2.data.ChannelNewData;
import com.sumavision.talktv2.data.CpData;
import com.sumavision.talktv2.data.NetPlayData;
import com.sumavision.talktv2.data.ParentVideoData;
import com.sumavision.talktv2.data.VideoData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.user.UserNow;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2012-12-10
 * @description 节目视频列表解析类
 * @changeLog
 */
public class ProgramVideoListParser {

	public String parse(String s, VodProgramData p) {
		JSONObject jAData = null;
		String msg = "";
		int errCode = -1;

		try {
			jAData = new JSONObject(s);
			if (jAData.has("code")) {
				errCode = jAData.getInt("code");
			} else if (jAData.has("errcode")) {
				errCode = jAData.getInt("errcode");
			}
			if (jAData.has("jsession")) {
				UserNow.current().jsession = jAData.getString("jsession");
			}
			if (errCode == JSONMessageType.SERVER_CODE_OK) {
				JSONObject content = jAData.getJSONObject("content");
				p.showPattern = content.getInt("showPattern");
				p.showSeason = content.getInt("showSeason");
				List<ParentVideoData> lp = new ArrayList<ParentVideoData>();
				JSONArray parentVideos = content.optJSONArray("parentVideo");
				if ((parentVideos != null) && (parentVideos.length() > 0))
					for (int j = 0; j < parentVideos.length(); ++j) {
						JSONObject parentVideo = parentVideos.getJSONObject(j);
						JSONArray videos = parentVideo.getJSONArray("video");
						ArrayList<VideoData> lv = new ArrayList<VideoData>();
						for (int i = 0; i < videos.length(); ++i) {
							JSONObject video = videos.getJSONObject(i);
							VideoData v = new VideoData();
							// content.video[].set string 视频期刊（集数）
							// content.video[].name string 视频名称
							// content.video[].isNew int 是否为NEW：0=不是，1=是
							// content.video[].playType int 播放方式：1=直接播放，2=网页播放
							// content.video[].playUrl string
							// 播放链接：直接播放时为直接播放链接，网页播放时为网址。
							// content.channelCount int 当天当前服务器时间之后有播放该节目的频道总数量
							// content.channel[] array 播放节目的频道列表
							// content.channel[].name string 频道名称
							// content.channel[].cpName string 在播节目单名称
							// content.channel[].startTime string 播放开始时间
							// content.channel[].endTime string 播放结束时间
							// content.channel[].playing int 1=正在播出，2=将要播出
							// content.channel[].cpid long 节目单id，预约时需要用
							// content. channel[].playType int
							// 播放方式：1=直接播放，2=网页播放
							// content. channel[].playUrl string
							// 播放链接：直接播放时为直接播放链接，网页播放时为网址。
							v.name = video.getString("name");
							v.isNew = video.getInt("isNew");
							v.playType = video.getInt("playType");
							v.url = video.getString("playUrl");
							v.fromString = video.optString("platformName");
							if (video.has("play")) {
								JSONArray play = video.getJSONArray("play");
								v.netPlayDatas = new ArrayList<NetPlayData>();
								for (int x = 0; x < play.length(); x++) {
									NetPlayData netPlayData = new NetPlayData();
									JSONObject playItem = play.getJSONObject(x);
									netPlayData.name = playItem
											.optString("name");
									netPlayData.pic = playItem.optString("pic");
									netPlayData.url = playItem.optString("url");
									netPlayData.videoPath = playItem
											.optString("videoPath");
									v.netPlayDatas.add(netPlayData);
								}
							}
							lv.add(v);
						}
						ParentVideoData tempP = new ParentVideoData();
						tempP.setVideos(lv);
						lp.add(tempP);
					}
				p.setVideo(lp);
				JSONArray channels = content.getJSONArray("channel");
				List<ChannelNewData> lc = new ArrayList<ChannelNewData>();
				for (int i = 0; i < channels.length(); ++i) {
					ChannelNewData c = new ChannelNewData();
					JSONObject channel = channels.getJSONObject(i);
					c.name = channel.getString("name");
					CpData cp = new CpData();
					cp.id = channel.getInt("cpid");
					cp.name = channel.getString("cpName");
					cp.startTime = channel.getString("startTime");
					cp.endTime = channel.getString("endTime");
					cp.isPlaying = channel.getInt("playing");
					cp.playType = channel.getInt("playType");
					cp.playUrl = channel.getString("playUrl");
					cp.order = channel.getInt("isRemind");
					if (channel.has("play")) {
						JSONArray play = channel.getJSONArray("play");
						c.netPlayDatas = new ArrayList<NetPlayData>();
						for (int x = 0; x < play.length(); x++) {
							NetPlayData netPlayData = new NetPlayData();
							JSONObject playItem = play.getJSONObject(x);
							netPlayData.name = playItem.optString("name");
							netPlayData.pic = playItem.optString("pic");
							netPlayData.url = playItem.optString("url");
							netPlayData.videoPath = playItem
									.optString("videoPath");
							c.netPlayDatas.add(netPlayData);
						}
					}
					c.now = cp;
					lc.add(c);
				}
				p.setChannel(lc);
			} else
				msg = jAData.getString("msg");
		} catch (JSONException e) {
			msg = JSONMessageType.SERVER_NETFAIL;
			e.printStackTrace();
		}
		return msg;
	}
}
