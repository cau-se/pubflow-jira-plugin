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
public class WorkflowBroker {
	static private Logger myLogger;

	static private final WorkflowBroker instance = new WorkflowBroker();
	static private Map<String, AbstractWorkflow> registeredWorkflows = new HashMap<String, AbstractWorkflow>();

	private WorkflowBroker() {
		myLogger = LoggerFactory.getLogger(this.getClass());
		myLogger.info("Starting WorkflowBroker");
	}

	static public WorkflowBroker getInstance() {
		return instance;
	}

	/**
	 * Handles new Workflow calls for PubFlow. They will be saved and send to
	 * the Workflow Microservice.
	 * Works only if a Workflow service should be called. 
	 * 
	 * @param callData
	 * @throws WFException
	 */
	public void receiveWFCall(ServiceCallData callData) throws WFException {

		// TODO Save Workflows in DB and load from it on startup

		if (!callData.isValid()) {
			myLogger.error("Workflow NOT deployed >> Msg is not valid ");
			return;
		}
		myLogger.info("Creating new Instance of the '" + callData.getWorkflowID() + "' Workflow");
		WorkflowRestCall wfRestCall = new WorkflowRestCall();

		wfRestCall.setID(callData.getJiraKey());

		// add Callback address to the REST call
		try {
			wfRestCall.setCallbackAddress(JiraRestConnector.getCallbackAddress());
		} catch (UnknownHostException e) {
			myLogger.error("Could not set callback address for the REST call");
			throw new WFException("  Could not set callback address");
		}

		myLogger.info("Deploying new Workflow");

		// set parameters for the REST call
		wfRestCall.setWorkflowParameters(computeParameter(callData));

		// lookup the URL
		AbstractWorkflow workflow = registeredWorkflows.get(callData.getWorkflowID());
		if (workflow == null) {
			myLogger.error(callData.getWorkflowID() + " was not found by the WorkflowBroker");
			throw new WFException("Workflow not found/registered");
		}
		String workflowURL = workflow.getCompleteServiceURL();
		myLogger.info("Using REST-API: " + workflowURL);
		try {
			WorkflowSender.initWorkflow(wfRestCall, workflowURL);
			myLogger.info("Workflow deployed");
		} catch (WFRestException e) {
			myLogger.error("Could not deploy workflow");
			throw e;
		}
	}

	private List<WFParameter> computeParameter(ServiceCallData data) {

		List<WFParameter> parameters = data.getParameters();
		List<WFParameter> filteredParameters = new LinkedList<WFParameter>();

		for (WFParameter parameter : parameters) {
			myLogger.info(parameter.getKey() + " : " + parameter.getValue());
			String key = parameter.getKey();

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
					} catch (Exception e) {
						myLogger.error(e.getCause().toString() + " : " + e.getMessage());
					}

				}
			}
		}
		// TODO the case of scheduled Workflows is not considered in the moment
		// and may be needed in the future (?)

		// msg.setParameters(filteredParameters);
		//
		// if(!quartzCron.equals("")){
		// myLogger.info("Scheduling new job");
		// final ServiceCallData schedulerMsg = msg;
		// Scheduler s = new Scheduler();
		//
		// s.schedule(quartzCron, new Runnable() {
		// public void run() {
		// PubFlowJob.execute(schedulerMsg);
		// }
		// });
		// s.start();
		//
		// }

		return filteredParameters;
	}

	/**
	 * Maps the answers from the Workflow Microservice to a
	 * {@link AbstractWorkflow Workflow} and delegates the handling of the
	 * answer to this object.
	 * 
	 * @throws WFRestException
	 */
	public void receiveWorkflowAnswer(String jiraKey, ReceivedWorkflowAnswer answer) {
		myLogger.debug("Receveived answer to issue " +jiraKey +".");
		// TODO a generic mapping for available Workflows, which tells what to
		// do in certain events
		if (JiraObjectGetter.getIssueByJiraKey(jiraKey) == null) {
			myLogger.info("Got answer to non-existing issue  with key: " + jiraKey + " with message: ");
			myLogger.info(answer.toString());
			return;
		}

		String workflowName = JiraObjectGetter.getIssueTypeNamebyJiraKey(jiraKey);

		AbstractWorkflow workflow = registeredWorkflows.get(workflowName);

		if (workflow == null) {
			myLogger.info("Got answer to issue with key " + jiraKey + " but the issue type is not registered.");
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
	static public void addWorkflow(AbstractWorkflow workflow) {
		registeredWorkflows.put(workflow.getWorkflowName(), workflow);
		myLogger.info("Registered Workflow: "+workflow.getWorkflowName() +" at the WorkflowBroker");
	}
}
