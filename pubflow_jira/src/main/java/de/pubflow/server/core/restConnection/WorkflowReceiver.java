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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.exceptions.WFRestException;
import de.pubflow.server.core.restMessages.WorkflowAnswer;
import de.pubflow.server.core.workflow.WorkflowBroker;

/**
 * 
 * @author Marc Adolf
 *
 */
@Path("/workflow")
public class WorkflowReceiver {
	private static final String updateAddress = "/workflowUpdate";

	// TODO static Url /init at the startup

	@PUT
	@Path(updateAddress)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response receiveWorkflowAnswer(WorkflowAnswer wfAnswer) {
		Logger myLogger = LoggerFactory.getLogger(this.getClass());

		myLogger.info("answer: " + wfAnswer);
		try {
			WorkflowBroker.getInstance().receiveWorkflowAnswer(wfAnswer);
		} catch (WFRestException e) {
			return Response.status(404).entity(null).build();
		}
		return Response.ok().build();
	}

	/**
	 * 
	 * @return the complete URL for callback of the Workflow engine
	 * @throws MalformedURLException
	 * @throws UnknownHostException
	 */
	public static URL getCallbackAddress() throws MalformedURLException, UnknownHostException {
		// TODO set URL at startup
		//TODO set port dynamically (@ startup)
		return new URL("http://" + InetAddress.getLocalHost().getHostAddress().toString()+ ":2990" + updateAddress);
	}
}
