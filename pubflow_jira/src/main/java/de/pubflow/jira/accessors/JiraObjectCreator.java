package de.pubflow.jira.accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.user.UserDetails;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
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
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.AssignableWorkflowScheme.Builder;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import de.pubflow.jira.JiraManagerPlugin;
import de.pubflow.jira.misc.Appendix;
import de.pubflow.jira.misc.ConditionDefinition;
import de.pubflow.jira.misc.CustomFieldDefinition;

public class JiraObjectCreator {

	private static Logger log = LoggerFactory.getLogger(JiraObjectCreator.class);

	/**
	 * Create a new User in Jira
	 * 
	 * @author abar
	 * 
	 * @param userName : the name of the user we want to add
	 * @param passwort: the user's passwort
	 * 
	 * @return return the created ApplicationUser object
	 */
	public static ApplicationUser createUser(String userName, String password)
			throws PermissionException, CreateException, AddException {
		ApplicationUser pubflowUser = ComponentAccessor.getUserManager().getUserByName(userName);

		if (pubflowUser == null) {
			UserDetails pubflowUserData = new UserDetails(userName, userName);
			pubflowUserData = pubflowUserData.withPassword(password);
			pubflowUser = ComponentAccessor.getUserManager().createUser(pubflowUserData);
		}
		return pubflowUser;
	}

	/**
	 * @author arl, abar
	 * 
	 */
	public static FieldScreen createActionScreen(String name) {
		FieldScreen fieldScreenAction = JiraObjectGetter.findFieldScreenByName(name);

		if(fieldScreenAction == null){
			fieldScreenAction = createFieldScreen(name);
		}
		return fieldScreenAction;
	}

