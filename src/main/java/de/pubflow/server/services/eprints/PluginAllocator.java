package de.pubflow.server.services.eprints;

import de.pubflow.server.core.jira.ComMap;
import de.pubflow.server.core.jira.JiraEndpoint;
import de.pubflow.server.core.jira.Entity.JiraIssue;
import de.pubflow.server.services.eprints.entity.RSSMsg;

public class PluginAllocator {
	
	public static ComMap checkRSSFeeds(ComMap data){
		HTMLReader htmlReader = new HTMLReader();
		data = htmlReader.checkRSSFeed(data);
				
		for(JiraIssue issue : data.getJiraIssuesAndFlush()){
			JiraEndpoint.createIssue(issue);
		}
		
		return data;
	}
	
	public static void main (String[] args) throws Exception{
		HTMLReader htmlReader = new HTMLReader();
//		for(RSSMsg msg : htmlReader.readFeeds("http://oceanrep.geomar.de/cgi/search/archive/advanced/export_geomar_RSS3.xml?screen=Search&dataset=archive&_action_export=1&output=RSS3&exp=0%7C1%7C-date%2Fcreators_name%2Ftitle%7Carchive%7C-%7Ccollection%3Acollection%3AANY%3AEQ%3Apublic%7Cdate%3Adate%3AALL%3AEQ%3A2015%7Cifmgeomar_type%3Aifmgeomar_type%3AANY%3AEQ%3Aarticle_sci_ref%7C-%7Ceprint_status%3Aeprint_status%3AANY%3AEQ%3Aarchive&n=")) {
//			System.out.println(msg.getLink());
//			System.out.println(msg.getDescription());
//			
//			try{
//				System.out.println(htmlReader.checkForValidDOI(htmlReader.readMeta(msg.getLink())));
//			}catch(Exception e){
//				System.out.println(e.getMessage());
//			}
//			System.out.println();
//		}
		
		ComMap data = new ComMap("");
		htmlReader.checkRSSFeed(data);
		
	}
}
