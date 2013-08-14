package de.pubflow.components.jiraConnector;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.ws.Endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import de.pubflow.common.properties.PropLoader;

public class JiraPlugin {

	private static final String KEYSTOREFILE="jira_keystore.ks";
	private static final String KEYSTOREPW="rainbowdash_1";

	private static Logger myLogger;
	private static final String START_WF = "";

	static{
		myLogger = LoggerFactory.getLogger(JiraPluginMsgProducer.class);
		System.getProperties().put("javax.net.ssl.keyStore", KEYSTOREFILE);
		System.getProperties().put("javax.net.ssl.keyStorePassword", KEYSTOREPW);
	}

	public void startHttpsServer(){
		String jiraEndpointURL = PropLoader.getInstance().getProperty("JiraEndpointURL", this.getClass().toString(), "http://localhost:" );
		String jiraEndpointPort = PropLoader.getInstance().getProperty("JiraEndpointPort", "class de.pubflow.components.jiraConnector.JiraPlugin", calleeSig, defaultValue);

		String jiraAdress = jiraEndpointURL + jiraEndpointPort + ("/");
		myLogger.info("Jira Adress: "+jiraAdress);


		try{
			HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(Integer.parseInt(PropLoader.getInstance().getProperty("JiraEndpointPort", this.getClass().toString(),"8890"))), 0);
			SSLContext sslContext = SSLContext.getInstance("TLS");

			// keystore
			char[] keystorePassword = KEYSTOREPW.toCharArray();
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(KEYSTOREFILE), keystorePassword);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, keystorePassword);

			// truststore
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			char[] truststorePassword = KEYSTOREPW.toCharArray();
			ks.load(new FileInputStream(KEYSTOREFILE), truststorePassword);
			tmf.init(ks);

			sslContext.init(kmf.getKeyManagers(), null, null);

			HttpsConfigurator configurator =  new HttpsConfigurator(sslContext) {
				/* (non-Javadoc)
				 * @see com.sun.net.httpserver.HttpsConfigurator#configure(com.sun.net.httpserver.HttpsParameters)
				 */
				@Override
				public void configure(HttpsParameters params) {
					SSLParameters sslParams = getSSLContext().getDefaultSSLParameters();
					sslParams.setNeedClientAuth(true);
					params.setSSLParameters(sslParams);
				}
			};

			httpsServer.setHttpsConfigurator(configurator);

			HttpContext httpContext = httpsServer.createContext(JiraToPubFlowConnector.class.getSimpleName());

			Endpoint endpoint = Endpoint.create( new JiraToPubFlowConnector());
			endpoint.publish(httpContext);

		}catch(Exception e){
			System.out.print("Unable to start https server");
		}
	}



	//	static{
	//
	//		Endpoint.publish("http://localhost:8889/" + JiraToPubFlowConnector.class.getSimpleName(), new JiraToPubFlowConnector());
	//	}
}
