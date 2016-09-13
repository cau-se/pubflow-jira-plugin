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
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.jira.accessors.JiraObjectGetter;
import de.pubflow.jira.accessors.JiraObjectManipulator;
import de.pubflow.server.common.entity.workflow.ParameterType;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.exceptions.WFException;
import de.pubflow.server.common.exceptions.WFRestException;
import de.pubflow.server.core.restConnection.JiraRestConnector;
import de.pubflow.server.core.restConnection.WorkflowSender;
import de.pubflow.server.core.workflow.messages.ReceivedWorkflowAnswer;
import de.pubflow.server.core.workflow.messages.ServiceCallData;
import de.pubflow.server.core.workflow.messages.WorkflowRestCall;

/**
 * Handles all Workflow execution. The initialization and updates are covered by
 * this class as well as the mapping of answers after a Workflow instance has
 * finished.
 * 
 * @author Marc Adolf, Peer Brauer
 *
 */
public class WorkflowBroker {

	private Logger myLogger;

	public WorkflowBroker() {
		myLogger = LoggerFactory.getLogger(this.getClass());
		myLogger.info("Starting WorkflowBroker");
	}

	/**
	 * Handles new Workflow calls for PubFlow. They will be saved and send to
	 * the Workflow Microservice
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
		String workflowURL = getCorrespondingWorkflowURL(callData.getWorkflowID());

		// TODO: testing remove this
		workflowURL = "/TestWorkflow";
		System.out.println(workflowURL);
		try {
			WorkflowSender.getInstance().initWorkflow(wfRestCall, workflowURL);
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
	 * Handles answers from the Workflow Microservice.
	 * 
	 * @throws WFRestException
	 */
	public void receiveWorkflowAnswer(String jiraKey, ReceivedWorkflowAnswer answer) {
		// TODO a generic mapping for available Workflows, which tells what to
		// do in certain events
		if (JiraObjectGetter.getIssueByJiraKey(jiraKey) == null) {
			myLogger.info("Got answer to non-existing issue  with key: " + jiraKey + " with message: ");
			myLogger.info(answer.toString());
			return;
		}

		if (answer.getResult().toLowerCase().contains("error")) {
			myLogger.error("Workflow with id " + jiraKey + " failed, with message: '" + answer.getErrorMessage() + "'");

			JiraObjectManipulator.addIssueComment(jiraKey,
					"Workflow  failed due to: '" + answer.getErrorMessage() + "'",
					JiraObjectGetter.getUserByName("PubFlow"));

		} else {
			// TODO in case of no error

		}

		if (answer.getNewStatus() != null) {
			JiraObjectManipulator.changeStatus(jiraKey, answer.getNewStatus());
		}

	}

	private String getCorrespondingWorkflowURL(String workflow) {
		String workflowURL = "";

		// TODO is there a better/prettier way to map the Workflow String from
		// Jira to
		// the REST URL?

		switch (workflow) {
		case "OCN":
			workflowURL = "/OCNWorkflow";
			break;

		case "CVOO":
			workflowURL = "/CVOOWorkflow";
			break;

		case "EPRINTS":
			workflowURL = "/EPrintsWorkflow";
			break;

		default:
			break;
		}

		return workflowURL;
	}

}
