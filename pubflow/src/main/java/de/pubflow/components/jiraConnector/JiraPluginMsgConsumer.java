package de.pubflow.components.jiraConnector;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.camel.Consume;

import de.pubflow.components.jiraConnector.wsArtifacts.IJiraEndpoint;
import de.pubflow.components.jiraConnector.wsArtifacts.JiraEndpointService;
import de.pubflow.core.communication.message.MessageToolbox;
import de.pubflow.core.communication.message.jira.CamelJiraMessage;
public class JiraPluginMsgConsumer {

	private static final String KEYSTOREFILE="pubflow_keystore.ks";
	private static final String KEYSTOREPW="rainbowdash_1";	
	private static IJiraEndpoint jiraEndpoint;
	private static JiraPluginMsgConsumer instance; 

	private JiraPluginMsgConsumer(){
	}

	public static JiraPluginMsgConsumer getInstance(){
		if(instance == null){
			instance = new  JiraPluginMsgConsumer();
		}
		return instance;
	}

	public static IJiraEndpoint getJiraEndpoint() {
		System.getProperties().put("javax.net.ssl.trustStore", KEYSTOREFILE);
		System.getProperties().put("javax.net.ssl.trustStorePassword", KEYSTOREPW);

		if(jiraEndpoint == null){
			try {
				Service service = JiraEndpointService.create(new URL("http://localhost:8889/JiraEndpoint?wsdl"), new QName("http://webservice.jira.pubflow.de/", "JiraEndpointService"));
				jiraEndpoint = service.getPort(IJiraEndpoint.class);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return jiraEndpoint;
	}

	//	private void addIssueComment(HashMap<String, String> map){
	//		getJiraEndpoint().addIssueComment(map.get("issueKey"), map.get("comment"));
	//	}
	//
	//	private void changeStatus(HashMap<String, String> map){
	//		getJiraEndpoint().changeStatus(map.get("issueKey"), map.get("statusId"));
	//	}
	//
	private void addAttachment(HashMap<String, String> map){
		byte[] data = Charset.forName(StandardCharsets.UTF_8.name()).encode(map.get("attachmentString")).array();

		System.out.println("received!");
		System.out.println(getJiraEndpoint().addAttachment(map.get("issueKey"), data,  map.get("attachmentFileName"), map.get("attachmentFileType")));
	}
	//
	//	private void createIssueType(HashMap<String, String> map){
	//		de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Parameters params = 
	//				new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Parameters();
	//
	//		
	//		for(Entry<String, String> entry : CamelJiraMessage.getMap("parameters", map).entrySet()){
	//			de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Parameters.Entry newEntry = 
	//					new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Parameters.Entry();
	//			
	//			newEntry.setKey(entry.getKey());
	//			newEntry.setValue(entry.getValue());
	//			params.getEntry().add(newEntry);
	//		}
	//
	//		getJiraEndpoint().createIssueType("PubFlow", map.get("wfName"), params);
	//	}
	//
	//	private void createIssue(HashMap<String, String> map){
	//		de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Parameters params = 
	//				new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Parameters();
	//		for(Entry<String, String> entry : CamelJiraMessage.getMap("parameters", map).entrySet()){
	//			de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Parameters.Entry newEntry = 
	//					new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Parameters.Entry();
	//			newEntry.setKey(entry.getKey());
	//			newEntry.setValue(entry.getValue());
	//			params.getEntry().add(newEntry);
	//		}
	//		getJiraEndpoint().createIssue("PUB", map.get("wfName"), map.get("summary"), map.get("description"), params, "admin");
	//	}

	@Consume(uri="t2-jms:jira:toJira.queue")
	public void onMsg(String env){

		CamelJiraMessage msg = MessageToolbox.loadFromString(env, CamelJiraMessage.class);

		switch(msg.getAction()){
		//		case "jira.newComment":  addIssueComment(msg.getMessage()) ;break;
		//		case "jira.newIssue": createIssue(msg.getMessage()) ;break;
		//		case "jira.newIssueType": createIssueType(msg.getMessage()) ;break;
		//		case "jira.changeStatus": changeStatus(msg.getMessage()) ;break;
		case "jira.addAttachment": addAttachment(msg.getMessage()) ;break;
		}
	}
}