	/**
	 * @author abar
	 * @param name : the name of the field screen that shall be created
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

		return fieldScreen;
	}

	/**
	 * Creates a FieldScreenScheme in Jira.
	 * The name will be fieldScreenSchemeName + FIELDSCREENSCHEME_APPENDIX (e.g. "OCN_FIELDSCREEN_SCHEME", if "OCN" is the fieldScreenSchemeName)
	 * @author abar
	 * @param fieldScreenCreate : the FieldScreen for the creating process of a ticket
	 * @param fieldScreenEdit : the FieldScreen for the editing process of a ticket
	 * @param fieldScreenView : the FieldScreen for the view of a ticket
	 * @param fieldScreenSchemeName : the FieldScreen for the creating process of a ticket
	 * @return the FieldScreenScheme object
	 */
	public static FieldScreenScheme generateNewFieldScreenScheme(FieldScreen fieldScreenCreate,
			FieldScreen fieldScreenEdit, FieldScreen fieldScreenView, String fieldScreenSchemeName)
					throws Exception {
		final FieldScreenManager fieldScreenManager = ComponentAccessor.getFieldScreenManager();
		if (fieldScreenCreate == null || fieldScreenEdit == null || fieldScreenView == null) {
			throw new Exception("One or more field screens are null");
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
		Collection<FieldScreenScheme> fieldScreenSchemes = JiraManagerPlugin.fieldScreenSchemeManager.getFieldScreenSchemes(fieldScreenCreate);

		if(!fieldScreenSchemes.isEmpty()) {
			fieldScreenScheme = fieldScreenSchemes.iterator().next();
		}

		if(fieldScreenScheme == null) {
			fieldScreenScheme = new FieldScreenSchemeImpl(
					JiraManagerPlugin.fieldScreenSchemeManager, null);
			fieldScreenScheme.setName(fieldScreenSchemeName + Appendix.FIELDSCREENSCHEME);
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
	 * @param customFields: a list of CustomFieldDefinition (contains names, types and transition references)
	 * @param project: the project in which the fields will be used (custom fields will be mapped to issue types)
	 * @return a list of all ids of the created custom fields
	 */  
	public static List<Long> createCustomFields(List<CustomFieldDefinition> customFields, Project project) throws GenericEntityException {
		final IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();
		final Collection<IssueType> issueTypes = issueTypeSchemeManager.getIssueTypesForProject(project);
		final List<IssueType> issueTypesList = new ArrayList<IssueType>();

		issueTypesList.addAll(issueTypes);
		List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
		contexts.add(GlobalIssueContext.getInstance());
		List<Long> customFieldIds = new ArrayList<Long>();
		for(CustomFieldDefinition e : customFields){

			//check if custom field already exists
			CustomField customFieldObject = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(e.getName()
					+ "_" + "OCN");

			if(customFieldObject == null){
				log.debug("newIssueType - customField search : " + e.getName() + "_"
						+ "OCN" + " null, creating");

				//create custom field
				customFieldObject = ComponentAccessor.getCustomFieldManager().createCustomField(e.getName()
						+ "_" + "OCN", e.getName() + "-CustomField for " +
								"OCN", ComponentAccessor.getCustomFieldManager().getCustomFieldType(e.getType()),
								null,
								contexts, issueTypesList); 

				customFieldIds.add(customFieldObject.getIdAsLong());
			}
		}

		return customFieldIds;

	}

	/**
	 * Creates a new scheme for an issue type in Jira.
	 * The name will be issueTypeName + ISSUETYPENSCHEME_APPENDIX (e.g. "OCN_ISSUETYPE_SCHEME", if "OCN" is the issueTypeName)
	 * @author abar
	 * @param projectKey : the projectKey which uses the issueType 
	 * @param issueTypeName : the name of the issue type we add a scheme for
	 * @return The issue type scheme which was created
	 */ 
	public static FieldConfigScheme createIssueTypeScheme(String projectKey, String issueTypeName) {
		final IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();
		final Collection<String> issueTypes = ComponentAccessor.getConstantsManager().getAllIssueTypeIds();
		FieldConfigScheme schemeExisting = issueTypeSchemeManager
				.getConfigScheme(ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey));
		if (schemeExisting == issueTypeSchemeManager.getDefaultIssueTypeScheme()) {
			schemeExisting = issueTypeSchemeManager.create(projectKey + Appendix.ISSUETYPESCHEME,
					"IssueType Scheme for Pubflow", (List<String>) issueTypes);
		}

		return schemeExisting;
	} 
	/**
	 * Creates a new IssueType in Jira.
	 * The name will be projectKey + ISSUETYPE_APPENDIX (e.g. "OCN_ISSUETYPE", if "OCN" is the issueTypeName)
	 * @author abar
	 * @param issueTypeName : the issueTypeName we add to our Jira configuration
	 * @param project : the project which uses the issueType
	 * @return The issue type which was created
	 */ 
	public static IssueType createIssueType(Project project, String issueTypeName)
			throws CreateException {
		IssueType issueType = JiraObjectGetter.findIssueTypeByName(project, issueTypeName + Appendix.ISSUETYPE);
		if (issueType == null) {
			issueType = ComponentAccessor.getConstantsManager().insertIssueType(
					issueTypeName + Appendix.ISSUETYPE, new Long(1), null, "Issue type for PubFlow",
					new Long(10300));
		}

		return issueType;
	}


	/**
	 * Creates a new workflow scheme in Jira. 
	 * The name will be projectKey + WorkflowAppendix (e.g. "PUB_WorkflowScheme", if "PUB" is the projectKey)
	 * @author abar
	 * @param projectKey : the workflow-scheme's name
	 * @param user : Admin-User to add the workflow-scheme (ApplicationUser)
	 * @param jiraWorkflow : the default workflow we add to the scheme
	 * @return returns WorkflowScheme
	 */

	public static WorkflowScheme createWorkflowScheme(String projectKey, ApplicationUser user, JiraWorkflow jiraWorkflow) {
		AssignableWorkflowScheme workflowScheme = ComponentAccessor.getWorkflowSchemeManager().getWorkflowSchemeObj(projectKey + Appendix.WORKFLOWSCHEME);

		if(workflowScheme == null) {
			Scheme scheme = ComponentAccessor.getWorkflowSchemeManager().createSchemeObject(projectKey + Appendix.WORKFLOWSCHEME, "Workflow scheme for the Pubflow project");
			workflowScheme = ComponentAccessor.getWorkflowSchemeManager().getWorkflowSchemeObj(scheme.getName()); // necessary intermediate step
			AssignableWorkflowScheme.Builder workflowSchemeBuilder = workflowScheme.builder();
			IssueType ocnIssueType = JiraObjectGetter.getIssueTypeByName("OCN" + Appendix.ISSUETYPE);

			workflowSchemeBuilder.setName(projectKey + Appendix.WORKFLOWSCHEME);
			workflowSchemeBuilder.setDescription("Workflow scheme for Pubflow.");
			//	      workflowSchemeBuilder.setDefaultWorkflow(jiraWorkflow.getName());
			workflowSchemeBuilder.setMapping(ocnIssueType.getId(), jiraWorkflow.getName());

			return JiraManagerPlugin.workflowSchemeManager.updateWorkflowScheme(workflowSchemeBuilder.build());    
		}

		return workflowScheme;
	}

	/** 
	 * Creates a new Issue in Jira
	 * 
	 * @param projectKey : the projects key
	 * @param issueTypeName : determines which issue type should be used as issue scheme  
	 * @param summary : value for default field 'summary'
	 * @param user : 
	 * @param description : value for default field 'description'
	 * @param parameters : map of custom field values (name : value)
	 * @return returns the issue id 
	 **/

//	public static String createIssue(String projectKey, String issueTypeName, String summary, String description, String reporter, ApplicationUser user, Map<String, String> parameters) throws Exception {
//
//		if (user == null) {
//			log.error("newIssue - user null");
//			throw new Exception("User is null");
//		}
//
//		log.info("newIssue - user : " + user.getUsername() + " / projectKey : " + projectKey + " / workflowName : " + issueTypeName + " / summary : " + summary + " / description : " + description + " / reporter : " + reporter);
//
//		MutableIssue mutableIssue = createNewMutableIssue(projectKey, summary, description, reporter, user, issueTypeName);
//
//		for (Entry<String, String> entry : parameters.entrySet()) {
//			CustomField customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(entry.getKey());
//
//			if (customField != null) {
//				if (entry.getValue() != null) {
//					mutableIssue.setCustomFieldValue(customField, entry.getValue());
//				} else {
//					mutableIssue.setCustomFieldValue(customField, "");
//				}
//				log.info("newIssue - custom field loop / entry.key : " + entry.getKey() + " / value : " + entry.getValue());
//			} else {
//				log.info("newIssue - custom field loop / customField search: entry.key : " + entry.getKey() + " null");
//			}
//		}
//
//		//TODO should be fixed when there is createIssueObject for ApplicationUser 
//		Issue issue = ComponentAccessor.getIssueManager().createIssueObject(ApplicationUsers.toDirectoryUser(user), mutableIssue);
//		return issue.getKey();
//	}

	private static FieldScreen createHumbleFieldScreen(String name) {
		log.info("generateHumbleFieldScreen - name : " + name);

		FieldScreen fieldScreen = new FieldScreenImpl(
				ComponentAccessor.getFieldScreenManager());
		fieldScreen.setName(name);
		FieldScreenTab fieldScreenTab = fieldScreen.addTab("Job");
		fieldScreenTab.setPosition(0);
		fieldScreenTab.addFieldScreenLayoutItem(ComponentAccessor
				.getFieldManager().getField(IssueFieldConstants.REPORTER)
				.getId());
		fieldScreenTab.addFieldScreenLayoutItem(ComponentAccessor
				.getFieldManager().getField(IssueFieldConstants.SUMMARY)
				.getId());

		return fieldScreen;
	}

	private static FieldScreenScheme createNewFieldScreenScheme(
			FieldScreen fieldScreenCreate, FieldScreen fieldScreenEdit,
			FieldScreen fieldScreenView, String fieldScreenSchemeName)
					throws Exception {
		log.info("generateNewFieldScreenScheme - fieldScreenSchemeName : "
				+ fieldScreenSchemeName);

		if (fieldScreenCreate == null || fieldScreenEdit == null
				|| fieldScreenView == null) {
			throw new Exception("One or more field screens are null");
		}

		FieldScreenSchemeItem fieldScreenSchemeItemCreate = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager,
				ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemCreate
		.setIssueOperation(IssueOperations.CREATE_ISSUE_OPERATION);
		fieldScreenSchemeItemCreate.setFieldScreen(fieldScreenCreate);

		FieldScreenSchemeItem fieldScreenSchemeItemEdit = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager,
				ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemEdit
		.setIssueOperation(IssueOperations.EDIT_ISSUE_OPERATION);
		fieldScreenSchemeItemEdit.setFieldScreen(fieldScreenEdit);

		FieldScreenSchemeItem fieldScreenSchemeItemView = new FieldScreenSchemeItemImpl(
				JiraManagerPlugin.fieldScreenSchemeManager,
				ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemView
		.setIssueOperation(IssueOperations.VIEW_ISSUE_OPERATION);
		fieldScreenSchemeItemView.setFieldScreen(fieldScreenView);

		FieldScreenScheme fieldScreenScheme = new FieldScreenSchemeImpl(
				JiraManagerPlugin.fieldScreenSchemeManager, null);
		fieldScreenScheme.setName(fieldScreenSchemeName
				+ Appendix.FIELDSCREENSCHEME.getName());
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemView);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemEdit);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemCreate);

		return fieldScreenScheme;
	}

