package de.cau.tf.ifi.se.pubflow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cau.tf.ifi.se.pubflow.assistance.Consumer;
import de.cau.tf.ifi.se.pubflow.assistance.MockupEndpoint;
import de.cau.tf.ifi.se.pubflow.common.exception.PropAlreadySetException;
import de.cau.tf.ifi.se.pubflow.common.exception.PropNotSetException;
import de.cau.tf.ifi.se.pubflow.communication.message.Message;
import de.cau.tf.ifi.se.pubflow.communication.message.text.TextMessage;
import de.cau.tf.ifi.se.pubflow.server.Server;
import de.cau.tf.ifi.se.pubflow.workflow.WFBroker;

/**
 * 
 * 
 */
public class PubFlowCore {

	private DefaultCamelContext context;
	private Properties pubflowConf;
	private static PubFlowCore instance;
	private Logger myLogger;
	private Server server;

	private static final String CONF_FILE = "Pubflow.conf";
	private static final String SERVERFLAG = "CONFSERVER";
	private static final String ACTIVEFLAG = "ON";

	private static final String WFBROKER_NAME = "WFBROKER";
	private static final String MOCKUPENDPOINT_NAME = "MOCKUP";

	// ----------------------------------------------------------------------------------------
	// Initializers
	// ----------------------------------------------------------------------------------------

	// ----------------------------------------------------------------------------------------
	//
	// ----------------------------------------------------------------------------------------

