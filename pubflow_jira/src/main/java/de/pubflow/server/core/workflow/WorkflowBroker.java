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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.entity.workflow.ParameterType;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.exceptions.WFException;
import de.pubflow.server.common.exceptions.WFRestException;
import de.pubflow.server.core.restConnection.WorkflowReceiver;
import de.pubflow.server.core.restConnection.WorkflowSender;
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

		// add Callback address to the REST call
		try {
			wfRestCall.setCallbackAddress(WorkflowReceiver.getCallbackAddress());
		} catch (MalformedURLException | UnknownHostException e) {
			myLogger.error("Could not set callback address for the REST call");
			throw new WFException("  Could not set callback address");
		}
		
		myLogger.info("Deploying new Workflow");

		// TODO extract Code to microService
		// --------
		// choose which workflow api should be called
//		WorkflowProvider provider = WorkflowProvider.getInstance();
//		WorkflowEntity wfEntity = provider.getByWFID(callData.getWorkflowID());
//		if (wfEntity == null) {
//			myLogger.error("Workflow +'" + callData.getWorkflowID() + "' not found");
//			throw new WFException("Workflow +'" + callData.getWorkflowID() + "' not found");
//		}

		String workflowPath = "/workflow/OCN";

		// _-----------------------_

		// set parameters for the REST call
		wfRestCall.setWorkflowParameters(computeParameter(callData));

		try {
			WorkflowSender.getInstance().initWorkflow(wfRestCall, workflowPath);
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
	synchronized public void receiveWorkflowAnswer(){
		// TODO or delete
	}
}
