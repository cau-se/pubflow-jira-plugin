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

public class JiraObjectGetter {

	private static Logger log = LoggerFactory.getLogger(JiraObjectGetter.class);

	/**
	 * @author abar
	 * 
	 */
	static FieldScreen findFieldScreenByName(String name) {
		final FieldScreenManager fieldScreenManager = ComponentAccessor.getFieldScreenManager();
		Collection<FieldScreen> fieldScreens = fieldScreenManager.getFieldScreens();

		for (FieldScreen fieldScreen : fieldScreens) {
			if (fieldScreen.getName().equals(name)) {
				return fieldScreen;
			}
		}

		return null;
	}

	/**
	 * @author abar
	 * @param issueTypeName:
	 *            The issue type we want to look up
	 * @return the issueType we looked for (null if it does not exist)
	 */
	public static IssueType getIssueTypeByName(String issueTypeName) {
	    IssueType issueType = null;
	    
	    Collection<IssueType> issueTypes = ComponentAccessor.getConstantsManager().getAllIssueTypeObjects();
	    
	    for(IssueType tempIssueType : issueTypes) {
	      if(tempIssueType.getName().equals(issueTypeName)) {
	        return tempIssueType;
	      }
	    }
	    
	    return issueType;
	}
	
	public static String getIssueTypeNamebyJiraKey(String jiraKey){
		return getIssueByJiraKey(jiraKey).getIssueType().getName();
	}

	/**
	 * @param id
	 * @return
	 * @throws GenericEntityException
	 */
	public static boolean lookupIssue(String name) throws GenericEntityException {
		long projectId = ComponentAccessor.getProjectManager().getProjectByCurrentKey("PUB").getId();
		Collection<Long> issueIds = ComponentAccessor.getIssueManager().getIssueIdsForProject(projectId);
		List<Issue> issues = ComponentAccessor.getIssueManager().getIssueObjects(issueIds);

		log.info("lookupIssue - name : " + name);

		for (Issue issue : issues) {
			if (issue.getSummary().equals(name)) {
				log.info("lookupIssue - return true");
				return true;
			}
		}

		log.info("lookupIssue - return false");
		return false;

	}

	/**
	 * @param id
	 * @return
	 */
	public static String getIssueKeyById(long id) {
		String key = ComponentAccessor.getIssueManager().getIssueObject(id).getKey();
		log.info("getIssueKeyById - id : " + id);
		log.info("getIssueKeyById - return " + key);

		return key;
	}

	public static List<String> getAllIssueSummariesBySummaryContains(String snippet) throws GenericEntityException {
		long projectId = ComponentAccessor.getProjectManager().getProjectByCurrentKey("PUB").getId();
		Collection<Long> issueIds = ComponentAccessor.getIssueManager().getIssueIdsForProject(projectId);
		List<Issue> issues = ComponentAccessor.getIssueManager().getIssueObjects(issueIds);
		log.info("getAllIssueSummariesBySummaryContains - snippet : " + snippet);

		List<String> resultList = new LinkedList<String>();
		for (Issue issue : issues) {
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
	public static long getIssueIdByKey(String key) {
		long id = ComponentAccessor.getIssueManager().getIssueObject(key).getId();
		log.info("getIssueIdByKey - id : " + id);
		log.info("getIssueIdByKey - return " + key);

		return id;
	}

	/**
	 * @param projectKey
	 * @param statusName
	 * @return
	 */
	public static Status getStatusByName(String projectKey, String statusName) {
		Collection<Status> statuses = JiraManagerPlugin.getStatusManager().getStatuses();
		log.info("getStatusByName - projectKey : " + projectKey);
		log.info("getStatusByName - statusName : " + statusName);
		log.info("getStatusByName - statuses.size : " + statuses.size());

		for (Status statusItem : statuses) {
			if (statusItem != null) {
				
				log.info("getStatusByName - status iteration 1:" + Arrays.toString(statusItem.getName().getBytes()));
				log.info("getStatusByName - status iteration 2: " + Arrays.toString(statusName.getBytes()));
				
				if (statusItem.getName().equalsIgnoreCase(statusName)) {
					return statusItem;
				}
			}
		}
		log.debug("getStatusByName: no status with name " + statusName + " was found.");
		return null;
	}

	/**
	 * Get available status names
	 * 
	 * @param projectKey
	 *            : the projects key
	 * @return returns a string array of all available status names
	 */

	public static List<String> getStatusNames(String projectKey) {
		List<Status> status = ComponentAccessor.getWorkflowManager()
				.getWorkflow(projectKey).getLinkedStatusObjects();
		List<String> result = new ArrayList<String>();

		for (Status statusItem : status) {
			result.add(statusItem.getName());
		}

		return result;
	}

	public static ApplicationUser getUserByName(String userName) {
		return ComponentAccessor.getUserManager().getUserByName(userName);
	}

	/**
	   * searches for an issue type by its name quite expensive
	   * 
	   * @param name
	   *          : the issue type's name
	   * 
	   * @return returns the issue type id returns null if no or more than one issue type with the
	   *         provided name has been found
	   * 
	   */

	  public static IssueType findIssueTypeByName(Project project, String name) {
	    log.debug("findIssueTypeByName - name : " + name);
	    Collection<IssueType> issueTypes = project.getIssueTypes();

	    for (IssueType issueType : issueTypes) {
	      if (issueType.getName().equals(name)) {
	        return issueType;
	      }
	    }
	    
	    log.debug("findIssueTypeByName: no issueType named "+name+" was found.");
	    return null;
	  }
	

	/**
	 * searches for an issue type by its name quite expensive
	 * 
	 * @param name
	 *            : the issue type's name
	 * @return returns the issue type id returns null if no or more than one
	 *         issue type with the provided name has been found
	 */

	public static IssueType findIssueTypeByName(String name) {
		int counter = 0;
		IssueType result = null;

		log.info("findIssueTypeByName - name : " + name);

		// iterate through all available issue types and check for equality of
		// names
		for (IssueType it : JiraManagerPlugin.getIssueTypeManager().getIssueTypes()) {
			if (it.getName().equals(name)) {
				counter++;
				result = it;
			}
		}

		if (counter == 1) {
			log.info("findIssueTypeByName - return");
			return result;
		} else {
			log.info("findIssueTypeByName - return null " + counter);
			return null;
		}
	}
	
	/**
	 * Looks up the issue to the given key.
	 * 
	 * @param issueKey
	 * @return
	 */
	public static Issue getIssueByJiraKey(String issueKey) {
		return ComponentAccessor.getIssueManager().getIssueObject(issueKey);
	}

}
