package de.pubflow.jira;

import javax.jws.WebService;

import de.pubflow.common.entity.PubFlowMessage;

@WebService(endpointInterface = "de.cau.tf.ifi.se.pubflow.jira.IJiraToPubFlowConnector")
public class JiraToPubFlowConnector implements IJiraToPubFlowConnector{

	@Override
	public void eventNotification(PubFlowMessage message) {
		JiraPluginMsgProducer msgProducer = new JiraPluginMsgProducer();
		msgProducer.onMsg(message);
	}
}
