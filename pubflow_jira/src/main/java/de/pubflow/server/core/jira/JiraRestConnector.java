package de.pubflow.server.core.jira;

import java.util.HashMap;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import de.pubflow.common.IJiraRestConnector;

@Path("/pubflow/jira/issues")
public class JiraRestConnector implements IJiraRestConnector{

	@POST
	@Path("/{issueKey}/status")
	public Response changeStatus(@PathParam("issueKey") String issueKey, @FormParam("statusName") String statusName){
		JiraEndpoint.changeStatus(issueKey, statusName);
		try{
			return Response.status(204).entity(null).build();
		}catch(Exception e){
			return Response.status(500).entity(null).build();				
		}
	}

	@POST
	@Path("/{issueKey}/attachments")
	public Response addAttachment(@PathParam("issueKey") String issueKey, @FormParam("barray") byte[]barray, @FormParam("fileName") String fileName, @FormParam("type") String type){
		JiraEndpoint.addAttachment(issueKey, barray, fileName, type);
		try{
			return Response.status(204).entity(null).build();	
		}catch(Exception e){
			return Response.status(500).entity(null).build();				
		}
	}

	@POST
	@Path("/{issueKey}/comments")
	public Response addIssueComment(@PathParam("issueKey") String issueKey, @FormParam("comment") String comment){
		JiraEndpoint.addIssueComment(issueKey, comment);
		try{
			return Response.status(204).entity(null).build();	
		}catch(Exception e){
			return Response.status(500).entity(null).build();				
		}
	}

	@POST
	@Path("/")
	public Response createIssue( @FormParam("issueTypeName") String issueTypeName,  @FormParam("summary") String summary,  @FormParam("description") String description, @FormParam("parameters") HashMap<String, String>  parameters,  @FormParam("reporter") String reporter){
		JiraEndpoint.createIssue(issueTypeName, summary, description, parameters, reporter);
		try{
			return Response.status(204).entity(null).build();	
		}catch(Exception e){
			return Response.status(500).entity(null).build();				
		}
	}

}
