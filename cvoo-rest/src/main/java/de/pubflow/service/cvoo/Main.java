package de.pubflow.service.cvoo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;

import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Main class.
 *
 */
public class Main {
	// Base URI the Grizzly HTTP server will listen on
	public static final String BASE_URI = "http://localhost:8080/";

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
	 * @return Grizzly HTTP server.
	 * @throws IOException 
	 */
	public static HttpServer startServer() throws IOException {
		// create a resource config that scans for JAX-RS resources and providers
		// in com.example package
		final ResourceConfig rc = new ResourceConfig().packages("de.pubflow.service.cvoo");

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);
		ErrorPageGenerator epg = new ErrorPageGenerator(){
			@Override
			public String generate(Request request, int status, String
					reasonPhrase,
					String description,
					Throwable exception) {
				StringBuilder sb = new StringBuilder();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				exception.printStackTrace(ps);
				ps.close();
				sb.append(new String(baos.toByteArray()));
				System.out.println(sb.toString());
				return sb.toString();
			}
		};

		server.getServerConfiguration().setDefaultErrorPageGenerator(epg);
		server.start(); 
		return server;
	}

	/**
	 * Main method.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final HttpServer server = startServer();
		System.out.println(String.format("Jersey app started with WADL available at "
				+ "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
		System.in.read();
		server.stop();
	}
}

