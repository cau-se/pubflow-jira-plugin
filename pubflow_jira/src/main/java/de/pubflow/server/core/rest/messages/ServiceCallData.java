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
package de.pubflow.server.core.rest.messages;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.enumeration.WFType;

@XmlRootElement(namespace = "http://pubflow.de/message/workflow")
public class ServiceCallData implements Serializable {

	private static final long serialVersionUID = -4931074023209271264L;

	/**
	 * The id used to get the (bpmn) workflow
	 */
	private String workflowID = "";
	
	private WFType type = null;
	private List<WFParameter> parameters = null;
	private String jiraKey;

	public ServiceCallData() {
	}

	/**
	 * @return the workflowID
	 */
	@XmlElement(name = "WorkflowRef")
	public synchronized String getWorkflowID() {
		return workflowID;
	}

	/**
	 * @param workflowID
	 *            the workflowID to set
	 */
	public synchronized void setWorkflowID(String workflowID) {
		this.workflowID = workflowID;
	}

	/**
	 * @return the wftype
	 */
	@XmlElement(name = "WorkFlowType")
	public synchronized WFType geType() {
		return type;
	}

	/**
	 * @param wftype
	 *            the wftype to set
	 */
	public synchronized void setType(WFType wftype) {
		this.type = wftype;
	}


	/**
	 * @return the wfparams
	 */
	@XmlElement(name = "Parameters")
	public synchronized List<WFParameter> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            the wfparams to set
	 */
	public synchronized void setParameters(List<WFParameter> parameters) {
		this.parameters = parameters;
	}

	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	public String getJiraKey() {
		return jiraKey;
	}

	public void setJiraKey(String jiraKey) {
		this.jiraKey = jiraKey;
	}


	public WFType getType() {
		return type;
	}
	
}
