package de.pubflow.server.core.workflow.store;

import java.util.HashMap;
import java.util.UUID;

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

	/**
	 * 
	 */
	private WorkflowStorage() {
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
	 * @param key The {@link UUID} which identifies the workflow
	 * @return
	 */
	synchronized public ServiceCallData lookupWorkflowCall(UUID key) {
		return processedWorkflows.get(key);
	}

	synchronized public void addWorkflowCall(ServiceCallData workflowData) throws WorkflowException {
		UUID id = workflowData.getWorkflowInstanceId();
		if (lookupWorkflowCall(id) != null) {
			throw new WorkflowException("WorkflowID already exists");
		} else {
			processedWorkflows.put(id, workflowData);
		}

	}
}
