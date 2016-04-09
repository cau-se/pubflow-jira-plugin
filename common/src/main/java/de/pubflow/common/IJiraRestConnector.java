package de.pubflow.common;

import javax.ws.rs.core.Response;


public interface IJiraRestConnector {
	public Response changeStatus(String issueKey, String statusName);
	public Response addAttachment(String issueKey, byte[]barray, String fileName, String type);	
	public Response addIssueComment(String issueKey, String comment);
}
