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

public final class JiraObjectCreator {
	private static final Logger LOGGER = LoggerFactory.getLogger(JiraObjectCreator.class);

	private JiraObjectCreator() {
		
	}
	

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
	public static ApplicationUser createUser(final String userName, final String password)
			throws PermissionException, CreateException, AddException {
		final UserManager userManager = ComponentAccessor.getUserManager();
		ApplicationUser pubflowUser = userManager.getUserByName(userName);

		if (pubflowUser == null) {
			UserDetails pubflowUserData = new UserDetails(userName, userName);
			pubflowUserData = pubflowUserData.withPassword(password);
			pubflowUser = userManager.createUser(pubflowUserData);
			LOGGER.info("createUser: created new user " + pubflowUser.getUsername());
		} else {
			LOGGER.debug("createUser: user " + pubflowUser.getUsername() + " already exists");
		}
		return pubflowUser;
	}

	/**
	 * @author arl, abar
	 * 
	 */
	public static FieldScreen createActionScreen(final String name) {
		FieldScreen fieldScreenAction = JiraObjectGetter.findFieldScreenByName(name);

		if (fieldScreenAction == null) {
			fieldScreenAction = createFieldScreen(name);
			LOGGER.info("createActionScreen: created new ActionScreen " + fieldScreenAction.getName());
		} else {
			LOGGER.debug("createActionScreen: ActionScreen " + fieldScreenAction.getName() + " already exists.");
		}

		return fieldScreenAction;
	}

	/**
	 * @author abar
	 * @param name
	 *            : the name of the field screen that shall be created
	 * @return the created FieldScreen Object
	 */
	public static FieldScreen createFieldScreen(final String name) {
		final FieldManager fieldManager = ComponentAccessor.getFieldManager();
		final FieldScreen fieldScreen = new FieldScreenImpl(ComponentAccessor.getFieldScreenManager());
		fieldScreen.setName(name);
		final FieldScreenTab fieldScreenTab = fieldScreen.addTab("Job");
		fieldScreenTab.setPosition(0);
		fieldScreenTab.addFieldScreenLayoutItem(fieldManager.getField(IssueFieldConstants.REPORTER).getId());
		fieldScreenTab.addFieldScreenLayoutItem(fieldManager.getField(IssueFieldConstants.SUMMARY).getId());

		LOGGER.debug("createFieldScreen: created new FieldScreen " + fieldScreen.getName());
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
	public static FieldScreenScheme generateNewFieldScreenScheme(final FieldScreen fieldScreenCreate,
			final FieldScreen fieldScreenEdit, final FieldScreen fieldScreenView, final String fieldScreenSchemeName)
			throws Exception {
		final FieldScreenManager fieldScreenManager = ComponentAccessor.getFieldScreenManager();
		final JiraManagerPlugin JiraManagerPlugin = ComponentAccessor
				.getOSGiComponentInstanceOfType(JiraManagerPlugin.class);

		if (fieldScreenCreate == null || fieldScreenEdit == null || fieldScreenView == null) {
			throw new Exception("generateNewFieldScreenScheme: One or more field screens are null");
		}

		final FieldScreenSchemeItem fieldScreenSchemeItemCreate = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, fieldScreenManager);
		fieldScreenSchemeItemCreate.setIssueOperation(IssueOperations.CREATE_ISSUE_OPERATION);
		fieldScreenSchemeItemCreate.setFieldScreen(fieldScreenCreate);

		final FieldScreenSchemeItem fieldScreenSchemeItemEdit = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, fieldScreenManager);
		fieldScreenSchemeItemEdit.setIssueOperation(IssueOperations.EDIT_ISSUE_OPERATION);
		fieldScreenSchemeItemEdit.setFieldScreen(fieldScreenEdit);