	private static MutableIssue createNewMutableIssue(String projectKey,
			String summary, String description, String reporter,
			ApplicationUser user, String issueTypeName) throws Exception {
		if (user == null) {
			log.error("generateNewMutableIssue - user null");
			throw new Exception("User is null");
		}

		log.info("generateNewMutableIssue - projectKey : " + projectKey
				+ " / issueTypeName : " + issueTypeName + " / user : "
				+ user.getName() + " / summary : " + summary
				+ " / description : " + description + " / reporter : "
				+ reporter);

		MutableIssue newIssue = ComponentAccessor.getIssueFactory().getIssue();
		newIssue.setProjectObject(ComponentAccessor.getProjectManager()
				.getProjectObjByKey(projectKey));
		newIssue.setIssueTypeObject(JiraObjectGetter
				.findIssueTypeByName(issueTypeName
						+ Appendix.ISSUETYPE.getName()));
		newIssue.setSummary(issueTypeName + " / " + summary);
		newIssue.setDescription(description);

		// TODO Set reporter
		// newIssue.setReporter(JiraManaPlugin.userManager.getUserByName(username));
		// newIssue.setReporter(JiraManaPlugin.userManager.getUserByName(reporter));
		return newIssue;
	}

	/**
	 * @param name
	 * @return
	 * @throws OperationNotPermittedException
	 * @throws InvalidGroupException
	 */
	public static Group createGroup(String name)
			throws OperationNotPermittedException, InvalidGroupException {
		Group group = ComponentAccessor.getGroupManager().getGroup(name);
		if (group == null) {
			group = ComponentAccessor.getGroupManager().createGroup(name);
		}

		return group;
	}
}
