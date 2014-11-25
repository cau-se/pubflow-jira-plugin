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

					//getAttachments results in NPE when attachment count is 0
					//at com.atlassian.jira.issue.managers.DefaultAttachmentManager.getStoredAttachments(DefaultAttachmentManager.java:133)
					//at com.atlassian.jira.issue.managers.DefaultAttachmentManager.getAttachments(DefaultAttachmentManager.java:125)
					//at com.atlassian.jira.issue.AbstractIssue.getAttachments(AbstractIssue.java:97)
					//at de.pubflow.jira.misc.IssueAttachmentCondition.passesCondition(IssueAttachmentCondition.java:25)

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
