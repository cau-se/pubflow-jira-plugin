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
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.user.ApplicationUser;

public final class JiraObjectRemover {

	private JiraObjectRemover() {
		
	}
	
	/**
	 * Removes an User in Jira
	 * 
	 * @author abar
	 * 
	 * @param loggedUser
	 *            : An user that can remove another user (Administrator in
	 *            general)
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
	 * @param issueKey
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
	 * removes an attachment
	 * 
	 * @param attachmentId
	 * @return
	 */
	public static boolean removeAttachment(final long attachmentId) {
		try {
			final Attachment attachment = ComponentAccessor.getAttachmentManager().getAttachment(attachmentId);
			ComponentAccessor.getAttachmentManager().deleteAttachment(attachment);
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
