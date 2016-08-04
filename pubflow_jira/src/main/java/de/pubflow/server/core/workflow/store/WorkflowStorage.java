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
package de.pubflow.server.core.workflow.store;

import java.util.HashMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.workflow.WorkflowException;

import de.pubflow.server.core.workflow.ServiceCallData;

/**
 * Saves the processed Workflows. Enables PubFlow to map incoming requests to
 * existing workflow instances .
 * 
 * @author Marc Adolf
 *
 */
public class WorkflowStorage {
	/**
	 * Saves all Workflow calls and uses the ID as key
	 */
	private HashMap<UUID, ServiceCallData> processedWorkflows;
	private static WorkflowStorage instance = null;
	private Logger myLogger;

	/**
	 * 
	 */
	private WorkflowStorage() {
		myLogger = LoggerFactory.getLogger(this.getClass());
		processedWorkflows = new HashMap<>();
	}

	synchronized static public WorkflowStorage getInstance() {
		if (instance == null) {
			instance = new WorkflowStorage();
		}
		return instance;
	}

	/**
	 * 
	 * 
	 * @param key
	 *            The {@link UUID} which identifies the workflow
	 * @return
	 */
	synchronized public ServiceCallData lookupWorkflowCall(UUID key) {
		return processedWorkflows.get(key);
	}

	/**
	 * Adds a WorkflowCall to the Storage, if the id does not already exists.
	 * 
	 * @param workflowData
	 * @throws WorkflowException
	 *             if the ID already exists and the Jira keys are different.
	 */
	synchronized public void addWorkflowCall(ServiceCallData workflowData) throws WorkflowException {
		UUID id = workflowData.getWorkflowInstanceId();
		ServiceCallData foundWf = lookupWorkflowCall(id);
		if (foundWf != null && !foundWf.getJiraKey().equals(workflowData.getJiraKey())) {
			throw new WorkflowException("WorkflowID already exists");
		} else {
			//TODO is a special handling needed if workflow was already saved?
			myLogger.debug("Registered Workflow with ID :" + id);
			processedWorkflows.put(id, workflowData);
		}

	}

	/**
	 * Stores a WorkflowCall and generates a new ID for the mapping in the
	 * storage. Does not overwrite the content of the ID field in the data.
	 * 
	 * @param workflowData
	 * @return the ID where the data is saved.
	 */
	synchronized public UUID addWorkflowCallWithNewID(ServiceCallData workflowData) {
		UUID id;
		// generate a new ID until it is not present in the wf-store
		do {
			id = UUID.randomUUID();
			myLogger.debug("Generating new ID");
		} while (lookupWorkflowCall(id) != null);
		processedWorkflows.put(id, workflowData);
		return id;
	}

}
