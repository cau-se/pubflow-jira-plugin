package de.pubflow.components.jiraConnector;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.ws.Endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import de.pubflow.common.properties.PropLoader;

public class JiraPlugin {

	private static final String KEYSTOREFILE="pubflow_keystore.ks";
	private static final String KEYSTOREPW="rainbowdash_1";

	private static final String PUBFLOWWS_PORT = "8890";

	private static Logger myLogger;
	private static final String START_WF = "";

	static{
		myLogger = LoggerFactory.getLogger(JiraPluginMsgProducer.class);	
	}

	@SuppressWarnings("restriction")
	public static void runHttpsService() throws Exception {

		final KeyStore ks = KeyStore.getInstance("JKS");
		FileInputStream keyStoreIn = new FileInputStream(KEYSTOREFILE);
		try {
			ks.load(keyStoreIn, KEYSTOREPW.toCharArray());
		} finally {
			keyStoreIn.close();
		}

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, KEYSTOREPW.toCharArray());

		TrustManager[] trustManagers = new TrustManager[] {
				new DummyTrustManager()};

		SSLContext sslCtx = SSLContext.getInstance("TLS");
		sslCtx.init(kmf.getKeyManagers(), trustManagers, null);

		HttpsConfigurator cfg = new HttpsConfigurator(sslCtx){
			public void configure(HttpsParameters params) {
				SSLParameters sslparams = getSSLContext().getDefaultSSLParameters();
				// Modify the default params: Will require client certificates
				sslparams.setNeedClientAuth(true);
				sslparams.setWantClientAuth(true);
				params.setSSLParameters(sslparams);
			}
		};

		ExecutorService httpThreadPool = Executors.newFixedThreadPool(10);

		String jiraEndpointPort = PropLoader.getInstance().getProperty("JiraEndpointURL", JiraPlugin.class.toString(), PUBFLOWWS_PORT);

		HttpsServer https = HttpsServer.create(new InetSocketAddress(Integer.parseInt(jiraEndpointPort)), 50);
		https.setHttpsConfigurator(cfg);
		https.setExecutor(httpThreadPool);
		https.start();

		HttpContext ctx = https.createContext("/ws");

		ctx.setAuthenticator(new Authenticator(){
			@Override
			public Result authenticate(HttpExchange exch) {
				try {

					if(exch instanceof HttpsExchange) {
						boolean validated = false;

						HttpsExchange httpsExch = (HttpsExchange)exch;
						System.out.println("authen: " + httpsExch.getSSLSession().getPeerPrincipal().getName());

						System.out.println(exch.getRemoteAddress());
						System.out.println(exch.getLocalAddress());
						System.out.println(((HttpsExchange) exch).getSSLSession().getCipherSuite());
						System.out.println(exch.getProtocol());

						System.out.println("===================================== LOCAL CERTS ===================================== " + ((HttpsExchange) exch).getSSLSession().getLocalCertificates().length);
						for(Certificate c : ((HttpsExchange) exch).getSSLSession().getLocalCertificates()){
							System.out.println(c);
						}

						System.out.println("=====================================  KEYSTORE  ====================================== " + ((HttpsExchange) exch).getSSLSession().getPeerCertificates().length);
						try {
							System.out.println(ks.getCertificate("mykey"));
						} catch (KeyStoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						System.out.println("===================================== PEER CERTS ====================================== " + ((HttpsExchange) exch).getSSLSession().getPeerCertificates().length);

						for(Certificate c : ((HttpsExchange) exch).getSSLSession().getPeerCertificates()){
							System.out.println(c);

							try {
								if (ks.getCertificate("mykey").equals(c)){
									validated=true;
								}

							} catch (KeyStoreException e) {
								throw new SSLPeerUnverifiedException("Some probs with the keystore?");
							}
						}

						httpsExch.getSSLSession().putValue("MY_PARAM_PEER_NAME", httpsExch.getSSLSession().getPeerPrincipal().getName());

						if(validated){
							return new Authenticator.Success(exch.getPrincipal());
						}else{
							throw new SSLPeerUnverifiedException("Peer not authorized");

						}
					}
				} catch (SSLPeerUnverifiedException e) {
					e.printStackTrace();
				}
				return new Authenticator.Failure(403);
			}
		});

		Endpoint endpoint = Endpoint.create(new JiraToPubFlowConnector());
		endpoint.publish(ctx);

		///publish also on HTTP server, so we can access
		//wsdl through HTTP, just for our example.
		HttpServer http = HttpServer.create(new InetSocketAddress(8082), 50);
		http.start();
		Endpoint endpoint2 = Endpoint.create(new JiraToPubFlowConnector());
		endpoint2.publish(http.createContext("/ws"));
	}

	public static class DummyTrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0]; 
		}

		public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
			System.out.println("===================================== checkClientTrusted ====================================== " + certs.length);

			for(Certificate c : certs){
				System.out.println(c);
			}			

		}
		public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {			
			System.out.println("===================================== checkServerTrusted ====================================== " + certs.length);

			for(Certificate c : certs){
				System.out.println(c);
			}		}
	}


	static{
		//		try {
		//			runHttpsService();
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		String jiraEndpointURL = PropLoader.getInstance().getProperty("JiraEndpointURL", JiraPlugin.class.toString(), PUBFLOWWS_PORT);
		Endpoint.publish("http://localhost:"+jiraEndpointURL + "/" + JiraToPubFlowConnector.class.getSimpleName(), new JiraToPubFlowConnector());
	}
}
