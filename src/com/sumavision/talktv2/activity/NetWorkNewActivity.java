package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import org.cybergarage.upnp.Device;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.dlna.DeviceDataInSearchList;
import com.sumavision.talktv2.dlna.common.DeviceData;
import com.sumavision.talktv2.dlna.services.DlnaService;
import com.sumavision.talktv2.utils.DLNAUtil;

public class NetWorkNewActivity extends Activity implements
		OnItemClickListener, OnClickListener {
	private ArrayList<DeviceDataInSearchList> devicesList = new ArrayList<DeviceDataInSearchList>();
	private DeviceAdapter deviceAdapter;
	DeviceFoundReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.network_device_chose);
		initViews();
		addSelectedDevice();
		receiver = new DeviceFoundReceiver();
		registerReceiver(receiver, new IntentFilter(
				DlnaService.NEW_DEVICES_FOUND));
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	private void initViews() {
		ListView listView = (ListView) findViewById(R.id.deviceListView);
		listView.setOnItemClickListener(this);
		deviceAdapter = new DeviceAdapter(devicesList);
		listView.setAdapter(deviceAdapter);
		findViewById(R.id.searchBtn).setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
	}

	/**
	 * 添加已经选择的设备
	 */
	private void addSelectedDevice() {
		Device selectedDevice = DeviceData.getInstance().getSelectedDevice();
		if (selectedDevice != null) {
			DeviceDataInSearchList temp = new DeviceDataInSearchList();
			temp.name = selectedDevice.getFriendlyName();
			temp.address = selectedDevice.getLocation()
					+ selectedDevice.getDescriptionFilePath();
			String url = DLNAUtil.getIcon(selectedDevice);
			temp.iconUrl = url;
			temp.isSelected = true;
			devicesList.add(temp);
			deviceAdapter.notifyDataSetChanged();
		}
	}

	class DeviceAdapter extends BaseAdapter {

		private ArrayList<DeviceDataInSearchList> list;

		public DeviceAdapter(ArrayList<DeviceDataInSearchList> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list == null) {
				return 0;
			}
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(NetWorkNewActivity.this);
				convertView = inflater.inflate(
						R.layout.network_device_list_item, null);
				// viewHolder.imageView = (ImageView) convertView
				// .findViewById(R.id.imageView);
				viewHolder.textView = (TextView) convertView
						.findViewById(R.id.textView);
				// viewHolder.selectedView = (TextView) convertView
				// .findViewById(R.id.selectedTextView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			DeviceDataInSearchList temp = list.get(position);
			String name = temp.name;
			if (name != null)
				viewHolder.textView.setText(name);
			// if (temp.iconUrl != null) {
			// Drawable drawable = Tools.getDrawable(temp.iconUrl);
			// viewHolder.imageView.setImageDrawable(drawable);
			// }
			// if (temp.isSelected) {
			// viewHolder.selectedView.setVisibility(View.VISIBLE);
			// } else {
			// viewHolder.selectedView.setVisibility(View.GONE);
			// }
			return convertView;
		}
	}

	public static class ViewHolder {
		// public ImageView imageView;
		public TextView textView;
		// public TextView selectedView;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		DeviceDataInSearchList temp = (DeviceDataInSearchList) deviceAdapter
				.getItem(arg2);
		Intent intent = new Intent(this, DlnaService.class);
		intent.setAction(DlnaService.DEVICE_SELECTED);
		intent.putExtra("selectedDevice", temp);
		startService(intent);
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.searchBtn:
			startSearchDevice();
			break;
		case R.id.back:
			finish();
			break;
		case R.id.shuai_small:
			openDLNAControlerActivity(1);
			break;
		default:
			break;
		}
	}

	/**
	 * 调用搜索服务的搜索功能
	 */
	private void startSearchDevice() {

		handler.sendEmptyMessage(MSG_SEARCH_START);
		startService(new Intent(DlnaService.SEARCH_DEVICES));
	}

	private static final int DIALOG_PROGRESS_ID = 1;

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case DIALOG_PROGRESS_ID:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("搜索中...");
			progressDialog.setOnCancelListener(onCancelListener);
			progressDialog.setCancelable(true);
			dialog = progressDialog;
			break;
		default:
			break;
		}
		if (dialog != null)
			return dialog;
		return super.onCreateDialog(id);
	}

	private OnCancelListener onCancelListener = new OnCancelListener() {

		@Override
		public void onCancel(DialogInterface dialog) {
			handler.sendEmptyMessage(MSG_CANCEL_SEARCH);
		}
	};

	private static final int MSG_SEARCH_START = 1;
	private static final int MSG_SEARCH_END = 2;
	private static final int MSG_CANCEL_SEARCH = 3;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SEARCH_START:
				isOver = false;
				showDialog(DIALOG_PROGRESS_ID);
				handler.sendEmptyMessageDelayed(MSG_SEARCH_END, 50000);
				break;
			case MSG_SEARCH_END:
				dismissDialog(DIALOG_PROGRESS_ID);
				break;
			case MSG_CANCEL_SEARCH:
				isOver = true;
				handler.removeMessages(MSG_SEARCH_END);
				break;
			default:
				break;
			}
		}
	};
	// 表示是不是取消了接受结果
	private boolean isOver;

	public class DeviceFoundReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (isOver) {
				return;
			}
			String action = intent.getAction();
			if (DlnaService.NEW_DEVICES_FOUND.equals(action)) {
				DeviceDataInSearchList newDevice = (DeviceDataInSearchList) intent
						.getSerializableExtra("device");
				if (newDevice == null || newDevice.name == null) {
					return;
				}
				if (!isDeviceExist(newDevice)) {
					devicesList.add(newDevice);
					deviceAdapter.notifyDataSetChanged();
					Log.e("deviceFoundService", deviceAdapter.getCount() + "");

				}
			}
		}

	}

	private boolean isDeviceExist(DeviceDataInSearchList device) {
		for (DeviceDataInSearchList temp : devicesList) {
			if (device.name.equals(temp.name)) {
				return true;
			}
		}
		return false;
	}

	private void openDLNAControlerActivity(int resume) {
		Intent i = new Intent(getApplicationContext(),
				RemoteControllerActivityNew.class);
		/** 是否继续播放 0否1是 */
		i.putExtra("resume", resume);
		startActivity(i);
	}
}
