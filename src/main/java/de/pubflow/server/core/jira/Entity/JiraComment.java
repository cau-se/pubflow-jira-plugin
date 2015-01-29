package de.pubflow.server.core.jira.Entity;

public class JiraComment {
	private String issueKey;
	private String text;
	
	public JiraComment(String issueKey, String comment) {
		super();
		this.issueKey = issueKey;
		this.text = comment;
	}
	
	public String getIssueKey() {
		return issueKey;
	}
	
	public void setIssueKey(String issueKey) {
		this.issueKey = issueKey;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
}
