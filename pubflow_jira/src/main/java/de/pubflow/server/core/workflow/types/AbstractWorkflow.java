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
package de.pubflow.server.core.workflow.types;

import java.util.List;

import de.pubflow.jira.misc.CustomFieldDefinition;

/**
 * Represents the general Workflow type/scheme. Contains the name, the path to
 * the Jira Workflow XML file and the API for the Rest call to the Webservice.
 * 
 * @author Marc Adolf
 *
 */
abstract public class AbstractWorkflow {

	private String workflowName;
	private String workflowID;
	private String jiraWorkflowXMLPath;
	//TODO this should be filled in a more dynamic way
	private String baseWebserviceURL = "http://localhost:8080";
	private String webServiceAPI;

	public AbstractWorkflow(String workflowName, String workflowID, String jiraWorkflowXMLPath, String webServiceAPI,
			String baseWebserviceURL) {
		this(workflowName, workflowID, jiraWorkflowXMLPath, webServiceAPI);
		this.baseWebserviceURL = baseWebserviceURL;

	}

	public AbstractWorkflow(String workflowName, String workflowID, String jiraWorkflowXMLPath, String webServiceAPI) {
		this.webServiceAPI = webServiceAPI;
		this.workflowID = workflowID;
		this.workflowName = workflowName;
		this.jiraWorkflowXMLPath = jiraWorkflowXMLPath;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public String getJiraWorkflowXMLPath() {
		return jiraWorkflowXMLPath;
	}

	public String getBaseWebserviceURL() {
		return baseWebserviceURL;
	}

	public String getWebServiceAPI() {
		return webServiceAPI;
	}

	public String getCompleteServiceURL() {
		return baseWebserviceURL + webServiceAPI;
	}
	
	public String getWorkflowID() {
		return workflowID;
	}


	abstract public List<String> getScreenNames();
	
	/**
	 * Defines the Data fields of the Workflow
	 * @return
	 */
	abstract public List<CustomFieldDefinition> getCustomFields();
	

	/**
	 * Defines how the result of the Workflow Rest call should be handled.
	 */
	abstract public void handleWorkflowResults();

	/**
	 * * Defines how errors during the Workflow Rest call should be handled.
	 * 
	 */
	abstract public void handleWorkflowError();
	
}
