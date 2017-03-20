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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.pubflow.server.common.exceptions.WFRestException;
import de.pubflow.server.core.rest.messages.WorkflowRestCall;

/**
 * The WorkflowSender is responsible for the outgoing communication with the
 * Workflow engine.
 * 
 * @author Marc Adolf
 *
 */
public class WorkflowSender {

	/**
	 * Sends a post request to the Workflow engine to use a certain Workflow
	 * specified through the given path.
	 * 
	 * @param wfCall
	 *            The message with all necessary informations to create a new
	 *            Workflow
	 * @param workflowPath
	 *            The path for the specific Workflow to be used.
	 * @throws WFRestException
	 *             if the connection responses with a HTTP response code other
	 *             than 2xx
	 */
	public static void initWorkflow(final WorkflowRestCall wfCall, final String workflowPath) throws WFRestException {
		final Logger myLogger = LoggerFactory.getLogger(WorkflowSender.class);

		myLogger.info("Trying to use workflow on: " + workflowPath);
		final Gson gson = new Gson();
		final HttpClient httpClient = HttpClientBuilder.create().build();
		final HttpPost post = new HttpPost(workflowPath);
		HttpResponse response = null;

		try {
			final StringEntity postingString = new StringEntity(gson.toJson(wfCall),ContentType.APPLICATION_JSON);

			post.setEntity(postingString);
			post.setHeader("Content-type", "application/json;charset=utf-8");
			response = httpClient.execute(post);
			System.out.println(post.getURI());
			myLogger.info("Http response: " + response.toString());

		} catch (final Exception e) {
			myLogger.error("Could not deploy new Workflow with ID: " + wfCall.getID());
			myLogger.error(e.toString());
			throw new WFRestException("Workflow could not be started");
		}
		if (response.getStatusLine().getStatusCode() >= 300) {
			throw new WFRestException(
					"The called WorkflowService send status code: " + response.getStatusLine().getStatusCode()
							+ " and error: " + response.getStatusLine().getReasonPhrase());
		}

	}

}