		final FieldScreenSchemeItem fieldScreenSchemeItemView = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, fieldScreenManager);
		fieldScreenSchemeItemView.setIssueOperation(IssueOperations.VIEW_ISSUE_OPERATION);
		fieldScreenSchemeItemView.setFieldScreen(fieldScreenView);

		FieldScreenScheme fieldScreenScheme = null;
		final Collection<FieldScreenScheme> fieldScreenSchemes = JiraManagerPlugin.fieldScreenSchemeManager
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
	public static List<Long> createCustomFields(final List<CustomFieldDefinition> customFields, final Project project,
			final String issueTypeName) throws GenericEntityException {
		final IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();
		final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
		final Collection<IssueType> issueTypes = issueTypeSchemeManager.getIssueTypesForProject(project);
		final List<IssueType> issueTypesList = new ArrayList<IssueType>();

		issueTypesList.addAll(issueTypes);
		final List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
		contexts.add(GlobalIssueContext.getInstance());
		final List<Long> customFieldIds = new ArrayList<Long>();
		for (final CustomFieldDefinition customField : customFields) {

			// check if custom field already exists
			final Collection<CustomField> foundCustomFields = customFieldManager.getCustomFieldObjectsByName(customField.getName());

			if (foundCustomFields.isEmpty()) {
				LOGGER.debug("newIssueType - customField search : " + customField.getName() + " null, creating");

				// create custom field
				final CustomField newCustomFieldObject = customFieldManager.createCustomField(customField.getName(),
						customField.getName() + "-CustomField for " + issueTypeName,
						customFieldManager.getCustomFieldType(customField.getType()), null, contexts, issueTypesList);

				customFieldIds.add(newCustomFieldObject.getIdAsLong());
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
	public static FieldConfigScheme createIssueTypeScheme(final Project project) {
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
	public static IssueType createIssueType(final Project project, final String issueTypeName, final String workflowID)
			throws CreateException {
		IssueType issueType = JiraObjectGetter.findIssueTypeByName(project, issueTypeName);
		if (issueType == null) {
			// ((IssueTypeImpl)
			// issueType).getPropertySet().setString("workflowID", workflowID);
			issueType = ComponentAccessor.getConstantsManager().insertIssueType(issueTypeName, new Long(1), null,
					"Issue type for PubFlow", new Long(10300));
			LOGGER.info("createIssueType: create new issuteType " + issueType.getName());
		} else {
			LOGGER.debug("createIssueType: issueType " + issueType.getName() + " already exists");
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

	public static WorkflowScheme createWorkflowScheme(final String projectKey, final ApplicationUser user,
			final JiraWorkflow jiraWorkflow, final String issueTypeName) {
		final WorkflowSchemeManager workflowSchemeManger = ComponentAccessor.getWorkflowSchemeManager();
		AssignableWorkflowScheme workflowScheme = workflowSchemeManger.getWorkflowSchemeObj(projectKey);

		if (workflowScheme == null) {
			final Scheme scheme = workflowSchemeManger.createSchemeObject(projectKey,
					"Workflow scheme for the Pubflow project");
			workflowScheme = workflowSchemeManger.getWorkflowSchemeObj(scheme.getName()); // necessary
																							// intermediate
																							// step

			final AssignableWorkflowScheme.Builder workflowSchemeBuilder = workflowScheme.builder();
			final IssueType ocnIssueType = JiraObjectGetter.getIssueTypeByName(issueTypeName);

			workflowSchemeBuilder.setName(projectKey);
			workflowSchemeBuilder.setDescription("Workflow scheme for Pubflow.");
			// workflowSchemeBuilder.setDefaultWorkflow(jiraWorkflow.getName());
			workflowSchemeBuilder.setMapping(ocnIssueType.getId(), jiraWorkflow.getName());
			LOGGER.info("createWorkflowScheme: created new WorkflowScheme " + scheme.getName());
			return workflowSchemeManger.updateWorkflowScheme(workflowSchemeBuilder.build());
		} else {
			LOGGER.debug("createWorkflowScheme: WorkflowScheme " + workflowScheme.getName() + " already exists");
		}

		return workflowScheme;
	}

	@SuppressWarnings("unused")
	private static FieldScreen createHumbleFieldScreen(final String name) {
		LOGGER.info("generateHumbleFieldScreen - name : " + name);

		final FieldScreen fieldScreen = new FieldScreenImpl(ComponentAccessor.getFieldScreenManager());
		fieldScreen.setName(name);
		final FieldScreenTab fieldScreenTab = fieldScreen.addTab("Job");
		fieldScreenTab.setPosition(0);
		fieldScreenTab.addFieldScreenLayoutItem(
				ComponentAccessor.getFieldManager().getField(IssueFieldConstants.REPORTER).getId());
		fieldScreenTab.addFieldScreenLayoutItem(
				ComponentAccessor.getFieldManager().getField(IssueFieldConstants.SUMMARY).getId());

		return fieldScreen;
	}

	@SuppressWarnings("unused")
	private FieldScreenScheme createNewFieldScreenScheme(final FieldScreen fieldScreenCreate,
			final FieldScreen fieldScreenEdit, final FieldScreen fieldScreenView, final String fieldScreenSchemeName)
			throws Exception {
		LOGGER.info("createNewFieldScreenScheme - fieldScreenSchemeName : " + fieldScreenSchemeName);

		if (fieldScreenCreate == null || fieldScreenEdit == null || fieldScreenView == null) {
			throw new Exception("One or more field screens are null");
		}

		final FieldScreenSchemeItem fieldScreenSchemeItemCreate = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemCreate.setIssueOperation(IssueOperations.CREATE_ISSUE_OPERATION);
		fieldScreenSchemeItemCreate.setFieldScreen(fieldScreenCreate);

		final FieldScreenSchemeItem fieldScreenSchemeItemEdit = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemEdit.setIssueOperation(IssueOperations.EDIT_ISSUE_OPERATION);
		fieldScreenSchemeItemEdit.setFieldScreen(fieldScreenEdit);

		final FieldScreenSchemeItem fieldScreenSchemeItemView = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemView.setIssueOperation(IssueOperations.VIEW_ISSUE_OPERATION);
		fieldScreenSchemeItemView.setFieldScreen(fieldScreenView);

		final FieldScreenScheme fieldScreenScheme = new FieldScreenSchemeImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, null);
		fieldScreenScheme.setName(fieldScreenSchemeName);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemView);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemEdit);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemCreate);

		return fieldScreenScheme;
	}

	private static MutableIssue createNewMutableIssue(final String projectKey, final String summary,
			final String description, final ApplicationUser reporter, final ApplicationUser user,
			final String issueTypeName) throws Exception {
		if (user == null) {
			LOGGER.error("generateNewMutableIssue - user null");
			throw new Exception("User is null");
		}

		LOGGER.info("generateNewMutableIssue - projectKey : " + projectKey + " / issueTypeName : " + issueTypeName
				+ " / user : " + user.getName() + " / summary : " + summary + " / description : " + description
				+ " / reporter : " + reporter);

		final MutableIssue newIssue = ComponentAccessor.getIssueFactory().getIssue();
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
	public static Group createGroup(final String name) throws OperationNotPermittedException, InvalidGroupException {
		final GroupManager groupManager = ComponentAccessor.getGroupManager();
		Group group = groupManager.getGroup(name);
		if (group == null) {
			group = groupManager.createGroup(name);
			LOGGER.info("createGroup: created a new group " + group.getName());
		} else {
			LOGGER.debug("createGroup: group " + group.getName() + " already exists");
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
	public static Map<String, String> addStatuses(final String projectKey, final List<String> statuses) {
		final StatusManager statusManager = ComponentAccessor.getComponent(StatusManager.class);
		// final StatusCategoryManager statusManagerCategory =
		// ComponentAccessor.getComponent(StatusCategoryManager.class);
		// final int catId = 2;
		final Map<String, String> statusMap = new HashMap<String, String>();

		for (final String status : statuses) {
			Status tempStatus = JiraObjectGetter.getStatusByName(projectKey, status);

			if (tempStatus == null) {
				tempStatus = statusManager.createStatus(status, "", "/images/icons/statuses/generic.png");
				LOGGER.info(
						"addStatuses: status " + tempStatus.getName() + " was created with ID: " + tempStatus.getId());
			} else {
				LOGGER.debug("addStatuses: status " + tempStatus.getName() + " already exists with ID: "
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
	public static JiraWorkflow addWorkflow(final String projectKey, final String workflowXML,
			final ApplicationUser user, final String issueTypeName) {
		final String workflowName = issueTypeName;
		final WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
		JiraWorkflow jiraWorkflow = workflowManager.getWorkflow(workflowName);

		if (jiraWorkflow == null && workflowXML != null) {
			try {
				jiraWorkflow = new ConfigurableJiraWorkflow(workflowName,
						WorkflowUtil.convertXMLtoWorkflowDescriptor(workflowXML), workflowManager);
				workflowManager.createWorkflow(user, jiraWorkflow);
				LOGGER.info("addWorkflow: Successfully added a new workflow " + jiraWorkflow.getName());
			} catch (final FactoryException e) {
				LOGGER.info("Error during initialization  xml -> Jira workflow");
				e.printStackTrace();
			}
		} else {
			LOGGER.info("addWorkflow: Workflow " + jiraWorkflow.getName() + " already exists.");
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
	public static String createIssue(final String projectKey, final String issueTypeName, final String summary,
			final String description, final ApplicationUser reporter, final ApplicationUser user,
			final Map<String, String> parameters) throws Exception {

		// create
		final MutableIssue newIssue = createNewMutableIssue(projectKey, summary, description, reporter, user,
				issueTypeName);
		// add custom fields from parameter
		fillCustomFields(newIssue, parameters);
		// save and return id;
		final Issue issue = ComponentAccessor.getIssueManager().createIssueObject(user, newIssue);
		return issue.getKey();

	}

	/**
	 * Fills existing custom fields with parameters. Values for non-existing
	 * fields are not inserted.
	 * 
	 * @param issue
	 * @param parameters
	 */
	private static void fillCustomFields(final MutableIssue issue, final Map<String, String> parameters) {
		final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
		for (final String entryKey : parameters.keySet()) {
			final CustomField currentField = customFieldManager.getCustomFieldObject(entryKey);
			if (currentField != null) {
				issue.setCustomFieldValue(currentField, parameters.get(entryKey));
			}
		}
	}

}
