package de.cau.tf.ifi.se.pubflow.jira;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointConfiguration;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.PollingConsumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;

public class JiraEndpoint implements Endpoint {

	@Override
	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void configureProperties(Map<String, Object> arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public Consumer createConsumer(Processor arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Exchange createExchange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Exchange createExchange(ExchangePattern arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public Exchange createExchange(Exchange arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PollingConsumer createPollingConsumer() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Producer createProducer() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CamelContext getCamelContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EndpointConfiguration getEndpointConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEndpointKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEndpointUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLenientProperties() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCamelContext(CamelContext arg0) {
		// TODO Auto-generated method stub
	}
}