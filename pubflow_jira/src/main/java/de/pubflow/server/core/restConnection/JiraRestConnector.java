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
import java.net.UnknownHostException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import de.pubflow.common.entity.JiraAttachment;
import de.pubflow.common.entity.JiraIssue;
import de.pubflow.server.core.jira.JiraEndpoint;
import de.pubflow.server.core.rest.messages.ReceivedWorkflowAnswer;
import de.pubflow.server.core.workflow.WorkflowBroker;

@Path(JiraRestConnector.basePath)
public class JiraRestConnector  {


	private static final String jiraRestPath = "/rest/receiver/1.0";
	static final String basePath = "/pubflow/issues";
	private static final String answerPath = "/{issueKey}/result";
//	private static final String port =":63922";
	private static final String port =":80";


	@POST
	@AnonymousAllowed
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{issueKey}/status")
	public Response changeStatus(@PathParam("issueKey") String issueKey, String statusName) {
		JiraEndpoint.changeStatus(issueKey, statusName.substring(1, statusName.length() - 1));
		try {
			return Response.status(204).entity("").build();
		} catch (Exception e) {
			return Response.status(500).entity("").build();
		}
	}

	@POST
	@AnonymousAllowed
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{issueKey}/attachments")
	public Response addAttachment(@PathParam("issueKey") String issueKey, JiraAttachment attachment) {
		JiraEndpoint.addAttachment(issueKey, attachment.getData(), attachment.getFilename(),
				attachment.getType());
		try {
			return Response.status(204).entity("").build();
		} catch (Exception e) {
			return Response.status(500).entity("").build();
		}
	}

	@POST
	@AnonymousAllowed
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{issueKey}/comments")
	public Response addIssueComment(@PathParam("issueKey") String issueKey, String comment) {
		JiraEndpoint.addIssueComment(issueKey, comment);
		try {
			return Response.status(204).entity("").build();
		} catch (Exception e) {
			return Response.status(500).entity("").build();
		}
	}

	@POST
	@AnonymousAllowed
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/")
	public Response createIssue(JiraIssue issue) {
		String newIssueKey = JiraEndpoint.createIssue(issue.getIssueTypeName(), issue.getSummary(),
				issue.getDescription(), issue.getParameters(), issue.getReporter());
		if (newIssueKey != null) {
			return Response.status(201).entity(newIssueKey).build();
		} else {
			return Response.status(400).entity("").build();

		}
	}

	@POST
	@AnonymousAllowed
	@Consumes(MediaType.APPLICATION_JSON)
	@Path(answerPath)
	public Response receiveWorkflowAnswer(@PathParam("issueKey") String issueKey, ReceivedWorkflowAnswer wfAnswer) {
		WorkflowBroker.getInstance().receiveWorkflowAnswer(issueKey, wfAnswer);
		return Response.ok().build();
	}

	/**
	 * 
	 * @return the complete URL for callback for generic answers of the Workflow
	 *         Engine
	 * @throws MalformedURLException
	 * @throws UnknownHostException
	 */
	public static String getCallbackAddress() throws UnknownHostException {
		// TODO is this the right place for this?

		// TODO set port dynamically (@ startup)
		
		return "http://" + InetAddress.getLocalHost().getHostAddress().toString() + port + jiraRestPath + basePath
				+ answerPath;
	}

}
