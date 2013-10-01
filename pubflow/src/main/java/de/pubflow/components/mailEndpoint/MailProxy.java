package de.pubflow.components.mailEndpoint;

import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class MailProxy {

	private static MailProxy instance;
	private static Logger myLogger;

	static {
		myLogger = LoggerFactory.getLogger(MailProxy.class);
	}

	private MailProxy() {

	}

	public static synchronized MailProxy getInstance() {
		if (instance == null) {
			instance = new MailProxy();
		}
		return instance;
	}

	@Consume(uri = "mail-jms:mailproxy:in.queue")
	public void produceMail(String pMsg) {
		myLogger.info("Recived msg: " + pMsg);

	}

	

}
