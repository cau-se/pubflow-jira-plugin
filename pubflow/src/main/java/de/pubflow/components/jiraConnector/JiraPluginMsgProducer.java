package de.pubflow.components.jiraConnector;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.PubFlowSystem;
import de.pubflow.common.entity.PubFlowMessage;
import de.pubflow.core.communication.message.MessageToolbox;
import de.pubflow.core.communication.message.workflow.WorkflowMessage;

public class JiraPluginMsgProducer {

	private static Logger myLogger;
	private static final String START_WF = "";

	static {
		myLogger = LoggerFactory.getLogger(JiraPluginMsgProducer.class);
	}

	public void onMsg(PubFlowMessage msg) {
		myLogger.info("Received Msg from Jira-Plugin");

		
		if (msg.getAction().equalsIgnoreCase(START_WF)) {
			// Mapping PubFlowMsg to WorkflowMessage
			myLogger.info("Transforming msg to wfMessage");
			WorkflowMessage wfMsg = new WorkflowMessage();

			myLogger.info("Transmitting Msg to pubflow core...");
			// Sending WFMsg
			ProducerTemplate producer;
			CamelContext context = PubFlowSystem.getInstance().getContext();
			producer = context.createProducerTemplate();
			producer.sendBody("test-jms:queue:testOut.queue",
					MessageToolbox.transformToString(wfMsg));

			myLogger.info("Msg sent!");
		}
		else {
			myLogger.warn("NO action defined for this JIRA msg-type!");
		}
	}
}
