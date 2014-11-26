package de.pubflow.jira.misc;

public enum Appendix{
	ISSUETYPE(""),
	FIELDSCREEN("_FieldScreen"),
	WORKFLOWSCHEME("_WorkflowScheme"),
	WORKFLOW("_Workflow"),
	FIELDSCREENSCHEME("_FieldScreenScheme"),
	ISSUETYPESCHEME("_IssueTypeScheme");

	private String type = "";

	Appendix(String type){
		this.setType(type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}