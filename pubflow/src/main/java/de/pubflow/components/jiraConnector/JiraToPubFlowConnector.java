package de.pubflow.components.jiraConnector;

import javax.jws.WebService;

import de.pubflow.common.entity.PubFlowMessage;

@WebService(endpointInterface = "de.pubflow.components.jiraConnector.IJiraToPubFlowConnector")
public class JiraToPubFlowConnector implements IJiraToPubFlowConnector{

	@Override
	public void eventNotification(PubFlowMessage message) {
		JiraPluginMsgProducer msgProducer = new JiraPluginMsgProducer();
		msgProducer.onMsg(message);
	}
}
