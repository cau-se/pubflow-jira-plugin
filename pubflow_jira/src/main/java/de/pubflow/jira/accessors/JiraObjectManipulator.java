package de.pubflow.jira.accessors;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
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
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.workflow.FactoryException;

import de.pubflow.jira.JiraManagerPlugin;
import de.pubflow.jira.misc.Appendix;

public class JiraObjectManipulator {



	/**
	 * Add a User to a group in Jira
	 * @author abar
	 * 
	 * @param name: the name of the group we want to creat
	 * 
	 * @return returns the created Group object
	 */
	public static Group createGroup(String name)
			throws OperationNotPermittedException, InvalidGroupException {
		Group group = ComponentAccessor.getGroupManager().getGroup(name);
		if (group == null) {
			group = ComponentAccessor.getGroupManager().createGroup(name);
		}

		return group;
	}

	private static Logger log = LoggerFactory.getLogger(JiraObjectManipulator.class);
	/**
	 * @author abar
	 * @param issueTypeScheme : the issue type Scheme we map to the project
	 */
	public static void addIssueTypeSchemeToProject(FieldConfigScheme issueTypeScheme,
			Project project) {
		final IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor
				.getIssueTypeSchemeManager();
		final FieldConfigSchemeManager fieldConfigSchemeManager = ComponentAccessor.getFieldConfigSchemeManager();
		final FieldManager fieldManager = ComponentAccessor.getFieldManager();

		Collection<Project> projects = new ArrayList<Project>();
		projects.add(project);

		if (issueTypeScheme != issueTypeSchemeManager.getDefaultIssueTypeScheme()) {
			List<Long> projectIds = new ArrayList<Long>();
			projectIds.add(project.getId());
			log.debug("projectIds: $projectIds");
			Long[] ids = new Long[projectIds.size()];

			List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(true,
					projectIds.toArray(ids), ComponentAccessor.getProjectManager());
			fieldConfigSchemeManager.updateFieldConfigScheme(issueTypeScheme,
					contexts, fieldManager.getConfigurableField(IssueFieldConstants.ISSUE_TYPE));
		}
	}



	/**
	 * Creates a new screen scheme for an issue type in Jira.
	 * @author abar
	 * @param project : the project which uses the issueType
	 * @param issueTypeScheme : the issue type scheme that will be used
	 * @param issueType : the issue type which will be used 
	 * @return The issue type screen scheme which was created
	 */ 
	public static IssueTypeScreenScheme addIssueTypeScreenSchemeToProject(Project project, FieldScreenScheme fieldScreenScheme, IssueType issueType) {

		final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager =  ComponentAccessor.getIssueTypeScreenSchemeManager();
		IssueTypeScreenScheme issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project);

