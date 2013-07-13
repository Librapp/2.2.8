package com.sumavision.talktv2.dlna;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.Icon;
import org.cybergarage.upnp.IconList;
import org.cybergarage.upnp.RemoteCP;
import org.cybergarage.util.Debug;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.RemoteControllerActivityNew;
import com.sumavision.talktv2.dlna.common.DeviceData;
import com.sumavision.talktv2.dlna.common.Tools;
import com.sumavision.talktv2.dlna.services.DlnaService;
import com.sumavision.tvfanmultiscreen.data.DLNAData;
import com.umeng.analytics.MobclickAgent;

/**
 * @description 2012/4/10 modified by guo
 * 
 */
public class NetworkActivity extends Activity implements OnClickListener {

	// 来自播放器
	private boolean formPlayer = false;
	private ImageView img;
	private final int FILE_RESULT_CODE = 1;
	private final String CONFIGFILE = "userSetting";
	private String dirNow;
	private RemoteCP c;
	private RelativeLayout all;
	// private Animation open2up;
	// private Animation close2bottom;
	private final int MSG_CLOSE_ACTIVITY = 42;

	private void loadSetting() {
		SharedPreferences sp = getSharedPreferences(CONFIGFILE, 0);
		dirNow = sp.getString("DLNA-dirNow", "");
	}

	private void saveSetting(String s) {
		SharedPreferences sp = getSharedPreferences(CONFIGFILE, 0);
		Editor spEd = sp.edit();
		spEd.putString("DLNA-dirNow", s);
		spEd.commit();
	}

	private class DeviceListAdapter extends BaseAdapter {
		private Device[] devices;
		private LayoutInflater mInflater;

		public DeviceListAdapter(Context context, Device[] devices) {
			mInflater = LayoutInflater.from(context);
			this.devices = devices;
			for (int i = 0; i < devices.length; i++) {
				Device device = devices[i];
				System.out.println("device:" + device.getFriendlyName());
			}
		}

		@Override
		public int getCount() {
			return devices.length;
		}

