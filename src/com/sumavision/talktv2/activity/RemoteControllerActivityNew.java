package com.sumavision.talktv2.activity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.CtUpnpListener;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.UPnPStatus;
import org.cybergarage.upnp.control.ActionRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.components.CircleProgress;
import com.sumavision.talktv2.components.CircleProgress.UpCircleCallBack;
import com.sumavision.talktv2.components.CircleProgressDown;
import com.sumavision.talktv2.components.CircleProgressDown.DownCircleCallBack;
import com.sumavision.talktv2.dlna.DeviceDataInSearchList;
import com.sumavision.talktv2.dlna.common.DeviceData;
import com.sumavision.talktv2.parser.AllUrlParser;
import com.sumavision.talktv2.parser.GetMutParser;
import com.sumavision.talktv2.parser.GetPositonInfoParser;
import com.sumavision.talktv2.parser.GetTransportInfoParser;
import com.sumavision.talktv2.parser.GetVolumeParser;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.tvfanmultiscreen.data.DLNAData;

/**
 * 
 * @author jianghao
 * @createTime 2012-5-22
 * @description 甩屏到电视
 * @changeLog DLNA Stack setAction over by 郭鹏 at 2012-6-22
 */
public class RemoteControllerActivityNew extends Activity implements
		CtUpnpListener, OnClickListener,
		// OnSeekBarChangeListener,
		HTTPNetListener