		if(issueTypeScreenScheme == null){
			//set default issue type screen scheme
			issueTypeScreenSchemeManager.associateWithDefaultScheme(project);
			issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project);
		}

		//compose
		IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(issueTypeScreenSchemeManager, (GenericValue) null, JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getConstantsManager());
		issueTypeScreenSchemeEntity.setIssueTypeId(issueType.getId());
		issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenScheme);
		issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);

		return issueTypeScreenScheme;
	} 
	/**
	 * Add custom statuses to Jira
	 * @author abar
	 * @param statuses : a list of all statuses we want to add to our Jira configuration
	 * @param projectKey : the project we add the statuses to
	 * @return A Map of statuses and their ids
	 */ 
	public static Map<String, String> addStatuses(String projectKey, List<String> statuses) {
		Map<String, String> statusMap = new HashMap<String, String>();

		StatusManager statusManager = ComponentAccessor.getComponent(StatusManager.class);
		int catId = 2;
		StatusCategoryManager statusManagerCategory = ComponentAccessor.getComponent(StatusCategoryManager.class);

		for (String status : statuses) {
			Status tempStatus = JiraObjectGetter.getStatusByName(projectKey, status);

			if (tempStatus == null) {
				tempStatus = statusManager.createStatus(status, "",
						"/images/icons/status_open.gif", statusManagerCategory.getStatusCategory(new Long(catId)));
			}
			statusMap.put(status, tempStatus.getId());
		}
		return statusMap;
	}

	/**
	 * Maps the workflow scheme of a given workflow to a given project.
	 * @author abar
	 * @param workflow: the workflow we take to map its scheme to a project
	 * @param project: the project we add the given workflow scheme to
	 */  
	public static void addWorkflowToProject(WorkflowScheme workflowScheme, Project project) {
		Scheme scheme = ComponentAccessor.getWorkflowSchemeManager().getSchemeObject(workflowScheme.getId());
		if(scheme != null && project != null) {
			ComponentAccessor.getWorkflowSchemeManager().addSchemeToProject(project, scheme);
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
	public static long addAttachment(String issueKey, byte [] barray, String fileName, String type, ApplicationUser user){

		try {
			MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);

			//TODO : path os?
			String filePath = "/tmp/pubflow_tmp" + new BigInteger(130, JiraManagerPlugin.secureRandom).toString(32);
			FileOutputStream stream = new FileOutputStream(filePath);

			stream.write(barray);	
			stream.close();
			File barrayFile = new File(filePath); 
			ChangeItemBean attachment = ComponentAccessor.getAttachmentManager().createAttachment(new CreateAttachmentParamsBean(barrayFile, fileName + type, "text/plain", user, issue, false, false, null, new Timestamp(System.currentTimeMillis()), true));

			// TODO: no id?
			return 0L;

		} catch (Exception e) {
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

	public static Comment addIssueComment(String issueKey, String comment, ApplicationUser user){
		log.info("addIssueComment - issueKey : " + issueKey + " / comment : " + comment);

		if (user != null) {
			log.info("addIssueComment - user : " + user.getName());
		} else {
			log.info("addIssueComment - user : null");
		}

		Issue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
		Comment commentObject = ComponentAccessor.getCommentManager().create(issue, user, comment, false);

		if (commentObject != null) {
			log.info("addIssueComment - return commentObject");
			return commentObject;
		} else {
			log.info("addIssueComment - return null");
			return null;
		}
	}

	/**
	 * Creates a new workflow in Jira
	 * @author arl, abar
	 * @param projectKey
	 * @param workflowXML
	 * @return returns the created JiraWorkflow object
	 */
	public static JiraWorkflow addWorkflow(String projectKey, String workflowXML,
			ApplicationUser user) {

		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(projectKey + Appendix.WORKFLOW);

		if (jiraWorkflow == null && workflowXML != null) {
			try {
				jiraWorkflow = new ConfigurableJiraWorkflow(projectKey + Appendix.WORKFLOW,
						WorkflowUtil.convertXMLtoWorkflowDescriptor(workflowXML), ComponentAccessor.getWorkflowManager());
				ComponentAccessor.getWorkflowManager().createWorkflow(user, jiraWorkflow);
			} catch (FactoryException e) {
				e.printStackTrace();
			}
		}

		return jiraWorkflow;
	}

	  /**
	   * Add a User to a group in Jira
	   * @author abar
	   * 
	   * @param pubFlowUser : a user we want to a group
	   * @param pGroup: the name of a group we want to add an user to 
	   * 
	   */
	  public static void addUserToGroup(ApplicationUser pubflowUser, String pGroup)
	      throws PermissionException, AddException, GroupNotFoundException, UserNotFoundException,
	      OperationNotPermittedException, OperationFailedException {
	    Group group = ComponentAccessor.getGroupManager().getGroup(pGroup);
	    if (group != null) {
	    	ComponentAccessor.getGroupManager().addUserToGroup(pubflowUser, group);
	    }
	  }

	  /**
	   * Add a User to a group in Jira
	   * @author abar
	   * 
	   * @param pubFlowUser : a user we want to a group
	   * @param group: the group we want to add an user to 
	   * 
	   */
	  public static void addUserToGroup(ApplicationUser pubflowUser, Group group)
	      throws PermissionException, AddException, GroupNotFoundException, UserNotFoundException,
	      OperationNotPermittedException, OperationFailedException {
		  ComponentAccessor.getGroupManager().addUserToGroup(pubflowUser, group);
	  }

	/**
	 * Changes the status of an issue
	 * 
	 * @param issueKey  : issue key
	 * @param statusName : has to be a preexisiting status name, eg. provided by getStatusNames(..) 
	 * @return returns true if the change has been processed successfully
	 */

	public static boolean changeStatus(String issueKey, String statusName) {
		MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issue);

		Status nextStatus = JiraObjectGetter.getStatusByName(issue.getProjectObject().getKey(), statusName);

		if (issue == null || jiraWorkflow == null || nextStatus == null) {
			return false;
		} else {
			ComponentAccessor.getWorkflowManager().migrateIssueToWorkflow(issue, jiraWorkflow, nextStatus);
			return true;
		}
	}

}
