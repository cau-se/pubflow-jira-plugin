package de.pubflow.jira.accessors;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.user.ApplicationUser;

public class JiraObjectRemover {


	/**
	 * Removes an User in Jira
	 * @author abar
	 * 
	 * @param loggedUser : An user that can remove another user (Administrator in general)
	 * @param userToDelete : the name of the user that shall be removed
	 */
	public static void deleteUser(ApplicationUser loggedUser, String userToDelete) {
		if (userToDelete != null) {
			ComponentAccessor.getUserUtil().removeUser(loggedUser, ComponentAccessor.getUserManager().getUserByName(userToDelete));
		}
	}

	/**
	 * @param issueKey
	 * @return
	 */
	public static boolean removeIssue(String issueKey) {
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
	public static boolean removeAttachment(long attachmentId) {
		try {
			Attachment attachment = ComponentAccessor.getAttachmentManager().getAttachment(attachmentId);
			ComponentAccessor.getAttachmentManager().deleteAttachment(attachment);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
