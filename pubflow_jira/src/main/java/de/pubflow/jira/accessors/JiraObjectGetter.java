package de.pubflow.jira.accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;

import de.pubflow.jira.JiraManagerPlugin;
import de.pubflow.jira.misc.Appendix;

public class JiraObjectGetter {

	private static Logger log = LoggerFactory.getLogger(JiraObjectGetter.class);

	/**
	 * @param id
	 * @return
	 * @throws GenericEntityException 
	 */
	public static boolean lookupIssue(String name) throws GenericEntityException  {	
		long projectId = ComponentAccessor.getProjectManager().getProjectByCurrentKey("PUB").getId();
		Collection<Long> issueIds = ComponentAccessor.getIssueManager().getIssueIdsForProject(projectId);
		List<Issue> issues = ComponentAccessor.getIssueManager().getIssueObjects(issueIds);

		log.info("lookupIssue - name : " + name);

		for(Issue issue : issues){
			if (issue.getSummary().equals(name)){
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

	public static List<String> getAllIssueSummariesBySummaryContains(String snippet) throws GenericEntityException{
		long projectId = ComponentAccessor.getProjectManager().getProjectByCurrentKey("PUB").getId();
		Collection<Long> issueIds = ComponentAccessor.getIssueManager().getIssueIdsForProject(projectId);
		List<Issue> issues = ComponentAccessor.getIssueManager().getIssueObjects(issueIds);
		log.info("getAllIssueSummariesBySummaryContains - snippet : " + snippet);

		List<String> resultList = new LinkedList<String>();
		for(Issue issue : issues){
			if(issue.getSummary().contains(snippet)){
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
		//List<Status> statuses = ComponentAccessor.getWorkflowManager().getWorkflow(projectKey + WORKFLOW_APPENDIX).getLinkedStatusObjects();
		Collection<Status> statuses = JiraManagerPlugin.statusManager.getStatuses();

		for (Status statusItem : statuses) {
			if (statusItem != null) {
				if (statusItem.getName().equals(statusName)) {
					return statusItem;
				}
			}
		}

		return null;
	}

	/**
	 * Get available status names
	 * 
	 * @param projectKey : the projects key
	 * @return returns a string array of all available status names
	 */

	public static List<String> getStatusNames(String projectKey) {
		List<Status> status = ComponentAccessor.getWorkflowManager().getWorkflow(projectKey + Appendix.WORKFLOW.getName()).getLinkedStatusObjects();
		List<String> result = new ArrayList<String>();

		for (Status statusItem : status) {
			result.add(statusItem.getName());
		}

		return result;
	}

	public static ApplicationUser getUserByName(String userName) {
		return ComponentAccessor.getUserUtil().getUserByName(userName);
	}

	/**
	 *  searches for an issue type by its name 
	 *	quite expensive
	 * 
	 * @param name : the issue type's name
	 * @return returns the issue type id returns null if no or more than one issue type with the provided name has been found 
	 */

	public static IssueType findIssueTypeByName(String name) {
		int counter = 0;
		IssueType result = null;

		log.info("findIssueTypeByName - name : " + name);

		//iterate through all available issue types and check for equality of names
		for (IssueType it : JiraManagerPlugin.issueTypeManager.getIssueTypes()) {
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

}