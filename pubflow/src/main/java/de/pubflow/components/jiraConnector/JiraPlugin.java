package de.pubflow.components.jiraConnector;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.ws.Endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import de.pubflow.common.properties.PropLoader;

public class JiraPlugin {
	private static final String KEYSTOREFILE="keystore_pubflow.ks";
	private static final String TRUSTSTOREFILE="truststore_pubflow.ks";
	private static final String KEYSTOREPW="changeit";
	private static final String TRUSTSTOREPW="changeit";
	private static final String PUBFLOWWS_PORT = "8889";
	private static Logger myLogger = LoggerFactory.getLogger(JiraPlugin.class.getSimpleName());

	@SuppressWarnings("restriction")
	public static void start() throws Exception {
		System.setProperty("https.cipherSuites","TLS_DHE_RSA_WITH_AES_128_CBC_SHA");
		//System.setProperty("javax.net.debug", "ssl,handshake,record"); 

		final KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(JiraPlugin.class.getClassLoader().getResource(KEYSTOREFILE).getFile()), KEYSTOREPW.toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, KEYSTOREPW.toCharArray());

		final KeyStore ts = KeyStore.getInstance("JKS");
		ts.load(new FileInputStream(JiraPlugin.class.getClassLoader().getResource(TRUSTSTOREFILE).getFile()), TRUSTSTOREPW.toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		tmf.init(ts);

		SSLContext sslCtx = SSLContext.getInstance("SSLv3");
		sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);		

		HttpsConfigurator cfg = new HttpsConfigurator(sslCtx){
			public void configure(HttpsParameters params) {
				SSLParameters sslparams = getSSLContext().getDefaultSSLParameters();				
				// Modify the default params: Will require client certificates
				sslparams.setNeedClientAuth(true);
				sslparams.setWantClientAuth(true);
				sslparams.setProtocols(new String[]{"SSLv3"});
				sslparams.setCipherSuites(new String[]{"TLS_DHE_RSA_WITH_AES_128_CBC_SHA"});
				params.setSSLParameters(sslparams);
				params.setProtocols(new String[]{"SSLv3"});
				params.setCipherSuites(new String[]{"TLS_DHE_RSA_WITH_AES_128_CBC_SHA"});
			}
		};

		ExecutorService httpThreadPool = Executors.newFixedThreadPool(100);
		String pubFlowWSPort = PropLoader.getInstance().getProperty("PubFlowWSPort", JiraPlugin.class.toString(), PUBFLOWWS_PORT);

		HttpsServer https = HttpsServer.create(new InetSocketAddress(Integer.parseInt(pubFlowWSPort)), 50);
		https.setHttpsConfigurator(cfg);
		https.setExecutor(httpThreadPool);
		https.start();

		HttpContext ctx = https.createContext("/JiraToPubFlowConnector");

		ctx.setAuthenticator(new Authenticator(){

			@Override
			public Result authenticate(HttpExchange exch) {
				try {
					
					if(exch instanceof HttpsExchange) {
						boolean authenticated = false;

						HttpsExchange httpsExch = (HttpsExchange)exch;
						
						/*
						myLogger.info("authen: " + httpsExch.getSSLSession().getPeerPrincipal().getName());
						myLogger.info(exch.getRemoteAddress().toString());
						myLogger.info(exch.getLocalAddress().toString());
						myLogger.info(((HttpsExchange) exch).getSSLSession().getCipherSuite());
						myLogger.info(exch.getProtocol());

						myLogger.info("===================================== LOCAL CERTS ===================================== " + ((HttpsExchange) exch).getSSLSession().getLocalCertificates().length);


						for(Certificate c : ((HttpsExchange) exch).getSSLSession().getLocalCertificates()){
							myLogger.info(c.toString());
						}

						myLogger.info("=====================================  KEYSTORE  ====================================== " + ((HttpsExchange) exch).getSSLSession().getPeerCertificates().length);
						try {
							myLogger.info(ts.getCertificate("client").toString());
						} catch (KeyStoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						myLogger.info("===================================== PEER CERTS ====================================== " + ((HttpsExchange) exch).getSSLSession().getPeerCertificates().length);
						 */
						
						for(Certificate c : ((HttpsExchange) exch).getSSLSession().getPeerCertificates()){
							//myLogger.info(c.toString());

							try {
								if (ts.getCertificate("client").equals(c)){
									authenticated=true;
								}

							} catch (KeyStoreException e) {
								throw new SSLPeerUnverifiedException("Some probs with the keystore?");
							}
						}

						httpsExch.getSSLSession().putValue("MY_PARAM_PEER_NAME", httpsExch.getSSLSession().getPeerPrincipal().getName());

						if(authenticated){
							return new Authenticator.Success(exch.getPrincipal());
						}else{
							throw new SSLPeerUnverifiedException("Peer not authorized");

						}
					}

				} catch (Exception e) {
					e.printStackTrace();

				}
				
				return new Authenticator.Failure(403);
			}
		});

		Endpoint endpoint = Endpoint.create(new JiraToPubFlowConnector());
		endpoint.publish(ctx);
	}
}
