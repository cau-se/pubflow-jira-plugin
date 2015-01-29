package de.pubflow.server.core.jira;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import de.pubflow.server.core.jira.Entity.JiraAttachment;
import de.pubflow.server.core.jira.Entity.JiraComment;


public class ComMap{	
	private HashMap<String, Object> map;
	private LinkedList<JiraAttachment> attachments;
	private LinkedList<JiraComment> comments;
	private String defaultIssueKey;

	public ComMap(String defaultIssueKey){
		this.defaultIssueKey = defaultIssueKey;

		attachments = new LinkedList<JiraAttachment>();
		comments = new LinkedList<JiraComment>();
		map =  new HashMap<String, Object>();
	}

	public String getDefaultIssueKey() {
		return defaultIssueKey;
	}

	public void setDefaultIssueKey(String defaultIssueKey) {
		this.defaultIssueKey = defaultIssueKey;
	}

	public Set<Entry<String, Object>> entrySet(){
		return map.entrySet();
	}

	public void newJiraComment(String text){
		comments.add(new JiraComment(defaultIssueKey, text));
	}

	public void newJiraAttachment(String fileName, byte[] data){
		attachments.add(new JiraAttachment(defaultIssueKey, "", fileName, data));
	}

	public LinkedList<JiraComment> getJiraComments(){
		return comments;
	}

	public void flushData(){
		attachments = new LinkedList<JiraAttachment>();
		comments = new LinkedList<JiraComment>();
	}

	public LinkedList<JiraAttachment> getJiraAttachments(){
		return attachments;
	}

	public String get(Object key) throws Exception {
		try{
			return (String) map.get(key);
		}catch(Exception e){
			throw new Exception("set with key " + key + "is protected!");
		}
	}

	public void put(String key, Object value) {
		map.put(key, value);
	}

}