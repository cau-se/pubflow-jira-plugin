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
import javax.ws.rs.core.Response;

/**
 * 
 * @author Marc Adolf
 *
 */
@Path("/workflow")
public class WorkflowReceiver {
	private static final String updateAddress="/workflowUpdate";
	 
	//TODO
	
	@PUT
	@Path(updateAddress)
	@Consumes("application/json")
	public Response receiveWorkflowUpdate(){
		return Response.ok().build();
	}
	
	/**
	 * 
	 * @return the complete URL for callback of the Workflow engine
	 * @throws MalformedURLException 
	 * @throws UnknownHostException
	 */
	public static URL getCallbackAddress() throws MalformedURLException, UnknownHostException {
		return new URL(InetAddress.getLocalHost().getHostAddress().toString()+updateAddress);
	}
}
