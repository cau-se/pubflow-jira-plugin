package de.pubflow.components.jiraConnector;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.camel.Consume;

import de.pubflow.components.jiraConnector.wsArtifacts.JiraEndpointService;
import de.pubflow.core.communication.message.jira.JiraMessage;
public class JiraPluginMsgConsumer {
	
	private static final String KEYSTOREFILE="pubflow_keystore.ks";
	private static final String KEYSTOREPW="rainbowdash_1";	
	
	static{
		System.getProperties().put("javax.net.ssl.trustStore", KEYSTOREFILE);
		System.getProperties().put("javax.net.ssl.trustStorePassword", KEYSTOREPW);
		
		try {
			Service service = JiraEndpointService.create(new URL("http://localhost:8889/JiraEndpointService?wsdl"), new QName("pubflow.de", "JiraEndpointService"));
			jiraEndpointService = service.getPort(JiraEndpointService.class);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static JiraEndpointService jiraEndpointService;

	private void addIssueComment(HashMap<String, String> map){
		jiraEndpointService.getJiraEndpointPort().addIssueComment(map.get("issueKey"), map.get("comment"));
	}

	private void changeStatus(HashMap<String, String> map){
		jiraEndpointService.getJiraEndpointPort().changeStatus(map.get("issueKey"), map.get("statusId"));
	}

	private void addAttachment(HashMap<String, String> map){
		byte[] data = Charset.forName(StandardCharsets.UTF_8.name()).encode(map.get("attachmentString")).array();
		
		jiraEndpointService.getJiraEndpointPort().addAttachment(map.get("issueKey"), data,  map.get("attachmentFileName"), map.get("attachmentFileType"));
	}
	
	private void createIssueType(HashMap<String, String> map){
		de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Arg2 params = 
				new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Arg2();
		
		for(Entry<String, String> entry : JiraMessage.getMap("parameters", map).entrySet()){
			de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Arg2.Entry newEntry = 
					new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Arg2.Entry();
			newEntry.setKey(entry.getKey());
			newEntry.setValue(entry.getValue());
			params.getEntry().add(newEntry);
		}
		
		jiraEndpointService.getJiraEndpointPort().createIssueType("PubFlow", map.get("wfName"), params);
	}
	
	private void createIssue(HashMap<String, String> map){
		de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Arg3 params = 
				new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Arg3();
		for(Entry<String, String> entry : JiraMessage.getMap("parameters", map).entrySet()){
			de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Arg3.Entry newEntry = 
					new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Arg3.Entry();
			newEntry.setKey(entry.getKey());
			newEntry.setValue(entry.getValue());
			params.getEntry().add(newEntry);
		}
		jiraEndpointService.getJiraEndpointPort().createIssue("PUB", map.get("wfName"), map.get("comment"), params, "admin");
	}
	
	@Consume(uri="t2-jms:jira:toJira.queue")
	public void onMsg(JiraMessage msg){

		switch(msg.getAction()){
			case "jira.newComment":  addIssueComment(msg.getMessage()) ;break;
			case "jira.newIssue": createIssue(msg.getMessage()) ;break;
			case "jira.newIssueType": createIssueType(msg.getMessage()) ;break;
			case "jira.changeStatus": changeStatus(msg.getMessage()) ;break;
			case "jira.addAttachment": addAttachment(msg.getMessage()) ;break;
		}
	}
}
