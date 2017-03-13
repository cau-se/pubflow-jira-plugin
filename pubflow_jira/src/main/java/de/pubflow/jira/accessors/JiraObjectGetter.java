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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

import de.pubflow.jira.JiraManagerPlugin;

public final class JiraObjectGetter {

	private static final Logger LOGGER = LoggerFactory.getLogger(JiraObjectGetter.class);

	private JiraObjectGetter() {

	}

	/**
	 * Find a Fieldscreen by its name.
	 * 
	 * @author abar
	 * 
	 * @param name
	 *            the name of a Fieldscreen to look for
	 */
	static FieldScreen findFieldScreenByName(final String name) {		
		final Collection<FieldScreen> fieldScreens = ComponentAccessor.getFieldScreenManager().getFieldScreens();

		for (final FieldScreen fieldScreen : fieldScreens) {
			if (fieldScreen.getName().equals(name)) {
				return fieldScreen;
			}
		}

		return null;
	}

	/**
	 * @author abar
	 * @param issueTypeName
	 *            The issue type we want to look up
	 * @return the issueType we looked for (null if it does not exist)
	 */
	public static IssueType getIssueTypeByName(final String issueTypeName) {
		final Collection<IssueType> issueTypes = ComponentAccessor.getConstantsManager().getAllIssueTypeObjects();

		for (final IssueType issueType : issueTypes) {
			if (issueType.getName().equals(issueTypeName)) {
				return issueType;
			}
		}

		return null;
	}

	public static String getIssueTypeNamebyJiraKey(final String jiraKey) {
		return getIssueByJiraKey(jiraKey).getIssueType().getName();
	}

	/**
	 * @param id
	 * @return
	 * @throws GenericEntityException
	 */
	public static boolean lookupIssue(final String name) throws GenericEntityException {
		final long projectId = ComponentAccessor.getProjectManager().getProjectByCurrentKey("PUB").getId();
		final Collection<Long> issueIds = ComponentAccessor.getIssueManager().getIssueIdsForProject(projectId);
		final List<Issue> issues = ComponentAccessor.getIssueManager().getIssueObjects(issueIds);

		LOGGER.info("lookupIssue - name : " + name);

		for (final Issue issue : issues) {
			if (issue.getSummary().equals(name)) {
				LOGGER.info("lookupIssue - return true");
				return true;
			}
		}

		LOGGER.info("lookupIssue - return false");
		return false;

	}

	/**
	 * @param issueID
	 *            the id of an issue
	 * 
	 * @return the key of an issue with the given id
	 */
	public static String getIssueKeyById(final long issueID) {
		final String key = ComponentAccessor.getIssueManager().getIssueObject(issueID).getKey();
		LOGGER.info("getIssueKeyById - id : " + issueID);
		LOGGER.info("getIssueKeyById - return " + key);

		return key;
	}

	/**
	 * Get all summaries containing a given snippet
	 * 
	 * @param snippet
	 *            the part a summary should contain
	 * @return all summaries that contain a given snippet
	 * @throws GenericEntityException
	 */
	public static List<String> getAllIssueSummariesBySummaryContains(final String snippet)
			throws GenericEntityException {
		final long projectId = ComponentAccessor.getProjectManager().getProjectByCurrentKey("PUB").getId();
		final Collection<Long> issueIds = ComponentAccessor.getIssueManager().getIssueIdsForProject(projectId);
		final List<Issue> issues = ComponentAccessor.getIssueManager().getIssueObjects(issueIds);
		LOGGER.info("getAllIssueSummariesBySummaryContains - snippet : " + snippet);

		final List<String> resultList = new LinkedList<String>();
		for (final Issue issue : issues) {
			if (issue.getSummary().contains(snippet)) {
				resultList.add(issue.getSummary());
			}
		}
		return resultList;
	}

	/**
	 * @param key
	 * @return
	 */
	public static long getIssueIdByKey(final String key) {
		final long issueID = ComponentAccessor.getIssueManager().getIssueObject(key).getId();
		LOGGER.info("getIssueIdByKey - id : " + issueID);
		LOGGER.info("getIssueIdByKey - return " + key);

		return issueID;
	}

	/**
	 * @param projectKey
	 * @param statusName
	 * @return
	 */
	public static Status getStatusByName(final String projectKey, final String statusName) {
		final Collection<Status> statuses = JiraManagerPlugin.statusManager.getStatuses();
		LOGGER.info("getStatusByName - projectKey : " + projectKey);
		LOGGER.info("getStatusByName - statusName : " + statusName);
		LOGGER.info("getStatusByName - statuses.size : " + statuses.size());

		for (final Status statusItem : statuses) {
			if (statusItem != null) {

				LOGGER.info("getStatusByName - status iteration 1:" + Arrays.toString(statusItem.getName().getBytes()));
				LOGGER.info("getStatusByName - status iteration 2: " + Arrays.toString(statusName.getBytes()));

				if (statusItem.getName().equalsIgnoreCase(statusName)) {
					return statusItem;
				}
			}
		}
		LOGGER.debug("getStatusByName: no status with name " + statusName + " was found.");
		return null;
	}

	/**
	 * Get available status names
	 * 
	 * @param projectKey
	 *            : the projects key
	 * @return returns a string array of all available status names
	 */

	public static List<String> getStatusNames(final String projectKey) {
		final List<Status> status = ComponentAccessor.getWorkflowManager().getWorkflow(projectKey)
				.getLinkedStatusObjects();
		final List<String> result = new ArrayList<String>();

		for (final Status statusItem : status) {
			result.add(statusItem.getName());
		}

		return result;
	}

	public static ApplicationUser getUserByName(final String userName) {
		return ComponentAccessor.getUserManager().getUserByName(userName);
	}

	/**
	 * searches for an issue type by its name quite expensive
	 * 
	 * @param name
	 *            : the issue type's name
	 * 
	 * @return returns the issue type id returns null if no or more than one
	 *         issue type with the provided name has been found
	 * 
	 */

	public static IssueType findIssueTypeByName(final Project project, final String name) {
		LOGGER.debug("findIssueTypeByName - name : " + name);
		final Collection<IssueType> issueTypes = project.getIssueTypes();

		for (final IssueType issueType : issueTypes) {
			if (issueType.getName().equals(name)) {
				return issueType;
			}
		}

		LOGGER.debug("findIssueTypeByName: no issueType named " + name + " was found.");
		return null;
	}

	/**
	 * Looks up the issue to the given key.
	 * 
	 * @param issueKey
	 * @return
	 */
	public static Issue getIssueByJiraKey(final String issueKey) {
		return ComponentAccessor.getIssueManager().getIssueObject(issueKey);
	}

}
