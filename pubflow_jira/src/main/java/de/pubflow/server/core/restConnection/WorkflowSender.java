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

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.web.client.RestClientException;
//import org.springframework.web.client.RestTemplate;

import de.pubflow.server.common.exceptions.WFRestException;
import de.pubflow.server.core.restMessages.WorkflowCall;
import de.pubflow.server.core.restMessages.WorkflowUpdateCall;

/**
 * The WorkflowSender is responsible for the outgoing communication with the
 * Workflow engine. It is designed as a singleton.
 * 
 * @author Marc Adolf
 *
 */
public class WorkflowSender {
	private static WorkflowSender instance;
	private URL targetURL;
	private Logger myLogger;

	private WorkflowSender() throws WFRestException {
		myLogger = LoggerFactory.getLogger(this.getClass());
		// TODO load url from file /config
		try {
			targetURL = new URL("http://localhost:8080/executeNewWF");
		} catch (MalformedURLException e) {
			throw new WFRestException("Could not load target URL");
		}
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
	 */
	public void initWorkflow(WorkflowCall wfCall) throws WFRestException {
//		myLogger.info("Trying to deploy workflow with ID: " + wfCall.getId());
//		RestTemplate restTemplate = new RestTemplate();
//		String response = "Initial REST call response: ";
//		try {
//			response += restTemplate.postForObject(targetURL.toString(), wfCall, String.class);
//		} catch (RestClientException e) {
//			myLogger.error("Could not deplay new Workflow with ID: " + wfCall.getId().toString());
//			myLogger.error(e.toString());
//			throw new WFRestException("Workflow could not be started");
//		} finally {
//			myLogger.info(response);
//		}
	}

	/**
	 * Updates a Workflow, for example if an event occurs.
	 * 
	 * @param wfUpdate
	 *            The description of the update or event
	 * @throws WFRestException
	 *             if the connection responses with a code other than 2xx
	 */
	public void updateWorkflow(WorkflowUpdateCall wfUpdate) throws WFRestException {
//		RestTemplate restTemplate = new RestTemplate();
//		String response = "Update REST call response: ";
//		try {
//			response += restTemplate.postForObject(targetURL.toString(), wfUpdate, String.class);
//		} catch (RestClientException e) {
//			myLogger.error("Could not deplay new Workflow with ID: " + wfUpdate.getId().toString());
//			myLogger.error(e.toString());
//			throw new WFRestException("Workflow could not be started");
//		}
//		myLogger.info(response);
	}

}