// ,NetServerListener
{

	// 直播时进度无法控制
	public boolean isLivePlay = false;
	public static CtUpnpListener cul;
	private Button back;
	private TextView title;
	// private TextView volumeRate;
	// private TextView selectedTime;
	// private Button back2pd;
	// 播放控制
	private ImageButton playPause;
	private ImageButton stopBtn;
	// 声音控制按钮
	private Button soundControl;
	// private CtUpnpSender upnp;
	private org.cybergarage.upnp.Service CM;
	public static org.cybergarage.upnp.Service AVT;
	private org.cybergarage.upnp.Service RCS;
	// RCS
	private Action getAllowedTransforms;
	private Action getVolume;
	private Action setVolume;
	private Action getMute;
	private Action setMute;
	// CM
	private Action getProtocolInfo;
	private Action prepareForConnection;
	private Action connectionComplete;
	// AVT
	private Action setTransportURL;
	private Action play;
	private Action pause;
	private Action stop;
	private Action seek;
	private Action getPositionInfo;

	private int func = -1;
	private final int GETALLOWEDTRANSFORMS = 1;
	private final int GETVOLUME = 2;
	private final int SETVOLUME = 3;
	private final int GETMUTE = 4;
	private final int SETMUTE = 5;
	private final int GETPROTOCOLINFO = 6;
	private final int PREPAREFORCONNECTION = 7;
	private final int CONNECTIONCOMPLETE = 8;
	private final int SETTRANSPORTURL = 9;
	private final int PLAY = 110;
	private final int PAUSE = 111;
	private final int STOP = 112;
	private final int SEEK = 113;
	private final int STOP_ONLY = 115;
	private final int GETPOSITIONINFO = 114;
	// 连接ID
	private int connectionId;
	private int AVTransportID = 1234;
	// 正在播放
	private boolean playing = false;
	// 地址设置出错
	private boolean setTranspotURI_Error = true;
	// 当前音量
	private int volume;
	// 是否静音，0为静音，1静音
	private int mute = 0;
	private int minimum;
	private int maxmum;
	private Vibrator vb;
	// private TextView programName;
	// seek时间
	private String seekTime;
	// 声音控制条
	private CircleProgressDown volSeekBar;
	// 控制条
	private CircleProgress positionSeekBar;
	// 已播放时间
	private TextView textPlayed;
	// 总时间
	private TextView textDuration;
	private ProgressDialog progressDialog;

	private String errorMessage;
	private final int MESSAGE_ERROR = 1;
	// 播放控制消息
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
	private final int SET_URI_OK = 15;
	private final int SET_URI_ERROR = 16;
	private final int PAUSE_START = 17;
	private final int PAUSE_OK = 18;
	private final int PAUSE_ERROR = 19;
	private final int PROCESS_VOLUME = 20;
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
	private final int MSG_PROCESS_PLAY = 27;
	private final int START_SHARE = 28;
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
	private final int MSG_DISMISS = 47;

	private final static int PROGRESS_CHANGED = 0;
	private int type = -1;
	private int minute;
	private int hour;
	private int second;
	// 当前视频位置
	private int currentPosition = 0;
	// DLNA get类型
	private int DLNAGetType;
	private long currentTimeInMillis;
	private final Timer timer = new Timer();
	private TimerTask task;
	private int allDurationMillionSeconds;

	// 是否为从其他界面恢复播放
	private int isResume;
	private HTTPNet net;
	// private HTTPNetURG netUrg;
	private String timeLine;
	private int actionType;
	private Button Share;
	private Button fav;
	private static final int ACTION_ADD_FAVORITE = 3;
	private ManualThread t;
	private boolean changePlay = false;
	private final int GET_POSITION_INFO_DELAY = 5000;
	private boolean DLNA_Api_lock = false;
	// 进入时记录，与上次甩到电视的是否为同一个节目
	private boolean isSameProgram = true;
	private boolean isResumeSameProgram = false;
	// 是否需要发送getPositonInfo指令
	private boolean isNeedSendGetPositionInfo = false;
	// private RelativeLayout all;
	private boolean hasVolumeAlready = false;

	private RelativeLayout all;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SHOW:
				try {
					showWaitDialog();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case MSG_DISMISS:
				dismissWaitDialog();
				break;
			case ASYNC_SET_TRANS_URL:
				func = SETTRANSPORTURL;
				firstDLNATask tt = new firstDLNATask();
				tt.execute(SETTRANSPORTURL);
				break;
			case ASYNC_STOP:
				func = STOP;
				firstDLNATask ts = new firstDLNATask();
				ts.execute(STOP);
				break;
			case ASYNC_PLAY:
				func = PLAY;
				firstDLNATask tp = new firstDLNATask();
				tp.execute(PLAY);
				break;
			case MSG_CLOSE_ACTIVITY:
				finish();
				break;
			case PLAY_ONLY:
				Toast.makeText(getApplicationContext(), "机顶盒开始播放",
						Toast.LENGTH_SHORT).show();
				playing = true;
				isResumeSameProgram = true;
				DLNAData.current().prevProgramDuration = DLNAData.current().nowSTBPlayPosition;

				positionSeekBar.setEnabled(true);
				dismissWaitDialog();
				playing = true;
				handler.sendEmptyMessage(PROGRESS_REFRESH);
				handler.sendEmptyMessage(GET_VIDEO_POSITION);
				playPause.setImageResource(R.drawable.dlna_pause);
				break;
			case SET_PLAY_STATUS:
				playing = true;
				setTimer();
				positionSeekBar.setMainProgress(currentPosition);

				Log.e(" play()  ", "play - OK");

				playPause.setImageResource(R.drawable.dlna_pause);
				sendEmptyMessage(PROGRESS_REFRESH);
				handler.sendEmptyMessage(GET_VIDEO_POSITION);
				break;
			case SET_PAUSE_STATUS:
				playing = false;
				playPause.setImageResource(R.drawable.dlna_play);
				break;
			case SETMUTE_ERR:
				Toast.makeText(getApplicationContext(), "机顶盒通信超时",
						Toast.LENGTH_SHORT).show();
				break;
			case GET_TRRANSPORT_INFO:

				break;
			case GET_TRRANSPORT_INFO_OK:
				if (DLNAData.current().CurrentTransportState.equals("PLAYING")) {
					sendEmptyMessage(SET_PLAY_STATUS);
				} else if (DLNAData.current().CurrentTransportState
						.equals("PAUSE_PLAYBACK")) {
					sendEmptyMessage(SET_PAUSE_STATUS);
				} else if (DLNAData.current().CurrentTransportState
						.equals("STOPPED")) {

				} else if (DLNAData.current().CurrentTransportState
						.equals("TRANSITIONING")) {

				} else {

				}
				break;
			case GET_TRRANSPORT_INFO_ERR:

				break;
			case PAUSE_GET_POSITON_INFO_THREAD:
				if (isNeedSendGetPositionInfo) {
					if (t != null && t.isAlive()) {
						t.onPause();
						t.interrupt();
						t = null;
						System.gc();
					}
				}
				break;
			case RESUME_GET_POSITON_INFO_THREAD:
				if (isNeedSendGetPositionInfo) {
					if (t != null && t.isAlive()) {
						t.onPause();
						t.interrupt();
						t = null;
						System.gc();
					}
					t = new ManualThread();
					t.start();
				}
				// t.onResume();
				DLNA_Api_lock = false;
				break;
			case SOCKET_TIMEOUT:
				Toast.makeText(getApplicationContext(), "机顶盒通信超时",
						Toast.LENGTH_SHORT).show();

				switch (DLNAData.current().DLNA_post_type) {
				// SetVolume
				case 3:
					break;
				// SetMute
				case 5:
					break;
				// SetAVTransportURI
				case 9:
					break;
				// Play
				case 110:
					handler.sendEmptyMessage(PLAY_ERROR);
					break;
				// Pause
				case 111:
					handler.sendEmptyMessage(PAUSE_ERROR);
					break;
				// Stop
				case 112:
					handler.sendEmptyMessage(STOP_ERROR);
					break;
				// Seek
				case 113:
					break;
				// Stop_only
				case 115:
					handler.sendEmptyMessage(STOP_ERROR);
					break;
				default:
					break;
				}

				break;
			case GET_POSITION_INFO_OK:
				dismissWaitDialog();
				processPosition();
				try {
					getVolume();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				break;
			case GET_POSITION_INFO_ERROR:

				break;
			case START_SHARE:
				getPositionInfo();
				break;
			case GET_MUTE_ERROR:
				// Toast.makeText(getApplicationContext(), "静音状态读取出错",
				// Toast.LENGTH_SHORT).show();
				break;
			case PROCESS_VOLUME:
				if (!DLNAData.current().CurrentVolume.equals("")) {
					volSeekBar.setMainProgress(Integer.parseInt(DLNAData
							.current().CurrentVolume));
					if (DeviceData.getInstance().getSelectedDevice()
							.getFriendlyName().equals("DaPingMu(Q-1000DF)")) {
						// volumeRate.setText(DLNAData.current().CurrentVolume);
					} else if (DeviceData.getInstance().getSelectedDevice()
							.getFriendlyName().equals("eHomeMediaCenter")) {
						// volumeRate.setText(DLNAData.current().CurrentVolume);
					}

					else {
						// volumeRate.setText(DLNAData.current().CurrentVolume
						// + "%");
					}
					hasVolumeAlready = true;
				}
				break;
			case MSG_OPEN:
				showWaitDialog();
				break;

			case MSG_CLOSE:
				dismissWaitDialog();
				break;

			case MSG_PROCESS_PLAY:
				processPlayPause();
				break;
			case PROGRESS_REFRESH:
				int i = currentPosition;
				currentTimeInMillis = i;

				// positionSeekBar.setProgress((int) (i / 1000));
				positionSeekBar.setMainProgress(i);
				i /= 1000;
				minute = (i / 60);
				hour = minute / 60;
				second = (i % 60);
				minute %= 60;
				// seekTime = String
				// .format("%02d:%02d:%02d", hour, minute, second);

				textPlayed.setText(String.format("%02d:%02d:%02d", hour,
						minute, second));

				if (currentPosition >= positionSeekBar.getMaxProgress()
						&& playing) {

					positionSeekBar.setMainProgress(0);

					handler.removeMessages(GET_VIDEO_POSITION);
					handler.removeMessages(PROGRESS_REFRESH);
					playPause.setImageResource(R.drawable.dlna_play);
					playing = false;
					textPlayed.setText("00:00:00");
					currentPosition = 0;
					DLNAData.current().prevProgramPlayingPosition = 0;
					positionSeekBar.setMainProgress(currentPosition);
					textDuration
							.setText(DLNAData.current().prevProgramDuration);
					// String name = DLNAData.current().prevProgramName;
					// if (name.length() > 18) {
					// name = name.substring(0, 18) + "...";
					// title.setTextSize(15);
					// }
					// title.setText(name);
					Toast.makeText(getApplicationContext(), "机顶盒已停止播放",
							Toast.LENGTH_SHORT).show();
					DLNAData.current().isEpisodePlaying = false;
					DLNAData.current().prevProgramSubID = null;
					DLNAData.current().prevProgramID = 0;
					DLNAData.current().isStopAlready = true;
					DLNAData.current().hasSetTransportURI = false;
					DLNAData.current().prevProgramPlayingPosition = 0;
					DLNAData.current().hasPlayingOnTV = false;

				} else {
					sendEmptyMessageDelayed(PROGRESS_REFRESH, 1000);
				}

				break;
			case GET_VIDEO_POSITION:
				currentPosition = currentPosition + 1000;
				handler.sendEmptyMessageDelayed(GET_VIDEO_POSITION, 1000);
				// Log.e("GET_VIDEO_POSITION", "here-" + currentPosition);
				break;
			case PROGRESS_CHANGED:

				// int i = mVideoView.getCurrentPosition();
				// positionSeekBar.setProgress(i);
				//
				// int j = mVideoView.getBufferPercentage();
				// positionSeekBar.setSecondaryProgress(j *
				// positionSeekBar.getMax() / 100);

				// Calendar cal = Calendar.getInstance();
				// System.out.println();
				// long i = cal.getTimeInMillis();
				//
				// i /= 1000;
				// minute = (int) (i / 60);
				// hour = minute / 60;
				// second = (int) (i % 60);
				// minute %= 60;
				//
				// seekTime = String
				// .format("%02d:%02d:%02d", hour, minute, second);
				// textPlayed.setText(String.format("%02d:%02d:%02d", hour,
				// minute, second));
				//
				// sendEmptyMessageDelayed(PROGRESS_CHANGED, 100);

				break;
			case MESSAGE_ERROR:
				// play = AVT.getAction("Play");
				// pause = AVT.getAction("Pause");
				// stop = AVT.getAction("Stop");
				// seek = AVT.getAction("Seek");
				// getPositionInfo = AVT.getAction("GetPositionInfo");
				// setTransportURL = AVT.getAction("SetTransportURL");
				// UserNow.current().controlUrl = AVT.getControlURL();
				Toast.makeText(getApplicationContext(), msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				dismissWaitDialog();
				// if (DLNAData.current().DLNA_post_type != 12)
				// stopPlay();
				// TODO:
				// setTransportURL();
				break;
			case SET_URI_START:
				// Toast.makeText(getApplicationContext(), "初始化地址...",
				// Toast.LENGTH_SHORT).show();
				break;
			case SET_URI_OK:
				setTranspotURI_Error = false;
				stopBtn.setOnClickListener(RemoteControllerActivityNew.this);
				title.setText(DLNAData.current().nowProgramName);
				positionSeekBar.setMainProgress(0);

				playPause.setImageResource(R.drawable.dlna_play);
				playing = false;
				textPlayed.setText("00:00:00");
				currentPosition = 0;

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
				break;
			case PLAY_START:
				// Toast.makeText(getApplicationContext(), "请等待机顶盒播放...",
				// Toast.LENGTH_SHORT).show();
				break;
			case PLAY_OK:
				// DLNAData.current().isResumeSameProgram = true;
				// isResumeSameProgram = true;
				isSameProgram = true;
				Toast.makeText(getApplicationContext(), "机顶盒开始播放",
						Toast.LENGTH_SHORT).show();
				playing = true;
				// if (ProgramData.current().isEpisodePlay) {
				// ProgramData.current().isEpisodePlayHolder = true;
				// }
				positionSeekBar.setEnabled(true);
				DLNAData.current().hasPlayingOnTV = true;
				DLNAData.current().prevIsLivePlay = isLivePlay;

				if (!DeviceData.getInstance().getSelectedDevice()
						.getFriendlyName()
						.equals("Realtek Embedded UPnP Render()")) {

					if (isNeedSendGetPositionInfo) {

						if (t != null && !t.isAlive()) {
							t.onPause();
							t.interrupt();
							t = null;
							System.gc();
							t = new ManualThread();
							t.start();
						}
					}
				}

				setTimer();

				if (DLNAData.current().hasPlayingOnTV) {
					handler.removeMessages(PROGRESS_REFRESH);
					handler.removeMessages(GET_VIDEO_POSITION);
					currentPosition = DLNAData.current().prevProgramPlayingPosition;
					positionSeekBar.setMainProgress(currentPosition);
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

				Log.e(" play()  ", "play - OK");

				DLNAData.current().prevProgramDuration = DLNAData.current().nowSTBPlayPosition;
				playPause.setImageResource(R.drawable.dlna_pause);
				// sendEmptyMessage(PROGRESS_REFRESH);

				// 纠正进度
				currentPosition -= 10;
				if (currentPosition < 0)
					currentPosition = 0;

				dismissWaitDialog();
				// 播放开始后取得播放状态
				// GetTransportInfo();
				break;
			case PLAY_ERROR:
				dismissWaitDialog();
				playing = false;
				playPause.setImageResource(R.drawable.dlna_play);
				// Toast.makeText(getApplicationContext(), "播放出错",
				// Toast.LENGTH_SHORT).show();
				// 暂停后后取得播放状态
				// GetTransportInfo();
				break;
			case STOP_START:
				// Toast.makeText(getApplicationContext(), "请等待机顶盒停止...",
				// Toast.LENGTH_SHORT).show();
				break;
			case STOP_OK:
				Log.e("stop()", "stop - OK - 2");
				stopBtn.setOnClickListener(RemoteControllerActivityNew.this);
				if (func == STOP_ONLY) {
					Toast.makeText(getApplicationContext(), "机顶盒已停止播放",
							Toast.LENGTH_SHORT).show();
				}
				setTranspotURI_Error = true;
				positionSeekBar.setMainProgress(0);

				handler.removeMessages(GET_VIDEO_POSITION);
				handler.removeMessages(PROGRESS_REFRESH);
				playPause.setImageResource(R.drawable.dlna_play);
				playing = false;
				textPlayed.setText("00:00:00");
				currentPosition = 0;

				Log.e("stop()", "stop - OK - 3");
				if (isNeedSendGetPositionInfo) {
					if (t != null && !t.isInterrupted()) {
						t.onPause();
						t.interrupt();
						t = null;
						System.gc();
					}
				}

				Log.e("stop()", "stop - OK - 4");

				if (func == STOP_ONLY) {
					dismissWaitDialog();
				}

				DLNAData.current().isEpisodePlaying = false;
				DLNAData.current().prevProgramSubID = null;
				DLNAData.current().prevProgramID = 0;
				DLNAData.current().prevProgramName = "";
				DLNAData.current().isStopAlready = true;
				DLNAData.current().hasPlayingOnTV = false;
				DLNAData.current().hasSetTransportURI = false;
				DLNAData.current().prevProgramPlayingPosition = 0;
				DLNAData.current().prevIsLivePlay = false;
				findViewById(R.id.stop).setOnClickListener(
						RemoteControllerActivityNew.this);
				Log.e("stop()", "stop - OK - 5");
				isResumeSameProgram = true;

				finish();
				break;
			case STOP_ERROR:
				dismissWaitDialog();
				if (func == STOP_ONLY) {
					// Toast.makeText(getApplicationContext(), "停止出错",
					// Toast.LENGTH_SHORT).show();
				}
				exeptionCatch();
				// 停止后取得播放状态
				// GetTransportInfo();
				break;
			case MUTE_START:
				dismissWaitDialog();
				// Toast.makeText(getApplicationContext(), "请等待机顶盒静音...",
				// Toast.LENGTH_SHORT).show();
				break;
			case MUTE_OK:
				dismissWaitDialog();
				// Toast.makeText(getApplicationContext(), "操作成功",
				// Toast.LENGTH_SHORT).show();

				if (mute == 0) {
					soundControl
							.setBackgroundResource(R.drawable.pd_m2tv_sound);
					volSeekBar.setEnabled(true);

				} else if (mute == 1) {
					soundControl
							.setBackgroundResource(R.drawable.pd_m2tv_sound_close);
					volSeekBar.setEnabled(false);
				}

				break;
			case MUTE_ERROR:
				// Toast.makeText(getApplicationContext(), "静音出错",
				// Toast.LENGTH_SHORT).show();

				exeptionCatch();
				break;
			case SEEK_START:
				// Toast.makeText(getApplicationContext(), "请等待机顶盒跳转...",
				// Toast.LENGTH_SHORT).show();
				break;
			case SEEK_OK:
				positionSeekBar.setEnabled(true);
				positionSeekBar.setMainProgress(currentPosition);
				textPlayed.setText(seekTime);
				Toast.makeText(getApplicationContext(), "机顶盒定位成功",
						Toast.LENGTH_SHORT).show();
				break;
			case SEEK_ERROR:
				positionSeekBar.setEnabled(true);
				// Toast.makeText(getApplicationContext(), "机顶盒跳转出错",
				// Toast.LENGTH_SHORT).show();
				exeptionCatch();
				break;
			case SET_VOLUME_OK:
				volSeekBar.setEnabled(true);

				break;
			case SET_VOLUME_ERROR:
				volSeekBar.setEnabled(true);
				exeptionCatch();
				break;
			case PAUSE_START:
				// Toast.makeText(getApplicationContext(), "请等待机顶盒暂停...",
				// Toast.LENGTH_SHORT).show();
				break;
			case PAUSE_OK:
				playing = false;
				Toast.makeText(getApplicationContext(), "机顶盒暂停成功",
						Toast.LENGTH_SHORT).show();
				positionSeekBar.setEnabled(false);
				// if (ProgramData.current().isEpisodePlay) {
				// ProgramData.current().isEpisodePlayHolder = true;
				// }
				playPause.setImageResource(R.drawable.dlna_play);
				dismissWaitDialog();
				isResumeSameProgram = true;
				DLNAData.current().prevProgramPlayingPosition = currentPosition;

				if (isNeedSendGetPositionInfo) {
					if (t != null && t.isAlive()) {
						t.onPause();
						t.interrupt();
						t = null;
						System.gc();
					}
				}

				// 暂停后后取得播放状态
				// GetTransportInfo();
				break;
			case PAUSE_ERROR:
				dismissWaitDialog();
				// Toast.makeText(getApplicationContext(), "机顶盒暂停出错",
				// Toast.LENGTH_SHORT).show();

				exeptionCatch();
				// 暂停后后取得播放状态
				// GetTransportInfo();
				break;
			default:
				break;
			}
		};
	};

	private void exeptionCatch() {
		DLNAData.current().isEpisodePlaying = false;
		DLNAData.current().prevProgramSubID = null;
		DLNAData.current().prevProgramID = 0;
		DLNAData.current().isStopAlready = true;
		DLNAData.current().hasPlayingOnTV = false;
		// if (ProgramData.current().isEpisodePlay) {
		// ProgramData.current().isEpisodePlayHolder = false;
		// }
		DLNAData.current().hasSetTransportURI = false;
		DLNAData.current().prevProgramPlayingPosition = 0;
		playing = false;
		textPlayed.setText("00:00:00");
		currentPosition = 0;
		handler.removeMessages(GET_VIDEO_POSITION);
		handler.removeMessages(PROGRESS_REFRESH);
		playPause.setImageResource(R.drawable.dlna_play);
	}

	private void processPosition() {

		String tmp = DLNAData.current().nowSTBPlayPosition;
		if (!tmp.equals("00:00:00") && !tmp.equals("0:00:00.000") && playing) {
			currentPosition = CommonUtils.parserString2TimeStamp(tmp);

			Log.e("processPosition", tmp);
			positionSeekBar.setMainProgress(currentPosition);

			int totalSec = CommonUtils.parserString2TimeStamp(DLNAData
					.current().nowSTBPlayDuration);
			if (totalSec != 0)
				positionSeekBar.setMaxProgress(totalSec);
			textPlayed.setText(tmp);
			textDuration.setText(DLNAData.current().nowSTBPlayDuration);
		}
	}

	private static final int DIALOG_PROGRESS = 1;

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_PROGRESS:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("加载中...");
			dialog = progressDialog;
			progressDialog.setCanceledOnTouchOutside(false);
			break;

		default:
			dialog = null;
			break;
		}
		return dialog;
	}

	private void setTimer() {

		setPositionSeekMax();
		handler.sendEmptyMessage(GET_VIDEO_POSITION);
	}

	private void setPositionSeekMax() {
		int totalSec = 0;
		if (DLNAData.current().nowSTBPlayDuration.contains(":")) {
			totalSec = CommonUtils
					.parserString2TimeStamp(DLNAData.current().nowSTBPlayDuration);

		} else {
			totalSec = Integer.parseInt(DLNAData.current().nowSTBPlayDuration);
		}
		allDurationMillionSeconds = totalSec;
		Log.e("setTimer - 369", "totalSec:" + totalSec);
		positionSeekBar.setMaxProgress(totalSec);
		positionSeekBar.setMainProgress(currentPosition);
	}

	// 标记是否为相同子集
	private boolean isSameEpisodeProgram = false;
	private WindowManager mWindowManager;

	private void getExtras() {
		Intent intent = getIntent();
		DeviceDataInSearchList deviceDataInSearchList = (DeviceDataInSearchList) intent
				.getSerializableExtra("selectedDevice");
		DeviceData.getInstance().setSelectedDevice(
				getDeviceByShortData(deviceDataInSearchList));
		isResume = intent.getIntExtra("isResume", 0);
		isLivePlay = intent.getBooleanExtra("isLivePlay", false);
		// DLNAData.current().initDlnaAction();
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dlna_controler);
		all = (RelativeLayout) findViewById(R.id.all);
		// open2up = AnimationUtils.loadAnimation(getApplicationContext(),
		// R.anim.activity_open2up);
		// close2bottom = AnimationUtils.loadAnimation(getApplicationContext(),
		// R.anim.activity_close2bottom);
		// all.startAnimation(open2up);

		Intent i = getIntent();
		isResume = i.getIntExtra("isResume", 0);
		isLivePlay = i.getBooleanExtra("isLivePlay", false);
		vb = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

		getExtras();
		if (DLNAData.current().prevProgramSubID != null) {
			Log.e("onCreate", "是同一子集");

			isSameEpisodeProgram = true;
			isSameProgram = true;
			// 有节目甩到电视
			if (DLNAData.current().hasPlayingOnTV) {
				isResume = 1;
			}
		} else if (
		// ProgramData.current().isEpisodePlay&&
		DLNAData.current().prevProgramSubID == null
				&& !DLNAData.current().hasPlayingOnTV) {
			isSameEpisodeProgram = true;
			isSameProgram = true;
		} else if (
		// ProgramData.current().isEpisodePlay&&
		DLNAData.current().prevProgramSubID == null
				&& DLNAData.current().hasPlayingOnTV) {
			isSameEpisodeProgram = false;
			isSameProgram = false;
		}

		if (
		// !ProgramData.current().isEpisodePlay&&
		DLNAData.current().prevProgramID != 0
				&& DLNAData.current().prevProgramName
						.equals(DLNAData.current().nowProgramName)) {
			isSameEpisodeProgram = false;
			isSameProgram = true;
			isResume = 1;
		} else if (
		// !ProgramData.current().isEpisodePlay &&
		DLNAData.current().prevProgramID != 0
				&& !DLNAData.current().prevProgramName.equals(DLNAData
						.current().nowProgramName)) {
			isSameEpisodeProgram = false;
			isSameProgram = false;
			// isResume = 0;
		}

		if (DeviceData.getInstance().getSelectedDevice() != null) {
			getDLNA_Server();
			initUI();
			// resizeForSmallScreen();
			// isResumeSameProgram = DLNAData.current().isStopAlready;
			setPositionSeekMax();
			// 先初始化当前音量
			try {
				getVolume();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

		}
		cul = this;
	}

	// private LinearLayout controllerLayout;
	// private RelativeLayout seekbarLayout;
	// private WindowManager mWindowManager;

	// private void resizeForSmallScreen() {
	// Display defaultDisplay = mWindowManager.getDefaultDisplay();
	// int height = defaultDisplay.getHeight();
	//
	// Log.e("RemoteConreoller", height + "");
	// if (height < 800) {
	// RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
	// ViewGroup.LayoutParams.FILL_PARENT, CommonUtils.dip2px(
	// RemoteControllerActivityNew.this, 120));
	// int h = CommonUtils.dip2px(RemoteControllerActivityNew.this, 5);
	// lp2.setMargins(0, h, 0, 0);
	// controllerLayout.setLayoutParams(lp2);
	// RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
	// ViewGroup.LayoutParams.FILL_PARENT,
	// ViewGroup.LayoutParams.WRAP_CONTENT);
	// int h1 = CommonUtils.dip2px(RemoteControllerActivityNew.this, 240);
	// lp3.setMargins(0, h1, 0, 0);
	// seekbarLayout.setLayoutParams(lp3);
	// }
	// }

	@Override
	protected void onDestroy() {
		setResult(RESULT_OK);
		super.onDestroy();
		DLNAData.current().isOnlyController = false;

		if (net != null) {
			net.removeListener();
		}

		handler.removeMessages(PROGRESS_REFRESH);
		handler.removeMessages(PROGRESS_CHANGED);
		handler.removeMessages(GET_VIDEO_POSITION);
		handler.removeMessages(START_SHARE);

		if (DLNAData.current().hasPlayingOnTV && isSameProgram) {
			// Calendar cal = Calendar.getInstance();
			// long i = cal.getTimeInMillis();

			if (
			// ProgramData.current().isEpisodePlay &&
			isSameEpisodeProgram) {
				Log.e("onDestroy", "子集播放");

				// DLNAData.current().prevProgramSubID =
				// SubProgramData.current().subProgramID;
				DLNAData.current().isEpisodePlaying = true;

				currentTimeInMillis = System.currentTimeMillis();
				DLNAData.current().prevProgramSystemMillions = currentTimeInMillis;
				DLNAData.current().prevProgramPlayingPosition = currentPosition;
				DLNAData.current().prevProgramID = DLNAData.current().nowProgramId;
				DLNAData.current().prevProgramDuration = DLNAData.current().nowSTBPlayPosition;
				DLNAData.current().prevProgramName = title.getText().toString();
				DLNAData.current().isPlayingOnTV = playing;

			} else if (
			// ProgramData.current().isEpisodePlay
			// &&
			!isSameEpisodeProgram) {
				DLNAData.current().isPlayingOnTV = playing;

			} else {
				Log.e("onDestroy", "currentPosition：" + currentPosition);

				currentTimeInMillis = System.currentTimeMillis();
				DLNAData.current().prevProgramSystemMillions = currentTimeInMillis;
				DLNAData.current().prevProgramPlayingPosition = currentPosition;
				DLNAData.current().prevProgramID = DLNAData.current().nowProgramId;
				DLNAData.current().prevProgramDuration = DLNAData.current().nowSTBPlayPosition;
				DLNAData.current().prevProgramName = title.getText().toString();
				DLNAData.current().isPlayingOnTV = playing;

			}
			Log.e("CtMobleToTV - onDestroy",
					DLNAData.current().prevProgramSystemMillions + "");

			DLNAData.current().playedTimeText = textPlayed.getText().toString();
		}

		if (isNeedSendGetPositionInfo) {

			if (t != null && !t.isInterrupted()) {
				t.onPause();
				t.interrupt();
			}
			t = null;
		}
		playing = false;

		System.gc();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DLNAData.current().hasPlayingOnTV) {
			getPositionInfo();
			if (DLNAData.current().prevProgramName
					.equals(DLNAData.current().nowProgramName)) {
				isSameProgram = true;
			} else {
				isSameProgram = false;
			}
		}

		Log.e("debug-onResume", "isResume:" + isResume + "\n"
				+ "isSameProgram：" + isSameProgram + "\n" + "isPlayingOnTV："
				+ DLNAData.current().isPlayingOnTV + "\n" + "hasPlayingOnTV："
				+ DLNAData.current().hasPlayingOnTV);

		if (DLNAData.current().hasPlayingOnTV) {
			switch (isResume) {
			case 0:
				if (isSameProgram) {
					stopBtn.setOnClickListener(this);
				} else {
					stopBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog();
						}
					});
					stopBtn.performClick();
				}
				break;
			// 从其他界面恢复时，播放按钮未暂停状态
			case 1:
				Log.e("RemoteController", "prevProgramPlayingPosition："
						+ DLNAData.current().prevProgramPlayingPosition
						+ "playedTimeText：" + DLNAData.current().playedTimeText
						+ "prevProgramSystemMillions："
						+ DLNAData.current().prevProgramSystemMillions
						+ "prevProgramID：" + DLNAData.current().prevProgramID);

				// 此参数仅用于在不同节目中返回播控时，控制不弹出有其他节目在播放的提示
				if (
				// ProgramData.current().isEpisodePlay
				// &&
				DLNAData.current().prevProgramSubID != null
				// && SubProgramData.current().subProgramID != null

				// && DLNAData.current().prevProgramSubID
				// .equals(SubProgramData.current().subProgramID)
				) {
					isResumeSameProgram = true;
				}

				// else if (ProgramData.current().isEpisodePlay
				// && DLNAData.current().prevProgramSubID != null
				// && SubProgramData.current().subProgramID != null
				// && !DLNAData.current().prevProgramSubID
				// .equals(SubProgramData.current().subProgramID)) {
				// isResumeSameProgram = false;
				// if (!DLNAData.current().isOnlyController) {
				// // playPause.performClick();
				// }
				// } else if (!ProgramData.current().isEpisodePlay
				// && DLNAData.current().prevProgramSubID != null
				// && SubProgramData.current().subProgramID == null) {
				// isResumeSameProgram = false;
				// if (!DLNAData.current().isOnlyController) {
				// playPause.performClick();
				// }
				// }
				//
				else {
					isResumeSameProgram = true;
				}

				setTranspotURI_Error = false;
				DLNAData.current().nowSTBPlayPosition = DLNAData.current().prevProgramDuration;

				if (DLNAData.current().isPlayingOnTV) {
					playing = true;
					playPause.setImageResource(R.drawable.dlna_pause);
					handler.removeMessages(PROGRESS_REFRESH);
					handler.removeMessages(GET_VIDEO_POSITION);

					long now = System.currentTimeMillis();
					int t = (int) (now - DLNAData.current().prevProgramSystemMillions);
					currentPosition = DLNAData.current().prevProgramPlayingPosition
							+ t;
					positionSeekBar.setEnabled(true);

					if (currentPosition >= positionSeekBar.getMaxProgress()
							&& playing) {

						positionSeekBar.setMainProgress(0);

						handler.removeMessages(GET_VIDEO_POSITION);
						handler.removeMessages(PROGRESS_REFRESH);
						playPause.setImageResource(R.drawable.dlna_play);
						playing = false;
						textPlayed.setText("00:00:00");
						currentPosition = 0;
						DLNAData.current().prevProgramPlayingPosition = 0;
						positionSeekBar.setMainProgress(currentPosition);
						textDuration
								.setText(DLNAData.current().prevProgramDuration);
						String name = DLNAData.current().prevProgramName;
						if (name.length() > 18) {
							name = name.substring(0, 18) + "...";
							title.setTextSize(18);
						}
						title.setText(name);
						Toast.makeText(getApplicationContext(), "机顶盒已停止播放",
								Toast.LENGTH_SHORT).show();

						DLNAData.current().isEpisodePlaying = false;
						DLNAData.current().prevProgramSubID = null;
						DLNAData.current().prevProgramID = 0;
						DLNAData.current().isStopAlready = true;
						DLNAData.current().hasSetTransportURI = false;
						DLNAData.current().prevProgramPlayingPosition = 0;
						DLNAData.current().hasPlayingOnTV = false;
						// if (ProgramData.current().isEpisodePlay) {
						// ProgramData.current().isEpisodePlayHolder = false;
						// }

					} else {
						positionSeekBar.setMainProgress(currentPosition + t);
						setPositionSeekMax();
						textPlayed.setText(DLNAData.current().playedTimeText);
						textDuration
								.setText(DLNAData.current().prevProgramDuration);
						String name = DLNAData.current().prevProgramName;
						if (name.length() > 18) {
							name = name.substring(0, 18) + "...";
							title.setTextSize(18);
						}
						title.setText(name);
						handler.sendEmptyMessage(PROGRESS_REFRESH);
						handler.sendEmptyMessage(GET_VIDEO_POSITION);

						// if (DLNAData.current().prevProgramID
						// == Integer.parseInt(ProgramData.current().programID))
						// {
						// getPositionInfo();
						// }

					}
				} else {
					playing = false;
					playPause.setImageResource(R.drawable.dlna_play);

					// long now = System.currentTimeMillis();
					// int t = (int) (now -
					// DLNAData.current().prevProgramSystemMillions);
					currentPosition = DLNAData.current().prevProgramPlayingPosition;
					positionSeekBar.setMainProgress(currentPosition);
					positionSeekBar.setEnabled(false);
					setPositionSeekMax();
					textPlayed.setText(DLNAData.current().playedTimeText);
					textDuration
							.setText(DLNAData.current().prevProgramDuration);
					String name = DLNAData.current().prevProgramName;
					if (name.length() > 18) {
						name = name.substring(0, 18) + "...";
						title.setTextSize(18);
					}
					title.setText(name);

					// handler.sendEmptyMessage(PROGRESS_REFRESH);
					// handler.sendEmptyMessage(GET_VIDEO_POSITION);

					// if (DLNAData.current().prevProgramID
					// == Integer.parseInt(ProgramData.current().programID)) {
					// getPositionInfo();
					// }
				}
				break;
			default:
				break;
			}
		} else {
			if (!DLNAData.current().isOnlyController) {
				try {
					playPause.performClick();
				} catch (NullPointerException e) {
					e.printStackTrace();
					finish();
				}
			}
		}
	}

	private void getDLNA_Server() {

		if (DeviceData.getInstance().getSelectedDevice() == null)
			finish();

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

	}

	// 设置地址
	private void setTransportURL() {
		// showWaitDialog();
		func = SETTRANSPORTURL;
		DLNAData.current().DLNA_post_type = SETTRANSPORTURL;
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// TODO Auto-generated method stub
		handler.sendEmptyMessage(SET_URI_START);
		// setTransportURL = AVT.getAction("SetAVTransportURI");
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

	// 第一次播放
	private void play() {
		// showWaitDialog();
		func = PLAY;
		DLNAData.current().DLNA_post_type = PLAY;
		handler.sendEmptyMessage(PLAY_START);
		play = DLNAData.current().getPlay();

		ArgumentList pauseal = play.getArgumentList();
		pauseal.getArgument("InstanceID").setValue("0");
		pauseal.getArgument("Speed").setValue("1");

		play.setInArgumentValues(pauseal);
		// if (UserNow.current().remote) {
		// ActionRequest ctrlReq = new ActionRequest();
		// ctrlReq.setRequest(play, pauseal);
		// Log.e(" play()  ", "play - OK");
		// // UserNow.current().SOAP_Address = "/AVTransport/control";
		// UserNow.current().SOAP_Address = AllUrlParser.checkDeviceURL(
		// DLNAData.current().AVT.getControlURL(), false);
		// remotePost(ctrlReq);
		// } else {
		if (play.postControlAction()) {
			// dismissWaitDialog();
			handler.sendEmptyMessage(PLAY_OK);
			return;
		} else {

			// dismissWaitDialog();
			UPnPStatus err = play.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
			handler.sendEmptyMessage(PLAY_ERROR);

		}
		// }
	}

	// 已经甩过的播放
	private void playOnly() {
		func = PLAY_ONLY;
		DLNAData.current().DLNA_post_type = PLAY_ONLY;
		handler.sendEmptyMessage(PLAY_START);
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
			dismissWaitDialog();
			handler.sendEmptyMessage(PLAY_OK);
			return;
		} else {

			dismissWaitDialog();
			UPnPStatus err = play.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
			handler.sendEmptyMessage(PLAY_ERROR);

		}
		// }
		// }
		// }).start();
	}

	// 暂停
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

			dismissWaitDialog();
			UPnPStatus err = pause.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
			handler.sendEmptyMessage(PAUSE_ERROR);
		}
		// }
		// }
		// }).start();
	}

	// 初次播放时先停止播放
	private void stopPlay() {
		// showWaitDialog();
		func = STOP;
		DLNAData.current().DLNA_post_type = STOP;
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		handler.sendEmptyMessage(STOP_START);
		stop = DLNAData.current().getStop();

		ArgumentList stopal = stop.getArgumentList();
		stopal.getArgument("InstanceID").setValue("0");
		stop.setInArgumentValues(stopal);
		// if (UserNow.current().remote) {
		// ActionRequest ctrlReq = new ActionRequest();
		// ctrlReq.setRequest(stop, stopal);
		// // UserNow.current().SOAP_Address = "/AVTransport/control";
		// UserNow.current().SOAP_Address = AllUrlParser.checkDeviceURL(
		// DLNAData.current().AVT.getControlURL(), false);
		// remotePost(ctrlReq);
		// } else {
		if (stop.postControlAction()) {
			handler.sendEmptyMessage(STOP_OK);

			return;
		} else {
			Log.e("stopPlay", "stopPlay - error");

			// dismissWaitDialog();

			handler.sendEmptyMessage(STOP_ERROR);
			UPnPStatus err = stop.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
		}
		// }
	}

	// 通过按钮停止播放
	private void btnStopPlay() {
		showWaitDialog();
		func = STOP_ONLY;
		DLNAData.current().DLNA_post_type = STOP_ONLY;
		handler.sendEmptyMessage(STOP_START);
		stop = DLNAData.current().getStop();

		ArgumentList stopal = stop.getArgumentList();
		stopal.getArgument("InstanceID").setValue("0");
		stop.setInArgumentValues(stopal);
		// if (UserNow.current().remote) {
		// // ActionRequest ctrlReq = new ActionRequest();
		// // ctrlReq.setRequest(stop, stopal);
		// // UserNow.current().SOAP_Address =
		// // AllUrlParser.checkDeviceURL(DLNAData
		// // .current().AVT.getControlURL(), false);
		// // remotePost(ctrlReq);
		// // upnp.setUpnpListener(CtMobileToTvActivity.this);
		// } else {
		if (stop.postControlAction()) {
			handler.sendEmptyMessage(STOP_OK);
			return;
		} else {
			Log.e("stopPlay", "stopPlay - error");
			dismissWaitDialog();
			handler.sendEmptyMessage(STOP_ERROR);
			UPnPStatus err = stop.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
		}
		// }
	}

	// 取得当前静音状态
	private void getMute() {

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
				+ "u:GetMute xmlns:u=\"urn:schemas-upnp-org:service:RenderingControl:1\""
				+ right_tringle + letf_tringle + "InstanceID" + right_tringle
				+ "0" + letf_tringle + "/InstanceID" + right_tringle
				+ letf_tringle + "Channel" + right_tringle + "Master"
				+ letf_tringle + "/Channel" + right_tringle + letf_tringle
				+ "/u:GetMute" + right_tringle + letf_tringle + "/s:Body"
				+ right_tringle + letf_tringle + "/s:Envelope" + right_tringle;
		// if (UserNow.current().remote) {
		// // netUrg = new HTTPNetURG();
		// // netUrg.setListener(CtMobileToTvActivity.this);
		// // try {
		// // DLNAData.current().DLNA_get_type =
		// // DLNAData.current().GET_MUTE_TYPE;
		// // netUrg.request(DLNAData.current().SOAP_Action_body);
		// // } catch (HttpHostConnectException e) {
		// // e.printStackTrace();
		// // } catch (ConnectTimeoutException e) {
		// // e.printStackTrace();
		// // } catch (SocketException e) {
		// // e.printStackTrace();
		// // } catch (SocketTimeoutException e) {
		// // e.printStackTrace();
		// // dismissWaitDialog();
		// // handler.sendEmptyMessage(SETMUTE_ERR);
		// // }
		// } else {
		net = new HTTPNet();
		net.setListener(RemoteControllerActivityNew.this);
		try {
			DLNAData.current().DLNA_get_type = DLNAData.current().GET_MUTE_TYPE;
			// net.request(
			// "http://"
			// + DLNAData.current().SOAP_Address
			// + ":"
			// + DLNAData.current().SOAP_Port
			// // + "/RenderingControl/control",
			// + AllUrlParser.checkDeviceURL(
			// DLNAData.current().RCS.getControlURL(),
			// false), DLNAData.current().SOAP_Port,
			// DLNAData.current().SOAP_Action_body);

			net.request(getURL, getPort, DLNAData.current().SOAP_Action_body);
		} catch (HttpHostConnectException e) {
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		// }
	}

	private void setMute(String b) {
		showWaitDialog();
		func = SETMUTE;
		DLNAData.current().DLNA_post_type = SETMUTE;
		// setMute = RCS.getAction("SetMute");
		setMute = DLNAData.current().getSetMute();
		ArgumentList setal = setMute.getArgumentList();
		// 暂时一直为0
		setal.getArgument("InstanceID").setValue(0);
		setal.getArgument("Channel").setValue("Master");

		Log.e("setMute()", b);

		setal.getArgument("DesiredMute").setValue(mute);
		setMute.setInArgumentValues(setal);
		// if (UserNow.current().remote) {
		// ActionRequest ctrlReq = new ActionRequest();
		// ctrlReq.setRequest(setMute, setal);
		// // UserNow.current().SOAP_Address = "/RenderingControl/control"
		// UserNow.current().SOAP_Address = AllUrlParser.checkDeviceURL(
		// DLNAData.current().RCS.getControlURL(), false);
		// remotePost(ctrlReq);
		// } else {
		if (setMute.postControlAction()) {
			handler.sendEmptyMessage(MUTE_OK);
			Log.e("setMute()", "setMute - ok");
			UPnPStatus err = setMute.getStatus();
			Log.e("setMute", err.getDescription());
			UPnPStatus err1 = setMute.getControlStatus();
			Log.e("setMute", err1.code2String(-1));

		} else {
			Log.e("setMute", "setMute - error");
			UPnPStatus err = setMute.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
		}
		// }
	}

	private void processSeek() {
		positionSeekBar.setEnabled(false);
		func = SEEK;
		firstDLNATask t = new firstDLNATask();
		t.execute(SEEK);
	}

	// 跳转
	private void seek() {
		func = SEEK;
		seek = DLNAData.current().getSeek();

		ArgumentList seekal = seek.getArgumentList();
		seekal.getArgument("Unit").setValue("REL_TIME");
		// seekTime = "00:05:00";
		// seekal.getArgument("Target").setValue(seekTime);
		seekal.getArgument("Target").setValue(seekTime);
		// String[] temp = seekTime.split(":");
		// seekTime = temp[0] + "0" + Integer.parseInt(temp[1], 1) + 1 +
		// temp[2];
		// Log.e(" seek() ", seekTime + temp[0] + temp[1] + temp[2]);
		String[] my = seekTime.split(":");
		int hour = Integer.parseInt(my[0]);
		int min = Integer.parseInt(my[1]);
		int sec = Integer.parseInt(my[2]);
		int totalSec = hour * 3600 + min * 60 + sec;
		totalSec = totalSec * 1000;
		Log.e("setTimer - 369", "totalSec:" + totalSec);
		currentPosition = totalSec;

		seekal.getArgument("InstanceID").setValue("0");
		seek.setInArgumentValues(seekal);
		// if (UserNow.current().remote) {
		// ActionRequest ctrlReq = new ActionRequest();
		// ctrlReq.setRequest(seek, seek.getInputArgumentList());
		// remotePost(ctrlReq);
		// } else {
		// if (UserNow.current().remote) {
		// // ActionRequest ctrlReq = new ActionRequest();
		// // ctrlReq.setRequest(seek, seekal);
		// // try {
		// // DLNAData.current().DLNA_post_type = 113;
		// // // UserNow.current().SOAP_Address = "/AVTransport/control";
		// // UserNow.current().SOAP_Address = DLNAData.current().AVT
		// // .getControlURL();
		// // upnp.request(ctrlReq, false);
		// // } catch (HttpHostConnectException e) {
		// // // TODO Auto-generated catch block
		// // e.printStackTrace();
		// // } catch (ConnectTimeoutException e) {
		// // // TODO Auto-generated catch block
		// // e.printStackTrace();
		// // } catch (SocketTimeoutException e) {
		// // // TODO Auto-generated catch block
		// // e.printStackTrace();
		// // } catch (SocketException e) {
		// // // TODO Auto-generated catch block
		// // e.printStackTrace();
		// // }
		// } else {
		if (seek.postControlAction()) {

			return;
		} else {
			UPnPStatus err = seek.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
		}
		// }
		// }
	}

	// 取得播放进度
	private void getPositionInfo() {
		func = GET_POSITION_INFO;
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
				+ "u:GetPositionInfo xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\""
				+ right_tringle + letf_tringle + "InstanceID" + right_tringle
				+ "0" + letf_tringle + "/InstanceID" + right_tringle
				+ letf_tringle + "/u:GetPositionInfo" + right_tringle
				+ letf_tringle + "/s:Body" + right_tringle + letf_tringle
				+ "/s:Envelope" + right_tringle;
		// if (UserNow.current().remote) {
		// // netUrg = new HTTPNetURG();
		// // netUrg.setListener(CtMobileToTvActivity.this);
		// // try {
		// // DLNAData.current().DLNA_get_type =
		// // DLNAData.current().GET_POSITION_INFO_TYPE;
		// // netUrg.request(DLNAData.current().SOAP_Action_body);
		// // } catch (HttpHostConnectException e) {
		// // e.printStackTrace();
		// // } catch (ConnectTimeoutException e) {
		// // e.printStackTrace();
		// // } catch (SocketException e) {
		// // e.printStackTrace();
		// // } catch (SocketTimeoutException e) {
		// // e.printStackTrace();
		// // }
		// } else {
		net = new HTTPNet();
		net.setListener(RemoteControllerActivityNew.this);
		try {

			DLNAData.current().DLNA_get_type = DLNAData.current().GET_POSITION_INFO_TYPE;
			net.request(
					"http://"
							+ DLNAData.current().SOAP_Address
							+ ":"
							+ DLNAData.current().SOAP_Port
							// + "/AVTransport/control",
							+ AllUrlParser.checkDeviceURL(
									DLNAData.current().AVT.getControlURL(),
									false), DLNAData.current().SOAP_Port,
					DLNAData.current().SOAP_Action_body);
		} catch (HttpHostConnectException e) {
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		// }

	}

	// 取得播放状态
	private void GetTransportInfo() {
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
				+ "u:GetTransportInfo xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\""
				+ right_tringle + letf_tringle + "InstanceID" + right_tringle
				+ "0" + letf_tringle + "/InstanceID" + right_tringle
				+ letf_tringle + "/u:GetTransportInfo" + right_tringle
				+ letf_tringle + "/s:Body" + right_tringle + letf_tringle
				+ "/s:Envelope" + right_tringle;
		// if (UserNow.current().remote) {
		// // netUrg = new HTTPNetURG();
		// // netUrg.setListener(CtMobileToTvActivity.this);
		// // try {
		// // DLNAData.current().DLNA_get_type =
		// // DLNAData.current().GET_TRANSPORT_INFO_TYPE;
		// // netUrg.request(DLNAData.current().SOAP_Action_body);
		// // } catch (HttpHostConnectException e) {
		// // e.printStackTrace();
		// // } catch (ConnectTimeoutException e) {
		// // e.printStackTrace();
		// // } catch (SocketException e) {
		// // e.printStackTrace();
		// // } catch (SocketTimeoutException e) {
		// // e.printStackTrace();
		// //
		// // }
		// } else {
		net = new HTTPNet();
		net.setListener(RemoteControllerActivityNew.this);
		try {
			DLNAData.current().DLNA_get_type = DLNAData.current().GET_TRANSPORT_INFO_TYPE;
			net.request(
					"http://"
							+ DLNAData.current().SOAP_Address
							+ ":"
							+ DLNAData.current().SOAP_Port
							// + "/AVTransport/control",
							+ AllUrlParser.checkDeviceURL(
									DLNAData.current().AVT.getControlURL(),
									false), DLNAData.current().SOAP_Port,
					DLNAData.current().SOAP_Action_body);
		} catch (HttpHostConnectException e) {
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		// }
	}

	// get指令的请求地址
	private String getURL = "www.baidu.com";
	// getz指令端口
	private int getPort = 0;

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
		// if (UserNow.current().remote) {
		// // netUrg = new HTTPNetURG();
		// // netUrg.setListener(CtMobileToTvActivity.this);
		// // try {
		// // DLNAData.current().DLNA_get_type =
		// // DLNAData.current().GET_VOLUME_TYPE;
		// // netUrg.request(DLNAData.current().SOAP_Action_body);
		// // } catch (HttpHostConnectException e) {
		// // e.printStackTrace();
		// // } catch (ConnectTimeoutException e) {
		// // e.printStackTrace();
		// // } catch (SocketException e) {
		// // e.printStackTrace();
		// // } catch (SocketTimeoutException e) {
		// // e.printStackTrace();
		// // }
		// } else {
		net = new HTTPNet();
		net.setListener(RemoteControllerActivityNew.this);
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
		// }
	}

	/**
	 * 初始化页面
	 */
	private void initUI() {

		findViewById(R.id.back).setOnClickListener(this);
		// selectedTime = (TextView) findViewById(R.id.selscted_time);
		title = (TextView) findViewById(R.id.title);
		title.setClickable(true);
		title.setOnClickListener(this);
		String name = "";
		// controllerLayout = (LinearLayout) findViewById(R.id.controller);
		// seekbarLayout = (RelativeLayout) findViewById(R.id.dlna_seekbar);
		Log.e("title-length", DLNAData.current().nowProgramName + "\n"
				+ DLNAData.current().nowProgramName.length() + "");
		if (DLNAData.current().nowProgramName.length() > 18) {
			name = DLNAData.current().nowProgramName.substring(0, 18) + "...";
			title.setTextSize(18);
		} else {
			name = DLNAData.current().nowProgramName;
			title.setTextSize(18);
		}

		title.setText(name);
		// volumeRate = (TextView) findViewById(R.id.pd_m2tv_vol_txt);
		// volumeRate.setText("...");
		findViewById(R.id.backward).setOnClickListener(this);
		playPause = (ImageButton) findViewById(R.id.play);
		playPause.setOnClickListener(this);
		findViewById(R.id.forward).setOnClickListener(this);
		stopBtn = (ImageButton) findViewById(R.id.stop);
		stopBtn.setOnClickListener(this);

		soundControl = (Button) findViewById(R.id.pd_m2tv_sound);
		soundControl.setOnClickListener(this);
		volSeekBar = (CircleProgressDown) findViewById(R.id.volumeseekbar);
		volSeekBar.setCallfuc(volCallBack);

		if (DeviceData.getInstance().getSelectedDevice().getFriendlyName()
				.equals("DaPingMu(Q-1000DF)")) {
			volSeekBar.setMaxProgress(15);
		} else if (DeviceData.getInstance().getSelectedDevice()
				.getFriendlyName().equals("eHomeMediaCenter")) {
			volSeekBar.setMaxProgress(15);
		}

		positionSeekBar = (CircleProgress) findViewById(R.id.positionseekbar);
		positionSeekBar.setCallfuc(posCallBack);
		textPlayed = (TextView) findViewById(R.id.currenttime);
		textDuration = (TextView) findViewById(R.id.endtime);
		textDuration.setText(DLNAData.current().nowSTBPlayPosition);

		if (isLivePlay) {
			positionSeekBar.setMainProgress(0);
			textPlayed.setText("00:00:00");
			textDuration.setText("99:99:99");
		}

		// 根据不同设备取舍对getPostionInfo的发送
		if (DeviceData.getInstance().getSelectedDevice().getFriendlyName()
				.equals("DaPingMu(Q-1000DF)")) {
			isNeedSendGetPositionInfo = true;
		} else if (DeviceData.getInstance().getSelectedDevice()
				.getFriendlyName().equals("Realtek Embedded UPnP Render()")) {
			isNeedSendGetPositionInfo = false;
		} else if (DeviceData.getInstance().getSelectedDevice()
				.getFriendlyName().equals("eHomeMediaCenter")) {
			isNeedSendGetPositionInfo = true;
		} else if (DeviceData.getInstance().getSelectedDevice()
				.getFriendlyName().contains("MPO-")) {
			isNeedSendGetPositionInfo = true;
		} else {
			isNeedSendGetPositionInfo = true;
		}

		if (isNeedSendGetPositionInfo) {
			t = new ManualThread();
		}
	}

	private DownCircleCallBack volCallBack = new DownCircleCallBack() {
		@Override
		public void downCircleCallBack() {
			// if (hasVolumeAlready) {
			volume = volSeekBar.getMainProgress();
			processSetVolume();
			// }
			// else {
			// volSeekBar.setMainProgress(0);
			// }
		}
	};

	private UpCircleCallBack posCallBack = new UpCircleCallBack() {

		@Override
		public void upCircleCallBack() {

			if (!isLivePlay) {
				if (positonProgressNow == allDurationMillionSeconds && playing) {
					handler.sendEmptyMessage(PROGRESS_REFRESH);
					handler.sendEmptyMessage(GET_VIDEO_POSITION);

					textPlayed.setText(DLNAData.current().programDuration);

					Toast.makeText(getApplicationContext(), "视频播放结束",
							Toast.LENGTH_SHORT).show();
					changePlay = false;
					stopPlay();
				}

				int i = positonProgressNow;

				i /= 1000;
				minute = (i / 60);
				hour = minute / 60;
				second = (i % 60);
				minute %= 60;
				currentPosition = positonProgressNow;

				seekTime = String
						.format("%02d:%02d:%02d", hour, minute, second);
				textPlayed.setText(seekTime);

				// Log.e("onPositionProgressChanged", seekTime);

				handler.removeMessages(PROGRESS_REFRESH);
				handler.removeMessages(GET_VIDEO_POSITION);
				positionSeekBar.setMainProgress(positonProgressNow);
				textPlayed.setText(seekTime);

				if (playing) {
					processSeek();
					handler.sendEmptyMessage(PROGRESS_REFRESH);
					handler.sendEmptyMessage(GET_VIDEO_POSITION);
				} else {
					positionSeekBar.setMainProgress(0);
					textPlayed.setText("00:00:00");
				}

			} else {

				positionSeekBar.setMainProgress(0);
				textPlayed.setText("00:00:00");
				textDuration.setText("99:99:99");
			}
		}

		@Override
		public void upCircleCallBackStartSeek() {

			if (isLivePlay) {
				positionSeekBar.setMainProgress(0);
				textPlayed.setText("00:00:00");
				textDuration.setText("99:99:99");
			} else {

				positonProgressNow = positionSeekBar.getMainProgress();

				int i = positonProgressNow;
				i /= 1000;
				minute = (i / 60);
				hour = minute / 60;
				second = (i % 60);
				minute %= 60;
				currentPosition = positonProgressNow;

				if (!playing
						&& positonProgressNow != positionSeekBar
								.getMaxProgress()) {
					Random ran = new Random(System.currentTimeMillis());
					second = ran.nextInt(59);
				}
				seekTime = String
						.format("%02d:%02d:%02d", hour, minute, second);
				textPlayed.setText(seekTime);
			}

		}
	};

	private int positonProgressNow = 0;

	// 播放进度条控制
	// private OnSeekBarChangeListener positionSeekBarListener = new
	// OnSeekBarChangeListener() {
	//
	// @Override
	// public void onStopTrackingTouch(SeekBar seekBar) {
	//
	// if (positonProgressNow == allDurationMillionSeconds && playing) {
	// handler.sendEmptyMessage(PROGRESS_REFRESH);
	// handler.sendEmptyMessage(GET_VIDEO_POSITION);
	//
	// textPlayed.setText(DLNAData.current().programDuration);
	//
	// Toast.makeText(getApplicationContext(), "视频播放结束",
	// Toast.LENGTH_SHORT).show();
	// changePlay = false;
	// stopPlay();
	// }
	//
	// int i = positonProgressNow;
	//
	// i /= 1000;
	// minute = (i / 60);
	// hour = minute / 60;
	// second = (i % 60);
	// minute %= 60;
	// currentPosition = positonProgressNow;
	//
	// seekTime = String.format("%02d:%02d:%02d", hour, minute, second);
	// textPlayed.setText(seekTime);
	//
	// // Log.e("onPositionProgressChanged", seekTime);
	//
	// handler.removeMessages(PROGRESS_REFRESH);
	// handler.removeMessages(GET_VIDEO_POSITION);
	// positionSeekBar.setMainProgress(positonProgressNow);
	// textPlayed.setText(seekTime);
	//
	// if (playing) {
	// seek();
	// handler.sendEmptyMessage(PROGRESS_REFRESH);
	// handler.sendEmptyMessage(GET_VIDEO_POSITION);
	// } else {
	// positionSeekBar.setMainProgress(0);
	// textPlayed.setText("00:00:00");
	// }
	// selectedTime.setVisibility(View.GONE);
	// }
	//
	// @Override
	// public void onStartTrackingTouch(SeekBar seekBar) {
	// selectedTime.setVisibility(View.VISIBLE);
	// }
	//
	// @Override
	// public void onProgressChanged(SeekBar seekBar, int progress,
	// boolean fromUser) {
	//
	// positonProgressNow = progress;
	// if (fromUser == true) {
	//
	// int i = positonProgressNow;
	// i /= 1000;
	// minute = (i / 60);
	// hour = minute / 60;
	// second = (i % 60);
	// minute %= 60;
	// currentPosition = positonProgressNow;
	//
	// if (!playing
	// && positonProgressNow != positionSeekBar
	// .getMaxProgress()) {
	// Random ran = new Random(System.currentTimeMillis());
	// second = ran.nextInt(59);
	// }
	// seekTime = String
	// .format("%02d:%02d:%02d", hour, minute, second);
	// textPlayed.setText(seekTime);

	// Log.e("onPositionProgressChanged", progress + "");

	// TODO:暂时取不出值
	// getPositionInfo();

	// if (progress == allDurationMillionSeconds) {
	// handler.sendEmptyMessage(PROGRESS_REFRESH);
	// handler.sendEmptyMessage(GET_VIDEO_POSITION);
	//
	// textPlayed.setText(ProgramData.current().duration);
	//
	// Toast.makeText(getApplicationContext(), "视频播放结束",
	// Toast.LENGTH_SHORT).show();
	// changePlay = false;
	// stopPlay();
	// }
	//
	// int i = progress;
	//
	// i /= 1000;
	// minute = (int) (i / 60);
	// hour = minute / 60;
	// second = (int) (i % 60);
	// minute %= 60;
	//
	// currentPosition = progress;
	//
	// seekTime = String
	// .format("%02d:%02d:%02d", hour, minute, second);
	// textPlayed.setText(seekTime);
	//
	// Log.e("onPositionProgressChanged", seekTime);
	//
	// handler.removeMessages(PROGRESS_REFRESH);
	// handler.removeMessages(GET_VIDEO_POSITION);
	// positionSeekBar.setProgress(progress);
	// textPlayed.setText(seekTime);
	//
	// seek();
	//
	// handler.sendEmptyMessage(PROGRESS_REFRESH);
	// handler.sendEmptyMessage(GET_VIDEO_POSITION);

	// }
	// }
	// };

	private void getProtocolInfo() {
		// if (UserNow.current().remote) {
		// ActionRequest ctrlReq = new ActionRequest();
		// ctrlReq.setRequest(getProtocolInfo,
		// getProtocolInfo.getInputArgumentList());
		// remotePost(ctrlReq);
		// } else {
		if (getProtocolInfo.postControlAction()) {
			ArgumentList outArgList = getProtocolInfo.getOutputArgumentList();
			outArgList.getArgument("Sink").getUserData();

		} else {
			UPnPStatus err = getProtocolInfo.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
		}
		// }
	}

	protected void processSetVolume() {
		volSeekBar.setEnabled(false);
		func = SETVOLUME;
		firstDLNATask t = new firstDLNATask();
		t.execute(SETVOLUME);
	}

	private void prepareForConnection() {
		ArgumentList pal = prepareForConnection.getArgumentList();
		pal.getArgument("RemoteProtocolInfo").setValue(
				"<rtsp-rtp-udp>:<*>:<application/itv>:<*>");
		pal.getArgument("PeerConnectionManager").setValue("");
		pal.getArgument("PeerConnectionID").setValue(-1);
		pal.getArgument("Direction").setValue("Input");
		prepareForConnection.setInArgumentValues(pal);
		// if (UserNow.current().remote) {
		// ActionRequest ctrlReq = new ActionRequest();
		// ctrlReq.setRequest(prepareForConnection,
		// prepareForConnection.getInputArgumentList());
		// remotePost(ctrlReq);
		// } else {
		if (prepareForConnection.postControlAction()) {
			ArgumentList outArgList = prepareForConnection
					.getOutputArgumentList();
			connectionId = outArgList.getArgument("ConnectionID")
					.getIntegerValue();
			AVTransportID = outArgList.getArgument("AVTransportID")
					.getIntegerValue();
			setTransportURL();
		} else {
			UPnPStatus err = prepareForConnection.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
		}
		// }
	}

	// 图片取回缩放范围，视频取回音量范围
	private void getAllowedTransforms() {
		ArgumentList gal = getAllowedTransforms.getArgumentList();
		gal.getArgument("InstanceID").setValue(AVTransportID);
		getAllowedTransforms.setInArgumentValues(gal);
		// if (UserNow.current().remote) {
		// ActionRequest ctrlReq = new ActionRequest();
		// ctrlReq.setRequest(getAllowedTransforms,
		// getAllowedTransforms.getInputArgumentList());
		// remotePost(ctrlReq);
		// } else {
		if (getAllowedTransforms.postControlAction()) {
			ArgumentList outArgList = getAllowedTransforms
					.getOutputArgumentList();
			Argument arg = outArgList
					.getArgument("CurrentAllowedTransformSettings");
			parserAllowedTransforms(arg);
		} else {
			UPnPStatus err = getAllowedTransforms.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
		}
		// }
	}

	private void parserAllowedTransforms(Argument arg) {
		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = dfactory.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(arg.getValue().getBytes(
					"UTF-8"));
			Document doc = documentBuilder.parse(is);
			NodeList TransformList = doc.getElementsByTagName("TransformList");
			for (int l = 0; l < TransformList.getLength(); ++l) {
				Node childNode = TransformList.item(l);
				if (childNode.getNodeName().equals("Volume")) {
					Node allowedValueRange = childNode.getFirstChild();
					minimum = Integer.parseInt(allowedValueRange
							.getFirstChild().getNodeValue());
					maxmum = Integer.parseInt(allowedValueRange.getLastChild()
							.getNodeValue());
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void connectionComplete() {
		ArgumentList cal = connectionComplete.getArgumentList();
		cal.getArgument("InstanceID").setValue(AVTransportID);
		connectionComplete.setInArgumentValues(cal);
		// if (UserNow.current().remote) {
		// ActionRequest ctrlReq = new ActionRequest();
		// ctrlReq.setRequest(connectionComplete,
		// connectionComplete.getInputArgumentList());
		// remotePost(ctrlReq);
		// } else {
		if (connectionComplete.postControlAction()) {
			ArgumentList outArgList = connectionComplete
					.getOutputArgumentList();
			outArgList.getArgument("CurrentTransportState").getValue();
			outArgList.getArgument("CurrentTransportStatus").getValue();
			outArgList.getArgument("Speed").getIntegerValue();

		} else {
			UPnPStatus err = connectionComplete.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
		}
		// }
	}

	private void setVolume() {
		func = SETVOLUME;
		DLNAData.current().DLNA_post_type = SETVOLUME;
		// setVolume = RCS.getAction("SetVolume");
		setVolume = DLNAData.current().getSetVolume();
		ArgumentList setal = setVolume.getArgumentList();
		setal.getArgument("InstanceID").setValue("0");
		setal.getArgument("DesiredVolume").setValue(volume);
		setal.getArgument("Channel").setValue("Master");
		setVolume.setInArgumentValues(setal);
		// if (UserNow.current().remote) {
		// ActionRequest ctrlReq = new ActionRequest();
		// ctrlReq.setRequest(setVolume, setal);
		// // UserNow.current().SOAP_Address = "/RenderingControl/control";
		// UserNow.current().SOAP_Address = AllUrlParser.checkDeviceURL(
		// DLNAData.current().RCS.getControlURL(), false);
		// remotePost(ctrlReq);
		// } else {
		if (setVolume.postControlAction()) {

			Log.e("setVolume", "setVolume - ok  value  is" + volume);
		} else {
			Log.e("setVolume", "setVolume - error value  is" + volume);

			UPnPStatus err = setVolume.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
		}
		// }
	}

	private void notifyError(String errorMessage) {
		Message msg = new Message();
		msg.what = MESSAGE_ERROR;
		msg.obj = errorMessage;
		handler.sendMessage(msg);
	}

	@Override
	public void onClick(View v) {
		vb.vibrate(100);

		switch (v.getId()) {
		case R.id.back:
			// all.startAnimation(close2bottom);
			handler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 400);
			setResult(RESULT_OK);
			break;
		case R.id.title:
			finish();
			break;
		case R.id.backward:
			if (playing) {
				Toast.makeText(getApplicationContext(), "-30秒",
						Toast.LENGTH_SHORT).show();

				String[] my = textPlayed.getText().toString().split(":");
				int hour = Integer.parseInt(my[0]);
				int min = Integer.parseInt(my[1]);
				int sec = Integer.parseInt(my[2]);
				int totalSec = hour * 3600 + min * 60 + sec;
				totalSec = totalSec * 1000;
				totalSec = totalSec - 30000;

				int i = totalSec;
				i /= 1000;
				minute = (i / 60);
				hour = minute / 60;
				second = (i % 60);
				minute %= 60;
				currentPosition = positonProgressNow;
				seekTime = String
						.format("%02d:%02d:%02d", hour, minute, second);
				processSeek();
			} else {

			}
			break;
		case R.id.play:
			// showWaitDialog();
			handler.sendEmptyMessage(MSG_OPEN);
			new Thread(new Runnable() {

				@Override
				public void run() {

					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					handler.sendEmptyMessage(MSG_PROCESS_PLAY);
				}
			}).start();
			break;
		case R.id.forward:
			if (playing) {
				Toast.makeText(getApplicationContext(), "+30秒",
						Toast.LENGTH_SHORT).show();

				String[] my1 = textPlayed.getText().toString().split(":");
				int hour1 = Integer.parseInt(my1[0]);
				int min1 = Integer.parseInt(my1[1]);
				int sec1 = Integer.parseInt(my1[2]);
				int totalSec1 = hour1 * 3600 + min1 * 60 + sec1;
				totalSec1 = totalSec1 * 1000;
				totalSec1 = totalSec1 + 30000;

				int i1 = totalSec1;
				i1 /= 1000;
				minute = (i1 / 60);
				hour = minute / 60;
				second = (i1 % 60);
				minute %= 60;
				currentPosition = positonProgressNow;
				seekTime = String
						.format("%02d:%02d:%02d", hour, minute, second);
				processSeek();
			} else {

			}
			break;
		// case R.id.ct_dlna_back:
		// finish();
		// break;

		case R.id.stop:
			changePlay = false;
			// stopPlay();
			btnStopPlay();
			break;

		case R.id.pd_m2tv_sound:
			// getMute();
			String st = null;

			switch (mute) {
			case 0:
				st = "FALSE";
				mute = 1;
				break;

			case 1:
				st = "TRUE";
				mute = 0;
				break;

			default:
				break;
			}

			setMute(st);

			// try {
			// Thread.sleep(3000);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// getVolume();
			//
			// try {
			// Thread.sleep(3000);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// GetTransportInfo();
			//
			// try {
			// Thread.sleep(3000);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// getPositionInfo();
			break;
		default:
			break;
		}
	}

	// 切换节目提示框
	protected void dialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("其他节目播放中，是否先停止其播放");
		builder.setCancelable(false);
		builder.setIcon(R.drawable.icon);
		builder.setPositiveButton("确定",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// changePlay = true;
						// stopPlay();
						// if (!UserNow.current().remote) {
						// setTransportURL();
						// if (setTranspotURI_Error) {
						// if (!DLNAData.current().hasSetTransportURI) {
						// setTransportURL();
						// play();
						// }
						// } else {
						// playOnly();
						// }
						// }

						// if (UserNow.current().remote) {
						// changePlay = true;
						// stopPlay();
						// } else {
						// if (setTranspotURI_Error) {
						// if (!DLNAData.current().hasSetTransportURI) {
						changePlay = true;
						// stopPlay();
						func = SETTRANSPORTURL;
						firstDLNATask t = new firstDLNATask();
						t.execute(SETTRANSPORTURL);
						// play();
						// }
						// } else {
						// play();
						// }
						// playPause
						// .setImageResource(R.drawable.dlna_pause);
						// handler.sendEmptyMessage(PROGRESS_REFRESH);
						// }
					}
				});
		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						dismissWaitDialog();
						finish();
					}
				});
		builder.create().show();

	}

	// @Override
	// public void onProgressChanged(SeekBar seekBar, int progress,
	// boolean fromUser) {
	//
	// positonProgressNow = progress;
	// if (fromUser == true) {
	// // Log.e("onProgressChanged", progress + "");
	//
	// String str = "";
	// if (DeviceData.getInstance().getSelectedDevice().getFriendlyName()
	// .equals("DaPingMu(Q-1000DF)")) {
	// str = progress + "";
	// } else if (DeviceData.getInstance().getSelectedDevice()
	// .getFriendlyName().equals("eHomeMediaCenter")) {
	// str = progress + "";
	// } else {
	// str = progress + "%";
	// }
	// volumeRate.setText(str);

	// TODO: 最终打开
	// setVolume(progress);
	// handler.removeMessages(PROGRESS_REFRESH);
	// positionSeekBar.setProgress(progress);
	// handler.sendEmptyMessage(PROGRESS_REFRESH);
	// }
	// }

	// @Override
	// public void onStartTrackingTouch(SeekBar seekBar) {
	// }
	//
	// @Override
	// public void onStopTrackingTouch(SeekBar seekBar) {
	// if (hasVolumeAlready) {
	// setVolume(positonProgressNow);
	// } else {
	// volSeekBar.setMainProgress(0);
	// // volumeRate.setText("...");
	// }
	// }

	// 处理播放暂停的点击事件
	private void processPlayPause() {
		if (DLNAData.current().hasPlayingOnTV) {
			if (playing) {
				Log.e("CtM2TV", "palying");
				pause();
				handler.removeMessages(GET_VIDEO_POSITION);
				handler.removeMessages(PROGRESS_REFRESH);
			} else {
				Log.e("CtM2TV", "pause");
				if (!DLNAData.current().hasSetTransportURI) {
					changePlay = true;
					func = SETTRANSPORTURL;
					firstDLNATask t = new firstDLNATask();
					t.execute(SETTRANSPORTURL);
				} else
					playOnly();
			}
		} else {
			func = SETTRANSPORTURL;
			firstDLNATask t = new firstDLNATask();
			t.execute(SETTRANSPORTURL);

			// func = STOP;
			// firstDLNATask t = new firstDLNATask();
			// t.execute(STOP);
		}
	}

	@Override
	public void onDLNAGetError(String errorMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDLNAGetResponse(StatusLine statusLine, Header[] headers,
			HttpEntity entit) {
		try {
			String s = EntityUtils.toString(entit);
			Log.e("ProgramDataPreViewActviity - 648", s);
			net.removeListener();

			switch (DLNAData.current().DLNA_get_type) {
			// getMute
			case 1:
				try {
					GetMutParser.parse(new ByteArrayInputStream(s.getBytes()));
					// 未知静音状态
					if (DLNAData.current().CurrentMute == null) {
						handler.sendEmptyMessage(GET_MUTE_ERROR);
					} else {
						// 当前未静音
						if (DLNAData.current().CurrentMute.equals("0")) {
							mute = 0;
						}
						// 当前已静音
						else if (DLNAData.current().CurrentMute.equals("1")) {
							mute = 1;
						}
					}
					handler.sendEmptyMessage(MUTE_OK);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
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
			// getTransportInfo
			case 4:
				try {
					GetTransportInfoParser.parse(new ByteArrayInputStream(s
							.getBytes()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				handler.sendEmptyMessage(GET_TRRANSPORT_INFO_OK);
				break;
			// getPositionInfo
			case 3:
				Log.e("onDLNAGetResponse", "getPositionInfo");
				try {
					GetPositonInfoParser.parse(new ByteArrayInputStream(s
							.getBytes()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				Log.e("onUpnpResponse", "getPositionInfo - ok");
				handler.sendEmptyMessage(GET_POSITION_INFO_OK);
				break;
			default:
				break;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		switch (DLNAGetType) {
		case GET_MUTE:

			break;
		case GET_VOLUME:

			break;
		case GET_TRRANSPORT_INFO:

			break;
		case GET_POSITION_INFO:

			break;

		default:
			break;
		}

	}

	@Override
	public void onDLNAGetBegin() {

	}

	// DLNA操作等待框
	private void showWaitDialog() {
		dismissWaitDialog();

		progressDialog = ProgressDialog.show(RemoteControllerActivityNew.this,
				"", "正在处理，请稍候...", true, true,
				new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
					}
				});

		progressDialog.setCanceledOnTouchOutside(false);

		// Toast.makeText(getApplicationContext(), "在处理，请稍候...",
		// Toast.LENGTH_LONG)
		// .show();

	}

	// DLNA操作等待框取消
	private void dismissWaitDialog() {

		try {
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	private ProgressDialog tipsDialog;

	private void showTipsDialog() {
		if (tipsDialog != null) {
			tipsDialog.dismiss();
			tipsDialog = null;
		}

		tipsDialog = ProgressDialog.show(RemoteControllerActivityNew.this, "",
				"处理中，请稍后...", true, true,
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			switch (requestCode) {

			default:
				break;
			}
		} else {

		}
	}

	class ManualThread extends Thread {
		private Object mPauseLock;
		private boolean mPauseFlag;

		public ManualThread(Runnable runnable) {
			super(runnable);
			mPauseLock = new Object();
			mPauseFlag = false;

		}

		public ManualThread() {
			mPauseLock = new Object();
			mPauseFlag = false;
		}

		public void onPause() {
			synchronized (mPauseLock) {
				mPauseFlag = true;
			}
		}

		public void onResume() {
			synchronized (mPauseLock) {
				mPauseFlag = false;
				mPauseLock.notifyAll();
			}
		}

		public void pauseThread() {
			synchronized (mPauseLock) {
				if (mPauseFlag) {
					try {
						mPauseLock.wait();
					} catch (Exception e) {
						Log.v("thread", "fails");
					}
				}
			}
		}

		@Override
		public void run() {
			super.run();

			try {
				while (t != null && !t.interrupted() && playing) {

					Thread.sleep(GET_POSITION_INFO_DELAY);
					if (!DLNA_Api_lock) {
						Thread.sleep(GET_POSITION_INFO_DELAY);
						Message msg = new Message();
						msg.what = START_SHARE;

						// if (DeviceData.getInstance().getSelectedDevice()
						// .getFriendlyName().equals("DaPingMu(Q-1000DF)")) {
						// handler.sendMessage(msg);
						// }

						if (isNeedSendGetPositionInfo) {
							handler.sendMessage(msg);
						}
					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			// all.startAnimation(close2bottom);
			handler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 400);
			setResult(RESULT_OK);
			return true;

		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	// @Override
	// public void onResponse(ActionResponse ctrlRes) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onError(String errorMessage) {
	// // TODO Auto-generated method stub
	//
	// }

	class firstDLNATask extends AsyncTask<Integer, Integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.e("AsyncTask", "step - 1");
			switch (func) {
			case SETTRANSPORTURL:
				handler.sendEmptyMessage(MSG_SHOW);
				handler.removeMessages(GET_VIDEO_POSITION);
				handler.removeMessages(PROGRESS_REFRESH);
				break;
			case PLAY:

				break;
			case PAUSE:

				break;
			case STOP:

				break;
			case SEEK:

				break;
			case SETVOLUME:

				break;
			default:
				break;
			}
		}

		@Override
		protected String doInBackground(Integer... params) {
			Log.e("AsyncTask", "step - 2");
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
				break;
			case PAUSE:

				break;
			case STOP:
				try {
					stopPlay();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				break;
			case SEEK:
				try {
					seek();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				break;
			case SETVOLUME:
				try {
					setVolume();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
			return "执行完毕";
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			switch (func) {
			case SETTRANSPORTURL:

				break;
			case PLAY:

				break;
			case PAUSE:

				break;
			case STOP:

				break;
			case SEEK:

				break;
			case SETVOLUME:

				break;
			default:
				break;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Log.e("AsyncTask", "step - 3");
			super.onPostExecute(result);
			handler.sendEmptyMessage(MSG_DISMISS);
			switch (func) {
			case SETTRANSPORTURL:
				handler.sendEmptyMessage(ASYNC_PLAY);
				break;
			case PLAY:
				handler.sendEmptyMessage(PLAY_OK);
				break;
			case PAUSE:

				break;
			case STOP:
				handler.sendEmptyMessage(ASYNC_SET_TRANS_URL);
				break;
			case SEEK:
				handler.sendEmptyMessage(SEEK_OK);
				break;
			case SETVOLUME:
				handler.sendEmptyMessage(SET_VOLUME_OK);
				break;
			default:
				break;
			}
		}

	}

	@Override
	public void getUpnpDevice() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpnpError(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpnpResponse() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onStop() {
		super.onStop();
		handler.removeMessages(MSG_SHOW);
		handler.removeMessages(MSG_CLOSE);
	}
}
