package com.sumavision.talktv2.dlna.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.UPnPStatus;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.cybergarage.upnp.device.InvalidDescriptionException;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.cybergarage.util.Debug;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sumavision.talktv2.dlna.DeviceDataInSearchList;
import com.sumavision.talktv2.dlna.common.DeviceData;
import com.sumavision.talktv2.dlna.common.Item;
import com.sumavision.talktv2.utils.DLNAUtil;
import com.sumavision.tvfanmultiscreen.data.DLNAData;

/**
 * @author ojalate
 * 
 */
public class DlnaService extends Service implements DeviceChangeListener {

	public class DlnaServiceBinder extends Binder {
		public DlnaService getService() {
			return DlnaService.this;
		}
	}

	public static final String BIND_SERVICE = "com.sumavision.talktv2.BIND_SERVICE";
	public static final String GET_ITEM_LIST = "com.sumavision.talktv2.GET_ITEM_LIST";
	public static final String ITEM_LIST_RESULT = "com.sumavision.talktv2.TEM_LIST_RESULT";
	public static final String NEW_DEVICES_FOUND = "com.sumavision.talktv2.NEW_DEVICES_FOUND";
	public static final String NO_DEVICES_FOUND = "com.sumavision.talktv2.NO_DEVICES_FOUND";
	public static final String RESET_STACK = "com.sumavision.talktv2.RESET_STACK";
	public static final String SEARCH_DEVICES = "com.sumavision.talktv2.SEARCH_DEVICES";
	public static final String DEVICE_SELECTED = "com.sumavision.talktv2.DEVICE_SELECTED";

