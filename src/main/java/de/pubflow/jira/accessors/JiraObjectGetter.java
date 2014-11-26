package de.pubflow.jira.accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;

import de.pubflow.jira.JiraManagerCore;
import de.pubflow.jira.JiraManagerPlugin;
import de.pubflow.jira.misc.Appendix;

public class JiraObjectGetter {

	private static Logger log = Logger.getLogger(JiraManagerCore.class.getName());
	
	public static String getIssueKeyById(long id){	
		String key = ComponentAccessor.getIssueManager().getIssueObject(id).getKey();
		log.debug("getIssueKeyById - id : " + id);
		log.debug("getIssueKeyById - key : " + key);
	
		return key;
	}

	public static long getIssueIdByKey(String key){
		long id = ComponentAccessor.getIssueManager().getIssueObject(key).getId();
		log.debug("getIssueIdByKey - id : " + id);
		log.debug("getIssueIdByKey - key : " + key);
	
		return id;
	}

	public static Status getStatusByName(String projectKey, String statusName){
		//List<Status> statuses = ComponentAccessor.getWorkflowManager().getWorkflow(projectKey + WORKFLOW_APPENDIX).getLinkedStatusObjects();
		Collection<Status> statuses = JiraManagerPlugin.statusManager.getStatuses();
	
		for(Status statusItem : statuses){
			if(statusItem != null){
				if(statusItem.getName().equals(statusName)){
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
	
	public static List<String> getStatusNames(String projectKey){
		List<Status> status = ComponentAccessor.getWorkflowManager().getWorkflow(projectKey + Appendix.WORKFLOW).getLinkedStatusObjects();
		List<String> result = new ArrayList<String>();
	
		for(Status statusItem : status){
			result.add(statusItem.getName());
		}
	
		return result;
	}

	public static ApplicationUser getUserByName(String userName){
		return ComponentAccessor.getUserUtil().getUserByName(userName);
	}

	/**
	 *  searches for an issue type by its name 
	 *	quite expensive
	 * 
	 * @param name : the issue type's name
	 * 
	 * @return returns the issue type id returns null if no or more than one issue type with the provided name has been found 
	 * 
	 */
	
	public static IssueType findIssueTypeByName(String name){
		int i = 0;
		IssueType result = null;
	
		log.debug("findIssueTypeByName - name : " + name);
	
		//iterate through all available issue types and check for equality of names
		for(IssueType it : JiraManagerPlugin.issueTypeManager.getIssueTypes()){
			if(it.getName().equals(name)){
				i++;
				result = it;
			}
		}
	
		if(i == 1){
			log.debug("findIssueTypeByName - return " + result);
			return result;
		}else{
			log.debug("findIssueTypeByName - return null " + i);
			return null;
		}
	}

}
