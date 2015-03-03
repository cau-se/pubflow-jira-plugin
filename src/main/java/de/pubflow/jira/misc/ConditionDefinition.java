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

