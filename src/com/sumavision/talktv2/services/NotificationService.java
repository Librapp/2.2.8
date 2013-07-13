package com.sumavision.talktv2.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyAtActivity;
import com.sumavision.talktv2.activity.MyFansActivity;
import com.sumavision.talktv2.activity.MyPrivateMsgActivity;
import com.sumavision.talktv2.activity.MyReplyActivity;
import com.sumavision.talktv2.activity.ProgramNewActivity;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.utils.Constants;

/**
 * 
 * @author jianghao
 * @createTime 2012-7-5
 * @description 招呼私信服务
 * @changeLog 2013-3-17 by姜浩 修改推送获取后的跳转以及代码优化
 */
public class NotificationService extends Service implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = "NotificationService";

	private SharedPreferences spUser;
	private int userId;

	private Editor pushMsgEditor;

	private int privateMessageCount;
	private int addFenFriendCount;
	private int beiAtCount;
	private int replyCount;

	private NotificationManager notificationManager;
	public static final int NOTIFICATION_ID_PROGRAM = 102;
	/** 好友通知ID */
	public static final int NOTIFICATION_ID_FELLOW = 103;
	/** 私信通知ID */
	public static final int NOTIFICATION_ID_PRIVATE_MSG = 104;
	/** 被@通知ID */
	public static final int NOTIFICATION_ID_BEIAT = 105;
	/** 被回复通知ID */
	public static final int NOTIFICATION_ID_REPLY = 106;

	/** 连接线程 负责连接服务器 */
	private ConnectThread connectThread;
	/** 管理线程 负责连接后与服务器通信 */
	private ManageThread manageThread;
	/*
	 * 长连接状态常量
	 */
	private static final int MESSAGE_FAIL_CONNECT = 0;
	private static final int MESSAGE_FAIL_GET_STREAM = 1;
	private static final int MESSAGE_OVER = 2;
	private static final int MESSAGE_ERROR_RETURNVALUE = 3;
	private static final int MESSAGE_ERROR_USER = 4;
	private static final int MESSAGE_RECONNECT = 6;
	private static final int MESSAGE_SETUP_CONNECT = 7;
	private static final int MESSAGE_CLOSE = 8;
	private static final int MESSAGE_NEW_PUSH_PROGRAM_INFO = 9;

	private static final int MESSAGE_FELLOW = 10;
	private static final int MESSAGE_PRIVATE_MSG = 11;
	private static final int MESSAGE_BEIAT = 12;
	private static final int MESSAGE_REPLY = 13;

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			String errMsg;
			switch (msg.what) {
			case MESSAGE_FAIL_GET_STREAM:
				Log.e(TAG, "fail to get inputStream or outputStream ");
				handler.sendEmptyMessageDelayed(MESSAGE_RECONNECT, 3000);
				break;
			case MESSAGE_FAIL_CONNECT:
				errMsg = getResources().getString(
						R.string.notification_fail_connect);
				Log.e(TAG, errMsg);
				handler.sendEmptyMessageDelayed(MESSAGE_RECONNECT, 6000 * 100);
				break;
			case MESSAGE_ERROR_RETURNVALUE:
				errMsg = getResources().getString(
						R.string.notification_err_returnvalue);
				Log.e(TAG, errMsg);
				break;
			case MESSAGE_ERROR_USER:
				errMsg = getResources().getString(
						R.string.notification_err_user);
				Log.e(TAG, errMsg);
				break;
			case MESSAGE_SETUP_CONNECT:
				errMsg = getResources().getString(
						R.string.notification_setup_connection);
				Log.e(TAG, errMsg);
				break;
			case MESSAGE_RECONNECT:
				connect();
				break;
			case MESSAGE_CLOSE:
				errMsg = getResources().getString(
						R.string.notification_close_connection);
				Log.e(TAG, errMsg);
				break;
			case MESSAGE_NEW_PUSH_PROGRAM_INFO:
				notifyProgram((Integer) msg.obj);
				break;
			case MESSAGE_FELLOW:
				sendFellowNotification();
				break;
			case MESSAGE_PRIVATE_MSG:
				sendPrivateMsgNotification();
				break;
			case MESSAGE_BEIAT:
				sendBeiAtNotification();
				break;
			case MESSAGE_REPLY:
				sendReplyNotification();
				break;
			default:
				break;
			}
		};
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		spUser = getSharedPreferences("userInfo", 0);
		userId = spUser.getInt("userID", 0);
		SharedPreferences pushMsgPreferences = getSharedPreferences(
				Constants.pushMessage, 0);
		pushMsgEditor = pushMsgPreferences.edit();
		spUser.registerOnSharedPreferenceChangeListener(this);

		pushMsgIndicatorPreference = getSharedPreferences("pusMsgIndicator", 0);
		pushMsgIndicatorPreference
				.registerOnSharedPreferenceChangeListener(pushMsgIndicatorListener);
		if (userId != 0 && getPushMsgState()) {
			connect();
		}

		return super.onStartCommand(intent, flags, startId);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
			handler.sendEmptyMessage(MESSAGE_CLOSE);
		}
		if (manageThread != null) {
			manageThread.cancel();
		}
		spUser.unregisterOnSharedPreferenceChangeListener(this);
		pushMsgIndicatorPreference
				.unregisterOnSharedPreferenceChangeListener(pushMsgIndicatorListener);
	}

	/**
	 * 建立连接
	 */
	private void connect() {
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}
		// connectThread = new ConnectThread("172.16.16.78", 10000);
		connectThread = new ConnectThread("59.151.82.78", 10000);
		connectThread.start();
	}

	private class ConnectThread extends Thread {
		private String ip;
		private int port;
		private Socket socket;

		public ConnectThread(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		@Override
		public void run() {
			InetSocketAddress address = new InetSocketAddress(ip, port);
			socket = new Socket();
			try {
				socket.connect(address, 60000);
			} catch (IOException e) {
				e.printStackTrace();
				handler.sendEmptyMessage(MESSAGE_FAIL_CONNECT);
				socket = null;
			}
			if (socket != null) {
				manageThread = new ManageThread(socket);
				manageThread.start();
			}
		}

		public void cancel() {
			try {
				if (socket != null) {
					socket.close();
					handler.sendEmptyMessage(MESSAGE_CLOSE);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ManageThread extends Thread {
		private final Socket socket;
		private InputStream is;
		private OutputStream os;

		public ManageThread(Socket socket) {
			this.socket = socket;
			try {
				is = this.socket.getInputStream();
				os = this.socket.getOutputStream();
				handler.sendEmptyMessage(MESSAGE_SETUP_CONNECT);
			} catch (IOException e) {
				is = null;
				os = null;
				handler.sendEmptyMessage(MESSAGE_FAIL_GET_STREAM);
			}
		}

		@Override
		public void run() {
			String request = getMsgPushRequest("ConnectCommon",
					JSONMessageType.SOURCE + "", "" + userId,
					JSONMessageType.APP_VERSION);
			write(request);
			byte[] buffer = new byte[512];
			int length;
			try {
				while ((length = is.read(buffer)) != -1) {
					String returnValue = new String(buffer, 0, length);
					parse(returnValue);
				}
			} catch (Exception e) {
				handler.sendEmptyMessage(MESSAGE_OVER);
			}
		}

		private void write(String message) {
			byte[] messageByte = message.getBytes();
			try {
				os.write(messageByte);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 客户端长连接请求JSONRequest
	 */
	private String getMsgPushRequest(String method, String client,
			String userId, String version) {
		JSONObject jsonHolder = new JSONObject();
		try {
			jsonHolder.put("method", method);
			jsonHolder.put("client", client);
			jsonHolder.put("userId", userId);
			jsonHolder.put("version", version);
		} catch (JSONException e) {
			return null;
		}

		return jsonHolder.toString();
	}

	private void parse(String returnValue) {
		int localprivateMessageCount = 0;
		int localaddFenFriendCount = 0;
		int localBeiAtCount = 0;
		int localReply = 0;
		privateMessageCount = 0;
		beiAtCount = 0;
		replyCount = 0;
		addFenFriendCount = 0;

		boolean isUserMsg = false;
		try {
			JSONObject returnJson = new JSONObject(returnValue);
			int code = returnJson.getInt("code");
			String method = returnJson.getString("method");
			if ("ConnectCommon".equals(method)) {
				return;
			}
			Log.e(TAG, returnValue);
			if (code == 1) {
				handler.sendEmptyMessage(MESSAGE_ERROR_RETURNVALUE);
				return;
			}
			JSONArray content = returnJson.optJSONArray("eventPush");
			if (content != null) {
				for (int i = 0; i < content.length(); i++) {
					JSONObject pushMsg = content.getJSONObject(i);
					int toObjectType = pushMsg.getInt("objectType");
					switch (toObjectType) {
					case 5:
						// TODO sixin
						localprivateMessageCount += 1;
						isUserMsg = true;
						pushMsgEditor
								.putBoolean(Constants.key_privateMsg, true);
						pushMsgEditor.commit();
						break;
					case 2:
						localReply += 1;
						isUserMsg = true;
						pushMsgEditor.putBoolean(Constants.key_reply, true);
						pushMsgEditor.commit();
						break;
					case 7:
						localaddFenFriendCount += 1;
						isUserMsg = true;
						pushMsgEditor.putBoolean(Constants.key_fans, true);
						pushMsgEditor.commit();
						break;
					case 8:
						localBeiAtCount += 1;
						isUserMsg = true;
						pushMsgEditor.putBoolean(Constants.key_beiAt, true);
						pushMsgEditor.commit();
						break;
					case 3:
						int tempId = pushMsg.getInt("objectId");
						Message msg = new Message();
						msg.what = MESSAGE_NEW_PUSH_PROGRAM_INFO;
						msg.obj = tempId;
						handler.sendMessage(msg);
					default:
						break;
					}
				}
			}
			JSONArray systemContent = returnJson.optJSONArray("systemPush");
			if (systemContent != null) {
				for (int i = 0; i < systemContent.length(); i++) {
					JSONObject pushMsg = systemContent.getJSONObject(i);
					int toObjectType = pushMsg.getInt("objectType");
					switch (toObjectType) {
					case 14:
						int tempId = pushMsg.getInt("objectId");
						Message msg = new Message();
						msg.what = MESSAGE_NEW_PUSH_PROGRAM_INFO;
						msg.obj = tempId;
						handler.sendMessage(msg);
					default:
						break;
					}
				}
			}
			if (localaddFenFriendCount > 0) {
				addFenFriendCount = localaddFenFriendCount;
				handler.sendEmptyMessage(MESSAGE_FELLOW);
			}
			if (localprivateMessageCount > 0) {
				privateMessageCount = localprivateMessageCount;
				handler.sendEmptyMessage(MESSAGE_PRIVATE_MSG);
			}
			if (localBeiAtCount > 0) {
				beiAtCount = localBeiAtCount;
				handler.sendEmptyMessage(MESSAGE_BEIAT);
			}
			if (localReply > 0) {
				replyCount = localReply;
				handler.sendEmptyMessage(MESSAGE_REPLY);
			}
			if (isUserMsg) {
				pushMsgEditor.putBoolean(Constants.key_msg_new, true);
				pushMsgEditor.commit();
			}
		} catch (JSONException e) {
			handler.sendEmptyMessage(MESSAGE_ERROR_RETURNVALUE);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		int tempUserId = spUser.getInt("userID", 0);
		if (userId == tempUserId) {
			return;
		} else {
			userId = tempUserId;
		}
		if (userId != 0) {
			privateMessageCount = 0;
			privateMessageCount = 0;
			addFenFriendCount = 0;
			beiAtCount = 0;
			replyCount = 0;
			clearPushMsgCount();
			clearFomerNotification();
			connect();
		} else {
			clearFomerNotification();
			if (connectThread != null) {
				connectThread.cancel();
				connectThread = null;
				handler.sendEmptyMessage(MESSAGE_CLOSE);
			}
			if (manageThread != null) {
				manageThread.cancel();
				manageThread = null;
			}
		}
	}

	/**
	 * 发送被关注notification
	 */
	private void sendFellowNotification() {
		Intent notificationIntent = new Intent(this, MyFansActivity.class);
		notificationIntent.putExtra("from", "notification");
		CharSequence title = "有" + addFenFriendCount + "人关注了您";
		sendNotification(title, notificationIntent, NOTIFICATION_ID_FELLOW);
	}

	/**
	 * 发送新私信提醒
	 */
	private void sendPrivateMsgNotification() {
		Intent notificationIntent = new Intent(this, MyPrivateMsgActivity.class);
		notificationIntent.putExtra("from", "notification");
		CharSequence title = "您有" + privateMessageCount + "条新私信";
		sendNotification(title, notificationIntent, NOTIFICATION_ID_PRIVATE_MSG);
	}

	/**
	 * 发送被回复提醒
	 */
	private void sendReplyNotification() {
		Intent notificationIntent = new Intent(this, MyReplyActivity.class);
		notificationIntent.putExtra("from", "notification");
		CharSequence title = "您的评论被回复了" + replyCount + "次";
		sendNotification(title, notificationIntent, NOTIFICATION_ID_REPLY);
	}

	/**
	 * 发送被@提醒
	 */
	private void sendBeiAtNotification() {
		Intent notificationIntent = new Intent(this, MyAtActivity.class);
		notificationIntent.putExtra("from", "notification");
		CharSequence title = "您被@了" + beiAtCount + "次";
		sendNotification(title, notificationIntent, NOTIFICATION_ID_BEIAT);
	}

	/**
	 * 发送提醒
	 * 
	 * @param title
	 * @param intent
	 * @param id
	 */
	private void sendNotification(CharSequence title, Intent intent, int id) {
		int icon = R.drawable.icon;
		Context context = getApplicationContext();
		CharSequence contentText = getText(R.string.notification_content_title);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, title, when);
		notification.sound = Uri.parse("android.resource://" + getPackageName()
				+ "/" + R.raw.notification1);
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.setLatestEventInfo(context, contentText, title,
				contentIntent);

		notificationManager.notify(id, notification);
	}

	/**
	 * 发送节目提醒
	 * 
	 * @param programId
	 */
	private void notifyProgram(int programId) {
		int icon = R.drawable.icon;
		Context context = getApplicationContext();
		CharSequence contentText = getText(R.string.notification_content_title);

		Intent notificationIntent = new Intent(this, ProgramNewActivity.class);
		String stringProgramId = String.valueOf(programId);
		notificationIntent.putExtra("programId", stringProgramId);
		notificationIntent.putExtra("fromNotification", "notification");

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		CharSequence title = "您有新的节目提醒!";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, title, when);
		notification.sound = Uri.parse("android.resource://" + getPackageName()
				+ "/" + R.raw.notification1);
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.setLatestEventInfo(context, contentText, title,
				contentIntent);

		notificationManager.notify(programId, notification);
	}

	private void clearPushMsgCount() {
		SharedPreferences sp = getSharedPreferences("pushMessage", 0);
		Editor editor = sp.edit();
		editor.clear();
		editor.commit();
	}

	private void clearFomerNotification() {
		notificationManager.cancel(NOTIFICATION_ID_PROGRAM);
		notificationManager.cancel(NOTIFICATION_ID_FELLOW);
		notificationManager.cancel(NOTIFICATION_ID_PRIVATE_MSG);
		notificationManager.cancel(NOTIFICATION_ID_BEIAT);
		notificationManager.cancel(NOTIFICATION_ID_REPLY);
	}

	private SharedPreferences pushMsgIndicatorPreference;
	OnSharedPreferenceChangeListener pushMsgIndicatorListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			boolean isOn = sharedPreferences.getBoolean("isOn", true);
			if (!isOn) {
				if (connectThread != null) {
					connectThread.cancel();
					connectThread = null;
				}
				if (manageThread != null) {
					manageThread.cancel();
				}
			} else {
				if (userId != 0) {
					connect();
				}
			}
		}
	};

	private boolean getPushMsgState() {
		SharedPreferences sp = getSharedPreferences("pusMsgIndicator", 0);
		return sp.getBoolean("isOn", true);
	}
}
