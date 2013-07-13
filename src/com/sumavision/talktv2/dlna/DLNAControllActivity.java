package com.sumavision.talktv2.dlna;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.util.EntityUtils;
import org.cybergarage.http.HTTPNet;
import org.cybergarage.http.HTTPNetListener;
import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.CtUpnpListener;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.UPnPStatus;
import org.cybergarage.upnp.control.ActionRequest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.VerticalSeekBar;
import com.sumavision.talktv2.dlna.common.DeviceData;
import com.sumavision.talktv2.dlna.common.DlNAConstants;
import com.sumavision.talktv2.dlna.task.DLNANetListener;
import com.sumavision.talktv2.dlna.task.SetTransportUrlTask;
import com.sumavision.talktv2.parser.AllUrlParser;
import com.sumavision.talktv2.parser.GetVolumeParser;
import com.sumavision.talktv2.utils.DialogUtil;
import com.sumavision.tvfanmultiscreen.data.DLNAData;

public class DLNAControllActivity extends Activity implements HTTPNetListener,
		OnClickListener, DLNANetListener {
	private Device device;

	private int isResume;
	public boolean isLivePlay = false;
	public CtUpnpListener cul;

	private boolean isSameProgram = true;
	private boolean isResumeSameProgram = false;
	private boolean isSameEpisodeProgram = false;

	private String deviceName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dlna_controll);
		getExtras();
		if (device != null) {
			if (device == null)
				return;
		}
		initViews();
		if (DLNAData.current().prevProgramSubID != null) {
			Log.e("onCreate", "是同一子集");

			isSameEpisodeProgram = true;
			isSameProgram = true;
			// 有节目甩到电视
			if (DLNAData.current().hasPlayingOnTV) {
				isResume = 1;
			}
		} else if (DLNAData.current().prevProgramSubID == null
				&& !DLNAData.current().hasPlayingOnTV) {
			isSameEpisodeProgram = true;
			isSameProgram = true;
		} else if (DLNAData.current().prevProgramSubID == null
				&& DLNAData.current().hasPlayingOnTV) {
			isSameEpisodeProgram = false;
			isSameProgram = false;
		}

		if (DLNAData.current().prevProgramID != 0
				&& DLNAData.current().prevProgramName
						.equals(DLNAData.current().nowProgramName)) {
			isSameEpisodeProgram = false;
			isSameProgram = true;
			isResume = 1;
		} else if (DLNAData.current().prevProgramID != 0
				&& !DLNAData.current().prevProgramName.equals(DLNAData
						.current().nowProgramName)) {
			isSameEpisodeProgram = false;
			isSameProgram = false;
		}

		// initServices();
		// TODO setPosition setPositionSeekMax();

		// TODO nullpoint exception getVolume();

	}

	// 传过来的播放地址
	private String playUrl;
	private String titleName;

	private void getExtras() {
		Intent intent = getIntent();
		// DeviceDataInSearchList deviceDataInSearchList =
		// (DeviceDataInSearchList) intent
		// .getSerializableExtra("selectedDevice");
		// device = getDeviceByShortData(deviceDataInSearchList);
		isResume = intent.getIntExtra("isResume", 0);
		isLivePlay = intent.getBooleanExtra("isLivePlay", false);
		playUrl = intent.getStringExtra("playAddress");
		titleName = intent.getStringExtra("titleName");
	}

	private Device getDeviceByShortData(DeviceDataInSearchList data) {
		Device[] list = DeviceData.getInstance().getDevices();
		for (int i = 0; i < list.length; i++) {
			Device device = list[i];
			if (data.name.equals(device.getFriendlyName())) {
				return device;
			}
		}
		return null;
	}

	// 界面按钮 文本
	private TextView currentTimeView, totalTimeView;
	private ImageButton playBtn, advanceBtn, goBackBtn;
	private VerticalSeekBar volSeekBar;
	private static final String namePre = "此节目正在\"";
	private static final String nameSuf = "\"上播放";

	private void initViews() {
		currentTimeView = (TextView) findViewById(R.id.currentTimeView);
		totalTimeView = (TextView) findViewById(R.id.totalTimeView);
		volSeekBar = (VerticalSeekBar) findViewById(R.id.vol);
		playBtn = (ImageButton) findViewById(R.id.tool_play);
		advanceBtn = (ImageButton) findViewById(R.id.tool_advance);
		goBackBtn = (ImageButton) findViewById(R.id.tool_back);
		playBtn.setOnClickListener(this);
		advanceBtn.setOnClickListener(this);
		goBackBtn.setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
		// if (device.getFriendlyName() != null) {
		// ((TextView) findViewById(R.id.device_name)).setText(namePre
		// + device.getFriendlyName() + nameSuf);
		// }
		if (titleName != null) {
			((TextView) findViewById(R.id.title)).setText(titleName);
		}
	}

	private org.cybergarage.upnp.Service CM, RCS;
	public static org.cybergarage.upnp.Service AVT;

	private void initServices() {

		if (DLNAData.current().AVT != null) {

			AVT = DLNAData.current().AVT;
		} else {
			AVT = DeviceData.getInstance().getSelectedDevice()
					.getService("urn:schemas-upnp-org:service:AVTransport:1");
			DLNAData.current().AVT = AVT;
		}

		if (DLNAData.current().CM != null) {
			CM = DLNAData.current().CM;

		} else {
			CM = DeviceData
					.getInstance()
					.getSelectedDevice()
					.getService(
							"urn:schemas-upnp-org:service:ConnectionManager:1");
		}

		if (DLNAData.current().RCS != null) {
			RCS = DLNAData.current().RCS;
		} else {
			RCS = DeviceData
					.getInstance()
					.getSelectedDevice()
					.getService(
							"urn:schemas-upnp-org:service:RenderingControl:1");
		}
		// DLNAData.current().initDlnaAction();
	}

	// get指令的请求地址
	private String getURL = null;
	// getz指令端口
	private int getPort = 0;

	private HTTPNet net;

	// 取得音量
	private void getVolume() {

		String letf_tringle = "<";
		String right_tringle = ">";

		DLNAData.current().SOAP_Action_body = letf_tringle
				+ "?xml version=\"1.0\" encoding=\"utf-8\"?"
				+ right_tringle
				+ letf_tringle
				+ "s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\""
				+ right_tringle
				+ letf_tringle
				+ "s:Body"
				+ right_tringle
				+ letf_tringle
				+ "u:GetVolume xmlns:u=\"urn:schemas-upnp-org:service:RenderingControl:1\""
				+ right_tringle + letf_tringle + "InstanceID" + right_tringle
				+ "0" + letf_tringle + "/InstanceID" + right_tringle
				+ letf_tringle + "Channel" + right_tringle + "Master"
				+ letf_tringle + "/Channel" + right_tringle + letf_tringle
				+ "/u:GetVolume" + right_tringle + letf_tringle + "/s:Body"
				+ right_tringle + letf_tringle + "/s:Envelope" + right_tringle;
		net = new HTTPNet();
		net.setListener(this);
		try {

			getURL = "http://" + DLNAData.current().SOAP_Address
					+ ":"
					+ DLNAData.current().SOAP_Port
					// + "/RenderingControl/control",
					+ AllUrlParser.checkDeviceURL(
							DLNAData.current().RCS.getControlURL(), false);
			getPort = DLNAData.current().SOAP_Port;
			DLNAData.current().DLNA_get_type = DLNAData.current().GET_VOLUME_TYPE;
			net.request(getURL, DLNAData.current().SOAP_Port,
					DLNAData.current().SOAP_Action_body);
		} catch (HttpHostConnectException e) {
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDLNAGetBegin() {

	}

	@Override
	public void onDLNAGetError(String arg0) {

	}

	@Override
	public void onDLNAGetResponse(StatusLine arg0, Header[] arg1,
			HttpEntity entit) {
		try {
			String s = EntityUtils.toString(entit);
			Log.e("ProgramDataPreViewActviity - 648", s);
			net.removeListener();

			switch (DLNAData.current().DLNA_get_type) {

			// getVolumeInfo
			case 2:
				try {
					GetVolumeParser
							.parse(new ByteArrayInputStream(s.getBytes()));

					handler.sendEmptyMessage(PROCESS_VOLUME);
					// 先取音量后，自动取静音状态
					// getMute();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private final int PROCESS_VOLUME = 20;
	private final int MSG_PROCESS_PLAY = 27;
	private final int MSG_DISMISS = 47;
	private final int SET_URI_OK = 15;
	private final int SET_URI_ERROR = 16;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {

			case PROCESS_VOLUME:
				if (!DLNAData.current().CurrentVolume.equals("")) {
					// TODO vol set

				}
				break;
			case MSG_PROCESS_PLAY:
				processPlayPause();
				break;
			case MSG_DISMISS:
				// TODO new
				break;

			case SET_URI_OK:
				setTranspotURI_Error = false;
				// stopBtn.setOnClickListener(RemoteControllerActivityNew.this);
				// title.setText(DLNAData.current().nowProgramName);
				// positionSeekBar.setMainProgress(0);
				//
				// playPause.setImageResource(R.drawable.dlna_play);
				playing = false;
				// textPlayed.setText("00:00:00");
				// currentPosition = 0;

				DLNAData.current().prevProgramID = DLNAData.current().nowProgramId;
				DLNAData.current().prevProgramName = DLNAData.current().nowProgramName;
				DLNAData.current().hasPlayingOnTV = true;
				DLNAData.current().hasSetTransportURI = true;
				DLNAData.current().prevProgramPlayingPosition = 0;

				play();
				break;
			case SET_URI_ERROR:
				setTranspotURI_Error = true;
				// Toast.makeText(getApplicationContext(), "初始化地址出错...",
				// Toast.LENGTH_SHORT).show();
				DialogUtil.alertToast(getApplicationContext(), "初始化失败");
				break;
			case PLAY_OK:
				isSameProgram = true;
				Toast.makeText(getApplicationContext(), "机顶盒开始播放",
						Toast.LENGTH_SHORT).show();
				playing = true;
				// if (ProgramData.current().isEpisodePlay) {
				// ProgramData.current().isEpisodePlayHolder = true;
				// }
				// positionSeekBar.setEnabled(true);
				DLNAData.current().hasPlayingOnTV = true;
				DLNAData.current().prevIsLivePlay = isLivePlay;
				// TODO getPositoninfo
				// if (!DeviceData.getInstance().getSelectedDevice()
				// .getFriendlyName()
				// .equals("Realtek Embedded UPnP Render()")) {
				//
				// if (isNeedSendGetPositionInfo) {
				//
				// if (t != null && !t.isAlive()) {
				// t.onPause();
				// t.interrupt();
				// t = null;
				// System.gc();
				// t = new ManualThread();
				// t.start();
				// }
				// }
				// }

				setTimer();

				if (DLNAData.current().hasPlayingOnTV) {
					handler.removeMessages(PROGRESS_REFRESH);
					handler.removeMessages(GET_VIDEO_POSITION);
					// currentPosition =
					// DLNAData.current().prevProgramPlayingPosition;
					// positionSeekBar.setMainProgress(currentPosition);
					if (DeviceData.getInstance().getSelectedDevice()
							.getFriendlyName().equals("MPO-V0398")) {
						handler.sendEmptyMessageDelayed(PROGRESS_REFRESH, 8000);
						handler.sendEmptyMessageDelayed(GET_VIDEO_POSITION,
								8000);
					} else {
						handler.sendEmptyMessageDelayed(PROGRESS_REFRESH, 3000);
						handler.sendEmptyMessageDelayed(GET_VIDEO_POSITION,
								3000);
					}
				}

				DLNAData.current().prevProgramDuration = DLNAData.current().nowSTBPlayPosition;
				// playPause.setImageResource(R.drawable.dlna_pause);
				// sendEmptyMessage(PROGRESS_REFRESH);

				// 纠正进度
				// currentPosition -= 10;
				// if (currentPosition < 0)
				// currentPosition = 0;

				// dismissWaitDialog();
				// 播放开始后取得播放状态
				// GetTransportInfo();
				break;
			case PLAY_ERROR:
				playing = false;
				// Toast.makeText(getApplicationContext(), "播放出错",
				// Toast.LENGTH_SHORT).show();
				// 暂停后后取得播放状态
				// GetTransportInfo();
				break;
			case ASYNC_PLAY:
				func = PLAY;
				firstDLNATask tp = new firstDLNATask();
				tp.execute(PLAY);
				break;
			default:
				break;
			}
		};
	};
	private boolean setTranspotURI_Error = true;
	private boolean changePlay = false;

	private void processPlayPause() {
		if (DLNAData.current().hasPlayingOnTV) {
			if (playing) {
				pause();
				handler.removeMessages(GET_VIDEO_POSITION);
				handler.removeMessages(PROGRESS_REFRESH);
			} else {
				if (!DLNAData.current().hasSetTransportURI) {
					changePlay = true;
					// func = SETTRANSPORTURL;
					// firstDLNATask t = new firstDLNATask();
					// t.execute(SETTRANSPORTURL);
					DLNAData.current().nowProgramLiveAddress = playUrl;
					executeSetTransportUrl(playUrl);
				} else
					playOnly();
			}
		} else {
			DLNAData.current().nowProgramLiveAddress = playUrl;
			// func = SETTRANSPORTURL;
			// firstDLNATask t = new firstDLNATask();
			// t.execute(SETTRANSPORTURL);
			executeSetTransportUrl(playUrl);
		}
	}

	private boolean playing = false;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tool_play:
			// handler.sendEmptyMessage(MSG_PROCESS_PLAY);
			processPlayPause();
			break;
		case R.id.back:
			finish();

		default:
			break;
		}
	}

	private int func = -1;
	public static final int SETTRANSPORTURL = 9;

	class firstDLNATask extends AsyncTask<Integer, Integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected String doInBackground(Integer... params) {
			switch (params[0]) {
			case SETTRANSPORTURL:
				try {
					setTransportURL();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				break;
			case PLAY:
				try {
					play();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			default:
				break;
			}
			return "执行完毕";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			handler.sendEmptyMessage(MSG_DISMISS);
			switch (func) {
			case SETTRANSPORTURL:
				handler.sendEmptyMessage(ASYNC_PLAY);
				break;

			default:
				break;
			}
		}

	}

	private Action setTransportURL;
	private Action play;
	private Action pause;
	private Action stop;
	private Action seek;
	private Action getPositionInfo;

	// 设置地址
	private void setTransportURL() {
		func = SETTRANSPORTURL;
		DLNAData.current().DLNA_post_type = SETTRANSPORTURL;

		setTransportURL = DLNAData.current().getSetTransportURL();

		ArgumentList sal = setTransportURL.getArgumentList();
		sal.getArgument("InstanceID").setValue("0");
		if (DeviceData.getInstance().getSelectedDevice().getFriendlyName()
				.equals("Realtek Embedded UPnP Render()")) {

			Log.e("CtMobileToTV",
					"setTransportURL-Realtek Embedded UPnP Render()");

			sal.getArgument("CurrentURIMetaData")
					.setValue(
							"<DIDL-Lite "
									+ "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" "
									+ "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
									+ "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" "
									+ "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\""
									+ ">"
									+ "<item><dc:title>"
									+ "video"
									+ "</dc:title><upnp:class>object.item.videoItem</upnp:class><res protocolInfo=\"http-get:*:video/mp4:*\"></res></item></DIDL-Lite>");
		} else if (DeviceData.getInstance().getSelectedDevice()
				.getFriendlyName().equals("DaPingMu(Q-1000DF)")) {
			sal.getArgument("CurrentURIMetaData").setValue("");
			Log.e("CtMobileToTV", "setTransportURL-DaPingMu(Q-1000DF)");
		} else {
			Log.e("CtMobileToTV", "setTransportURL-default");
			sal.getArgument("CurrentURIMetaData")
					.setValue(
							"<DIDL-Lite "
									+ "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" "
									+ "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
									+ "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" "
									+ "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\""
									+ ">"
									+ "<item>"
									+ "<dc:title>"
									+ "video"
									+ "</dc:title>"
									+ "<upnp:class>object.item.videoItem</upnp:class>"
									+ "<res protocolInfo=\"http-get:*:video/mp4:*\"></res>"
									+ "</item></DIDL-Lite>");
		}

		sal.getArgument("CurrentURI").setValue(
				DLNAData.current().nowProgramLiveAddress);
		setTransportURL.setInArgumentValues(sal);
		// if (UserNow.current().remote) {
		ActionRequest ctrlReq = new ActionRequest();
		// if (UserNow.current().remote) {
		// ctrlReq.setRequest(setTransportURL, sal);
		// // UserNow.current().SOAP_Address = "/AVTransport/control";
		// UserNow.current().SOAP_Address = AllUrlParser.checkDeviceURL(
		// DLNAData.current().AVT.getControlURL(), false);
		// remotePost(ctrlReq);
		// } else {
		if (setTransportURL.postControlAction()) {
			DLNAData.current().hasPlayingOnTV = true;
			// dismissWaitDialog();
			DLNAData.current().hasSetTransportURI = true;
			handler.sendEmptyMessage(SET_URI_OK);
		} else {
			// dismissWaitDialog();
			handler.sendEmptyMessage(SET_URI_ERROR);
			UPnPStatus err = setTransportURL.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
		}
		// }
		// }
		// }).start();

	}

	private final int PLAY = 110;
	private final int PAUSE = 111;
	private final int STOP = 112;
	private final int SEEK = 113;
	private final int STOP_ONLY = 115;

	private void play() {
		// showWaitDialog();
		func = PLAY;
		DLNAData.current().DLNA_post_type = PLAY;
		play = DLNAData.current().getPlay();

		ArgumentList pauseal = play.getArgumentList();
		pauseal.getArgument("InstanceID").setValue("0");
		pauseal.getArgument("Speed").setValue("1");

		play.setInArgumentValues(pauseal);
		if (play.postControlAction()) {
		} else {
			UPnPStatus err = play.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
			handler.sendEmptyMessage(PLAY_ERROR);

		}
	}

	private void pause() {
		// showWaitDialog();
		func = PAUSE;
		DLNAData.current().DLNA_post_type = PAUSE;
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		handler.sendEmptyMessage(PAUSE_START);
		// pause = AVT.getAction("Pause");
		pause = DLNAData.current().getPause();
		ArgumentList pauseal = pause.getArgumentList();
		pauseal.getArgument("InstanceID").setValue("0");

		pause.setInArgumentValues(pauseal);
		// if (UserNow.current().remote) {
		// ActionRequest acr = new ActionRequest();
		// acr.setRequest(pause, pauseal);
		// // UserNow.current().SOAP_Address = "/AVTransport/control";
		// UserNow.current().SOAP_Address = AllUrlParser.checkDeviceURL(
		// DLNAData.current().AVT.getControlURL(), false);
		// remotePost(acr);
		// } else {
		if (pause.postControlAction()) {
			Log.e(" pause()  ", "pause - OK");
			handler.sendEmptyMessage(PAUSE_OK);
			return;
		} else {

			// TODO dismissWaitDialog();
			UPnPStatus err = pause.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
			handler.sendEmptyMessage(PAUSE_ERROR);
		}
		// }
		// }
		// }).start();
	}

	// 已经甩过的播放
	private void playOnly() {
		func = PLAY_ONLY;
		DLNAData.current().DLNA_post_type = PLAY_ONLY;
		play = DLNAData.current().getPlay();

		ArgumentList pauseal = play.getArgumentList();
		pauseal.getArgument("InstanceID").setValue("0");
		pauseal.getArgument("Speed").setValue("1");

		play.setInArgumentValues(pauseal);
		// if (UserNow.current().remote) {
		// ActionRequest ctrlReq = new ActionRequest();
		// ctrlReq.setRequest(play, pauseal);
		// Log.e(" play-only()  ", "play-only() - OK");
		// // UserNow.current().SOAP_Address = "/AVTransport/control";
		// UserNow.current().SOAP_Address = AllUrlParser.checkDeviceURL(
		// DLNAData.current().AVT.getControlURL(), false);
		// remotePost(ctrlReq);
		// } else {
		if (play.postControlAction()) {
			// TODO dismissWaitDialog();
			handler.sendEmptyMessage(PLAY_OK);
			return;
		} else {

			// TODO dismissWaitDialog();
			UPnPStatus err = play.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
			handler.sendEmptyMessage(PLAY_ERROR);

		}
		// }
		// }
		// }).start();
	}

	private final int PROGRESS_REFRESH = 21;
	private final int PLAY_START = 2;
	private final int PLAY_OK = 3;
	private final int PLAY_ERROR = 4;
	private final int STOP_START = 5;
	private final int STOP_OK = 6;
	private final int STOP_ERROR = 7;
	private final int MUTE_START = 8;
	private final int MUTE_OK = 9;
	private final int MUTE_ERROR = 10;
	private final int SEEK_START = 11;
	private final int SEEK_OK = 12;
	private final int SEEK_ERROR = 13;
	private final int SET_URI_START = 14;
	private final int PAUSE_START = 17;
	private final int PAUSE_OK = 18;
	private final int PAUSE_ERROR = 19;
	private final int GET_VOLUME_ERROR = 21;
	private final int GET_MUTE_ERROR = 22;
	private final int SET_VOLUME_OK = 48;
	private final int SET_VOLUME_ERROR = 49;
	private final int GET_MUTE = 10;
	private final int GET_VOLUME = 11;
	private final int GET_POSITION_INFO = 13;
	private final int GET_VIDEO_POSITION = 23;
	private final int MSG_OPEN = 25;
	private final int MSG_CLOSE = 26;
	private final int GET_POSITION_INFO_OK = 29;
	private final int GET_POSITION_INFO_ERROR = 30;
	private final int SOCKET_TIMEOUT = 31;
	private final int PAUSE_GET_POSITON_INFO_THREAD = 32;
	private final int RESUME_GET_POSITON_INFO_THREAD = 33;
	private final int GET_TRRANSPORT_INFO_OK = 34;
	private final int GET_TRRANSPORT_INFO_ERR = 35;
	private final int GET_TRRANSPORT_INFO = 36;
	private final int SETMUTE_ERR = 37;
	private final int SET_PLAY_STATUS = 38;
	private final int SET_PAUSE_STATUS = 39;
	private final int PLAY_ONLY = 40;
	private final int ASYNC_SET_TRANS_URL = 43;
	private final int ASYNC_STOP = 44;
	private final int ASYNC_PLAY = 45;
	private final int MSG_CLOSE_ACTIVITY = 42;
	private final int MSG_SHOW = 46;

	private void setTimer() {
		setPositionSeekMax();
		handler.sendEmptyMessage(GET_VIDEO_POSITION);
	}

	private void setPositionSeekMax() {
		String[] my = DLNAData.current().nowSTBPlayPosition.split(":");

		int hour = Integer.parseInt(my[0]);
		int min = Integer.parseInt(my[1]);
		int sec = Integer.parseInt(my[2]);
		int totalSec = hour * 3600 + min * 60 + sec;
		totalSec = totalSec * 1000;
		// TODO new
		//
		// allDurationMillionSeconds = totalSec;
		// Log.e("setTimer - 369", "totalSec:" + totalSec);
		// positionSeekBar.setMaxProgress(totalSec);
		// positionSeekBar.setMainProgress(currentPosition);
	}

	private SetTransportUrlTask setTransportUrlTask;

	private void executeSetTransportUrl(String playUrl) {
		if (setTransportUrlTask == null) {
			setTransportUrlTask = new SetTransportUrlTask(this,
					DlNAConstants.setTransportUrl);
			setTransportUrlTask.execute(playUrl);
		}
	}

	@Override
	public void onNetEnd(int method, boolean isSuccess) {
		switch (method) {
		case DlNAConstants.setTransportUrl:
			hideTipsDialog();
			if (isSuccess) {
				handler.sendEmptyMessage(ASYNC_PLAY);
			} else {
				DialogUtil.alertToast(getApplicationContext(), "初始化播放失败!");
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onNetStart(int method) {
		switch (method) {
		case DlNAConstants.setTransportUrl:
			showTipsDialog();
			break;
		default:
			break;
		}
	}

	private ProgressDialog tipsDialog;

	private void showTipsDialog() {
		if (tipsDialog != null) {
			tipsDialog.dismiss();
			tipsDialog = null;
		}

		tipsDialog = ProgressDialog.show(this, "", "处理中，请稍后...", true, true,
				new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
					}
				});
	}

	private void hideTipsDialog() {
		if (tipsDialog != null) {
			tipsDialog.dismiss();
			tipsDialog = null;
		}
	}

}
