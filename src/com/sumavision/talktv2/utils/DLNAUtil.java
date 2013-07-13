package com.sumavision.talktv2.utils;

import java.util.Iterator;

import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.Icon;
import org.cybergarage.upnp.IconList;

public class DLNAUtil {
	public static String getIcon(Device device) {
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
					return urlBase + iconUrl;
				}
				break;
			}
		}
		return null;
	}
}
