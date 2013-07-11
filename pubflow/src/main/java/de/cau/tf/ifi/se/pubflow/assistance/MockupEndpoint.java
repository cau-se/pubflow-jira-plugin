package de.cau.tf.ifi.se.pubflow.assistance;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MockupEndpoint {
	
	@EndpointInject(uri = "Jira:from:msg.queue")
	ProducerTemplate producer;
	
	Logger myLogger;
	
	public MockupEndpoint()
	{
		myLogger = LoggerFactory.getLogger(this.getClass());
		myLogger.info("TESTMSG SENDER STARTED");
	}
	
	public void doSomething() {
		myLogger.info("Preparing TestMsg");
		try {
			
			producer.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		producer.sendBody("TestMsg");
		myLogger.info("TestMsg send");
	}

}
