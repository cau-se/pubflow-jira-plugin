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
package de.pubflow.jira.accessors;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.user.ApplicationUser;

/**
 * A utility class to delete objects in Jira.
 */
public final class JiraObjectRemover {

	private JiraObjectRemover() {

	}

	/**
	 * Deletes an user account in Jira.
	 * 
	 * @author abar
	 * 
	 * @param loggedUser
	 *            : An user with permissions that can remove another user
	 * @param userToDelete
	 *            : the name of the user that shall be removed
	 */
	public static void deleteUser(final ApplicationUser loggedUser, final String userToDelete) {
		if (userToDelete != null) {
			ComponentAccessor.getUserUtil().removeUser(loggedUser,
					ComponentAccessor.getUserManager().getUserByName(userToDelete));
		}
	}

	/**
	 * Removes and issue in Jira.
	 * 
	 * @param issueKey
	 *            the key of an issue to be removed.
	 * @return
	 */
	public static boolean removeIssue(final String issueKey) {
		try {
			final Issue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
			ComponentAccessor.getIssueManager().deleteIssueNoEvent(issue);
			return true;

		} catch (final RemoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Removes an attachment.
	 * 
	 * @param attachmentId
	 *            the id of an attachment to be removed.
	 * @return
	 */
	public static boolean removeAttachment(final long attachmentId) {
		try {
			final AttachmentManager attachmentManager = ComponentAccessor.getAttachmentManager();
			final Attachment attachment = attachmentManager.getAttachment(attachmentId);
			attachmentManager.deleteAttachment(attachment);
			return true;
		} catch (final Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}

}
