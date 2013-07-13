package com.sumavision.tvfanmultiscreen.data;

import java.util.ArrayList;
import java.util.List;

import org.cybergarage.upnp.Action;

import com.sumavision.talktv2.data.DLNAGetPositionInfoData;

/**
 * 
 * @author 郭鹏
 * 
 */
public class DLNAData {
	private static DLNAData current;

	public org.cybergarage.upnp.Service CM;
	public org.cybergarage.upnp.Service AVT;
	public org.cybergarage.upnp.Service RCS;
	// RCS
	public Action getAllowedTransforms;
	public Action getVolume;
	public Action setVolume;
	public Action getMute;
	public Action setMute;
	// CM
	public Action getProtocolInfo;
	public Action prepareForConnection;
	public Action connectionComplete;
	// AVT
	public Action setTransportURL;
	public Action play;
	public Action pause;
	public Action stop;
	public Action seek;
	public Action getPositionInfo;
	// 连接ID
	public int AVTransportID = 0;
	// 设置过TranspotURI
	public boolean hasSetTransportURI = false;

	// 当前的SOAP Action头
	public String SOAP_Action_name;
	// 当前的Action消息体
	public String SOAP_Action_body;
	// 当前的Action全部头
	public String SOAP_Action_header;
	// 当前的Action全部头名字列表
	public List<String> SOAP_Action_header_names = new ArrayList<String>();
	// 当前的Action全部头内容列表
	public List<String> SOAP_Action_header_values = new ArrayList<String>();

	// 当前静音参数
	public String CurrentMute;
	// 当前静音参数
	public String CurrentVolume;
	// 当前TranportInfo参数,PLAYING or not
	public String CurrentTransportState;
	// 当前TranportInfo参数,OK or not
	public String CurrentTransportStatus;
	// 当前TranportInfo参数,当前速度，默认均为1
	public String CurrentSpeed;
	// GetPositionInfo
	public DLNAGetPositionInfoData data;
	// 当前DLNA get指令类型
	public int DLNA_get_type;
	// RCS
	public static final int GET_MUTE_TYPE = 1;
	public static final int GET_VOLUME_TYPE = 2;
	// AVT
	public static final int GET_POSITION_INFO_TYPE = 3;
	public static final int GET_TRANSPORT_INFO_TYPE = 4;
	public static final int GET_MEDIA_INFO_TYPE = 5;

	// 上次甩到电视上的节目
	public int prevProgramID;
	// 上次甩到电视上的节目得到子ID，如电视剧类型的子集，子集的集数
	public String prevProgramSubID;
	// 上次甩到电视上的节目的类型，0直播，1点播
	public String prevProgramType;
	// 上次甩到电视上的节目的名字
	public String prevProgramName;
	// 上次甩到电视后，返回的四叶草时保存的节目播放位置，毫秒值
	public int prevProgramPlayingPosition;
	// 上次甩到电视后，返回的四叶草时保存的系统毫秒数值
	public long prevProgramSystemMillions;
	// 是否已经甩过界面到电视
	public boolean hasPlayingOnTV = false;
	// 上次甩的是直播
	public boolean prevIsLivePlay = false;
	// 上次退出时机顶盒状态
	public boolean isPlayingOnTV = false;
	// 是否为子集播放
	public boolean isEpisodePlaying = false;
	// 甩之前的播放时间
	public String playedTimeText;

	// DLNA设备的FriendlyName
	public String friendlyName = "";

	// <!-----------------仅用于甩图到电视------------------------->
	// 图片上传成功
	public boolean hasPicAlready = false;
	// 图片已存在
	public boolean isExistPic = false;
	// ////////////////////////////////////////////////////////////////////////
	// 关闭Debug
	public boolean isCloseDebug = true;
	public boolean isStopAlready;
	public String prevProgramDuration;
	public int DLNA_post_type;
	public boolean isOnlyController = false;

	public String nowSTBPlayPosition = "23:59:59";
	// 当前播放时长
	public String nowSTBPlayDuration = "??:??:??";
	// 当前播放中的视频URL
	public String nowSTBPlayURL = "";

	// 节目名
	public String nowProgramName = "电视遥控器";
	// 节目播放地址
	public String nowProgramLiveAddress = "";
	// 当前播放的节目ID
	public int nowProgramId;
	// 当前播放的时长，字符串
	public String programDuration;
	// 当前播放的时长，毫秒数
	public int programDurationMicroSecond;
	// 当前播放的节目子集ID
	public int programSubIdNow;
	// 节目播放类型
	public int playType = 0;
	// 音量
	public int vol = 0;
	public static final int STOP = 0;
	public static final int PLAY = 1;
	public static final int PAUSE = 2;
	// 播放状态
	public int playState = STOP;

