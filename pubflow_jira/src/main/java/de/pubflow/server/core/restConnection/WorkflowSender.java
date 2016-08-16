/**
 * Copyright (C) 2016 Marc Adolf, Arnd Plumhoff (http://www.pubflow.uni-kiel.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.pubflow.server.core.restConnection;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import de.pubflow.server.common.exceptions.WFRestException;
import de.pubflow.server.core.restMessages.WorkflowCall;

/**
 * The WorkflowSender is responsible for the outgoing communication with the
 * Workflow engine. It is designed as a singleton.
 * 
 * @author Marc Adolf
 *
 */
public class WorkflowSender {
	private static WorkflowSender instance;
	private String targetURL;
	private Logger myLogger;

	private WorkflowSender() throws WFRestException {
		myLogger = LoggerFactory.getLogger(this.getClass());
		// TODO load url from file /config
		targetURL = new String("http://localhost:8080/executeNewWF");
	}

	/**
	 * Accesses the singleton and initializes it if needed
	 * 
	 * @return the one and only {@link WorkflowSender}
	 * @throws WFRestException
	 */
	synchronized public static WorkflowSender getInstance() throws WFRestException {
		if (instance == null) {
			try {
				instance = new WorkflowSender();
			} catch (WFRestException e) {
				// TODO better exception handling
				e.printStackTrace();
				throw e;
			}
		}
		return instance;
	}

	/**
	 * Sends a post request to the Workflow engine to create a new Workflow.
	 * 
	 * @param wfCall
	 *            The message with all necessary informations to create a new
	 *            Workflow
	 * @throws WFRestException
	 *             if the connection responses with a code other than 2xx
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public void initWorkflow(WorkflowCall wfCall) throws WFRestException {
		myLogger.info("Trying to deploy workflow with ID: " + wfCall.getId());
		Gson gson = new Gson();
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(targetURL);
		HttpResponse response = null;

		try {
			StringEntity postingString = new StringEntity(gson.toJson(wfCall));

			post.setEntity(postingString);
			post.setHeader("Content-type", "application/json");
			response = httpClient.execute(post);

			System.out.println("response: " + response);

			myLogger.info("Http response: "+ response.toString());

		} catch (Exception e) {
			myLogger.error("Could not deplay new Workflow with ID: " + wfCall.getId().toString());
			myLogger.error(e.toString());
			throw new WFRestException("Workflow could not be started");
		}

	}

}
