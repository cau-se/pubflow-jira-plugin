package de.pubflow.components.jiraConnector;

import java.io.FileInputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.camel.Consume;

import de.pubflow.components.jiraConnector.wsArtifacts.IJiraEndpoint;
import de.pubflow.components.jiraConnector.wsArtifacts.JiraEndpointService;
import de.pubflow.core.communication.message.MessageToolbox;
import de.pubflow.core.communication.message.jira.CamelJiraMessage;

public class JiraPluginMsgConsumer {
	private static IJiraEndpoint jiraEndpoint;
	private static JiraPluginMsgConsumer instance; 
	private static final String JIRAWS_PORT = "8890";
	private static final String KEYSTOREFILE="keystore_pubflow.ks";
	private static final String TRUSTTOREFILE="truststore_pubflow.ks";
	private static final String KEYSTOREPW="changeit";
	private static final String TRUSTSTOREPW="changeit";


	private JiraPluginMsgConsumer(){
	}

	public static JiraPluginMsgConsumer getInstance(){
		if(instance == null){
			instance = new  JiraPluginMsgConsumer();
		}
		return instance;
	}

	public static IJiraEndpoint getJiraEndpoint() throws Exception{
		//System.setProperty("javax.net.debug", "ssl,handshake,record"); 
		System.setProperty("https.cipherSuites","TLS_DHE_RSA_WITH_AES_128_CBC_SHA");
		
		SSLContext sc = SSLContext.getInstance("SSLv3");

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(JiraPluginMsgConsumer.class.getClassLoader().getResource(KEYSTOREFILE).getFile()), KEYSTOREPW.toCharArray());
		kmf.init( ks, KEYSTOREPW.toCharArray());

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		KeyStore ts = KeyStore.getInstance("JKS");
		ts.load(new FileInputStream(JiraPluginMsgConsumer.class.getClassLoader().getResource(TRUSTTOREFILE).getFile()), TRUSTSTOREPW.toCharArray());
		tmf.init(ts);
		
		sc.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		
		Service service = JiraEndpointService.create(new URL("https://localhost:" + JIRAWS_PORT + "/JiraEndpoint?wsdl"), new QName("http://webservice.jira.pubflow.de/", "JiraEndpointService"));
		jiraEndpoint = service.getPort(IJiraEndpoint.class);
		
		return jiraEndpoint;
	}

	private void addIssueComment(HashMap<String, String> map) throws Exception{
		getJiraEndpoint().addIssueComment(map.get("issueKey"), map.get("comment"));
	}

	private void changeStatus(HashMap<String, String> map) throws Exception{
		getJiraEndpoint().changeStatus(map.get("issueKey"), map.get("statusId"));
	}


	//TODO: return handling?
	private List<String> getStatusNames(HashMap<String, String> map) throws Exception{
		return getJiraEndpoint().getStatusNames(map.get("projectKey"));
	}


	private void addAttachment(HashMap<String, String> map) throws Exception{
		byte[] data = Charset.forName(StandardCharsets.UTF_8.name()).encode(map.get("attachmentString")).array();

		System.out.println("received!");
		System.out.println(getJiraEndpoint().addAttachment(map.get("issueKey"), data,  map.get("attachmentFileName"), map.get("attachmentFileType")));
	}

	private void createIssueType(HashMap<String, String> map) throws Exception{
		de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Parameters params = 
				new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Parameters();

		for(Entry<String, String> entry : CamelJiraMessage.getMap("parameters", map).entrySet()){
			de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Parameters.Entry newEntry = 
					new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssueType.Parameters.Entry();

			newEntry.setKey(entry.getKey());
			newEntry.setValue(entry.getValue());
			params.getEntry().add(newEntry);
		}

		getJiraEndpoint().createIssueType("PubFlow", map.get("wfName"), params);
	}

	private void createIssue(HashMap<String, String> map) throws Exception{
		de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Parameters params = 
				new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Parameters();
		for(Entry<String, String> entry : CamelJiraMessage.getMap("parameters", map).entrySet()){
			de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Parameters.Entry newEntry = 
					new de.pubflow.components.jiraConnector.wsArtifacts.CreateIssue.Parameters.Entry();
			newEntry.setKey(entry.getKey());
			newEntry.setValue(entry.getValue());
			params.getEntry().add(newEntry);
		}
		getJiraEndpoint().createIssue("PUB", map.get("wfName"), map.get("summary"), map.get("description"), params, "admin");
	}

	@Consume(uri="t2-jms:jira:toJira.queue")
	public void onMsg(String env){	
		try{
			CamelJiraMessage msg = MessageToolbox.loadFromString(env, CamelJiraMessage.class);

			switch(msg.getAction()){
			case "jira.newComment":  addIssueComment(msg.getMessage()) ;break;
			case "jira.newIssue": createIssue(msg.getMessage()) ;break;
			case "jira.newIssueType": createIssueType(msg.getMessage()) ;break;
			case "jira.changeStatus": changeStatus(msg.getMessage()) ;break;
			case "jira.addAttachment": addAttachment(msg.getMessage()) ;break;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
