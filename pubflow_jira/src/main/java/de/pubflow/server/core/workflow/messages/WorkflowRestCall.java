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
package de.pubflow.server.core.workflow.messages;

import java.net.URL;
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
	private String jiraKey;
	/**
	 * Parameters used to execute the workflow.
	 */

	/**
	 * Url to response to
	 */
	private URL callbackAddress;

	private List<WFParameter> workflowParameters;

	public WorkflowRestCall() {
	}

	public String getId() {
		return jiraKey;
	}

	public void setId(String id) {
		this.jiraKey = id;
	}

	public List<WFParameter> getWorkflowParameters() {
		return workflowParameters;
	}

	public void setWorkflowParameters(List<WFParameter> workflowParameters) {
		this.workflowParameters = workflowParameters;
	}

	public URL getCallbackAddress() {
		return callbackAddress;
	}

	public void setCallbackAddress(URL callbackAddress) {
		this.callbackAddress = callbackAddress;
	}

}