package com.sumavision.talktv2.parser;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.sumavision.talktv2.data.DLNAGetPositionInfoData;
import com.sumavision.tvfanmultiscreen.data.DLNAData;

/**
 * 
 * @author 郭鹏
 * @description 解析GetTransportInfoParser参数
 * 
 */
public class GetPositonInfoParser {

	public static String letf_tringle = "<";
	public static String right_tringle = ">";

	public static String firstLine = "s:Envelope";
	public static String twoLine = "s:Body";
	public static String threeLine = "u:GetTransportInfoResponse";
	public static String fourLine_1 = "Track";
	public static String fourLine_2 = "TrackDuration";
	public static String fourLine_3 = "TrackMetaData";
	public static String fourLine_4 = "TrackURI";
	public static String fourLine_5 = "RelTime";
	public static String fourLine_6 = "AbsTime";
	public static String fourLine_7 = "RelCount";
	public static String fourLine_8 = "AbsCount";

	public static void parse(InputStream is) throws Exception {

		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;

		docBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		// try {
		doc = docBuilder.parse(is);
		Element messageElement = doc.getDocumentElement();
		NodeList nodeList = messageElement.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element) nodeList.item(i);

			if (element.getNodeName().equalsIgnoreCase(twoLine)) {
				NodeList oelementNodeList = element.getChildNodes();

				for (int j = 0; j < oelementNodeList.getLength(); j++) {
					Element element_node = (Element) oelementNodeList.item(j);
					NodeList elementNodeList = element_node.getChildNodes();

					for (int l = 0; l < elementNodeList.getLength(); l++) {

						Element mute = (Element) elementNodeList.item(l);

						DLNAGetPositionInfoData data = new DLNAGetPositionInfoData();

						if (mute.getNodeName().equalsIgnoreCase(fourLine_1)) {
							data.Track = mute.getFirstChild().getNodeValue();
							Log.e("GetPositionInfoParser", data.Track);
						} else if (mute.getNodeName().equalsIgnoreCase(
								fourLine_2)) {
							data.TrackDuration = mute.getFirstChild()
									.getNodeValue();
							Log.e("GetPositionInfoParser", data.TrackDuration);
						} else if (mute.getNodeName().equalsIgnoreCase(
								fourLine_3)) {
							data.TrackMetaData = mute.getFirstChild()
									.getNodeValue();
							Log.e("GetPositionInfoParser", data.TrackMetaData);
						} else if (mute.getNodeName().equalsIgnoreCase(
								fourLine_4)) {
							data.TrackURI = mute.getFirstChild().getNodeValue();
							Log.e("GetPositionInfoParser", data.TrackURI);
						} else if (mute.getNodeName().equalsIgnoreCase(
								fourLine_5)) {
							data.RelTime = mute.getFirstChild().getNodeValue();
							Log.e("GetPositionInfoParser", data.RelTime);
						} else if (mute.getNodeName().equalsIgnoreCase(
								fourLine_6)) {
							data.AbsTime = mute.getFirstChild().getNodeValue();
							Log.e("GetPositionInfoParser", data.AbsTime);
						} else if (mute.getNodeName().equalsIgnoreCase(
								fourLine_7)) {
							data.RelCount = mute.getFirstChild().getNodeValue();
							Log.e("GetPositionInfoParser", data.RelCount);
						} else if (mute.getNodeName().equalsIgnoreCase(
								fourLine_8)) {
							data.AbsCount = mute.getFirstChild().getNodeValue();
							Log.e("GetPositionInfoParser", data.AbsCount);
						} else {
							Log.e("GetTransportInfoParser", "no  data");
						}

						DLNAData.current().data = data;
					}
				}

			}
		}
	}

}