	private PubFlowCore() {

		String art = " _____       _     ______ _"+"\n"
				+ "|  __ \\     | |   |  ____| |"+"\n"
				+ "| |__) |   _| |__ | |__  | | _____      __"+"\n"
				+ "|  ___/ | | | '_ \\|  __| | |/ _ \\ \\ /\\ / /"+"\n"
				+ "| |   | |_| | |_) | |    | | (_) \\ V  V /"+"\n"
				+ "|_|    \\__,_|_.__/|_|    |_|\\___/ \\_/\\_/ "+"\n"
				+ "__________________________________________"+"\n"
				+ "Version 2.0 \n";
		System.out.println(art);
		
		myLogger = LoggerFactory.getLogger(this.getClass());
		myLogger.info("Starting PubFlow System");

		FileInputStream fi = null;
		try {
			fi = new FileInputStream(CONF_FILE);
		} catch (Exception e) {
			myLogger.error("Could not find Properties File");

			// e.printStackTrace();
		}
		pubflowConf = new Properties();
		try {
			pubflowConf.loadFromXML(fi);
		} catch (Exception e) {
			myLogger.error("Could not load Properties File");
			// e.printStackTrace();
		}
		pubflowConf.list(System.out);

		// Create CamelContext
		context = new DefaultCamelContext();
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				"vm://localhost?broker.persistent=false");
		// Add the queues to the DefaultContext
		context.addComponent("test-jms",
				JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		context.addComponent("t2-jms",
				JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		myLogger.info("Creating Routes");
		try {
			initRoutes();
		} catch (Exception e1) {
			myLogger.error("An error occured while creating the CamelRoutes");
			e1.printStackTrace();
		}
		myLogger.info("Starting the Routes");
		try {
			startConnections();
		} catch (Exception e1) {
			myLogger.error("An Error occured, while Starting the routes");
			e1.printStackTrace();
		}
		// Start the Confserver
		myLogger.info("Starting Configuration GUI");
		try {
			String sflag = getProperty(SERVERFLAG, this.getClass().getName());
			if (sflag.equalsIgnoreCase(ACTIVEFLAG)) {
				myLogger.info("Configuration GUI is active");
				// TODO start a conf GUI
			}
		} catch (PropNotSetException e) {
			myLogger.warn("Configuration GUI is disabled");
			e.printStackTrace();
		}
		// start the internal Server
		myLogger.info("Starting the internal PubFlow Server");
		server = new Server();
		try {
			server.startup();
			myLogger.info("PubFlow Server up and running");
		} catch (Exception e) {
			myLogger.error("Failed to start the internal PubFlow Server");
			e.printStackTrace();
		}

		// Register shutdownhook
		Thread t = new Thread(new ShutdownActions());
		Runtime.getRuntime().addShutdownHook(t);
	}

	public static PubFlowCore getInstance() {
		if (instance == null) {
			instance = new PubFlowCore();
		}
		return instance;
	}

	// ----------------------------------------------------------------------------------------
	// Public methods
	// ----------------------------------------------------------------------------------------

	public String getProperty(String key, String calleeSig)
			throws PropNotSetException {
		String prop = pubflowConf.getProperty(calleeSig + "-" + key);
		if ((prop == null) || (prop.equals("")))
			throw new PropNotSetException();
		return prop;
	}

	public void setProperty(String key, String calleeSig, String prop)
			throws PropAlreadySetException {
		String temp = pubflowConf.getProperty(calleeSig + "-" + key);
		if (temp != null)
			throw new PropAlreadySetException();
		pubflowConf.setProperty(calleeSig + "-" + key, prop);
	}

	public void updateProperty(String key, String calleeSig, String prop)
			throws PropNotSetException {
		String temp = pubflowConf.getProperty(calleeSig + "-" + key);
		if ((temp == null) || (temp.equals("")))
			throw new PropNotSetException();
		pubflowConf.setProperty(calleeSig + "-" + key, prop);
	}

	public void saveProperties() {
		FileOutputStream fs = null;
		try {
			fs = new FileOutputStream(CONF_FILE);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			pubflowConf.storeToXML(fs, "PubFlow Properties File (last updated "
					+ Calendar.getInstance().getTime().toLocaleString() + ")");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopInternalServer() {
		server.stop();
	}
	
	public void stopCamel()
	{
		myLogger.info("Stopping CamelContext");
		try {
			context.stop();
		} catch (Exception e) {
			myLogger.error("Ups ...");
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------------------------------------
	// Private methods
	// ----------------------------------------------------------------------------------------

	/**
	 * @throws Exception
	 * 
	 */
	private void initRoutes() throws Exception {
		context.addRoutes(new RouteBuilder() {
			public void configure() {
				from("t2-jms:queue:test.queue").to(
						"test-jms:queue:out.queue")
						.bean(Consumer.getInstance()).to("test-jms:queue:out2.queue")
						.bean(WFBroker.getInstance());
				from("test-jms:queue:testOut.queue").to(
						"test-jms:queue:out.queue")
						.bean(Consumer.getInstance());
			}
		});
	}

	/**
	 * @throws Exception
	 * 
	 */
	private void startConnections() throws Exception {
		// Now everything is set up - lets start the context
		context.start();
	}

	// ----------------------------------------------------------------------------------------
	// Getter & Setters
	// ----------------------------------------------------------------------------------------

	public DefaultCamelContext getContext() {
		return context;
	}

	public void setContext(DefaultCamelContext context) {
		this.context = context;
	}
	
	// ----------------------------------------------------------------------------------------
	// Main-method for starting PubFlow
	// ----------------------------------------------------------------------------------------

	

	public static void main(String[] args) {
		
		PubFlowCore c = PubFlowCore.getInstance();
		// Do some tests
		ProducerTemplate template = c.getContext().createProducerTemplate();
		for (int i = 0; i < 1; i++) {
			TextMessage text = new TextMessage();
			text.setContent("Test");
            template.sendBody("t2-jms:queue:test.queue", text.transformToString());
            
        }
	}

	// ----------------------------------------------------------------------------------------
	// Inner Classes
	// ----------------------------------------------------------------------------------------

	/**
	 * 
	 * @author pcb
	 * 
	 */
	class ShutdownActions implements Runnable {

		
		// Register all shutdown actions here
		public void run() {
			Thread.currentThread().setName("PubFlow Shutdownhook");
			Logger shutdownLogger = LoggerFactory.getLogger(this.getClass());
			shutdownLogger.info("<< Shutting down PubFlow >>");
			PubFlowCore core = PubFlowCore.getInstance();
			// Shutdown Camel & free ports
			shutdownLogger.debug("Stopping Camel Context");
			core.stopCamel();
			shutdownLogger.debug("Camel Context is down");
			// Stop internal server
			shutdownLogger.debug("Stopping internal server");
			core.stopInternalServer();
			// Write props to file
			shutdownLogger.debug("Saving Properties to file");
			core.saveProperties();
			shutdownLogger.info("<< BYE >>");
		}

	}

}