	public static int SOAP_Port = 0;
	// 当前链接的设备地址
	public static String SOAP_Address = "8.8.8.8";
	public static String SOAP_PREFIX = "/";
	// 搜完设备需要跳转播控
	public boolean needOpenRemoteController = false;

	public void initDlnaAction() {
		// DLNAData.current().AVT = DeviceData.getInstance().getSelectedDevice()
		// .getService("urn:schemas-upnp-org:service:AVTransport:1");
		// DLNAData.current().CM = DeviceData.getInstance().getSelectedDevice()
		// .getService("urn:schemas-upnp-org:service:ConnectionManager:1");
		// DLNAData.current().RCS = DeviceData.getInstance().getSelectedDevice()
		// .getService("urn:schemas-upnp-org:service:RenderingControl:1");

		setVolume = RCS.getAction("SetVolume");
		getVolume = RCS.getAction("GetVolume");
		setMute = RCS.getAction("SetMute");
		getMute = RCS.getAction("GetMute");

		setTransportURL = AVT.getAction("SetAVTransportURI");
		seek = DLNAData.current().AVT.getAction("Seek");
		stop = AVT.getAction("Stop");
		play = AVT.getAction("Play");
		pause = AVT.getAction("Pause");
		getPositionInfo = AVT.getAction("GetPositionInfo");

	}

	public Action getGetAllowedTransforms() {
		if (getAllowedTransforms == null) {
		}
		return getAllowedTransforms;
	}

	public void setGetAllowedTransforms(Action getAllowedTransforms) {
		this.getAllowedTransforms = getAllowedTransforms;
	}

	public Action getGetVolume() {
		if (getVolume == null) {
			getVolume = RCS.getAction("GetVolume");
		}
		return getVolume;
	}

	public void setGetVolume(Action getVolume) {
		this.getVolume = getVolume;
	}

	public Action getSetVolume() {
		if (setVolume == null) {
			setVolume = RCS.getAction("SetVolume");
		}
		return setVolume;
	}

	public void setSetVolume(Action setVolume) {
		this.setVolume = setVolume;
	}

	public Action getGetMute() {
		return getMute;
	}

	public void setGetMute(Action getMute) {
		this.getMute = getMute;
	}

	public Action getSetMute() {
		return setMute;
	}

	public void setSetMute(Action setMute) {
		this.setMute = setMute;
	}

	public Action getGetProtocolInfo() {
		return getProtocolInfo;
	}

	public void setGetProtocolInfo(Action getProtocolInfo) {
		this.getProtocolInfo = getProtocolInfo;
	}

	public Action getPrepareForConnection() {
		return prepareForConnection;
	}

	public void setPrepareForConnection(Action prepareForConnection) {
		this.prepareForConnection = prepareForConnection;
	}

	public Action getConnectionComplete() {
		return connectionComplete;
	}

	public void setConnectionComplete(Action connectionComplete) {
		this.connectionComplete = connectionComplete;
	}

	public Action getSetTransportURL() {
		if (setTransportURL == null) {
			setTransportURL = current().AVT.getAction("SetAVTransportURL");
		}
		return setTransportURL;
	}

	public void setSetTransportURL(Action setTransportURL) {
		this.setTransportURL = setTransportURL;
	}

	public Action getPlay() {
		if (play == null) {
			play = current().AVT.getAction("Play");
		}
		return play;
	}

	public void setPlay(Action play) {
		this.play = play;
	}

	public Action getPause() {
		if (pause == null) {
			pause = current().AVT.getAction("Pause");
		}
		return pause;
	}

	public void setPause(Action pause) {
		this.pause = pause;
	}

	public Action getStop() {
		if (stop == null) {
			stop = AVT.getAction("Stop");
		}
		return stop;
	}

	public void setStop(Action stop) {
		this.stop = stop;
	}

	public Action getSeek() {
		if (seek == null) {
			seek = AVT.getAction("Seek");
		}
		return seek;
	}

	public void setSeek(Action seek) {
		this.seek = seek;
	}

	public Action getGetPositionInfo() {
		return getPositionInfo;
	}

	public void setGetPositionInfo(Action getPositionInfo) {
		this.getPositionInfo = getPositionInfo;
	}

	public static DLNAData current() {
		if (current == null) {
			current = new DLNAData();
		}
		return current;
	}

}
