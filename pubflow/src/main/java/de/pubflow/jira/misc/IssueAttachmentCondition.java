package de.pubflow.jira.misc;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

public class IssueAttachmentCondition extends com.atlassian.jira.workflow.condition.AbstractJiraCondition{

	@Override
	public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

		if(transientVars.get("issue") != null){
			if(transientVars.get("issue") instanceof Issue){
				Issue issue = (Issue) transientVars.get("issue");
				if(issue != null){
					try{
						if(issue.getAttachments().size() >= 1){
							return true;
						}else{
							return false;
						}

					}catch(NullPointerException e){
						return false;
					}
				}
			}
		}
		return true;
	}

}
