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

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.workflow.WorkflowException;

import de.pubflow.jira.accessors.JiraObjectGetter;
import de.pubflow.jira.accessors.JiraObjectManipulator;
import de.pubflow.server.common.entity.WorkflowEntity;
import de.pubflow.server.common.entity.workflow.ParameterType;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.enumeration.WorkflowState;
import de.pubflow.server.common.exceptions.WFException;
import de.pubflow.server.common.exceptions.WFRestException;
import de.pubflow.server.common.repository.WorkflowProvider;
import de.pubflow.server.core.restConnection.WorkflowReceiver;
import de.pubflow.server.core.restConnection.WorkflowSender;
import de.pubflow.server.core.restMessages.WorkflowAnswer;
import de.pubflow.server.core.restMessages.WorkflowCall;
import de.pubflow.server.core.workflow.store.WorkflowStorage;

/**
 * Handles all Workflow execution. The initialization and updates are covered by
 * this class as well as the mapping of answers after a Workflow instance has
 * finished.
 * 
 * @author Marc Adolf, Peer Brauer
 *
 */
public class WorkflowBroker {

	private static volatile WorkflowBroker instance;

	private Logger myLogger;

	private WorkflowBroker() {
		myLogger = LoggerFactory.getLogger(this.getClass());
		myLogger.info("Starting WorkflowBroker");
	}

	public static synchronized WorkflowBroker getInstance() {
		if (instance == null) {
			instance = new WorkflowBroker();
		}
		return instance;
	}

	/**
	 * Handles new Workflow calls for Pubflow. They will be saved and send to
	 * the Workflow Microservice
	 * 
	 * @param wm
	 * @throws WFException
	 */
	public void receiveWFCall(ServiceCallData wm) throws WFException {

		// TODO Save Workflows in DB and load from it on startup

		if (!wm.isValid()) {
			myLogger.error("Workflow NOT deployed >> Msg is not valid ");
			return;
		}
		myLogger.info("Creating new Instance of the '" + wm.getWorkflowID() + "' Workflow");
		WorkflowCall wfRestCall = new WorkflowCall();

		myLogger.info("Saving and deploying new Workflow");
		WorkflowStorage storage = WorkflowStorage.getInstance();
		try {
			wm.setState(WorkflowState.REGISTERED);
			storage.addWorkflowCall(wm);
		} catch (WorkflowException e) {
			UUID newID = storage.addWorkflowCallWithNewID(wm);
			// update ID
			wm.setWorkflowInstanceId(newID);
			myLogger.debug("Workflow ID already exists. Updating new ID.");
		}

		// add Callback address to the REST call
		try {
			wfRestCall.setCallbackAddress(WorkflowReceiver.getCallbackAddress());
		} catch (MalformedURLException | UnknownHostException e) {
			myLogger.error("Could not set callback address for the REST call");
			throw new WFException("Could not set callback address");
		}

		WorkflowProvider provider = WorkflowProvider.getInstance();
		WorkflowEntity wfEntity = provider.getByWFID(wm.getWorkflowID());
		if(wfEntity == null){
			myLogger.error("Workflow +'"+wm.getWorkflowID() +"' not found");
			throw new WFException("Workflow +'"+wm.getWorkflowID() +"' not found");
		}

		// set parameters for the REST call
		wfRestCall.setType(wfEntity.getType().toString());
		wfRestCall.setWf(wfEntity.getgBpmn());
		wfRestCall.setId(wm.getWorkflowInstanceId());
		wfRestCall.setWorkflowParameters(computeParameter(wm));
		
		wm.setState(WorkflowState.RUNNING);

		try {
			WorkflowSender.getInstance().initWorkflow(wfRestCall);
			myLogger.info("Workflow deployed");
		} catch (WFRestException e) {
			wm.setState(WorkflowState.DEPLOY_ERROR);
			myLogger.error("Could not deploy workflow");
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
					data.setState(null);
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
						// if(msg.getWorkflowID().substring(msg.getWorkflowID().lastIndexOf(".")
						// + 1).equals(key.substring(key.lastIndexOf("_") +
						// 1))){
						// parameter.setKey(key.substring(0,
						// key.lastIndexOf("_")));
						parameter.setKey(key);
						filteredParameters.add(parameter);
						// }
					} catch (Exception e) {
						myLogger.error(e.getCause().toString() + " : " + e.getMessage());
					}

				}
			}
		}
		// TODO this case is not considered and may be needed in the future
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
	 * TODO handles updates/ events for existing workflows
	 */
	public void receiveWorkflowUpdate() {
		// TODO
	}

	/**
	 * Handles answers from the Workflow Microservice
	 * 
	 * @throws WFRestException
	 */
	synchronized public void receiveWorkflowAnswer(WorkflowAnswer wfAnswer) throws WFRestException {
		ServiceCallData currentWorkflow = WorkflowStorage.getInstance().lookupWorkflowCall(wfAnswer.getId());
		if (currentWorkflow == null) {
			throw new WFRestException("Workflow not found");
		}
		if (!wfAnswer.getResult().contains("error")) {
			// since the workflow will currently call the rest api (to support
			// all "legacy" workflows), we only need to update the state of the
			// saved ServiceCall
			currentWorkflow.setState(WorkflowState.FINISHED);

		} else {
			// Error handling needs to update the Jira issue since the Workflow
			// could not execute
			// maybe some other worker has already successfully completed the
			// work
			if (currentWorkflow.getState() == WorkflowState.FINISHED) {
				return;
			} else {
				currentWorkflow.setState(WorkflowState.ERROR);
				myLogger.error("Workflow with id " + wfAnswer.getId() + " failed, with message: '"
						+ wfAnswer.getErrorMessage() + "'");
				// Error handling from the class JiraManagerPlugin
				// TODO may be improved in the future
				JiraObjectManipulator.addIssueComment(
						currentWorkflow.getJiraKey(), "Workflow " + currentWorkflow.getWorkflowID()
								+ " failed due to: '" + wfAnswer.getErrorMessage() + "'",
						JiraObjectGetter.getUserByName("PubFlow"));
			}

		}
	}
}
