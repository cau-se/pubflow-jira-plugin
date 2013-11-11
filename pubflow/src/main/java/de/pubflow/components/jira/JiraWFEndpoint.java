package de.pubflow.components.jira;

import java.util.HashMap;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import de.pubflow.PubFlowSystem;
import de.pubflow.core.communication.message.MessageToolbox;
import de.pubflow.core.communication.message.jira.CamelJiraMessage;

public class JiraWFEndpoint {

	public static void addAttachment(String issueKey, byte[] file, String fileName, String fileType) {
		CamelJiraMessage msg = new CamelJiraMessage();
		msg.setAction("jira.addAttachment");
		HashMap<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("issueKey", issueKey);
		msgBody.put("attachmentString", new String(file));
		msgBody.put("attachmentFileName", fileName);
		msgBody.put("attachmentFileType", fileType);
		msg.setMessage(msgBody);
		// Sending Msg
		ProducerTemplate producer;
		CamelContext context = PubFlowSystem.getInstance().getContext();
		producer = context.createProducerTemplate();
		//System.out.println(MessageToolbox.transformToString(msg));
		producer.sendBody("t2-jms:jiraendpoint:out.queue",
				MessageToolbox.transformToString(msg));
	}
	
	public static void newComment(String issueKey, String comment) {
		CamelJiraMessage msg = new CamelJiraMessage();
		msg.setAction("jira.newComment");
		HashMap<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("issueKey", issueKey);
		msgBody.put("comment", comment);
		msg.setMessage(msgBody);
		// Sending Msg
		ProducerTemplate producer;
		CamelContext context = PubFlowSystem.getInstance().getContext();
		producer = context.createProducerTemplate();
		//System.out.println(MessageToolbox.transformToString(msg));
		producer.sendBody("t2-jms:jiraendpoint:out.queue",
				MessageToolbox.transformToString(msg));
	}
	
	public static void changeStatus(String issueKey, String statusId) {
		CamelJiraMessage msg = new CamelJiraMessage();
		msg.setAction("jira.changeStatus");
		HashMap<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("issueKey", issueKey);
		msgBody.put("statusId", statusId);
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
