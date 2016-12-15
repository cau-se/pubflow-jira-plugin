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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserDetails;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.workflow.FactoryException;

import de.pubflow.jira.JiraManagerPlugin;
import de.pubflow.jira.misc.CustomFieldDefinition;

public class JiraObjectCreator {

	private static Logger log = LoggerFactory.getLogger(JiraObjectCreator.class);

	/**
	 * Create a new User in Jira
	 * 
	 * @author abar
	 * 
	 * @param userName:
	 *            the name of the user we want to add
	 * @param passwort:
	 *            the user's passwort
	 * 
	 * @return return the created ApplicationUser object
	 */
	public static ApplicationUser createUser(String userName, String password)
			throws PermissionException, CreateException, AddException {
		final UserManager userManager = ComponentAccessor.getUserManager();
		ApplicationUser pubflowUser = userManager.getUserByName(userName);

		if (pubflowUser == null) {
			UserDetails pubflowUserData = new UserDetails(userName, userName);
			pubflowUserData = pubflowUserData.withPassword(password);
			pubflowUser = userManager.createUser(pubflowUserData);
			log.info("createUser: created new user " + pubflowUser.getUsername());
		} else {
			log.debug("createUser: user " + pubflowUser.getUsername() + " already exists");
		}
		return pubflowUser;
	}

	/**
	 * @author arl, abar
	 * 
	 */
	public static FieldScreen createActionScreen(String name) {
		FieldScreen fieldScreenAction = JiraObjectGetter.findFieldScreenByName(name);

		if (fieldScreenAction == null) {
			fieldScreenAction = createFieldScreen(name);
			log.info("createActionScreen: created new ActionScreen " + fieldScreenAction.getName());
		} else {
			log.debug("createActionScreen: ActionScreen " + fieldScreenAction.getName() + " already exists.");
		}

		return fieldScreenAction;
	}

	/**
	 * @author abar
	 * @param name
	 *            : the name of the field screen that shall be created
	 * @return the created FieldScreen Object
	 */
	public static FieldScreen createFieldScreen(String name) {
		final FieldManager fieldManager = ComponentAccessor.getFieldManager();
		FieldScreen fieldScreen = new FieldScreenImpl(ComponentAccessor.getFieldScreenManager());
		fieldScreen.setName(name);
		FieldScreenTab fieldScreenTab = fieldScreen.addTab("Job");
		fieldScreenTab.setPosition(0);
		fieldScreenTab.addFieldScreenLayoutItem(fieldManager.getField(IssueFieldConstants.REPORTER).getId());
		fieldScreenTab.addFieldScreenLayoutItem(fieldManager.getField(IssueFieldConstants.SUMMARY).getId());

		log.debug("createFieldScreen: created new FieldScreen " + fieldScreen.getName());
		return fieldScreen;
	}

	/**
	 * Creates a FieldScreenScheme in Jira. The name will be
	 * fieldScreenSchemeName + FIELDSCREENSCHEME_APPENDIX (e.g.
	 * "OCN_FIELDSCREEN_SCHEME", if "OCN" is the fieldScreenSchemeName)
	 * 
	 * @author abar
	 * @param fieldScreenCreate
	 *            : the FieldScreen for the creating process of a ticket
	 * @param fieldScreenEdit
	 *            : the FieldScreen for the editing process of a ticket
	 * @param fieldScreenView
	 *            : the FieldScreen for the view of a ticket
	 * @param fieldScreenSchemeName
	 *            : the FieldScreen for the creating process of a ticket
	 * @return the FieldScreenScheme object
	 */
	public static FieldScreenScheme generateNewFieldScreenScheme(FieldScreen fieldScreenCreate,
			FieldScreen fieldScreenEdit, FieldScreen fieldScreenView, String fieldScreenSchemeName) throws Exception {
		final FieldScreenManager fieldScreenManager = ComponentAccessor.getFieldScreenManager();
		if (fieldScreenCreate == null || fieldScreenEdit == null || fieldScreenView == null) {
			throw new Exception("generateNewFieldScreenScheme: One or more field screens are null");
		}

		FieldScreenSchemeItem fieldScreenSchemeItemCreate = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, fieldScreenManager);
		fieldScreenSchemeItemCreate.setIssueOperation(IssueOperations.CREATE_ISSUE_OPERATION);
		fieldScreenSchemeItemCreate.setFieldScreen(fieldScreenCreate);

