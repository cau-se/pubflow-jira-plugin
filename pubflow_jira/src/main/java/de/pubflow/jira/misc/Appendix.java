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
package de.pubflow.jira.misc;

/**
 * Appendix that are added workflows, fieldscreens, issueTypes, and schemes
 *
 */
public enum Appendix{
//	ISSUETYPE(""),
	FIELDSCREEN("_FieldScreen"),
	WORKFLOWSCHEME("_WorkflowScheme"),
	WORKFLOW("_Workflow"),
	FIELDSCREENSCHEME("_FieldScreenScheme"),
	ISSUETYPESCHEME("_IssueTypeScheme");

	/**
	 * The name of the appendix.
	 */
	private String name = "";

	/**
	 * 
	 * @param type
	 */
	Appendix (final String type) {
		this.getType(type);
	}

	/**
	 * @return String the name of the appendix
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param type set the name of the appendix.
	 */
	public void getType(final String type) {
		this.name = type;
	}
}