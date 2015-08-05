package de.pubflow.server.core.jira.Entity;

import java.util.HashMap;

public class JiraIssue {
	private String issueTypeName; 
	private String summary; 
	private String description; 
	private HashMap<String, String> parameters; 
	private String reporter;

	public JiraIssue(String issueTypeName, String summary, String description, HashMap<String, String> parameters, String reporter) {
		this.issueTypeName = issueTypeName;
		this.summary = summary;
		this.description = description;
		this.parameters = parameters;
		this.reporter = reporter;
	}

	public String getIssueTypeName() {
		return issueTypeName;
	}

	public void setIssueTypeName(String issueTypeName) {
		this.issueTypeName = issueTypeName;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}
}
