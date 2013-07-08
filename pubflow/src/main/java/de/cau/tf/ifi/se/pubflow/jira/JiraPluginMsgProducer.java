package de.cau.tf.ifi.se.pubflow.jira;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import de.cau.tf.ifi.se.pubflow.common.entity.PubFlowMessage;

public class JiraPluginMsgProducer{

	@Produce(uri="activemq:jira.input")
	protected ProducerTemplate producer;


	public void onMsg(PubFlowMessage msg){
		if(producer == null){
			System.out.println("Producer not init");
		}
		producer.sendBody(msg);
	}
}
