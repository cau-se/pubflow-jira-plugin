package de.pubflow.jira.misc;

public enum Appendix{
	ISSUETYPE(""),
	FIELDSCREEN("_FieldScreen"),
	WORKFLOWSCHEME("_WorkflowScheme"),
	WORKFLOW("_Workflow"),
	FIELDSCREENSCHEME("_FieldScreenScheme"),
	ISSUETYPESCHEME("_IssueTypeScheme");

	private String name = "";

	Appendix(String type){
		this.getType(type);
	}

	public String getName() {
		return name;
	}

	public void getType(String type) {
		this.name = type;
	}
}