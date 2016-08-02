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
