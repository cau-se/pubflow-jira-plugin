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
package de.pubflow.common.entity;

import java.util.HashMap;

public class JiraIssue {
	private String issueTypeName; 
	private String summary; 
	private String description; 
	private HashMap<String, String> parameters; 
	private String reporter;

	public JiraIssue(){	
	}
	
	public JiraIssue(String issueTypeName, String summary, String description, HashMap<String, String> parameters, String reporter) {
		this.issueTypeName = issueTypeName;
		this.summary = summary;
		this.description = description;
		this.parameters = parameters;
		this.reporter = reporter;
	}

	public String getIssueTypeName() {
		return issueTypeName;
	}

	public void setIssueTypeName(String issueTypeName) {
		this.issueTypeName = issueTypeName;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	@Override
	public String toString() {
		return "JiraIssue [issueTypeName=" + issueTypeName + ", summary=" + summary + ", description=" + description
				+ ", parameters=" + parameters + ", reporter=" + reporter + "]";
	}
}
