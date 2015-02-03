package de.pubflow.server.common.properties;

import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import de.pubflow.server.core.jira.ComMap;

public class PluginManifestValidator {
	public static boolean check(String methodName, ComMap data, InputStream manifestStream) throws Exception{
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		xmlif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		XMLEventReader xmler = xmlif.createXMLEventReader(manifestStream);

		while (xmler.hasNext()) {
			XMLEvent e = xmler.nextEvent();
			if (e.isStartElement()) {
				StartElement localPart = e.asStartElement();
				switch (localPart.getName().getLocalPart()) {
				case "parameter": 
					if(e.asStartElement().getAttributeByName(new QName("required")).toString().equals("yes")){
						if (!data.containsKey(e.asStartElement().getAttributeByName(new QName("name")).toString())){
							throw new Exception(methodName + " expects parameter " + "");
						}
					}
					break;
				}
			}
		}
		
		return true;
	}

}
