package de.pubflow.jira.accessors;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.workflow.FactoryException;

import de.pubflow.jira.JiraManagerCore;
import de.pubflow.jira.JiraManagerPlugin;
import de.pubflow.jira.misc.Appendix;

public class JiraObjectManipulator {

	private static Logger log = Logger.getLogger(JiraManagerCore.class.getName());

	/**
	 * Appends an attachment to an issue
	 * 
	 * @param issueKey
	 * @param barray
	 * @param fileName
	 * @param type
	 * @return
	 */
	public static long addAttachment(String issueKey, byte [] barray, String fileName, String type, ApplicationUser user){
	
		try{
			MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
	
			//TODO : path os?
			String filePath = "/tmp/pubflow_tmp" + new BigInteger(130, JiraManagerPlugin.secureRandom).toString(32);
			FileOutputStream stream = new FileOutputStream(filePath);
	
			stream.write(barray);	
			stream.close();
			File barrayFile = new File(filePath); 
			ChangeItemBean attachment = ComponentAccessor.getAttachmentManager().createAttachment(new CreateAttachmentParamsBean(barrayFile, fileName+type, "text/plain", user, issue, false, false, null, new Timestamp(System.currentTimeMillis()), true));
	
			// TODO: no id?
			return 0l;
	
		}catch(Exception e){
			e.printStackTrace();
			return 0l;
		}
	}

	/**
	 * Adds a new comment to an issue
	 * 
	 * @param issueId 
	 * @param comment 
	 * @return returns if the new comment has been added successful
	 */
	
	public static Comment addIssueComment(String issueKey, String comment, ApplicationUser user){
		log.debug("addIssueComment - issueKey : " + issueKey + " / comment : " + comment);
	
		if(user != null){
			log.debug("addIssueComment - user : " + user.getName());
		}else{
			log.debug("addIssueComment - user : null");
		}
	
		Issue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
		Comment commentObject = ComponentAccessor.getCommentManager().create(issue, user, comment, false);
	
		if(commentObject != null){
			log.debug("addIssueComment - return commentObject");
			return commentObject;
		}else{
			log.debug("addIssueComment - return null");
			return null;
		}
	}

	/**
	 * @param issueTypeName
	 * @param workflowXML
	 */
	public static JiraWorkflow addWorkflow(String issueTypeName, String workflowXML, ApplicationUser user){
	
		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issueTypeName + Appendix.WORKFLOW);
	
		if(jiraWorkflow == null && workflowXML != null){
			try {
				jiraWorkflow = new ConfigurableJiraWorkflow(issueTypeName + Appendix.WORKFLOW, WorkflowUtil.convertXMLtoWorkflowDescriptor(workflowXML), ComponentAccessor.getWorkflowManager());
				//ComponentAccessor.getWorkflowManager().createWorkflow(projectKey + WORKFLOW_APPENDIX, jiraWorkflow);
				ComponentAccessor.getWorkflowManager().createWorkflow(user, jiraWorkflow);
			} catch (FactoryException e) {
				e.printStackTrace();
			}
		}
	
		return jiraWorkflow;
	}

	public static void addUserToGroup(ApplicationUser pubflowUser, String group) throws PermissionException, AddException{
		ComponentAccessor.getUserUtil().addUserToGroup(ComponentAccessor.getGroupManager().getGroupObject(group), ApplicationUsers.toDirectoryUser(pubflowUser));
	}

	public static void addUserToGroup(ApplicationUser pubflowUser, Group group) throws PermissionException, AddException{
		ComponentAccessor.getUserUtil().addUserToGroup(group, ApplicationUsers.toDirectoryUser(pubflowUser));
	}

	/**
	 * Changes the status of an issue
	 * 
	 * @param projectKey : the projects key
	 * @param issueId  : issue id
	 * @param statusName : has to be a preexisiting status name, eg. provided by getStatusNames(..) 
	 * @return returns true if the change has been processed successfully
	 */
	
	public static boolean changeStatus(String issueKey, String statusName){
		MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issue);
	
		Status nextStatus = JiraObjectGetter.getStatusByName(issue.getProjectObject().getKey(), statusName);
	
		if(issue == null || jiraWorkflow == null || nextStatus == null){
			return false;
		}else{
			ComponentAccessor.getWorkflowManager().migrateIssueToWorkflow(issue, jiraWorkflow, nextStatus);
			return true;
		}
	}

}
