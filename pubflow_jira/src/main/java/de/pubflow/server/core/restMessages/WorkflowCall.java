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
/**
 * 
 */
package de.pubflow.server.core.restMessages;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.pubflow.server.common.entity.workflow.WFParameter;

/**
 * Contains all needed information to execute a certain workflow and to respond
 * to the caller.
 *
 * @author Marc Adolf
 */
@JsonIgnoreProperties(ignoreUnknown=true)
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