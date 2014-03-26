package de.pubflow.components.jiraConnector.ws;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import de.pubflow.components.jiraConnector.JiraMessage;


@WebService(targetNamespace = "pubflow.de")
@SOAPBinding(style = Style.DOCUMENT)
public interface IJiraToPubFlowConnector {

	@javax.jws.WebMethod
	public void eventNotification(@javax.jws.WebParam(name = "jiraMessage") JiraMessage message);

	@javax.jws.WebMethod
	public HashMapStringClassWrapper getParameterMap(@javax.jws.WebParam(name = "workflowName") String workflowName);

	@javax.jws.WebMethod
	public HashMapStringLongWrapper getWorkflowNames();


}
