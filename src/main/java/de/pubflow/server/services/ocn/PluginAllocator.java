package de.pubflow.server.services.ocn;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import de.pubflow.server.core.jira.ByteRay;
import de.pubflow.server.core.jira.JiraEndpoint;

public class PluginAllocator {

	public static HashMap<String, byte[]> getData(String legid, String issue){
		OCNDataLoader loader = new OCNDataLoader();
		HashMap <String, byte[]> data = new HashMap <String, byte[]>();

		try {
			JiraEndpoint.changeStatus(issue, Messages.getString("PluginAllocator.0")); //$NON-NLS-1$

			data = loader.getData(legid, 0);

			for(String s : (LinkedList<String>)ByteRay.getJiraComments(data)){
				JiraEndpoint.addIssueComment(issue, s);
			} 

			for(Entry <String, byte[]> e : ((HashMap<String, byte[]>) ByteRay.getJiraAttachments(data)).entrySet()){
				JiraEndpoint.addAttachment(issue, e.getValue(), e.getKey(), ""); //$NON-NLS-1$
			} 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			JiraEndpoint.changeStatus(issue, Messages.getString("PluginAllocator.2")); //$NON-NLS-1$
			e.printStackTrace();
			JiraEndpoint.addIssueComment(issue, e.getMessage());
		}finally{
			data = ByteRay.flushData(data);		
		}

		return data;
	}

	public static HashMap<String, byte[]> convert(String issuekey, HashMap<String, byte[]> tempResult){
		OCNToPangaeaMapper mapper = new OCNToPangaeaMapper();
		HashMap<String, byte[]> result = new HashMap<String, byte[]>();

		try {
			result = mapper.mapValues(tempResult, 0);

			for(String s : (LinkedList<String>)ByteRay.getJiraComments(result)){
				JiraEndpoint.addIssueComment(issuekey, s);
			} 

			for(Entry <String, byte[]> e : ((HashMap<String, byte[]>)ByteRay.getJiraAttachments(result)).entrySet()){
				JiraEndpoint.addAttachment(issuekey, e.getValue(), e.getKey(), ""); //$NON-NLS-1$
			} 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			JiraEndpoint.changeStatus(issuekey, Messages.getString("PluginAllocator.2")); //$NON-NLS-1$
			e.printStackTrace();
			JiraEndpoint.addIssueComment(issuekey, e.getMessage());
		}finally{
			result = ByteRay.flushData(result);		
		}

		return result;
	}

	public static void toCSV(String issuekey, HashMap<String, byte[]> data, String pid, String login, String source, String author, String project, String topology, String status, String zielpfad, String reference, String filename, String legcomment){
		FileCreator4D fc4d = new FileCreator4D();

		try {
			HashMap<String, byte[]> tResult = fc4d.toCSV((HashMap<String, byte[]>)data, (String) pid, (String) login, (String) source, (String) author, (String) project, (String) topology, (String) status, (String) zielpfad, (String) reference, (String) filename, (String) legcomment, 0);

			for(String s : (LinkedList<String>)ByteRay.getJiraComments(tResult)){
				JiraEndpoint.addIssueComment(issuekey, s);
			} 

			for(Entry <String, byte[]> e : ((HashMap<String, byte[]>)ByteRay.getJiraAttachments(tResult)).entrySet()){
				JiraEndpoint.addAttachment(issuekey, e.getValue(), e.getKey(), ""); //$NON-NLS-1$
			} 

			try {
				Thread.sleep(60000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			JiraEndpoint.changeStatus(issuekey, Messages.getString("PluginAllocator.6")); //$NON-NLS-1$

		} catch (Exception e) {
			JiraEndpoint.changeStatus(issuekey, Messages.getString("PluginAllocator.2")); //$NON-NLS-1$
			e.printStackTrace();
			JiraEndpoint.addIssueComment(issuekey, e.getMessage());
		}
	}
}
