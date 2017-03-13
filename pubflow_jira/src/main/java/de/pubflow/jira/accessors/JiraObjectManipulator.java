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

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

import de.pubflow.jira.JiraManagerPlugin;

public class JiraObjectManipulator {

	private static Logger log = LoggerFactory.getLogger(JiraObjectManipulator.class);

	/**
	 * @author abar
	 * @param issueTypeScheme
	 *            : the issue type Scheme we map to the project
	 */
	public static void addIssueTypeSchemeToProject(final FieldConfigScheme issueTypeScheme, final Project project) {
		final IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();
		final FieldConfigSchemeManager fieldConfigSchemeManager = ComponentAccessor.getFieldConfigSchemeManager();
		final FieldManager fieldManager = ComponentAccessor.getFieldManager();

		final Collection<Project> projects = new ArrayList<Project>();
		projects.add(project);

		if (issueTypeScheme != issueTypeSchemeManager.getDefaultIssueTypeScheme()) {
			final List<Long> projectIds = new ArrayList<Long>();
			projectIds.add(project.getId());
			log.debug("projectIds: $projectIds");
			final Long[] ids = new Long[projectIds.size()];

			final List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(true, projectIds.toArray(ids),
					ComponentAccessor.getProjectManager());
			fieldConfigSchemeManager.updateFieldConfigScheme(issueTypeScheme, contexts,
					fieldManager.getConfigurableField(IssueFieldConstants.ISSUE_TYPE));
		}
	}

	/**
	 * Creates a new screen scheme for an issue type in Jira.
	 * 
	 * @author abar
	 * @param project
	 *            : the project which uses the issueType
	 * @param issueTypeScheme
	 *            : the issue type scheme that will be used
	 * @param issueType
	 *            : the issue type which will be used
	 * @return The issue type screen scheme which was created
	 */
	public static IssueTypeScreenScheme addIssueTypeScreenSchemeToProject(final Project project,
			final FieldScreenScheme fieldScreenScheme, final IssueType issueType) {

		final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = ComponentAccessor
				.getIssueTypeScreenSchemeManager();
		IssueTypeScreenScheme issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project);

