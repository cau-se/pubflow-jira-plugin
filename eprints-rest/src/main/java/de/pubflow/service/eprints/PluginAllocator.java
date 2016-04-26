package de.pubflow.service.eprints;

import java.util.LinkedList;

import de.pubflow.common.DataContainer;
import de.pubflow.common.JiraRestConnectorHelper;
import de.pubflow.common.entity.JiraAttachment;
import de.pubflow.common.entity.JiraComment;
import de.pubflow.common.entity.JiraIssue;

public class PluginAllocator {
	private static JiraRestConnectorHelper restConnector = new JiraRestConnectorHelper("http://localhost:2990/jira/rest/myrestresource/1.0/pubflow/");

	public static DataContainer checkRSSFeeds(DataContainer data){
		try{
			HTMLReader htmlReader = new HTMLReader();
			data = htmlReader.checkRSSFeed(data);

			for(JiraIssue issue : data.getJiraIssuesAndFlush()){
				restConnector.createIssue(issue.getIssueTypeName(), issue.getSummary(),issue.getDescription(), issue.getParameters(), issue.getReporter());
			}

			for(JiraComment comment : (LinkedList<JiraComment>)data.getJiraCommentsAndFlush()){
				restConnector.addIssueComment(comment.getIssueKey(), comment.getText());
			} 

			for(JiraAttachment attachment : (data.getJiraAttachmentsAndFlush())){
				restConnector.addAttachment(attachment.getIssueKey(), attachment.getData(), attachment.getFilename(), attachment.getType());
			} 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			restConnector.changeStatus(data.getDefaultIssueKey(), "Data Needs Correction");
			e.printStackTrace();
			restConnector.addIssueComment(data.getDefaultIssueKey(), e.getMessage());
		}
		return data;
	}
}
