package de.cau.tf.ifi.se.pubflow.jira;

import javax.xml.ws.Endpoint;

public class TestProvider {
	public static void main(String[]args){
		Endpoint.publish("http://localhost:8889/" + JiraToPubFlowConnector.class.getSimpleName(), new JiraToPubFlowConnector());
	}
}
