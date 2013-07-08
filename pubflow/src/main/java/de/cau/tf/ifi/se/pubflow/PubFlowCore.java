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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cau.tf.ifi.se.pubflow.common.exception.PropAlreadySetException;
import de.cau.tf.ifi.se.pubflow.common.exception.PropNotSetException;
import de.cau.tf.ifi.se.pubflow.server.Server;

/**
 * 
 * 
 */
public class PubFlowCore {

	private CamelContext context;
	private Properties pubflowConf;
	private static PubFlowCore instance;
	private Logger myLogger;
	private Server server;
	

	private static final String CONF_FILE = "Pubflow.conf";
	private static final String SERVERFLAG = "CONFSERVER";
	private static final String ACTIVEFLAG = "ON";

	// ----------------------------------------------------------------------------------------
	// Initialisers
	// ----------------------------------------------------------------------------------------

	
	
	

	// ----------------------------------------------------------------------------------------
	//
	// ----------------------------------------------------------------------------------------

	private PubFlowCore() {
		
		
		myLogger = LoggerFactory.getLogger(this.getClass());
		myLogger.info("Starting PubFlow System");
		
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(CONF_FILE);
		} catch (Exception e) {
			myLogger.error("Could not find Properties File");
			
			
			//e.printStackTrace();
		}
		pubflowConf = new Properties();
		try {
			pubflowConf.loadFromXML(fi);
		} catch (Exception e) {
			myLogger.error("Could not load Properties File");
			//e.printStackTrace();
		}
		pubflowConf.list(System.out);
		
		context = new DefaultCamelContext();
		initRoutes();
		startConnections();
		// Start the Confserver
		myLogger.info("Starting Configuration GUI");
		try {
			String sflag = getProperty(SERVERFLAG, this.getClass().getName());
			if(sflag.equalsIgnoreCase(ACTIVEFLAG))
			{
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

	public void stopContext() {
		try {
			context.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getProperty(String key, String calleeSig)
			throws PropNotSetException {
		String prop = pubflowConf.getProperty(calleeSig + "-" + key);
		if ((prop == null)||(prop.equals("")))
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
		if ((temp == null)||(temp.equals("")) )
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
			pubflowConf.storeToXML(fs, "PubFlow Properties File (last updated "+Calendar.getInstance().getTime().toLocaleString()+")");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stopInternalServer()
	{
		server.stop();
	}

	// ----------------------------------------------------------------------------------------
	// Private methods
	// ----------------------------------------------------------------------------------------

	/**
	 * 
	 */
	private void initRoutes() {
		try {
			context.addRoutes(new RouteBuilder() {
				public void configure() {
					// Add the Basic Routes
					from("Jira:from:msg.queue").to("WFBroker:to:msg.queue");
					// Route from WFBroker to Jira
					from("WFBroker:from:msg.queue").to("Jira:to:msg.queue");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void startConnections() {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				"vm://localhost?broker.persistent=false");
		// Note we can explicit name the component
		context.addComponent("Jira",
				JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		context.addComponent("WFBroker",
				JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

		try {
			context.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------------------------------------
	// Getter & Setters
	// ----------------------------------------------------------------------------------------

	// ----------------------------------------------------------------------------------------
	// Main-method for starting PubFlow
	// ----------------------------------------------------------------------------------------

	public static void main(String[] args) {
		PubFlowCore.getInstance();
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
			Logger shutdownLogger = LoggerFactory.getLogger(this.getClass());
			shutdownLogger.info("<< Shutting down PubFlow >>");
			PubFlowCore core = PubFlowCore.getInstance();
			// Shutdown Camel & free ports
			shutdownLogger.debug("Stopping Camel Context");
			core.stopContext();
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
