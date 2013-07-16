package de.cau.tf.ifi.se.pubflow.assistance;

import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cau.tf.ifi.se.pubflow.communication.message.text.TextMessage;

public class Consumer {

	private static Consumer instance;
	private Logger myLogger;
	
	private Consumer(){myLogger = LoggerFactory.getLogger(this.getClass());	}
	
	public static Consumer getInstance(){
		if (instance == null) return instance = new Consumer();
		return instance;
	}
	
	@Consume(uri = "test-jms:queue:out.queue")
	public void onCheese(String name) {
		myLogger.info("Input recived >> "+name);
		TextMessage tm = new TextMessage();
		tm.initFromString(name);
		myLogger.info("content >> "+tm.getContent());
		myLogger.info("type >> "+tm.getType());
	}
	
	
}
