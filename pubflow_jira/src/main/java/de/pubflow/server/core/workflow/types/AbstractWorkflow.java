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
package de.pubflow.server.core.workflow.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.jira.accessors.JiraObjectGetter;
import de.pubflow.jira.accessors.JiraObjectManipulator;
import de.pubflow.jira.misc.Appendix;
import de.pubflow.jira.misc.CustomFieldDefinition;
import de.pubflow.server.core.rest.messages.ReceivedWorkflowAnswer;

/**
 * Represents the general Jira Workflow type/scheme. Contains the name, the path
 * to the Jira Workflow XML file and the API for the Rest call to the
 * Webservice.
 * 
 * @author Marc Adolf
 *
 */
abstract public class AbstractWorkflow {
	/**
	 * 
	 */
	protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractWorkflow.class);
	/**
	 * 
	 */
	private final String workflowName;
	/**
	 * 
	 */
	private final String workflowID;
	/**
	 * 
	 */
	private final String jiraWorkflowXMLPath;
	/**
	 * 
	 */
	private String baseWebserviceURL = "http://localhost:8080";
	/**
	 * 
	 */
	private final String webServiceAPI;

	/**
	 * 
	 * @param workflowName
	 * @param workflowID
	 * @param jiraWorkflowXMLPath
	 * @param webServiceAPI
	 * @param baseWebserviceURL
	 */
	public AbstractWorkflow(final String workflowName, final String workflowID, final String jiraWorkflowXMLPath,
			final String webServiceAPI, final String baseWebserviceURL) {
		this(workflowName, workflowID, jiraWorkflowXMLPath, webServiceAPI);
		this.baseWebserviceURL = baseWebserviceURL;

	}

	/**
	 * 
	 * @param workflowName
	 * @param workflowID
	 * @param jiraWorkflowXMLPath
	 * @param webServiceAPI
	 */
	public AbstractWorkflow(final String workflowName, final String workflowID, final String jiraWorkflowXMLPath,
			final String webServiceAPI) {
		this.webServiceAPI = webServiceAPI;
		this.workflowID = workflowID;
		this.workflowName = workflowName;
		this.jiraWorkflowXMLPath = jiraWorkflowXMLPath;
	}

	/**
	 * 
	 * @return
	 */
	public final String getWorkflowName() {
		return workflowName;
	}

	/**
	 * 
	 * @return
	 */
	public final String getJiraWorkflowXMLPath() {
		return jiraWorkflowXMLPath;
	}

	/**
	 * 
	 * @return
	 */
	public final String getBaseWebserviceURL() {
		return baseWebserviceURL;
	}

	/**
	 * 
	 * @return
	 */
	public final String getWebServiceAPI() {
		return webServiceAPI;
	}

	/**
	 * 
	 * @return
	 */
	public final String getCompleteServiceURL() {
		return baseWebserviceURL + webServiceAPI;
	}

	/**
	 * 
	 * @return
	 */
	public final String getWorkflowID() {
		return workflowID;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, String> getScreenNames() {
		final String issueType = this.getWorkflowName();

		final Map<String, String> basicScreens = new HashMap<String, String>();
		basicScreens.put("create", issueType + Appendix.FIELDSCREEN + "ActionCreate");
		basicScreens.put("edit", issueType + Appendix.FIELDSCREEN + "ActionEdit");
		basicScreens.put("view", issueType + Appendix.FIELDSCREEN + "ActionView");

		return basicScreens;
	}

	/**
	 * Defines the Data fields of the Workflow
	 * 
	 * @return
	 */
	abstract public List<CustomFieldDefinition> getCustomFields();

	/**
	 * Process the received answer from the Workflow Microservice. Decides if
	 * the result is an error or a valid result. Uses
	 * {@link #handleWorkflowError(String, ReceivedWorkflowAnswer)} and
	 * {@link #handleWorkflowResults(String, ReceivedWorkflowAnswer)},
	 * respectively.
	 * 
	 * @param jiraKey
	 * @param answer
	 */
	public void handleWorkflowAnswer(final String jiraKey, final ReceivedWorkflowAnswer answer) {

		if (answer.getResult().toLowerCase().contains("error")) {
			LOGGER.error("Workflow with id " + jiraKey + " failed, with message: '" + answer.getErrorMessage() + "'");

			handleWorkflowError(jiraKey, answer);

		} else {
			LOGGER.info("Workflow with id " + jiraKey + " returned valid result: '" + answer.getResult() + "'");

			handleWorkflowResults(jiraKey, answer);
		}

		// maybe the called service wants to add a comment
		if (!answer.getCommentMessage().isEmpty()) {
			JiraObjectManipulator.addIssueComment(jiraKey, answer.getCommentMessage(),
					JiraObjectGetter.getUserByName("PubFlow"));

		}
		// maybe the called service defines which status should be set next
		if (answer.getNewStatus() != null) {
			JiraObjectManipulator.changeStatus(jiraKey, answer.getNewStatus());
		}
	}

	/**
	 * Defines how the result of the Workflow Rest call should be handled.
	 * 
	 * @param jiraKey
	 * @param answer
	 */

	protected void handleWorkflowResults(final String jiraKey, final ReceivedWorkflowAnswer answer) {
		// default is to do nothing, since the current workflows call the
		// JiraRestConnector by themselves
		final String newStatus = answer.getNewStatus();
		if (newStatus != null && !newStatus.isEmpty()) {
			LOGGER.info("Issue ' " + jiraKey + "' try to change status to: " + newStatus);
			JiraObjectManipulator.changeStatus(jiraKey, newStatus);
		}
		final String comment = answer.getResult();
		if (!comment.isEmpty()) {
			JiraObjectManipulator.addIssueComment(jiraKey, comment, null);
		}
	}

	/**
	 * Defines how the errors during the execution of the Workflow Rest call
	 * should be handled.
	 * 
	 * @param jiraKey
	 * @param answer
	 */
	protected void handleWorkflowError(final String jiraKey, final ReceivedWorkflowAnswer answer) {
		JiraObjectManipulator.addIssueComment(jiraKey, "Workflow  failed due to: '" + answer.getErrorMessage() + "'",
				JiraObjectGetter.getUserByName("PubFlow"));
	}

}
