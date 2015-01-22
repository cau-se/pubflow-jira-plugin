package de.pubflow.server.services.eprints;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import de.pubflow.server.services.eprints.entity.MetaMsg;
import de.pubflow.server.services.eprints.entity.RSSMsg;


public class RSSReader {

	public static List<RSSMsg> readFeeds(String url) throws IOException {
		List<RSSMsg> feeds = new LinkedList<RSSMsg>();

		try {
			XMLInputFactory xmlif = XMLInputFactory.newInstance();
			InputStream is = new URL(url).openStream();
			XMLEventReader xmler = xmlif.createXMLEventReader(is);
			RSSMsg msg = new RSSMsg();

			while (xmler.hasNext()) {
				XMLEvent e = xmler.nextEvent();
				if (e.isStartElement()) {
					String localPart = e.asStartElement().getName().getLocalPart();
					switch (localPart) {
					case "item":
						e = xmler.nextEvent();
						break;
					case "title":
						msg.setTitle(getCharData(e, xmler));;
						break;
					case "description":
						msg.setDescription(getCharData(e, xmler));
						break;
					case "link":
						msg.setLink(getCharData(e, xmler));
						break;
					case "guid":
						msg.setGuid(getCharData(e, xmler));
						break;
					case "author":
						msg.setAuthor(getCharData(e, xmler));
						break;
					case "pubDate":
						msg.setPubDate(getCharData(e, xmler));
						break;
					}
				} else if (e.isEndElement()) {
					if(e.asEndElement().getName().getLocalPart().equals("item")){
						feeds.add(msg);
						msg = new RSSMsg();
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}

		return feeds;
	}

	private static String getCharData(XMLEvent e, XMLEventReader xmler) throws XMLStreamException {
		e = xmler.nextEvent();
		if (e instanceof Characters) {
			return e.asCharacters().getData();
		}else{
			return "";
		}
	}

	private static LinkedList<MetaMsg> readMeta(String url){
		LinkedList<MetaMsg> metas = new LinkedList<MetaMsg>();
		try {
			XMLInputFactory xmlif = XMLInputFactory.newInstance();
			InputStream is = new URL(url).openStream();
			XMLEventReader xmler = xmlif.createXMLEventReader(is);
			boolean firstMeta = false;

			while (xmler.hasNext()) {
				XMLEvent e = xmler.nextEvent();
				if (e.isStartElement()) {
					String localPart = e.asStartElement().getName().getLocalPart();
					switch (localPart) {
					case "meta": 
						try{
							MetaMsg meta = new MetaMsg();
							meta.setName(e.asStartElement().getAttributeByName(new QName("name")).getValue());
							meta.setContent(e.asStartElement().getAttributeByName(new QName("content")).getValue());
							metas.add(meta);
						}catch(NullPointerException e1){}

						firstMeta = true;
						break;
					default: 
						if(firstMeta){
							break;
						}
						;break;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return metas;
	}

	public static void main (String[] args) throws IOException{
		for(RSSMsg msg : readFeeds("http://oceanrep.geomar.de/cgi/search/archive/advanced/export_geomar_RSS2.xml?screen=Search&dataset=archive&_action_export=1&output=RSS2&exp=0%7C1%7C-date%2Fcreators_name%2Ftitle%7Carchive%7C-%7Ccollection%3Acollection%3AANY%3AEQ%3Apublic%7Cdate%3Adate%3AALL%3AEQ%3A2015%7Cifmgeomar_type%3Aifmgeomar_type%3AANY%3AEQ%3Aarticle_sci_ref%7C-%7Ceprint_status%3Aeprint_status%3AANY%3AEQ%3Aarchive&n=")) {
			System.out.println(msg.toString());
			System.out.println(readMeta(msg.getLink()).toString());
		}
	}


}
