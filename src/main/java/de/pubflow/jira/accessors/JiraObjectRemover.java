package de.pubflow.jira.accessors;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;

public class JiraObjectRemover {

	public static boolean removeIssue(String issueKey){
		try {
			Issue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
			ComponentAccessor.getIssueManager().deleteIssueNoEvent(issue);
			return true;
	
		} catch (RemoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * removes an attachment
	 * 
	 * @param attachmentId
	 * @return
	 */
	public static boolean removeAttachment(long attachmentId){
		try{
			Attachment attachment = ComponentAccessor.getAttachmentManager().getAttachment(attachmentId);
			ComponentAccessor.getAttachmentManager().deleteAttachment(attachment);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

}
