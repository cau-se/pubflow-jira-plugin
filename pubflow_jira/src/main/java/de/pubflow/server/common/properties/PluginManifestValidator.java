package de.pubflow.server.common.properties;


public class PluginManifestValidator {
//	public static boolean check(String methodName, ComMap data, InputStream manifestStream) throws Exception{
//		XMLInputFactory xmlif = XMLInputFactory.newInstance();
//		xmlif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
//		XMLEventReader xmler = xmlif.createXMLEventReader(manifestStream);
//
//		String currentSignature = "";
//
//		while (xmler.hasNext()) {
//			XMLEvent e = xmler.nextEvent();
//			if (e.isStartElement()) {
//				StartElement localPart = e.asStartElement();
//				switch (localPart.getName().getLocalPart()) {
//
//				case "signature": 
//					currentSignature = e.asStartElement().getAttributeByName(new QName("name")).getValue(); 
//					break;
//
//				case "parameter": 
//					if(currentSignature.equals(methodName)){
//						if(e.asStartElement().getAttributeByName(new QName("required")).toString().equals("yes")){
//							if (!data.containsKey(e.asStartElement().getAttributeByName(new QName("name")).toString())){
//								throw new Exception(methodName + " expects parameter " + "");
//							}
//						}
//					}
//					break;
//				}
//			}
//		}
//
//		return true;
//	}

}