		if (issueTypeScreenScheme == null) {
			// set default issue type screen scheme
			issueTypeScreenSchemeManager.associateWithDefaultScheme(project);
			issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project);
		}

		// compose
		final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(
				issueTypeScreenSchemeManager, (GenericValue) null, JiraManagerPlugin.fieldScreenSchemeManager,
				ComponentAccessor.getConstantsManager());
		issueTypeScreenSchemeEntity.setIssueTypeId(issueType.getId());
		issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenScheme);
		issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);

		return issueTypeScreenScheme;
	}

	/**
	 * Maps the workflow scheme of a given workflow to a given project.
	 * 
	 * @author abar
	 * @param workflow:
	 *            the workflow we take to map its scheme to a project
	 * @param project:
	 *            the project we add the given workflow scheme to
	 */
	public static void addWorkflowToProject(final WorkflowScheme workflowScheme, final Project project) {
		final WorkflowSchemeManager workflowSchemeManager = ComponentAccessor.getWorkflowSchemeManager();
		final Scheme scheme = workflowSchemeManager.getSchemeObject(workflowScheme.getId());
		if (scheme != null && project != null) {
			workflowSchemeManager.addSchemeToProject(project, scheme);
			log.info("addWorkflowToProject: the workflowscheme " + scheme.getName() + " was added to the project "
					+ project.getName());
		} else {
			log.debug("addWorkflowToProject: the project is already mapped to the scheme " + scheme.getName());
		}
	}

	/**
	 * Appends an attachment to an issue
	 * 
	 * @param issueKey
	 * @param barray
	 * @param fileName
	 * @param type
	 * @param user
	 * @return
	 */
	public static long addAttachment(final String issueKey, final byte[] barray, final String fileName, final String type,
			final ApplicationUser user) {

		try {
			final MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);

			// TODO : path os?
			final String filePath = "/tmp/pubflow_tmp" + new BigInteger(130, JiraManagerPlugin.secureRandom).toString(32);
			final FileOutputStream stream = new FileOutputStream(filePath);

			stream.write(barray);
			stream.close();
			final File barrayFile = new File(filePath);
			ComponentAccessor.getAttachmentManager()
					.createAttachment(new CreateAttachmentParamsBean(barrayFile, fileName + type, "text/plain", user,
							issue, false, false, null, new Timestamp(System.currentTimeMillis()), true));

			// TODO: no id?
			return 0L;

		} catch (final Exception e) {
			e.printStackTrace();
			return 0L;
		}
	}

	/**
	 * Adds a new comment to an issue
	 * 
	 * @param issueKey
	 * @param comment
	 * @param user
	 * @return returns if the new comment has been added successful
	 */

	public static Comment addIssueComment(final String issueKey, final String comment, final ApplicationUser user) {
		log.info("addIssueComment - issueKey : " + issueKey + " / comment : " + comment);

		if (user != null) {
			log.info("addIssueComment - user : " + user.getName());
		} else {
			log.info("addIssueComment - user : null");
		}

		final Issue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
		final Comment commentObject = ComponentAccessor.getCommentManager().create(issue, user, comment, false);

		if (commentObject != null) {
			log.info("addIssueComment - return commentObject");
			return commentObject;
		} else {
			log.info("addIssueComment - return null");
			return null;
		}
	}

	/**
	 * Add a User to a group in Jira
	 * 
	 * @author abar
	 * 
	 * @param pubFlowUser
	 *            : a user we want to a group
	 * @param pGroup:
	 *            the name of a group we want to add an user to
	 * 
	 */
	public static void addUserToGroup(final ApplicationUser pubflowUser, final String pGroup)
			throws PermissionException, AddException, GroupNotFoundException, UserNotFoundException,
			OperationNotPermittedException, OperationFailedException {
		final Group group = ComponentAccessor.getGroupManager().getGroup(pGroup);
		if (pubflowUser != null && group != null) {
			ComponentAccessor.getGroupManager().addUserToGroup(pubflowUser, group);
			log.info("addUserToGroup: added the user " + pubflowUser.getUsername() + " to group " + group.getName());
		} else {
			log.error("addUserToGroup: user " + pubflowUser + " can't be added to group " + group);
		}
	}

	/**
	 * Add a User to a group in Jira
	 * 
	 * @author abar
	 * 
	 * @param pubFlowUser
	 *            : a user we want to a group
	 * @param group:
	 *            the group we want to add an user to
	 * 
	 */
	public static void addUserToGroup(final ApplicationUser pubflowUser, final Group group)
			throws PermissionException, AddException, GroupNotFoundException, UserNotFoundException,
			OperationNotPermittedException, OperationFailedException {
		if (pubflowUser != null && group != null) {
			ComponentAccessor.getGroupManager().addUserToGroup(pubflowUser, group);
			log.info("addUserToGroup: added the user " + pubflowUser.getUsername() + " to group " + group.getName());
		} else {
			log.error("addUserToGroup: user " + pubflowUser + " can't be added to group " + group);
		}

	}

	/**
	 * Changes the status of an issue
	 * 
	 * @param issueKey
	 *            : issue key
	 * @param statusName
	 *            : has to be a preexisiting status name, eg. provided by
	 *            getStatusNames(..)
	 * @return returns true if the change has been processed successfully
	 */

	public static boolean changeStatus(final String issueKey, final String statusName) {
		final MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
		final JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issue);

		final Status nextStatus = JiraObjectGetter.getStatusByName(issue.getProjectObject().getKey(), statusName);

		if (issue == null || jiraWorkflow == null || nextStatus == null) {
			log.debug("changeStatus: issue, jiraworklfow, or nextStatus is null.");
			return false;
		} else {
			ComponentAccessor.getWorkflowManager().migrateIssueToWorkflow(issue, jiraWorkflow, nextStatus);
			log.info("changeStatus: successfully changed current status of an issue to next status.");
			return true;
		}
	}

}
