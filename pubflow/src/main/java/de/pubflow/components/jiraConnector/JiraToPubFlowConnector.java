package de.pubflow.components.jiraConnector;

import java.util.Map.Entry;

import javax.jws.WebService;

import de.pubflow.core.communication.message.jira.CamelJiraMessage;

@WebService(endpointInterface = "de.pubflow.components.jiraConnector.IJiraToPubFlowConnector")
public class JiraToPubFlowConnector implements IJiraToPubFlowConnector{

	@Override
	public void eventNotification(JiraMessage message) {
		System.out.println("Starting");
		JiraPluginMsgProducer msgProducer = new JiraPluginMsgProducer();
		
		
		CamelJiraMessage camelMsg = new CamelJiraMessage();
		
		camelMsg.setAction(message.getAction());
		camelMsg.setTarget(message.getTarget());
		camelMsg.setType(message.getType());
		for(Entry<String, String> e : message.getMessage().entrySet()){
			camelMsg.getMessage().put(e.getKey(), e.getValue());
		}
		
		
		msgProducer.onMsg(camelMsg);
	}
}
