package de.pubflow.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

	private Logger logger;
	private static org.eclipse.jetty.server.Server server;
	private String jettyHome;

	public Server(){
		logger = LoggerFactory.getLogger(Server.class);
		logger.debug("Entering application.");
	}

	// TODO load the webapps from the database ...
	public void startup() throws Exception{

		server = new org.eclipse.jetty.server.Server();

		// Define the Jetty home directory
//		String[] configFiles = {"etc/jetty.xml"};  
//
//		for(String configFile : configFiles) {  
//			XmlConfiguration configuration = new XmlConfiguration(new File(configFile).toURI().toURL());  
//			configuration.configure(server);  
//		}

		jettyHome = System.getProperty("jetty.home",".");

//		logger.info("Initializing ODE");
//		// Init the ODE WebApp
//		WebAppContext l_ODEAppContainer = new WebAppContext();
//		l_ODEAppContainer.setContextPath("/ode");
//		l_ODEAppContainer.setWar(jettyHome + "/webapps/ode.war");
//		logger.info("done!");
//		
//		logger.info("Initializing Ocn-WS");
//		// Init the ocn WebApp
//		WebAppContext l_ocnContainer = new WebAppContext();
//		l_ocnContainer.setContextPath("/genericWS");
//		l_ocnContainer.setWar(jettyHome + "/webapps/genericWS.war");
//		logger.info("done!");
		
//		logger.info("Initializing ArduinoTherm-WS");
//		// Init the arduinoTherm WebApp
//		WebAppContext l_thermContainer = new WebAppContext();
//		l_thermContainer.setContextPath("/arduinoTherm");
//		l_thermContainer.setWar(jettyHome + "/webapps/arduinoTherm.war");
//		logger.info("done!");

//		logger.info("Initializing Controller");
//		// Init the Controller WebApp
//		WebAppContext l_Controller_webapp = new WebAppContext();
//		l_Controller_webapp.setContextPath("/controller");
//		l_Controller_webapp.setWar(jettyHome + "/webapps/Controller-1.0-SNAPSHOT.war");
//		logger.info("done!");

		logger.info("Setting Webapps");
		HandlerList contexts = new HandlerList();  
		contexts.setHandlers(new Handler[] {  new HelloHandler()/*, l_thermContainer*/});
		server.setHandler(new HelloHandler());
		logger.info("done!");

		// start the server
		try {		
			logger.info("Starting Jetty");
			server.start();
			//	i_server.join();
			logger.info("done!");

		} catch (Exception e) {
			logger.info("Unable to start Server!");
		}
	}
	
	public void stop()
	{
		logger.info("Trying to stop server!");
		try {
			server.stop();
		} catch (Exception e) {
			logger.error("Unable to stop Server!");
			e.printStackTrace();
		}
		logger.info("Server is stopped");
	}
	
	class HelloHandler extends AbstractHandler
	{
	    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	        throws IOException, ServletException
	    {
	        response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        response.getWriter().println("<h1>Hello World</h1>");
	    }

	}
}
