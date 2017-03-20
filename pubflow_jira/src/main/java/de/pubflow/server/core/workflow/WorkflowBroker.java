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
package de.pubflow.server.core.workflow;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.jira.accessors.JiraObjectGetter;
import de.pubflow.server.common.entity.workflow.ParameterType;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.exceptions.WFException;
import de.pubflow.server.common.exceptions.WFRestException;
import de.pubflow.server.core.rest.messages.ReceivedWorkflowAnswer;
import de.pubflow.server.core.rest.messages.ServiceCallData;
import de.pubflow.server.core.rest.messages.WorkflowRestCall;
import de.pubflow.server.core.restConnection.JiraRestConnector;
import de.pubflow.server.core.restConnection.WorkflowSender;
import de.pubflow.server.core.workflow.types.AbstractWorkflow;

/**
 * Handles all Workflow execution. The initialization and updates are covered by
 * this class as well as the mapping of answers after a Workflow instance has
 * finished.
 * 
 * @author Marc Adolf, Peer Brauer
 *
 */
public final class WorkflowBroker {
	/**
	 * 
	 */
	private final static Logger LOGGER = LoggerFactory.getLogger(WorkflowBroker.class);
	
	/**
	 * 
	 */
	private static final Map<String, AbstractWorkflow> REGISTEREDWORKFLOWS = new HashMap<String, AbstractWorkflow>();

	private WorkflowBroker() {
		LOGGER.info("Starting WorkflowBroker");
	}

	/**
	 * 
	 * @return
	 */
	static public WorkflowBroker getInstance() {
		return new WorkflowBroker();
	}

	/**
	 * Handles new Workflow calls for PubFlow. They will be saved and send to
	 * the Workflow Microservice. Works only if a Workflow service should be
	 * called.
	 * 
	 * @param callData
	 * @throws WFException
	 */
	public void receiveWFCall(final ServiceCallData callData) throws WFException {

		// TODO Save Workflows in DB and load from it on startup

		if (!callData.isValid()) {
			LOGGER.error("Workflow NOT deployed >> Msg is not valid ");
			return;
		}
		LOGGER.info("Creating new Instance of the '" + callData.getWorkflowID() + "' Workflow");
		final WorkflowRestCall wfRestCall = new WorkflowRestCall();

		wfRestCall.setID(callData.getJiraKey());

		// add Callback address to the REST call
		try {
			wfRestCall.setCallbackAddress(JiraRestConnector.getCallbackAddress());
		} catch (final UnknownHostException exception) {
			LOGGER.error("Could not set callback address for the REST call");
			throw new WFException("  Could not set callback address");
		}

		LOGGER.info("Deploying new Workflow");

		// set parameters for the REST call
		wfRestCall.setWorkflowParameters(computeParameter(callData));

		// lookup the URL
		final AbstractWorkflow workflow = REGISTEREDWORKFLOWS.get(callData.getWorkflowID());
		if (workflow == null) {
			LOGGER.error(callData.getWorkflowID() + " was not found by the WorkflowBroker");
			throw new WFException("Workflow not found/registered");
		}
		final String workflowURL = workflow.getCompleteServiceURL();
		LOGGER.info("Using REST-API: " + workflowURL);
		try {
			WorkflowSender.initWorkflow(wfRestCall, workflowURL);
			LOGGER.info("Workflow deployed");
		} catch (final WFRestException e) {
			LOGGER.error("Could not deploy workflow");
			throw e;
		}
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	private List<WFParameter> computeParameter(final ServiceCallData data) {

		final List<WFParameter> parameters = data.getParameters();
		final List<WFParameter> filteredParameters = new LinkedList<WFParameter>();

		for (final WFParameter parameter : parameters) {
			LOGGER.info(parameter.getKey() + " : " + parameter.getValue());
			final String key = parameter.getKey();

			if (parameter.getPayloadClazz().equals(ParameterType.STRING)) {

				switch (key) {
				case "quartzCron":
					break;

				case "status":
					break;

				case "assignee":
					break;

				case "eventType":
					break;

				case "date":
					break;

				case "issueKey":
					filteredParameters.add(parameter);
					break;

				case "reporter":
					break;

				case "workflowName":
					break;

				default:
					try {
						parameter.setKey(key);
						filteredParameters.add(parameter);
						// }
					} catch (final Exception exception) {
						LOGGER.error(exception.getCause().toString() + " : " + exception.getMessage());
					}

				}
			}
		}

		return filteredParameters;
	}

	/**
	 * Maps the answers from the Workflow Microservice to a
	 * {@link AbstractWorkflow Workflow} and delegates the handling of the
	 * answer to this object.
	 * 
	 * @throws WFRestException
	 */
	public void receiveWorkflowAnswer(final String jiraKey, final ReceivedWorkflowAnswer answer) {
		LOGGER.debug("Receveived answer to issue " + jiraKey + ".");

		if (JiraObjectGetter.getIssueByJiraKey(jiraKey) == null) {
			LOGGER.info("Got answer to non-existing issue  with key: " + jiraKey + " with message: ");
			LOGGER.info(answer.toString());
			return;
		}

		final String workflowName = JiraObjectGetter.getIssueTypeNamebyJiraKey(jiraKey);

		final AbstractWorkflow workflow = REGISTEREDWORKFLOWS.get(workflowName);

		if (workflow == null) {
			LOGGER.info("Got answer to issue with key " + jiraKey + " but the issue type is not registered.");
			return;
		}

		workflow.handleWorkflowAnswer(jiraKey, answer);
	}

	/**
	 * Saves the Workflow in a Map. The entry is used to lookup the Workflow
	 * during execution of PubFlow.
	 * 
	 * @param workflow
	 */
	static public void addWorkflow(final AbstractWorkflow workflow) {
		REGISTEREDWORKFLOWS.put(workflow.getWorkflowName(), workflow);
		LOGGER.info("Registered Workflow: " + workflow.getWorkflowName() + " at the WorkflowBroker");
	}
}
