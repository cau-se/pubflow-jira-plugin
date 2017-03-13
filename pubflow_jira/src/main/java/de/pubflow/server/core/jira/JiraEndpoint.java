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
package de.pubflow.server.core.jira;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;

import de.pubflow.common.entity.JiraAttachment;
import de.pubflow.common.entity.JiraComment;
import de.pubflow.common.entity.JiraIssue;
import de.pubflow.jira.JiraManagerPlugin;
import de.pubflow.jira.accessors.JiraObjectCreator;
import de.pubflow.jira.accessors.JiraObjectGetter;
import de.pubflow.jira.accessors.JiraObjectManipulator;

public class JiraEndpoint {

	private static final Logger myLogger = LoggerFactory.getLogger(JiraEndpoint.class);

	/**
	 * Creates a new Issue in Jira
	 *
	 * @param issueTypeName
	 * @param summary
	 * @param description
	 * @param parameters
	 * @param reporter
	 * @return
	 */
	public static String createIssue(final String issueTypeName, final String summary, final String description,
			final Map<String, String> parameters, final String reporter) {
		try {
			final ApplicationUser pubflowUser = JiraObjectGetter.getUserByName("PubFlow");
			myLogger.info("Creating new issue with type: " + issueTypeName);
			return JiraObjectCreator.createIssue("PUB", issueTypeName, summary, description, pubflowUser, pubflowUser,
					parameters);
		} catch (final Exception e) {
			myLogger.warn("Failed to create new Issue through PubFlow", e);
			return null;
		}
	}

	public static List<String> getAllIssuesBySummaryContains(final String snippet) {
		try {
			return JiraObjectGetter.getAllIssueSummariesBySummaryContains(snippet);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static boolean lookupIssue(final String name) {
		try {
			return JiraObjectGetter.lookupIssue(name);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static String createIssue(final JiraIssue issue) {
		try {
			// return JiraObjectCreator.createIssue("PUB",
			// issue.getIssueTypeName(), issue.getSummary(),
			// issue.getDescription(), issue.getReporter(),
			// JiraManagerPlugin.user, issue.getParameters());

		} catch (final Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Creates a new IssueType in Jira
	 * 
	 * @param projectKey
	 *            : the projects key
	 * @param issueTypeName
	 *            :
	 * @param parameters
	 *            : list of custom fields (name : default value)
	 * @return returns the issue type id
	 */

	public static String createIssueType(final String projectKey, final String issueTypeName, final Map<String, String> parameters) {

		final String id = null;
		// id = JiraObjectCreator.createIssueType(projectKey, issueTypeName,
		// parameters);

		return id;
	}

	/**
	 * Changes the status of an issue
	 * 
	 * @param issueKey
	 *            : issue key
	 * @param statusName
	 *            : has to be a already exisiting status name, eg. provided by
	 *            getStatusNames(..)
	 * @return returns true if the change has been processed successfully
	 */

	public static boolean changeStatus(final String issueKey, final String statusName) {
		return JiraObjectManipulator.changeStatus(issueKey, statusName);
	}

	/**
	 * Adds a new comment to an issue
	 * 
	 * @param issueKey
	 * @param comment
	 * @return returns true if the new comment has been added successfully
	 */

	public static boolean addIssueComment(final String issueKey, final String comment) {
		if (JiraObjectManipulator.addIssueComment(issueKey, comment, JiraManagerPlugin.user) == null) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean addIssueComment(final JiraComment comment) {
		if (JiraObjectManipulator.addIssueComment(comment.getIssueKey(), comment.getText(),
				JiraManagerPlugin.user) == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get available status names
	 * 
	 * @param projectKey
	 *            : the projects key
	 * @return returns a string array of all available status names
	 */

	public static LinkedList<String> getStatusNames(final String projectKey) {
		final List<String> statusNames = JiraObjectGetter.getStatusNames(projectKey);
		final LinkedList<String> namesList = new LinkedList<String>();

		namesList.addAll(statusNames);

		return namesList;
	}

	/**
	 * Creates a new Jira project
	 * 
	 * @param projectName
	 *            : the name of the new project
	 * @param projectKey
	 *            : the project's key
	 * @param workflowXML
	 *            : the Jira workflow, can be null
	 * @param steps
	 *            : list of statuses (steps) provided by the assigned workflow
	 * 
	 * @return returns true if project has been created successfully
	 */

	public static boolean createProject(final String projectName, final String projectKey, final String workflowXML,
			final LinkedList<String> steps) {
		try {
			// JiraObjectCreator.createProject(projectName, projectKey,
			// JiraManagerPlugin.user, false);

			return true;
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * Appends a file to an issue
	 * 
	 * @param issueKey
	 * @param barray
	 * @param fileName
	 * @param type
	 */

	public static boolean addAttachment(final String issueKey, final byte[] barray, final String fileName, final String type) {

		JiraObjectManipulator.addAttachment(issueKey, barray, fileName, type, JiraManagerPlugin.user);

		return true;
	}

	public static boolean addAttachment(final JiraAttachment attachment) {
		JiraObjectManipulator.addAttachment(attachment.getIssueKey(), attachment.getData(), attachment.getFilename(),
				"", JiraManagerPlugin.user);

		return true;
	}

}