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

import java.util.Map;

public class ConditionDefinition{

	public enum ConditionDefinitionType {

		ATTACHMENT("de.pubflow.jira.misc.IssueAttachmentCondition"),
		USERINGROUP("com.atlassian.jira.workflow.condition.UserInGroupCondition");

		private String clazz = "";

		ConditionDefinitionType(String clazz) {
			this.setClazz(clazz);
		}

		public String getClazz() {
			return clazz;
		}

		public void setClazz(String clazz) {
			this.clazz = clazz;
		}

	}

	private ConditionDefinitionType type;
	private Map<String, String> params;
	private int[] transitions;

	/**
	 * @param type
	 * @param params
	 * @param transitions
	 */
	public ConditionDefinition(ConditionDefinitionType type, Map<String, String> params, int[] transitions) {
		this.type = type;
		this.params = params;
		this.transitions = transitions;
	}
	
	public String getType() {
		return type.getClazz();
	}
	
	public void setType(ConditionDefinitionType type) {
		this.type = type;
	}
	
	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public int[] getTransitions() {
		return transitions;
	}
	
	public void setTransitions(int[] transitions) {
		this.transitions = transitions;
	}

}

