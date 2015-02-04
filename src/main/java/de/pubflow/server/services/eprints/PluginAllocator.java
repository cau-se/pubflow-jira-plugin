package de.pubflow.server.services.eprints;

import de.pubflow.server.core.jira.ComMap;
import de.pubflow.server.core.jira.JiraEndpoint;
import de.pubflow.server.core.jira.Entity.JiraIssue;

public class PluginAllocator {
	public static void checkRSSFeeds(String issuekey, ComMap data){
		data = HTMLReader.checkRSSFeed(data);
		
		for(JiraIssue issue : data.getJiraIssues()){
			JiraEndpoint.createIssue(issue);
		}
	}
}
