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

import javax.ws.rs.Path;

/**
 * 
 * @author Marc Adolf
 *
 */
@Path("/workflow")
public class WorkflowReceiver {

	// TODO receive and process generic answers from the workflows

	// private static final String updateAddress = "/workflowUpdate";
	// @PUT
	// @AnonymousAllowed
	// @Path(updateAddress)
	// @Produces(MediaType.APPLICATION_JSON)
	// @Consumes(MediaType.APPLICATION_JSON)
	// public Response receiveWorkflowAnswer() {
	//
	// return Response.ok().build();
	// }

	/**
	 * 
	 * @return the complete URL for callback of the Workflow engine
	 * @throws MalformedURLException
	 * @throws UnknownHostException
	 */
	public static URL getCallbackAddress() throws MalformedURLException, UnknownHostException {
		// TODO set URL at startup
		// TODO set port dynamically (@ startup)
		return new URL("http://" + InetAddress.getLocalHost().getHostAddress().toString() + ":2990");
	}
}
