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
	private String jiraKey;
	/**
	 * The type of the workflow (e.g. BPMN2)
	 */
	private String type;
	/**
	 * Parameters used to execute the workflow.
	 */
	private List<WFParameter> workflowParameters;

	public WorkflowCall() {
	}

	public String getId() {
		return jiraKey;
	}

	public void setId(String id) {
		this.jiraKey = id;
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
		return "WF message: id: " + jiraKey + " wf: " + type;
	}


}