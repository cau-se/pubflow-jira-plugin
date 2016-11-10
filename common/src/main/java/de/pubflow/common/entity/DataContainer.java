/**
 * Copyright (C) 2016 Marc Adolf, Arnd Plumhoff (http://www.pubflow.uni-kiel.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.pubflow.common.entity;

import java.util.HashMap;
import java.util.LinkedList;

import org.codehaus.jackson.annotate.JsonIgnore;

public class DataContainer{	
	
	private String defaultIssueKey;
	private String status;
	private LinkedList<JiraAttachment> attachments;
	private LinkedList<JiraComment> comments;
	private LinkedList<JiraIssue> issues;
	private HashMap<String, String> map;
	
	public DataContainer(){
		this.status = "";
		this.defaultIssueKey = "";
		issues = new LinkedList<JiraIssue>();
		attachments = new LinkedList<JiraAttachment>();
		comments = new LinkedList<JiraComment>();
		map =  new HashMap<String, String>();
	}
	
	public DataContainer(String defaultIssueKey){
		this();
		this.defaultIssueKey = defaultIssueKey;
	}

	public String getDefaultIssueKey() {
		return defaultIssueKey;
	}

	public void setDefaultIssueKey(String defaultIssueKey) {
		this.defaultIssueKey = defaultIssueKey;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status){
		this.status = status;
	}

	public LinkedList<JiraAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(LinkedList<JiraAttachment> attachments) {
		this.attachments = attachments;
	}

	public LinkedList<JiraComment> getComments() {
		return comments;
	}

	public void setComments(LinkedList<JiraComment> comments) {
		this.comments = comments;
	}

	public LinkedList<JiraIssue> getIssues() {
		return issues;
	}

	public void setIssues(LinkedList<JiraIssue> issues) {
		this.issues = issues;
	}

	public HashMap<String, String> getMap() {
		return map;
	}

	public void setMap(HashMap<String, String> map) {
		this.map = map;
	}

	public String get(String key) {
		return map.get(key);
	}

	public void put(String key, String value) {
		map.put(key, value);
	}

	public boolean containsKey(String key){
		return map.containsKey(key);
	}
	
	@JsonIgnore
	public void newJiraIssue(String workflowName, String summary, String description, String reporter, HashMap<String, String> parameters){
		issues.add(new JiraIssue(workflowName, summary, description, parameters, reporter));
	}

	@JsonIgnore
	public void newJiraComment(String text){
		comments.add(new JiraComment(defaultIssueKey, text));
	}

	@JsonIgnore
	public void newJiraAttachment(String fileName, byte[] data){
		attachments.add(new JiraAttachment(defaultIssueKey, "", fileName, data));
	}
	
	@JsonIgnore
	public void flush(){
		this.comments = new LinkedList<JiraComment>();
		this.issues = new LinkedList<JiraIssue>();
		this.attachments = new LinkedList<JiraAttachment>();
		this.status = "";
	}
}