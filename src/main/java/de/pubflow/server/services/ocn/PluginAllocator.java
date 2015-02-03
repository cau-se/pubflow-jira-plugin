package de.pubflow.server.services.ocn;

import java.util.LinkedList;
import java.util.Map.Entry;

import de.pubflow.server.core.jira.ComMap;
import de.pubflow.server.core.jira.JiraEndpoint;
import de.pubflow.server.core.jira.Entity.JiraAttachment;
import de.pubflow.server.core.jira.Entity.JiraComment;

public class PluginAllocator {

	public static ComMap getData(String issueKey, ComMap data){

		if(PluginManfestValidator.check("de.pubflow.server.services.ocn.getData", data, PluginAllocator.class.getResourceAsStream("PluginManifest.xml"))){

			OCNDataLoader loader = new OCNDataLoader();

			try {
				JiraEndpoint.changeStatus(issueKey, Messages.getString("PluginAllocator.0"));

				data = loader.getData(data, 0);

				for(JiraComment comment : (LinkedList<JiraComment>)data.getJiraComments()){
					JiraEndpoint.addIssueComment(comment);
				} 

				for(JiraAttachment attachment : (data.getJiraAttachments())){
					JiraEndpoint.addAttachment(attachment);
				} 

			} catch (Exception e) {
				// TODO Auto-generated catch block
				JiraEndpoint.changeStatus(issueKey, Messages.getString("PluginAllocator.2"));
				e.printStackTrace();
				JiraEndpoint.addIssueComment(new JiraComment(issueKey, e.getMessage()));
			}finally{
				data.flushData();		
			}

			return data;
		}
	}

	public static ComMap convert(String issueKey, ComMap data){

		if(PluginManfestValidator.check("de.pubflow.server.services.ocn.convert", data, PluginAllocator.class.getResourceAsStream("PluginManifest.xml"))){

			OCNToPangaeaMapper mapper = new OCNToPangaeaMapper();

			try {
				data = mapper.mapValues(data, 0);

				for(JiraComment comment : (LinkedList<JiraComment>)data.getJiraComments()){
					JiraEndpoint.addIssueComment(comment);
				} 

				for(JiraAttachment attachment : (data.getJiraAttachments())){
					JiraEndpoint.addAttachment(attachment);
				} 

			} catch (Exception e) {
				// TODO Auto-generated catch block
				JiraEndpoint.changeStatus(issueKey, Messages.getString("PluginAllocator.2"));
				e.printStackTrace();
				JiraEndpoint.addIssueComment(new JiraComment(issueKey, e.getMessage()));
			}finally{
				data.flushData();		
			}
			return data;
		}
	}

	public static void toCSV(String issueKey, ComMap data){
		if(PluginManfestValidator.check("de.pubflow.server.services.ocn.toCSV", data, PluginAllocator.class.getResourceAsStream("PluginManifest.xml"))){

			FileCreator4D fc4d = new FileCreator4D();
			data.setDefaultIssueKey(issueKey);

			try {		
				data = fc4d.toCSV(data, 0);

				for(JiraComment comment : (LinkedList<JiraComment>)data.getJiraComments()){
					JiraEndpoint.addIssueComment(comment);
				} 

				for(JiraAttachment attachment : data.getJiraAttachments()){
					JiraEndpoint.addAttachment(attachment); 
				}

				JiraEndpoint.changeStatus(issueKey, Messages.getString("PluginAllocator.6"));

			} catch (Exception e) {
				JiraEndpoint.changeStatus(issueKey, Messages.getString("PluginAllocator.2"));
				e.printStackTrace();
				JiraEndpoint.addIssueComment(new JiraComment(issueKey, e.getMessage()));
			}
		}
	}
}
