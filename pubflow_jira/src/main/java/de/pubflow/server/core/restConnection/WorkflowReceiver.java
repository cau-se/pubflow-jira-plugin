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
