package de.pubflow.components.jiraConnector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.camel.Consume;

import de.pubflow.common.entity.PubFlowMessage;
import de.pubflow.components.jiraConnector.wsArtifacts.IPubFlowToJiraConnector;
import de.pubflow.components.jiraConnector.wsArtifacts.PubFlowToJiraConnectorService;

public class JiraPluginMsgConsumer {
	
	private static final String KEYSTOREFILE="pubflow_keystore.ks";
	private static final String KEYSTOREPW="rainbowdash_1";	
	
	static{
		System.getProperties().put("javax.net.ssl.trustStore", KEYSTOREFILE);
		System.getProperties().put("javax.net.ssl.trustStorePassword", KEYSTOREPW);
		
		try {
			Service service = PubFlowToJiraConnectorService.create(new URL("http://localhost:8889/PubFlowToJiraConnector?wsdl"), new QName("pubflow.de", "JiraToPubFlowConnectorService"));
			pubFlowToJiraConnector = service.getPort(IPubFlowToJiraConnector.class);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static IPubFlowToJiraConnector pubFlowToJiraConnector;

	private void addIssueComment(HashMap<String, String> map){
		pubFlowToJiraConnector.addIssueComment(Long.parseLong(map.get("issueId")), map.get("comment"));
	}

	private void changeStatus(HashMap<String, String> map){
		pubFlowToJiraConnector.changeStatus(Long.parseLong(map.get("issueId")), map.get("statusId"));
	}

	private void createIssueType(HashMap<String, String> map){
		de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Params params = 
				new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Params();
		
		for(Entry<String, String> entry : PubFlowMessage.getMap("parameters", map).entrySet()){
			de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Params.Entry newEntry = 
					new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Params.Entry();
			newEntry.setKey(entry.getKey());
			newEntry.setValue(entry.getValue());
			params.getEntry().add(newEntry);
		}
		
		pubFlowToJiraConnector.createIssueType(map.get("wfName"), params);
	}
	
	private void createIssue(HashMap<String, String> map){
		de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Params params = 
				new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Params();
		
		for(Entry<String, String> entry : PubFlowMessage.getMap("parameters", map).entrySet()){
			de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Params.Entry newEntry = 
					new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Params.Entry();
			newEntry.setKey(entry.getKey());
			newEntry.setValue(entry.getValue());
			params.getEntry().add(newEntry);
		}
		
		pubFlowToJiraConnector.createIssue(map.get("wfName"), map.get("comment"), params);
	}
	
	@Consume(uri="activemq:jira.input")
	public void onMsg(PubFlowMessage msg){

		switch(msg.getAction()){
			case "jira.newComment":  addIssueComment(msg.getMessage()) ;break;
			case "jira.newIssue": createIssue(msg.getMessage()) ;break;
			case "jira.newIssueType": createIssueType(msg.getMessage()) ;break;
			case "jira.changeStatus": changeStatus(msg.getMessage()) ;break;
		}
	}
}
