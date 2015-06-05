package de.pubflow.server.services.eprints;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.pubflow.server.core.jira.ComMap;
import de.pubflow.server.core.jira.JiraEndpoint;
import de.pubflow.server.services.eprints.entity.MetaMsg;
import de.pubflow.server.services.eprints.entity.RSSMsg;

public class HTMLReader {

	private Logger log = LoggerFactory.getLogger(HTMLReader.class);
	protected static CopyOnWriteArrayList<String> missingSupplementIssueSummaries = new CopyOnWriteArrayList<String>(JiraEndpoint.getAllIssuesBySummaryContains("Missing pangaea supplement for "));

	public List<RSSMsg> readFeeds(String url) throws IOException {
		List<RSSMsg> feeds = new LinkedList<RSSMsg>();

		try {
			XMLInputFactory xmlif = XMLInputFactory.newInstance();
			xmlif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			XMLEventReader xmler = xmlif.createXMLEventReader(new URL(url).openStream());

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

		feeds = Lists.reverse(feeds);
		
		return feeds;
	}

	private String getCharData(XMLEvent e, XMLEventReader xmler) throws XMLStreamException {
		e = xmler.nextEvent();
		if (e instanceof Characters) {
			return e.asCharacters().getData();
		}else{
			return "";
		}
	}

	public LinkedList<MetaMsg> readMeta(String url){
		LinkedList<MetaMsg> metas = new LinkedList<MetaMsg>();

		try {
			XMLInputFactory xmlif = XMLInputFactory.newInstance();
			xmlif.setProperty(XMLInputFactory.SUPPORT_DTD, false);

			XMLEventReader xmler = xmlif.createXMLEventReader(new URL(url).openStream());

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
							//							log.info(meta.getName() + " : ");
							//							log.info(meta.getContent());

						}catch(NullPointerException e1){}
						break;
					}
				}

			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return metas;
	}

	public boolean checkForValidDOI(String doi) throws Exception{
		URL urlObj = new URL("http://linkinghub.pangaea.de/epic/redirectToSupplement/" + doi);
		Scanner sc = new Scanner(urlObj.openStream(), "UTF-8");
		String out = sc.useDelimiter("\\A").next();
		sc.close();

		if(out.contains("No supplement available at pangaea.de")){
			return false;
		}

		return true;
	}

	public boolean checkFor404(String doi) throws Exception{
		if(!doi.equals("")){
			URL urlObj = new URL("http://doi.pangaea.de/" + doi);
			HttpURLConnection http = (HttpURLConnection)urlObj.openConnection();
			int statusCode = http.getResponseCode();
			if(statusCode != 404){
				return true;
			}
		}else{
			throw new Exception("empty doi as parameter");
		}
		return false;
	}

	public boolean checkForValidDOI(List<MetaMsg> list) throws Exception{
		for(MetaMsg msg : list){
			if(msg.getName().equals("eprints.id_number")){
				System.out.println(msg.getContent());
				//				log.info(msg.getContent());
				return checkForValidDOI(msg.getContent());
			}
		}

		throw new Exception("eprints.id_number is empty!");	
	}

	public ComMap checkRSSFeed(ComMap data){
		try{
			// for(RSSMsg msg : readFeeds("http://oceanrep.geomar.de/cgi/search/archive/advanced/export_geomar_RSS2.xml?screen=Search&dataset=archive&_action_export=1&output=RSS2&exp=0%7C1%7C-date%2Fcreators_name%2Ftitle%7Carchive%7C-%7Ccollection%3Acollection%3AANY%3AEQ%3Apublic%7Cdate%3Adate%3AALL%3AEQ%3A2015%7Cifmgeomar_type%3Aifmgeomar_type%3AANY%3AEQ%3Aarticle_sci_ref%7C-%7Ceprint_status%3Aeprint_status%3AANY%3AEQ%3Aarchive&n=")) {
			for(RSSMsg msg : readFeeds("http://oceanrep.geomar.de/cgi/search/archive/advanced/export_geomar_RSS3.xml?screen=Search&dataset=archive&_action_export=1&output=RSS3&exp=0%7C1%7C-date%2Fcreators_name%2Ftitle%7Carchive%7C-%7Ccollection%3Acollection%3AANY%3AEQ%3Apublic%7Cdate%3Adate%3AALL%3AEQ%3A2015%7Cifmgeomar_type%3Aifmgeomar_type%3AANY%3AEQ%3Aarticle_sci_ref%7C-%7Ceprint_status%3Aeprint_status%3AANY%3AEQ%3Aarchive&n=")) {
				//				log.info(msg.getLink());
				int index1 = msg.getDescription().indexOf("DOI ") + 4;
				int index2 = msg.getDescription().indexOf(" <", index1);
				
				if(index1 < index2){
					String doi = msg.getDescription().substring(index1, index2);
					boolean pangaeaDataOk = checkForValidDOI(doi);

					if(!pangaeaDataOk){
						if (!missingSupplementIssueSummaries.contains("EPRINTS / " + "Missing pangaea supplement for " + msg.getLink())) {
							log.info("checkRSSFeed - doi without pangaea supplement : " + doi);
							data.newJiraIssue("EPRINTS", "Missing pangaea supplement for " + msg.getLink(), msg.getDescription(), "PubFlow", new HashMap<String, String>());
							missingSupplementIssueSummaries.add("EPRINTS / " + "Missing pangaea supplement for " + msg.getLink());
						}
					}else{
						log.info("checkRSSFeed - doi with pangaea supplement : " + doi);
					}

				}else{
					data.newJiraIssue("EPRINTS", "No DOI for " + msg.getLink(), msg.getDescription(), "PubFlow", new HashMap<String, String>());
					log.info("checkRSSFeed - no DOI for " + msg.getTitle());
					missingSupplementIssueSummaries.add("EPRINTS / " + "Missing pangaea supplement for " + msg.getLink());

				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return data;
	}
}
