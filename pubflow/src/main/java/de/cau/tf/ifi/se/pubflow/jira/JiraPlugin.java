package de.cau.tf.ifi.se.pubflow.jira;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.ws.Endpoint;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

public class JiraPlugin {

	private static final String KEYSTOREFILE="jira_keystore.ks";
	private static final String KEYSTOREPW="rainbowdash_1";

	public void startHttpsServer(){
		try{
			HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(8889), 0);
			SSLContext sslContext = SSLContext.getInstance("TLS");

			// keystore
			char[] keystorePassword = KEYSTOREPW.toCharArray();
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(KEYSTOREFILE), keystorePassword);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, keystorePassword);

			sslContext.init(kmf.getKeyManagers(), null, null);

			HttpsConfigurator configurator = new HttpsConfigurator(sslContext);
			httpsServer.setHttpsConfigurator(configurator);

			HttpContext httpContext = httpsServer.createContext(JiraToPubFlowConnector.class.getSimpleName());

			Endpoint endpoint = Endpoint.create( new JiraToPubFlowConnector());
			endpoint.publish(httpContext);

		}catch(Exception e){
			System.out.print("Unable to start https server");
		}
	}
	
	static{
		new JiraPlugin().startHttpsServer();
	}
	
//	static{
//
//		Endpoint.publish("http://localhost:8889/" + JiraToPubFlowConnector.class.getSimpleName(), new JiraToPubFlowConnector());
//	}
}