		@Override
		public Object getItem(int position) {
			return devices[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		@SuppressWarnings("unchecked")
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = mInflater.inflate(R.layout.device_list_item, null);

			TextView text = (TextView) v.findViewById(R.id.deviceName);
			ImageView image = (ImageView) v.findViewById(R.id.deviceIcon);
			Device device = this.devices[position];
			if (device != null) {
				text.setText(device.getFriendlyName());

				IconList iconList = device.getIconList();
				Iterator iterator = iconList.iterator();
				while (iterator.hasNext()) {
					Icon icon = (Icon) iterator.next();
					System.out.println("icon url:" + icon.getURL() + " "
							+ icon.getWidth());
					if (icon.getWidth() > 40 && icon.getWidth() < 50) {
						String iconUrl = icon.getURL();
						String urlBase = device.getURLBase();
						if (iconUrl.startsWith("/") && urlBase.endsWith("/")) {
							iconUrl = iconUrl.substring(1);
						}
						System.out.println("urlBase:" + urlBase);
						if (urlBase != null && urlBase.length() > 0) {
							try {
								Drawable drawable = Tools.getDrawable(urlBase
										+ iconUrl);
								image.setImageDrawable(drawable);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						break;
					}
				}
			}

			return v;
		}
	}

	public class DeviceSearchReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (DlnaService.NEW_DEVICES_FOUND.equals(intent.getAction())) {

				ListView lv = (ListView) findViewById(R.id.dlna_deviceList);

				lv.setAdapter(new DeviceListAdapter(NetworkActivity.this,
						DeviceData.getInstance().getDevices()));

				lv.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						Device device = (Device) parent
								.getItemAtPosition(position);
						DeviceData.getInstance().setSelectedDevice(device);
						// try {
						initServices();

						ListView sdl = (ListView) findViewById(R.id.selectedDeviceList);
						List<Device> selected = new ArrayList<Device>();
						selected.add(device);

						DeviceListAdapter deviceListAdapter = new DeviceListAdapter(
								NetworkActivity.this, selected
										.toArray(new Device[] {}));

						sdl.setAdapter(deviceListAdapter);

						// Toast.makeText(getApplicationContext(),
						// device.getFriendlyName(), Toast.LENGTH_SHORT)
						// .show();

						if (!dontHasService)
							Toast.makeText(getApplicationContext(),
									"已选择设备：" + "\n" + device.getFriendlyName(),
									Toast.LENGTH_SHORT).show();
						// all.startAnimation(close2bottom);
						serverHandler.sendEmptyMessageDelayed(
								MSG_CLOSE_ACTIVITY, 400);
						// } catch (NullPointerException e) {
						// e.printStackTrace();
						// DeviceData.getInstance().setSelectedDevice(null);
						// Toast.makeText(getApplicationContext(), "此设备不支持切屏",
						// Toast.LENGTH_SHORT).show();
						// }
					}
				});
			}
		}
	}

	private boolean dontHasService = false;

	private void initServices() {
		DLNAData.current().hasPlayingOnTV = false;
		if (DeviceData.getInstance().getSelectedDevice() != null) {
			DLNAData.current().AVT = DeviceData.getInstance()
					.getSelectedDevice()
					.getService("urn:schemas-upnp-org:service:AVTransport:1");
			DLNAData.current().CM = DeviceData
					.getInstance()
					.getSelectedDevice()
					.getService(
							"urn:schemas-upnp-org:service:ConnectionManager:1");
			DLNAData.current().RCS = DeviceData
					.getInstance()
					.getSelectedDevice()
					.getService(
							"urn:schemas-upnp-org:service:RenderingControl:1");
			DLNAData.current().initDlnaAction();
			if (Debug.isOn())
				Log.e("DLNASeve", "step - 3");
			Intent i = new Intent(DlnaService.NEW_DEVICES_FOUND);
			sendBroadcast(i);
			if (formPlayer)
				DLNAData.current().needOpenRemoteController = true;
			else
				DLNAData.current().needOpenRemoteController = false;
		} else {
			dontHasService = true;
			Toast.makeText(getApplicationContext(), "设备连接出错，请稍后重试",
					Toast.LENGTH_SHORT).show();
		}
	}

	// 已经存在的设备无需继续添加
	private void checkAlreadyHas() {

	}

	DeviceSearchReceiver receiver;
	// Intent iC;
	Button startServer;
	private ImageButton back;
	private ImageButton openRemoteController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.network);
		all = (RelativeLayout) findViewById(R.id.network_all);
		// open2up = AnimationUtils.loadAnimation(getApplicationContext(),
		// R.anim.activity_open2up);
		// close2bottom = AnimationUtils.loadAnimation(getApplicationContext(),
		// R.anim.activity_close2bottom);
		// all.startAnimation(open2up);

		img = (ImageView) findViewById(R.id.dlna_net_img);
		// iC = new Intent(NetworkActivity.this, ContentActivity.class);
		back = (ImageButton) this.findViewById(R.id.network_back);
		back.setOnClickListener(this);
		openRemoteController = (ImageButton) this
				.findViewById(R.id.shuai_small);
		openRemoteController.setOnClickListener(this);

		int from = getIntent().getIntExtra("fromPlayer", 0);
		if (from == 0) {
			formPlayer = false;
		} else if (from == 1) {
			formPlayer = true;
		}

		if (isWiFiActive()) {
			img.setVisibility(View.GONE);

			ListView lv = (ListView) findViewById(R.id.dlna_deviceList);
			lv.setAdapter(new DeviceListAdapter(this, DeviceData.getInstance()
					.getDevices()));
			lv.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					Device device = (Device) arg0.getItemAtPosition(arg2);
					DeviceData.getInstance().setSelectedDevice(device);
					// startActivity(iC);
					startService(new Intent(DlnaService.RESET_STACK));

					return false;
				}

			});

			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					Device device = (Device) parent.getItemAtPosition(position);
					DeviceData.getInstance().setSelectedDevice(device);

				}
			});

			if (DeviceData.getInstance().getDevices() != null
					&& DeviceData.getInstance().getDevices().length > 0)
				startService(new Intent(DlnaService.RESET_STACK));
			else
				startService(new Intent(DlnaService.SEARCH_DEVICES));

			loadSetting();
			startServer = (Button) this.findViewById(R.id.dlna_settingButton);
			startServer.setOnClickListener(this);

			if (!dirNow.equals("")) {
				startServer.setTextSize(14);
				startServer.setText("已设置共享目录" + "\n" + dirNow);
			}

			Button cmd_search = (Button) this
					.findViewById(R.id.dlna_searchButton);
			cmd_search.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// startService(new Intent(DlnaServiceNew.SEARCH_DEVICES));
					startService(new Intent(DlnaService.RESET_STACK));
				}
			});

			receiver = new DeviceSearchReceiver();
			registerReceiver(receiver, new IntentFilter(
					DlnaService.NEW_DEVICES_FOUND));

			Device selectedDevice = DeviceData.getInstance()
					.getSelectedDevice();
			ListView lv1 = (ListView) findViewById(R.id.selectedDeviceList);
			List<Device> selected = new ArrayList<Device>();
			if (selectedDevice != null) {
				selected.add(selectedDevice);
				lv1.setAdapter(new DeviceListAdapter(this, selected
						.toArray(new Device[] {})));
			} else {
				List<String> list = new ArrayList<String>();
				list.add("No device selected");
				lv1.setAdapter(new ArrayAdapter<String>(this,
						R.id.selectedDeviceList, list) {
					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {

						TextView tv = new TextView(NetworkActivity.this);
						tv.setText(getItem(position));

						return tv;
					}
				});
			}

		} else {
			img.setVisibility(View.VISIBLE);
			dialog();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);

	}

	@Override
	protected void onResume() {
		super.onResume();

		MobclickAgent.onResume(this);

		if (DLNAData.current().hasPlayingOnTV) {
			openRemoteController.setVisibility(View.VISIBLE);
		} else {
			// openRemoteController.setVisibility(View.GONE);
			openRemoteController.setVisibility(View.GONE);
		}

		if (isWiFiActive()) {
			img.setVisibility(View.GONE);

			ListView lv = (ListView) findViewById(R.id.dlna_deviceList);
			lv.setAdapter(new DeviceListAdapter(this, DeviceData.getInstance()
					.getDevices()));
			lv.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					Device device = (Device) arg0.getItemAtPosition(arg2);
					DeviceData.getInstance().setSelectedDevice(device);
					// startActivity(iC);
					startService(new Intent(DlnaService.RESET_STACK));

					return false;
				}

			});

			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					Device device = (Device) parent.getItemAtPosition(position);

					DeviceData.getInstance().setSelectedDevice(device);
					// try {
					initServices();

					if (!dontHasService)
						Toast.makeText(getApplicationContext(),
								"已选择设备：" + "\n" + device.getFriendlyName(),
								Toast.LENGTH_SHORT).show();
					// all.startAnimation(close2bottom);
					serverHandler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY,
							400);
					// } catch (NullPointerException e) {
					// e.printStackTrace();
					// DeviceData.getInstance().setSelectedDevice(null);
					// Toast.makeText(getApplicationContext(), "此设备不支持切屏",
					// Toast.LENGTH_SHORT).show();
					// }

					// ListView lv = (ListView)
					// findViewById(R.id.selectedDeviceList);
					// List<Device> selected = new ArrayList<Device>();
					// selected.add(device);
					// lv.setAdapter(new DeviceListAdapter(NetworkActivity.this,
					// selected.toArray(new Device[] {})));

					// startActivity(iC);
					// startService(new Intent(DlnaService.RESET_STACK));
					//

				}
			});
		} else {
			img.setVisibility(View.VISIBLE);
			dialog();
		}

	}

	private AlertDialog checkAppDialog;

	protected void dialog() {
		if (checkAppDialog != null) {
			checkAppDialog.dismiss();
			checkAppDialog = null;
		}

		checkAppDialog = new AlertDialog.Builder(this).create();
		checkAppDialog.setIcon(R.drawable.icon);
		checkAppDialog.setTitle("要使用遥控器，请先开启WIFI");
		checkAppDialog.setButton("开启WIFI",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						checkAppDialog.dismiss();
						Intent intent = new Intent();
						intent.setAction("android.settings.WIFI_SETTINGS");
						startActivity(intent);
					}

				});
		checkAppDialog.setButton2("退出",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						checkAppDialog.dismiss();
						finish();
					}

				});
		checkAppDialog.setCancelable(false);
		checkAppDialog.show();
	}

	private void dismissAppDialog() {
		if (checkAppDialog != null) {
			checkAppDialog.dismiss();
			checkAppDialog = null;
		}
	}

	public boolean isWiFiActive() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] infos = connectivity.getAllNetworkInfo();
			if (infos != null) {
				for (NetworkInfo ni : infos) {
					if (ni.getTypeName().equals("WIFI") && ni.isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dlna_settingButton:

			// try {
			// TalkTvDLNAServer.main();
			//
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//
			// startService(new Intent(this, AndroidUpnpServiceImpl.class));

			// Intent i = new Intent(this, FileManagerActivity.class);
			// startActivityForResult(i, FILE_RESULT_CODE);
			break;
		case R.id.network_back:
			// all.startAnimation(close2bottom);
			serverHandler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 400);
			break;
		case R.id.network_title_hot_type:
			Intent iD = new Intent(this, RemoteControllerActivityNew.class);
			DLNAData.current().isOnlyController = true;
			startActivity(iD);
			break;
		case R.id.shuai_small:
			openDLNAControlerActivity(1);
			break;
		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {

			switch (requestCode) {
			case FILE_RESULT_CODE:
				Bundle bundle = null;
				if (data != null && (bundle = data.getExtras()) != null) {
					String str = bundle.getString("file");

					startServer.setTextSize(14);
					startServer.setText("已设置共享目录" + "\n" + str);

					saveSetting(str);
				}

				break;

			default:
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// all.startAnimation(close2bottom);
			serverHandler.sendEmptyMessageDelayed(MSG_CLOSE_ACTIVITY, 400);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	private Handler serverHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_CLOSE_ACTIVITY:
				finish();

				break;
			default:
				break;
			}

		}
	};

	private void openDLNAControlerActivity(int resume) {
		Intent i = new Intent(getApplicationContext(),
				RemoteControllerActivityNew.class);
		/** 是否继续播放 0否1是 */
		i.putExtra("resume", resume);
		startActivity(i);
	}
}