	public static List<Item> parseResult(Argument result) {

		List<Item> list = new ArrayList<Item>();

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = dfactory.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(result.getValue()
					.getBytes("UTF-8"));

			Document doc = documentBuilder.parse(is);

			NodeList containers = doc.getElementsByTagName("container");
			for (int j = 0; j < containers.getLength(); ++j) {
				Node container = containers.item(j);
				String title = null;
				String objectClass = null;
				int id = 0;
				NodeList childNodes = container.getChildNodes();
				for (int l = 0; l < childNodes.getLength(); ++l) {
					Node childNode = childNodes.item(l);
					if (childNode.getNodeName().equals("dc:title")) {
						title = childNode.getFirstChild().getNodeValue();
						id = Integer
								.parseInt(container.getAttributes()
										.getNamedItem("id").getNodeValue()
										.substring(2));

					} else if (childNode.getNodeName().equals("upnp:class")) {
						objectClass = childNode.getFirstChild().getNodeValue();
					}
				}
				Item i = new Item(id, title, null, null, objectClass);
				list.add(i);
			}

			NodeList items = doc.getElementsByTagName("item");
			for (int j = 0; j < items.getLength(); ++j) {
				Node item = items.item(j);
				int id = 0;
				String title = null;
				String artist = null;
				String album = null;
				String objectClass = null;
				String res = null;
				String duration = null;

				id = Integer.parseInt(item.getAttributes().getNamedItem("id")
						.getNodeValue().replace("/", ""));

				NodeList childNodes = item.getChildNodes();
				for (int l = 0; l < childNodes.getLength(); ++l) {
					Node childNode = childNodes.item(l);
					if (Debug.isOn())
						Log.i(childNodes.item(l).getNodeName(), childNode
								.getFirstChild().getNodeValue());
					if (childNode.getNodeName().equals("dc:title")) {
						title = childNode.getFirstChild().getNodeValue();
					} else if (childNode.getNodeName().equals("upnp:artist")) {
						artist = childNode.getFirstChild().getNodeValue();
					} else if (childNode.getNodeName().equals("upnp:album")) {
						album = childNode.getFirstChild().getNodeValue();
					} else if (childNode.getNodeName().equals("upnp:class")) {
						objectClass = childNode.getFirstChild().getNodeValue();
					} else if (childNode.getNodeName().equals("res")) {
						res = childNode.getFirstChild().getNodeValue();
						if (childNode.getAttributes().getNamedItem("duration") != null) {
							duration = childNode.getAttributes()
									.getNamedItem("duration").getNodeValue();
						}
					}

				}
				Item i = new Item(id, title, artist, album, objectClass);
				if ("object.item.audioItem.musicTrack".equals(objectClass)
						|| "object.item.videoItem".equals(objectClass)
						|| "object.item.imageItem.photo".equals(objectClass)) {
					i.setRes(res);
					i.setDuration(duration);
				}
				list.add(i);
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
		return list;
	}

	private final IBinder binder = new DlnaServiceBinder();
	private ControlPoint c;

	private List<Item> currentLevelItems;

	private Stack<Integer> stack = new Stack<Integer>();

	boolean started = false;

	@Override
	public void deviceAdded(Device dev) {
		if (Debug.isOn())
			Log.e("DLNAServer-deviceAdded", dev.getFriendlyName());

		if ("urn:schemas-upnp-org:device:MediaRenderer:1".equals(dev
				.getDeviceType())
		// || "urn:schemas-upnp-org:device:MediaServer:1".equals(dev
		// .getDeviceType())
		) {
			DeviceData.getInstance().addDevice(dev);
			// if (Debug.isOn())
			// Log.e("DlnaService", "org:device:MediaRenderer设备已经添加");
			// Intent i = new Intent(NEW_DEVICES_FOUND);
			// sendBroadcast(i);
			// } else {
			// DeviceData.getInstance().addDevice(dev);
			// Intent i = new Intent(NEW_DEVICES_FOUND);
			// sendBroadcast(i);
			// }

			// if
			// ("Realtek Embedded UPnP Render()".equals(dev.getFriendlyName()))
			// {
			// if (Debug.isOn())
			// Log.e("DlnaService", "Realtek Embedded UPnP Render()设备已找到");
			// DeviceData.getInstance().setSelectedDevice(dev);
			// DLNAData.current().friendlyName = dev.getFriendlyName();
			// if (Debug.isOn())
			// Log.e("DLNASeve", "step - 1");
			// DLNAData.current().AVT = DeviceData.getInstance()
			// .getSelectedDevice()
			// .getService("urn:schemas-upnp-org:service:AVTransport:1");
			// DLNAData.current().CM = DeviceData
			// .getInstance()
			// .getSelectedDevice()
			// .getService(
			// "urn:schemas-upnp-org:service:ConnectionManager:1");
			// DLNAData.current().RCS = DeviceData
			// .getInstance()
			// .getSelectedDevice()
			// .getService(
			// "urn:schemas-upnp-org:service:RenderingControl:1");
			// DLNAData.current().initDlnaAction();
			// if (Debug.isOn())
			// Log.e("DLNASeve", "step - 3");
			Intent i = new Intent(NEW_DEVICES_FOUND);
			DeviceDataInSearchList tempData = new DeviceDataInSearchList();
			tempData.name = dev.getFriendlyName();
			tempData.address = dev.getLocation() + dev.getDescriptionFilePath();
			String url = DLNAUtil.getIcon(dev);
			tempData.iconUrl = url;
			i.putExtra("device", tempData);
			sendBroadcast(i);

			// }
			// // V视
			// else if (("MPO-V0398").equals(dev.getFriendlyName())) {
			//
			// }
			// // 快播大屏幕
			// else if (("DaPingMu(Q-1000DF)").equals(dev.getFriendlyName())) {
			//
			// }

		}
	}

	@Override
	public void deviceRemoved(Device dev) {

	}

	public List<Item> getCurrentLevelItems() {
		return currentLevelItems;
	}

	public List<Item> getItems(int id) {

		if (currentLevelItems != null && currentLevelItems.size() > 0
				&& !stack.empty() && stack.peek().equals(id)) {
			return currentLevelItems;
		}
		if (DeviceData.getInstance().getSelectedDevice() == null) {
			return null;
		}

		org.cybergarage.upnp.Service service = DeviceData.getInstance()
				.getSelectedDevice()
				.getService("urn:schemas-upnp-org:service:ContentDirectory:1");

		// Action action = service.getAction("Browse");
		ArgumentList argumentList;
		Action action;
		if (service != null) {
			action = service.getAction("Browse");
			argumentList = action.getArgumentList();
		} else {
			return null;
		}
		argumentList.getArgument("ObjectID").setValue(id);
		argumentList.getArgument("BrowseFlag").setValue("BrowseDirectChildren");
		argumentList.getArgument("StartingIndex").setValue("0");
		argumentList.getArgument("RequestedCount").setValue("0");
		argumentList.getArgument("Filter").setValue("*");
		argumentList.getArgument("SortCriteria").setValue("");

		if (action.postControlAction()) {
			ArgumentList outArgList = action.getOutputArgumentList();
			Argument result = outArgList.getArgument("Result");
			System.out.println("Result:" + result.getValue());
			List<Item> items = parseResult(result);
			if (stack.size() == 0 || !stack.peek().equals(id)) {
				stack.push(id);
			}
			currentLevelItems = items;
			return items;
		} else {
			UPnPStatus err = action.getControlStatus();
			System.out.println("Error Code = " + err.getCode());
			System.out.println("Error Desc = " + err.getDescription());
		}
		return null;
	}

	public Stack<Integer> getStack() {
		return stack;
	}

	public void initControlPoint() {
		c = new ControlPoint();
		c.addDeviceChangeListener(this);
		c.addSearchResponseListener(new SearchResponseListener() {

			@Override
			public void deviceSearchResponseReceived(SSDPPacket ssdpPacket) {
			}
		});
		// c.start();
	}

	public void moveUp() {
		stack.pop();
		currentLevelItems = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		initControlPoint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (DlnaService.SEARCH_DEVICES.equals(intent.getAction())) {
			refreshDevices();
		} else if (DlnaService.RESET_STACK.equals(intent.getAction())) {
			this.stack.clear();
		} else if (DlnaService.DEVICE_SELECTED.equals(intent.getAction())) {
			DeviceDataInSearchList data = (DeviceDataInSearchList) intent
					.getSerializableExtra("selectedDevice");
			Device device = getDeviceByShortData(data);
			if (device != null) {
				DeviceData.getInstance().setSelectedDevice(device);
				initServices();
				Toast.makeText(this, "已连接设备", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "无法连接该设备", Toast.LENGTH_SHORT).show();
			}
		}
		return Service.START_NOT_STICKY;
	}

	private void initServices() {
		DLNAData.current().hasPlayingOnTV = false;
		if (DeviceData.getInstance().getSelectedDevice() != null) {
			Log.e("aa", DeviceData.getInstance().getSelectedDevice()
					.getLocation()
					+ DeviceData.getInstance().getSelectedDevice()
							.getDescriptionFilePath());
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
		}
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

	private void refreshDevices() {
		multicastSearch();
	}

	public void directConnection() {

		Device d = null;
		try {
			try {
				URL url = new URL("http://10.4.53.12:49152/description.xml");
				// URL url = new
				// URL("http://192.168.0.101:49152/description.xml");
				d = new Device(url.openStream());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("loaded:" + d.getFriendlyName());
		} catch (InvalidDescriptionException e2) {
			e2.printStackTrace();
		}

		deviceAdded(d);
	}

	public void multicastSearch() {

		DeviceList deviceList = c.getDeviceList();
		Iterator iterator = deviceList.iterator();
		while (iterator.hasNext()) {
			Device next = (Device) iterator.next();
			System.out.println(next.getFriendlyName());

			DeviceData.getInstance().addDevice(next);
			Intent i = new Intent(NEW_DEVICES_FOUND);
			DeviceDataInSearchList tempData = new DeviceDataInSearchList();
			tempData.name = next.getFriendlyName();
			tempData.address = next.getLocation()
					+ next.getDescriptionFilePath();
			String url = DLNAUtil.getIcon(next);
			tempData.iconUrl = url;
			i.putExtra("device", tempData);
			sendBroadcast(i);
		}
		if (!started) {
			try {
				new Thread() {
					@Override
					public void run() {
						c.start();

					}
				}.start();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			started = true;
		} else {
			new Thread() {
				@Override
				public void run() {
					c.search();

				}
			}.start();

		}

	}

	public void setCurrentLevelItems(List<Item> items) {
		currentLevelItems = items;
	}
}
