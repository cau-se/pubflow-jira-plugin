package de.pubflow.components.jiraConnector;

import javax.xml.ws.Endpoint;

import de.pubflow.components.jiraConnector.ws.JiraToPubFlowConnector;

public class TestProvider {
	public static void main(String[]args){
		Endpoint.publish("http://localhost:8889/" + JiraToPubFlowConnector.class.getSimpleName(), new JiraToPubFlowConnector());
		try {
			//JiraPlugin.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}