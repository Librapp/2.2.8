package com.sumavision.talktv2.parser;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.sumavision.tvfanmultiscreen.data.DLNAData;

/**
 * 
 * @author 郭鹏
 * @description 解析GetVolume参数
 * 
 */
public class GetVolumeParser {

	public static String letf_tringle = "<";
	public static String right_tringle = ">";

	public static String firstLine = "s:Envelope";
	public static String twoLine = "s:Body";
	public static String threeLine = "u:GetVolumeResponse";
	public static String fourLine = "CurrentVolume";

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

						if (mute.getNodeName().equalsIgnoreCase(fourLine)) {
							DLNAData.current().CurrentVolume = mute
									.getFirstChild().getNodeValue();
							Log.e("GetVolumeParser",
									DLNAData.current().CurrentVolume);
						} else {
							Log.e("GetVolumeParser", "no  data");
						}
					}
				}

			}
		}
	}

}
