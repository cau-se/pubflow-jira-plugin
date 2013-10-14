package de.pubflow.components.jiraConnector;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

<<<<<<< HEAD
=======
import de.pubflow.core.communication.message.jira.JiraMessage;

//import de.pubflow.core.communication.message.jira.JiraMessage;

>>>>>>> e3d3706b4067258e7e43b3364356019fa39e779c

@WebService(targetNamespace = "pubflow.de")
@SOAPBinding(style = Style.RPC)
public interface IJiraToPubFlowConnector {

	@javax.jws.WebMethod
	public void eventNotification(@javax.jws.WebParam(name = "jiraMessage") JiraMessage message);

}
