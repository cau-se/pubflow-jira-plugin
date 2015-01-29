package de.pubflow.server.core.jira.Entity;

import java.util.HashMap;

public class JiraIssue {
	private String projectKey; 
	private String workflowName; 
	private String summary; 
	private String description; 
	private HashMap<String, String> parameters; 
	private String reporter;


	public JiraIssue(String projectKey, String workflowName, String summary, String description, HashMap<String, String> parameters, String reporter) {
		super();
		this.projectKey = projectKey;
		this.workflowName = workflowName;
		this.summary = summary;
		this.description = description;
		this.parameters = parameters;
		this.reporter = reporter;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
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
