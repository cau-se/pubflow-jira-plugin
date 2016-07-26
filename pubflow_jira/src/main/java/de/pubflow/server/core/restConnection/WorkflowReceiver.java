package de.pubflow.server.core.restConnection;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.ws.rs.Consumes;
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
	
	@Path(updateAddress)
	@Consumes("application/json")
	public Response receiveWorkflowUpdate(){
		return Response.ok().build();
	}
	
	/**
	 * 
	 * @return the complete URI for callback of the Workflow engine
	 * @throws UnknownHostException
	 * @throws URISyntaxException
	 */
	public static URI getCallbackAddress() throws UnknownHostException, URISyntaxException{
		return new URI(InetAddress.getLocalHost().getHostAddress().toString()+updateAddress);
	}
}
