package de.pubflow.components.jiraConnector;

import javax.jws.WebService;

import de.pubflow.core.communication.message.jira.JiraMessage;

@WebService(endpointInterface = "de.pubflow.components.jiraConnector.IJiraToPubFlowConnector")
public class JiraToPubFlowConnector implements IJiraToPubFlowConnector{

	@Override
	public void eventNotification(JiraMessage message) {
		System.out.println("Starting");
		JiraPluginMsgProducer msgProducer = new JiraPluginMsgProducer();
		msgProducer.onMsg(message);
	}
}
