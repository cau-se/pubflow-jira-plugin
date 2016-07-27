/**
 * 
 */
package de.pubflow.server.core.communication;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import de.pubflow.server.common.entity.workflow.WFParameter;

/**
 * Contains all needed information to execute a certain workflow and to respond
 * to the caller.
 *
 * @author Marc Adolf
 */
public class WorkflowCall {

	/**
	 * Used to map the workflow to other services, events and responses.
	 */
	private UUID id;
	/**
	 * The workflow to be executed, as byte array.
	 */
	private byte[] wf;
	/**
	 * The type of the workflow (e.g. BPMN2)
	 */
	private String type;
	/**
	 * Parameters used to execute the workflow.
	 */
	private List<WFParameter> workflowParameters;
	/**
	 * Url to response to
	 */
	private URL callbackAddress;

	public WorkflowCall() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public byte[] getWf() {
		return wf;
	}

	public void setWf(byte[] wf) {
		this.wf = wf;

	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<WFParameter> getWorkflowParameters() {
		return workflowParameters;
	}

	public void setWorkflowParameters(List<WFParameter> workflowParameters) {
		this.workflowParameters = workflowParameters;
	}

	public String toString() {
		return "WF message: id: " + id + " wf: " + wf;
	}

	public URL getCallbackAddress() {
		return callbackAddress;
	}

	public void setCallbackAddress(URL callbackAddress) {
		this.callbackAddress = callbackAddress;
	}

}