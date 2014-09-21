package de.pubflow;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.policy.VMPendingQueueMessageStoragePolicy;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.common.properties.PropLoader;
import de.pubflow.components.jiraConnector.JiraPlugin;
import de.pubflow.components.jiraConnector.JiraPluginMsgConsumer;
import de.pubflow.core.workflow.WFBroker;

/**
 * 
 * 
 */
public class PubFlowSystem {

	private DefaultCamelContext context;
	private static PubFlowSystem instance = null;
	private Logger myLogger;

	private PubFlowSystem() {

		String art = " _____       _     ______ _" + "\n"
				+ "|  __ \\     | |   |  ____| |" + "\n"
				+ "| |__) |   _| |__ | |__  | | _____      __" + "\n"
				+ "|  ___/ | | | '_ \\|  __| | |/ _ \\ \\ /\\ / /" + "\n"
				+ "| |   | |_| | |_) | |    | | (_) \\ V  V /" + "\n"
				+ "|_|    \\__,_|_.__/|_|    |_|\\___/ \\_/\\_/ " + "\n"
				+ "__________________________________________" + "\n"
				+ "Version lite \n";
		System.out.println(art);

		myLogger = LoggerFactory.getLogger(this.getClass());
		myLogger.info("Starting PubFlow System");
		context = new DefaultCamelContext();

		try {
			BrokerService broker = new BrokerService();
			broker.addConnector("vm://localhost");

			broker.setPersistent(false);
			broker.getSystemUsage().getMemoryUsage().setLimit(2000000000);

			PolicyMap policyMap = new PolicyMap();
			PolicyEntry policyEntry = new PolicyEntry();
			policyEntry.setQueue(">");
			policyEntry.setProducerFlowControl(false);
			policyEntry.setPendingQueuePolicy(new VMPendingQueueMessageStoragePolicy());
			policyEntry.setGcInactiveDestinations(true);
			policyEntry.setInactiveTimoutBeforeGC(200000);
			policyEntry.setQueuePrefetch(1);
			policyMap.put(new ActiveMQQueue(">"), policyEntry);
			broker.setDestinationPolicy(policyMap);
			broker.start();

		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				"vm://localhost");

		// Add the queues to the DefaultContext
		context.addComponent("test-jms",
				JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		context.addComponent("t2-jms",
				JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		context.addComponent("mail-jms",
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
		
		myLogger.info("Starting JiraPlugin Endpoint");
		try {
			JiraPlugin.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Register shutdownhook
		Thread t = new Thread(new ShutdownActions());
		Runtime.getRuntime().addShutdownHook(t);
		myLogger.info("Running");
	}

	public static synchronized PubFlowSystem getInstance() {

		if (instance == null) {
			instance = new PubFlowSystem();
		}
		return instance;
	}

	// ----------------------------------------------------------------------------------------
	// Public methods
	// ----------------------------------------------------------------------------------------

	public void stopCamel() {
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
				//ws ( -> quartz) -> jira
				from("t2-jms:jiraendpoint:out.queue").to(
						"t2-jms:jira:toJira.queue")
						.bean(JiraPluginMsgConsumer.getInstance());

				//quartz/ws-mapper -> jbpm
				from("test-jms:queue:testOut.queue").to(
						"test-jms:wfbroker:in.queue").bean(
								WFBroker.getInstance());
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
		//return null;
	}

	public void setContext(DefaultCamelContext context) {
		this.context = context;
	}

	// ----------------------------------------------------------------------------------------
	// Main-method for starting PubFlow
	// ----------------------------------------------------------------------------------------

	public static void main(String[] args) {

		PubFlowSystem c = PubFlowSystem.getInstance();
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
			PubFlowSystem core = PubFlowSystem.getInstance();
			// Shutdown Camel & free ports
			shutdownLogger.debug("Stopping Camel Context");
			core.stopCamel();
			shutdownLogger.debug("Camel Context is down");
			// Stop internal server
			shutdownLogger.info("Stopping Quartz");

			// Write props to file
			shutdownLogger.debug("Saving Properties to file");
			PropLoader.getInstance().saveProperties();
			shutdownLogger.info("<< BYE >>");
		}

	}
}
