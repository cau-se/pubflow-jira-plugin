package de.pubflow.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.properties.PropLoader;
import de.pubflow.server.core.jira.JiraConnector;

public class PubFlowSystem {

	private static PubFlowSystem instance = null;
	private Logger myLogger;
	public final String pubflowHome = System.getProperty("pubflow_home", "/home/arl/pubflow_home/");
 
	
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
		myLogger.info("Starting JiraPlugin Endpoint");
		
		try {
			JiraConnector.getInstance().start();
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
	

	class ShutdownActions implements Runnable {
		// Register all shutdown actions here
		public void run() {
			Thread.currentThread().setName("PubFlow Shutdownhook");
			Logger shutdownLogger = LoggerFactory.getLogger(this.getClass());
			shutdownLogger.info("<< Shutting down PubFlow >>");
			PubFlowSystem core = PubFlowSystem.getInstance();
			// Shutdown Camel & free ports	
			shutdownLogger.info("Stopping Quartz");
			
			// Write props to file
			shutdownLogger.debug("Saving Properties to file");
			PropLoader.getInstance().saveProperties();
			shutdownLogger.info("<< BYE >>");
		}

	}
}