		FieldScreenSchemeItem fieldScreenSchemeItemEdit = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, fieldScreenManager);
		fieldScreenSchemeItemEdit.setIssueOperation(IssueOperations.EDIT_ISSUE_OPERATION);
		fieldScreenSchemeItemEdit.setFieldScreen(fieldScreenEdit);

		FieldScreenSchemeItem fieldScreenSchemeItemView = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, fieldScreenManager);
		fieldScreenSchemeItemView.setIssueOperation(IssueOperations.VIEW_ISSUE_OPERATION);
		fieldScreenSchemeItemView.setFieldScreen(fieldScreenView);

		FieldScreenScheme fieldScreenScheme = null;
		Collection<FieldScreenScheme> fieldScreenSchemes = JiraManagerPlugin.fieldScreenSchemeManager
				.getFieldScreenSchemes(fieldScreenCreate);

		if (!fieldScreenSchemes.isEmpty()) {
			fieldScreenScheme = fieldScreenSchemes.iterator().next();
		}

		if (fieldScreenScheme == null) {
			fieldScreenScheme = new FieldScreenSchemeImpl(JiraManagerPlugin.fieldScreenSchemeManager, null);
			fieldScreenScheme.setName(fieldScreenSchemeName);
			fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemView);
			fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemEdit);
			fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemCreate);

		}

		return fieldScreenScheme;
	}

	/**
	 * Creates custom fields for Jira
	 * 
	 * @author abar
	 * @param customFields:
	 *            a list of CustomFieldDefinition (contains names, types and
	 *            transition references)
	 * @param project:
	 *            the project in which the fields will be used (custom fields
	 *            will be mapped to issue types)
	 * @return a list of all ids of the created custom fields
	 */
	public static List<Long> createCustomFields(List<CustomFieldDefinition> customFields, Project project,
			String issueTypeName) throws GenericEntityException {
		final IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();
		final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
		final Collection<IssueType> issueTypes = issueTypeSchemeManager.getIssueTypesForProject(project);
		final List<IssueType> issueTypesList = new ArrayList<IssueType>();

		issueTypesList.addAll(issueTypes);
		List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
		contexts.add(GlobalIssueContext.getInstance());
		List<Long> customFieldIds = new ArrayList<Long>();
		for (CustomFieldDefinition e : customFields) {

			// check if custom field already exists
			CustomField customFieldObject = customFieldManager
					.getCustomFieldObjectByName(e.getName());

			if (customFieldObject == null) {
				log.debug(
						"newIssueType - customField search : " + e.getName() + " null, creating");

				// create custom field
				customFieldObject = customFieldManager.createCustomField(e.getName(),
						e.getName() + "-CustomField for " + issueTypeName,
						customFieldManager.getCustomFieldType(e.getType()), null, contexts, issueTypesList);

				customFieldIds.add(customFieldObject.getIdAsLong());
			}
		}

		return customFieldIds;

	}

	/**
	 * Creates a new scheme for an issue type in Jira. The name will be
	 * issueTypeName + ISSUETYPENSCHEME_APPENDIX (e.g. "OCN_ISSUETYPE_SCHEME",
	 * if "OCN" is the issueTypeName)
	 * 
	 * @author abar
	 * @param projectKey
	 *            : the projectKey which uses the issueType
	 * @param issueTypeName
	 *            : the name of the issue type we add a scheme for
	 * @return The issue type scheme which was created
	 */
	public static FieldConfigScheme createIssueTypeScheme(Project project) {
		final IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();
		final Collection<String> issueTypes = ComponentAccessor.getConstantsManager().getAllIssueTypeIds();
		FieldConfigScheme schemeExisting = issueTypeSchemeManager.getConfigScheme(project);
		if (schemeExisting == issueTypeSchemeManager.getDefaultIssueTypeScheme()) {
			schemeExisting = issueTypeSchemeManager.create(project.getKey(),
					"IssueType Scheme for " + project.getName(), (List<String>) issueTypes);
		} else {
			issueTypeSchemeManager.update(schemeExisting, issueTypes);
		}

		return schemeExisting;
	}

	/**
	 * Creates a new IssueType in Jira. The name will be projectKey +
	 * ISSUETYPE_APPENDIX (e.g. "OCN_ISSUETYPE", if "OCN" is the issueTypeName)
	 * 
	 * @author abar
	 * @param issueTypeName
	 *            : the issueTypeName we add to our Jira configuration
	 * @param project
	 *            : the project which uses the issueType
	 * @return The issue type which was created
	 */
	public static IssueType createIssueType(Project project, String issueTypeName, String workflowID)
			throws CreateException {
		IssueType issueType = JiraObjectGetter.findIssueTypeByName(project, issueTypeName);
		if (issueType == null) {
			// ((IssueTypeImpl)
			// issueType).getPropertySet().setString("workflowID", workflowID);
			issueType = ComponentAccessor.getConstantsManager().insertIssueType(issueTypeName, new Long(1), null,
					"Issue type for PubFlow", new Long(10300));
			log.info("createIssueType: create new issuteType " + issueType.getName());
		} else {
			log.debug("createIssueType: issueType " + issueType.getName() + " already exists");
		}
		return issueType;
	}

	/**
	 * Creates a new workflow scheme in Jira. The name will be projectKey +
	 * WorkflowAppendix (e.g. "PUB_WorkflowScheme", if "PUB" is the projectKey)
	 * 
	 * @author abar
	 * @param projectKey
	 *            : the workflow-scheme's name
	 * @param user
	 *            : Admin-User to add the workflow-scheme (ApplicationUser)
	 * @param jiraWorkflow
	 *            : the default workflow we add to the scheme
	 * @return returns WorkflowScheme
	 */

	public static WorkflowScheme createWorkflowScheme(String projectKey, ApplicationUser user,
			JiraWorkflow jiraWorkflow, String issueTypeName) {
		final WorkflowSchemeManager workflowSchemeManger = ComponentAccessor.getWorkflowSchemeManager();
		AssignableWorkflowScheme workflowScheme = workflowSchemeManger
				.getWorkflowSchemeObj(projectKey);

		if (workflowScheme == null) {
			Scheme scheme = workflowSchemeManger.createSchemeObject(projectKey ,
					"Workflow scheme for the Pubflow project");
			workflowScheme = workflowSchemeManger.getWorkflowSchemeObj(scheme.getName()); // necessary
																							// intermediate
																							// step

			AssignableWorkflowScheme.Builder workflowSchemeBuilder = workflowScheme.builder();
			IssueType ocnIssueType = JiraObjectGetter.getIssueTypeByName(issueTypeName);

			workflowSchemeBuilder.setName(projectKey);
			workflowSchemeBuilder.setDescription("Workflow scheme for Pubflow.");
			// workflowSchemeBuilder.setDefaultWorkflow(jiraWorkflow.getName());
			workflowSchemeBuilder.setMapping(ocnIssueType.getId(), jiraWorkflow.getName());
			log.info("createWorkflowScheme: created new WorkflowScheme " + scheme.getName());
			return workflowSchemeManger.updateWorkflowScheme(workflowSchemeBuilder.build());
		} else {
			log.debug("createWorkflowScheme: WorkflowScheme " + workflowScheme.getName() + " already exists");
		}

		return workflowScheme;
	}

	@SuppressWarnings("unused")
	private static FieldScreen createHumbleFieldScreen(String name) {
		log.info("generateHumbleFieldScreen - name : " + name);

		FieldScreen fieldScreen = new FieldScreenImpl(ComponentAccessor.getFieldScreenManager());
		fieldScreen.setName(name);
		FieldScreenTab fieldScreenTab = fieldScreen.addTab("Job");
		fieldScreenTab.setPosition(0);
		fieldScreenTab.addFieldScreenLayoutItem(
				ComponentAccessor.getFieldManager().getField(IssueFieldConstants.REPORTER).getId());
		fieldScreenTab.addFieldScreenLayoutItem(
				ComponentAccessor.getFieldManager().getField(IssueFieldConstants.SUMMARY).getId());

		return fieldScreen;
	}

	@SuppressWarnings("unused")
	private static FieldScreenScheme createNewFieldScreenScheme(FieldScreen fieldScreenCreate,
			FieldScreen fieldScreenEdit, FieldScreen fieldScreenView, String fieldScreenSchemeName) throws Exception {
		log.info("createNewFieldScreenScheme - fieldScreenSchemeName : " + fieldScreenSchemeName);

		if (fieldScreenCreate == null || fieldScreenEdit == null || fieldScreenView == null) {
			throw new Exception("One or more field screens are null");
		}

		FieldScreenSchemeItem fieldScreenSchemeItemCreate = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemCreate.setIssueOperation(IssueOperations.CREATE_ISSUE_OPERATION);
		fieldScreenSchemeItemCreate.setFieldScreen(fieldScreenCreate);

		FieldScreenSchemeItem fieldScreenSchemeItemEdit = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemEdit.setIssueOperation(IssueOperations.EDIT_ISSUE_OPERATION);
		fieldScreenSchemeItemEdit.setFieldScreen(fieldScreenEdit);

		FieldScreenSchemeItem fieldScreenSchemeItemView = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemView.setIssueOperation(IssueOperations.VIEW_ISSUE_OPERATION);
		fieldScreenSchemeItemView.setFieldScreen(fieldScreenView);

		FieldScreenScheme fieldScreenScheme = new FieldScreenSchemeImpl(JiraManagerPlugin.fieldScreenSchemeManager,
				null);
		fieldScreenScheme.setName(fieldScreenSchemeName );
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemView);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemEdit);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemCreate);

		return fieldScreenScheme;
	}

	private static MutableIssue createNewMutableIssue(String projectKey, String summary, String description,
			ApplicationUser reporter, ApplicationUser user, String issueTypeName) throws Exception {
		if (user == null) {
			log.error("generateNewMutableIssue - user null");
			throw new Exception("User is null");
		}

		log.info("generateNewMutableIssue - projectKey : " + projectKey + " / issueTypeName : " + issueTypeName
				+ " / user : " + user.getName() + " / summary : " + summary + " / description : " + description
				+ " / reporter : " + reporter);

		MutableIssue newIssue = ComponentAccessor.getIssueFactory().getIssue();
		newIssue.setProjectObject(ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey));
		newIssue.setIssueTypeObject(JiraObjectGetter.findIssueTypeByName(issueTypeName));
		newIssue.setSummary(issueTypeName + " / " + summary);
		newIssue.setDescription(description);
		// Assignment for an entire group would be nicer
		newIssue.setAssignee(user);
		newIssue.setReporter(reporter);
		// newIssue.setReporter(JiraManaPlugin.userManager.getUserByName(reporter));
		return newIssue;
	}

	/**
	 * Add a User to a group in Jira
	 * 
	 * @author abar
	 * 
	 * @param name:
	 *            the name of the group we want to creat
	 * 
	 * @return returns the created Group object
	 */
	public static Group createGroup(String name) throws OperationNotPermittedException, InvalidGroupException {
		final GroupManager groupManager = ComponentAccessor.getGroupManager();
		Group group = groupManager.getGroup(name);
		if (group == null) {
			group = groupManager.createGroup(name);
			log.info("createGroup: created a new group " + group.getName());
		} else {
			log.debug("createGroup: group " + group.getName() + " already exists");
		}

		return group;
	}

	/**
	 * Add custom statuses to Jira
	 * 
	 * @author abar
	 * @param statuses
	 *            : a list of all statuses we want to add to our Jira
	 *            configuration
	 * @param projectKey
	 *            : the project we add the statuses to
	 * @return A Map of statuses and their ids
	 */
	public static Map<String, String> addStatuses(String projectKey, List<String> statuses) {
		final StatusManager statusManager = ComponentAccessor.getComponent(StatusManager.class);
		final StatusCategoryManager statusManagerCategory = ComponentAccessor.getComponent(StatusCategoryManager.class);
		final int catId = 2;
		Map<String, String> statusMap = new HashMap<String, String>();

		for (String status : statuses) {
			Status tempStatus = JiraObjectGetter.getStatusByName(projectKey, status);

			if (tempStatus == null) {
				tempStatus = statusManager.createStatus(status, "", "/images/icons/status_open.gif",
						statusManagerCategory.getStatusCategory(new Long(catId)));
				log.info("addStatuses: status " + tempStatus.getName() + " was created with ID: " + tempStatus.getId());
			} else {
				log.debug("addStatuses: status " + tempStatus.getName() + " already exists with ID: "
						+ tempStatus.getId());
			}

			statusMap.put(status, tempStatus.getId());

		}
		return statusMap;
	}

	/**
	 * Creates a new workflow in Jira
	 * 
	 * @author arl, abar
	 * @param projectKey
	 * @param workflowXML
	 * @return returns the created JiraWorkflow object
	 */
	public static JiraWorkflow addWorkflow(String projectKey, String workflowXML, ApplicationUser user,
			String issueTypeName) {
		String workflowName = issueTypeName;
		final WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
		JiraWorkflow jiraWorkflow = workflowManager.getWorkflow(workflowName);

		if (jiraWorkflow == null && workflowXML != null) {
			try {
				jiraWorkflow = new ConfigurableJiraWorkflow(workflowName,
						WorkflowUtil.convertXMLtoWorkflowDescriptor(workflowXML), workflowManager);
				workflowManager.createWorkflow(user, jiraWorkflow);
				log.info("addWorkflow: Successfully added a new workflow " + jiraWorkflow.getName());
			} catch (FactoryException e) {
				log.info("Error during initialization  xml -> Jira workflow");
				e.printStackTrace();
			}
		} else {
			log.info("addWorkflow: Workflow " + jiraWorkflow.getName() + " already exists.");
		}

		return jiraWorkflow;
	}

	/**
	 * Creates a new {@link Issue}, maps given values to {@link CustomField
	 * Customfields} and saves it.
	 * 
	 * @param projectKey
	 * @param issueTypeName
	 * @param summary
	 * @param description
	 * @param reporter
	 * @param user
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public static String createIssue(String projectKey, String issueTypeName, String summary, String description,
			ApplicationUser reporter, ApplicationUser user, HashMap<String, String> parameters) throws Exception {

		System.out.println(projectKey);
		System.out.println(issueTypeName);
		System.out.println(summary);
		System.out.println(description);
		System.out.println(reporter);
		System.out.println(user);
		// create
		MutableIssue newIssue = createNewMutableIssue(projectKey, summary, description, reporter, user, issueTypeName);
		// add custom fields from parameter
		fillCustomFields(newIssue, parameters);
		// save and return id;
		Issue issue = ComponentAccessor.getIssueManager().createIssueObject(user, newIssue);
		return issue.getKey();

	}

	/**
	 * Fills existing custom fields with parameters. Values for non-existing
	 * fields are not inserted.
	 * 
	 * @param issue
	 * @param parameters
	 */
	private static void fillCustomFields(MutableIssue issue, HashMap<String, String> parameters) {
		CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
		for (String entryKey : parameters.keySet()) {
			CustomField currentField = customFieldManager.getCustomFieldObject(entryKey);
			if (currentField != null) {
				issue.setCustomFieldValue(currentField, parameters.get(entryKey));
			}
		}
	}

}
