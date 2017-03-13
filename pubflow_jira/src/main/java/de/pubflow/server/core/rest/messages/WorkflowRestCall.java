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
package de.pubflow.server.core.rest.messages;

import java.util.List;

import de.pubflow.server.common.entity.workflow.WFParameter;

/**
 * Contains all needed information to execute a certain workflow and to respond
 * to the caller.
 *
 * @author Marc Adolf
 */
public class WorkflowRestCall {

	/**
	 * Used to map the workflow to other services, events and responses.
	 */
	private String workflowID;
	/**
	 * Url to response to
	 */
	private String callbackAddress;
	/**
	 * Parameters used to execute the workflow.
	 */
	private List<WFParameter> workflowParameters;

	/**
	 * 
	 * @return
	 */
	public String getID() {
		return this.workflowID;
	}

	/**
	 * 
	 * @param workflowID
	 */
	public void setID(final String workflowID) {
		this.workflowID = workflowID;
	}

	/**
	 * 
	 * @return
	 */
	public List<WFParameter> getWorkflowParameters() {
		return workflowParameters;
	}

	/**
	 * 
	 * @param workflowParameters
	 */
	public void setWorkflowParameters(final List<WFParameter> workflowParameters) {
		this.workflowParameters = workflowParameters;
	}

	/**
	 * 
	 * @return
	 */
	public String getCallbackAddress() {
		return callbackAddress;
	}

	/**
	 * 
	 * @param callbackAddress
	 */
	public void setCallbackAddress(final String callbackAddress) {
		this.callbackAddress = callbackAddress;
	}

}