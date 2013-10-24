package de.pubflow.components.jira;

import java.util.HashMap;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import de.pubflow.PubFlowSystem;
import de.pubflow.core.communication.message.MessageToolbox;
import de.pubflow.core.communication.message.jira.CamelJiraMessage;

public class JiraWFEndpoint {

	public static void RespondToJira(String issue, byte[] file) {
		CamelJiraMessage msg = new CamelJiraMessage();
		msg.setAction("jira.addAttachment");
		HashMap<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("issueKey", issue);
		msgBody.put("attachmentString", new String(file));
		msgBody.put("attachmentFileName", "data");
		msgBody.put("attachmentFileType", ".4d");
		msg.setMessage(msgBody);
		// Sending Msg
		ProducerTemplate producer;
		CamelContext context = PubFlowSystem.getInstance().getContext();
		producer = context.createProducerTemplate();
		//System.out.println(MessageToolbox.transformToString(msg));
		producer.sendBody("t2-jms:jiraendpoint:out.queue",
				MessageToolbox.transformToString(msg));
	}
}
