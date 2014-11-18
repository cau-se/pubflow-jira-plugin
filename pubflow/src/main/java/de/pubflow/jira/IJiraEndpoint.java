package de.pubflow.jira;

import java.util.HashMap;
import java.util.LinkedList;

import javax.jws.WebParam;

//@WebService(targetNamespace = "pubflow.de")
public interface IJiraEndpoint {

	@javax.jws.WebMethod
	public String createIssue(@WebParam(name = "projectKey") String projectKey, @WebParam(name = "wfName") String wfName, @WebParam(name = "summary") String summary, @WebParam(name = "description") String description, @WebParam(name = "parameters") HashMap<String, String> parameters, @WebParam(name = "reporter") String reporter);

	@javax.jws.WebMethod
	public String createIssueType(@WebParam(name = "projectKey") String projectKey, @WebParam(name = "issueTypeName") String issueTypeName, @WebParam(name = "parameters") HashMap<String, String> parameters);

	@javax.jws.WebMethod
	public boolean changeStatus(@WebParam(name = "issueKey") String issueKey, @WebParam(name = "statusId") String statusId);

	@javax.jws.WebMethod
	public LinkedList<String> getStatusNames(@WebParam(name = "projectKey") String projectKey);

	@javax.jws.WebMethod
	public boolean addIssueComment(@WebParam(name = "issueKey") String issueKey, @WebParam(name = "comment") String comment);

	@javax.jws.WebMethod
	public boolean createProject(@WebParam(name = "projectName") String projectName, @WebParam(name = "projectKey") String projectKey, @WebParam(name = "workflowXML") String workflowXML, @WebParam(name = "steps") LinkedList<String> steps);

	@javax.jws.WebMethod
	public boolean  addAttachment(@WebParam(name = "issueKey") String issueKey, @WebParam(name = "bArray") byte[]barray, @WebParam(name = "fileName") String fileName, @WebParam(name = "type") String type);

	@javax.jws.WebMethod
	public void removeAttachment(@WebParam(name = "attachmentId") long attachmentId);
	
	@javax.jws.WebMethod
	boolean addWorkflow(@WebParam(name = "projectKey") String projectKey, @WebParam(name = "workflowXML") String workflowXML);
}