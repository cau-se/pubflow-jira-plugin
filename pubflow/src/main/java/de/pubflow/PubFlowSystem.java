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
import de.pubflow.core.server.AppServer;
import de.pubflow.core.workflow.WFBroker;

/**
 * 
 * 
 */
public class PubFlowSystem {

	private DefaultCamelContext context;
	private static PubFlowSystem instance = null;
	private Logger myLogger;
	private AppServer server;

	private static final String SERVERFLAG = "CONFSERVER";
	private static final String ACTIVEFLAG = "ON";

	private static final String WFBROKER_NAME = "WFBROKER";
	private static final String MOCKUPENDPOINT_NAME = "MOCKUP";

	/** DEFAULT PROPERTIES **/
	private static final String DEFAULT_ACTIVEFLAG = "ON";

	// ----------------------------------------------------------------------------------------
	// Initializers
	// ----------------------------------------------------------------------------------------

	// ----------------------------------------------------------------------------------------
	//
	// ----------------------------------------------------------------------------------------

	private PubFlowSystem() {

		String art = " _____       _     ______ _" + "\n"
				+ "|  __ \\     | |   |  ____| |" + "\n"
				+ "| |__) |   _| |__ | |__  | | _____      __" + "\n"
				+ "|  ___/ | | | '_ \\|  __| | |/ _ \\ \\ /\\ / /" + "\n"
				+ "| |   | |_| | |_) | |    | | (_) \\ V  V /" + "\n"
				+ "|_|    \\__,_|_.__/|_|    |_|\\___/ \\_/\\_/ " + "\n"
				+ "__________________________________________" + "\n"
				+ "Version 2.0 \n";
		System.out.println(art);

		myLogger = LoggerFactory.getLogger(this.getClass());
		myLogger.info("Starting PubFlow System");
		//
		// Create CamelContext
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
		// Start the Confserver
		myLogger.info("Starting Configuration GUI");

		String sflag = PropLoader.getInstance().getProperty(SERVERFLAG,
				this.getClass().toString(), DEFAULT_ACTIVEFLAG);
		if (sflag.equalsIgnoreCase(ACTIVEFLAG)) {
			myLogger.info("Configuration GUI is active");
			// TODO start a conf GUI
		} else {
			myLogger.warn("Configuration GUI is disabled");
			// e.printStackTrace();
		}
		// start the internal Server
		myLogger.info("Starting the internal PubFlow Server");
		//server = new AppServer();
		try {
			server.startup();
			myLogger.info("PubFlow Server up and running");
		} catch (Exception e) {
			myLogger.error("Failed to start the internal PubFlow Server");
			e.printStackTrace();
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
		//return null;
	}

	// ----------------------------------------------------------------------------------------
	// Public methods
	// ----------------------------------------------------------------------------------------

	public void stopInternalServer() {
		server.stop();
	}

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
				//müll?
				//from("t2-jms:queue:test.queue").to(
				//		"test-jms:queue:out.queue")
				//		.bean(Consumer.getInstance());

				//ws ( -> quartz) -> jira
				from("t2-jms:jiraendpoint:out.queue").to(
						"t2-jms:jira:toJira.queue")
						.bean(JiraPluginMsgConsumer.getInstance());

				//quartz/ws-mapper -> jbpm
				from("test-jms:queue:testOut.queue").to(
						"test-jms:wfbroker:in.queue").bean(
								WFBroker.getInstance());

				//müll?
				//from("mail-jms:mailqueue:in.queue").to(
				//		"mail-jms:mailproxy:in.queue").bean(
				//		MailProxy.getInstance());
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
		//System.out.println(">>>>>>>>>>>>>> Testing <<<<<<<<<<<<<<<<");
		// Do some tests
		//ProducerTemplate template = c.getContext().createProducerTemplate();
		//for (int i = 0; i < 1; i++) {
		// System.out.println("sending test textmsg");
		// TextMessage text = new TextMessage();
		// text.setContent("Test");
		// template.sendBody("t2-jms:queue:test.queue",
		// MessageToolbox.transformToString(text));

		//			System.out.println("sending test textmsg");
		//
		//			System.out.println("Composing test wfmsg");
		//			WFParameter param0 = new WFParameter();
		//			param0.setKey("issueKey");
		//			param0.setStringValue("3");
		//			param0.setPayloadClazz(ParameterType.STRING);
		//			WFParameter param1 = new WFParameter();
		//			param1.setKey("legID");
		//			param1.setIntValue(3);
		//			param1.setPayloadClazz(ParameterType.INTEGER);
		//			WFParamList params = new WFParamList();
		//			params.add(param0);
		//			params.add(param1);
		//			WorkflowMessage wm = new WorkflowMessage();
		//			wm.setWfparams(params);
		//			wm.setWorkflowID(WorkflowProvider.getInstance().getIDByWFName(
		//					"de.pubflow.OCN"));
		//			wm.setWftype(WFType.BPMN2);
		//			wm.setComments("It's alive!");
		//			//
		//			// Sending WFMsg
		//			System.out.println(MessageToolbox.transformToString(wm));
		//			ProducerTemplate producer;
		//			
		//			CamelContext context = PubFlowSystem.getInstance().getContext();
		//			producer = context.createProducerTemplate();
		//			
		//			producer.sendBody("test-jms:queue:testOut.queue",
		//					MessageToolbox.transformToString(wm));
		//			System.out.println("DONE!");

		//		}

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
			shutdownLogger.debug("Stopping internal server");
			core.stopInternalServer();
			shutdownLogger.info("Stopping Quartz");

			// Write props to file
			shutdownLogger.debug("Saving Properties to file");
			PropLoader.getInstance().saveProperties();
			shutdownLogger.info("<< BYE >>");
		}

	}
}
