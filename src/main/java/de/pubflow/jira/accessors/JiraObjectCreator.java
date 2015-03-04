package de.pubflow.jira.accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
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
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.AssignableWorkflowScheme.Builder;
import com.atlassian.jira.workflow.JiraWorkflow;
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
	 * Creates a new Jira project
	 * 
	 * @param projectName : the name of the new project
	 * @param projectKey : the project's key
	 * @throws Exception 
	 */
	
	public static void createProject(String projectName, String projectKey, ApplicationUser user, boolean kill) throws Exception{
		log.info("createProject - projectName : " + projectName + " / projectKey : " + projectKey + " / kill : " + kill);

		if (user != null) {
			log.info("createProject - user : " + user.getUsername());
		} else {
			log.error("createProject - user null");
			throw new Exception("User is null");
		}

		if (projectKey.length() > 4) {
			throw new Exception("error: project key length > 4 ! ");
		}

		// if kill is set ALL issue types will be deleted
		if (kill) {
			for (IssueType it :JiraManagerPlugin.issueTypeManager.getIssueTypes()) {
				try {
					JiraManagerPlugin.issueTypeManager.removeIssueType(it.getId(), null);
				} catch (Exception e) {
					System.out.println("Unable to delete IssueType " + it.getName());
				}
			}	
		}

		//create a list of project contexts for which the custom field needs to be available
		List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
		contexts.add(GlobalIssueContext.getInstance());

		Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey);

		if (project == null) {

			project = ComponentAccessor.getProjectManager().createProject(projectName, projectKey, "", user.getUsername(), "", 0L);
			ComponentAccessor.getPermissionSchemeManager().addDefaultSchemeToProject(project);
		}
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
	
	public static String createIssue(String projectKey, String issueTypeName, String summary, String description, String reporter, ApplicationUser user, Map<String, String> parameters) throws Exception {
	
		if (user == null) {
			log.error("newIssue - user null");
			throw new Exception("User is null");
		}

		log.info("newIssue - user : " + user.getUsername() + " / projectKey : " + projectKey + " / workflowName : " + issueTypeName + " / summary : " + summary + " / description : " + description + " / reporter : " + reporter);
	
		MutableIssue mutableIssue = createNewMutableIssue(projectKey, summary, description, reporter, user, issueTypeName);

		for (Entry<String, String> entry : parameters.entrySet()) {
			CustomField customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(entry.getKey());

			if (customField != null) {
				if (entry.getValue() != null) {
					mutableIssue.setCustomFieldValue(customField, entry.getValue());
				} else {
					mutableIssue.setCustomFieldValue(customField, "");
				}
				log.info("newIssue - custom field loop / entry.key : " + entry.getKey() + " / value : " + entry.getValue());
			} else {
				log.info("newIssue - custom field loop / customField search: entry.key : " + entry.getKey() + " null");
			}
		}

		//TODO should be fixed when there is createIssueObject for ApplicationUser 
		Issue issue = ComponentAccessor.getIssueManager().createIssueObject(ApplicationUsers.toDirectoryUser(user), mutableIssue);
		return issue.getKey();
	}

	/**
	 * Creates a new IssueType in Jira
	 * 
	 * @param projectKey : the projects key
	 * @param issueTypeName :
	 * @param user :  
	 * @param workflowXMLString :
	 * @param customFields : list of custom fields (name : default value)
	 * conditions : 
	 * @return returns the issue type id
	 * @throws Exception 
	 */

	@SuppressWarnings("unchecked")
	public static String createIssueType(String projectKey, String issueTypeName, ApplicationUser user, String workflowXMLString, LinkedList<CustomFieldDefinition> customFields, List<ConditionDefinition> conditions) throws Exception{
		LinkedList<String> statuses = JiraManagerPlugin.getSteps(workflowXMLString);

		//Add Workflow
		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issueTypeName + Appendix.WORKFLOW.getName());

		if (jiraWorkflow == null) {
			log.info("newIssueType - creating jiraWorkflow : " + issueTypeName);
			jiraWorkflow = JiraObjectManipulator.addWorkflow(issueTypeName, workflowXMLString, user);
		}

		log.info("newIssueType - found jiraWorkflow : " + jiraWorkflow.getName());

		//Add statuses
		Map<String, String> statusMap = new HashMap<String, String>();

		for (String status : statuses) {
			Status statusName = JiraObjectGetter.getStatusByName(projectKey, status);

			if (statusName == null) {
				log.info("newIssueType - creating status" + status);
				statusName = JiraManagerPlugin.statusManager.createStatus(status , "", "/images/icons/statuses/generic.png");
			}

			log.info("newIssueType - found status : " + statusName.getId() + " / " + statusName.getName());

			statusMap.put(status, statusName.getId());

		}		

		//Map Steps to Statuses
		for (StepDescriptor step : (List<StepDescriptor>)jiraWorkflow.getDescriptor().getSteps()) {

			log.info("newIssueType - step : " + step.getId() + " / " + step.getName());

			String stepName = step.getName();
			String id = "";

			if (statuses.contains(stepName)) {
				id = statusMap.get(stepName);
				log.info("newIssueType - step : workflow contains step " + stepName);

			}

			for (Entry<String, String> entry : (Set<Entry<String, String>>) step.getMetaAttributes().entrySet()) {
				if (entry.getKey().equals("jira.status.id")) {
					step.getMetaAttributes().put("jira.status.id", id);
					log.info("newIssueType - setting jira.status.id : " + id + " in step");

				}
			}

			//ComponentAccessor.getWorkflowManager().updateWorkflow(issueTypeName + WORKFLOW_APPENDIX, jiraWorkflow);
			log.info("newIssueType - updating workflow " + jiraWorkflow.getName() + " / user : " + user.getName());
			ComponentAccessor.getWorkflowManager().updateWorkflow(user, jiraWorkflow);

		}

		//check if issue type already exists
		IssueType issueType = JiraObjectGetter.findIssueTypeByName(issueTypeName + Appendix.ISSUETYPE.getName());

		if (issueType == null) {
			log.info("newIssueType - creating issuetype " + issueTypeName + Appendix.ISSUETYPE.getName());

			//create new issue type
			issueType = JiraManagerPlugin.issueTypeManager.createIssueType(issueTypeName + Appendix.ISSUETYPE.getName(), "", "/images/icons/ico_epic.png");
			//issueTypes.add(issueType);
			issueType.getPropertySet().setBoolean("PubFlow", true);

			//prepare list of project's issue types
			List<GenericValue> issueTypesGenericValue = new ArrayList<GenericValue>();
			issueTypesGenericValue.add(issueType.getGenericValue());

			//create a list of project contexts for which the custom field needs to be available
			List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
			contexts.add(GlobalIssueContext.getInstance());

			//generate custom fields
			for (CustomFieldDefinition e : customFields) {

				//check if custom field already exists
				CustomField customFieldObject = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(e.getName());

				if (customFieldObject == null) {
					log.info("newIssueType - creating customField " + e.getName());

					//create custom field
					customFieldObject = ComponentAccessor.getCustomFieldManager().createCustomField(e.getName(), e.getName() + "-CustomField for " + issueTypeName, 
							ComponentAccessor.getCustomFieldManager().getCustomFieldType(e.getType()),
							null, 
							contexts, issueTypesGenericValue);
				}
				log.info("newIssueType - found customField : " + e.getName() + " / type : " + e.getType());
			}


			//Map Screens to Transitions

			Map<String, LinkedList<CustomFieldDefinition>> availableActionFieldScreens = new HashMap<String, LinkedList<CustomFieldDefinition>>();

			for (CustomFieldDefinition customFieldDefinition : customFields) {
				for (String id : customFieldDefinition.getScreens()) {
					if (availableActionFieldScreens.get(id) == null) {
						LinkedList<CustomFieldDefinition> sameKeyDefs = new LinkedList<CustomFieldDefinition>();
						sameKeyDefs.add(customFieldDefinition);
						availableActionFieldScreens.put(id, sameKeyDefs);
					} else {
						availableActionFieldScreens.get(id).add(customFieldDefinition);
					}
					log.info("newIssueType - transition screen grouping /  id : " + id + " / name : " + customFieldDefinition.getName());
				}
			}

			FieldScreen fieldScreenCreate = null;
			FieldScreen fieldScreenView = null;
			FieldScreen fieldScreenEdit = null;

			if (!availableActionFieldScreens.containsKey("Create")) {
				log.info("newIssueType - transition screen /  create ActionCreate / name : " + issueTypeName + Appendix.FIELDSCREEN.getName());
				fieldScreenCreate = createHumbleFieldScreen(issueTypeName + Appendix.FIELDSCREEN.getName() + "ActionCreate");
			}

			if (!availableActionFieldScreens.containsKey("View")) {
				log.info("newIssueType - transition screen / create ActionView / name : " + issueTypeName + Appendix.FIELDSCREEN.getName());
				fieldScreenView = createHumbleFieldScreen(issueTypeName + Appendix.FIELDSCREEN.getName() + "ActionView");
			}

			if (!availableActionFieldScreens.containsKey("Edit")) {
				log.info("newIssueType - transition screen / create ActionEdit / name : " + issueTypeName + Appendix.FIELDSCREEN.getName());
				fieldScreenEdit = createHumbleFieldScreen(issueTypeName + Appendix.FIELDSCREEN.getName() + "ActionEdit");
			}

			for (Entry<String, LinkedList<CustomFieldDefinition>> e : availableActionFieldScreens.entrySet()) {
				List<String> customFieldIds = new LinkedList<String>(); 

				for (CustomFieldDefinition c : e.getValue()) {
					log.info("newIssueType - transition screen id / name : " + c.getName());
					String customfieldId = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(c.getName()).getId();

					if (customfieldId != null) {
						customFieldIds.add(customfieldId);
					} else {
						log.error("newIssueType - custom field is null / name : " + c.getName());
					}
				}

				log.info("newIssueType - transition screen id loops / fieldscreen.name : " + issueTypeName + Appendix.FIELDSCREEN.getName() + "Action" + e.getKey() + " creating");
				FieldScreen fieldScreen = new FieldScreenImpl(ComponentAccessor.getFieldScreenManager());
				fieldScreen.setName(issueTypeName + Appendix.FIELDSCREEN.getName() + "Action" + e.getKey());
				FieldScreenTab fieldScreenTab = fieldScreen.addTab("Job");
				fieldScreenTab.setPosition(0);

				for (String s : customFieldIds) {
					fieldScreenTab.addFieldScreenLayoutItem(s);
				}

				fieldScreenTab.addFieldScreenLayoutItem(ComponentAccessor.getFieldManager().getField(IssueFieldConstants.REPORTER).getId());
				fieldScreenTab.addFieldScreenLayoutItem(ComponentAccessor.getFieldManager().getField(IssueFieldConstants.SUMMARY).getId());

				if (e.getKey().equals("Create")) {
					fieldScreenCreate = fieldScreen;
				} else if (e.getKey().equals("Edit")) {
					fieldScreenEdit = fieldScreen;
				} else if (e.getKey().equals("View")) {
					fieldScreenView = fieldScreen;
				} else {
					log.info("newIssueType - transition screen id loops, manipulating action descriptor / id : " + e.getKey() + " / view: " + fieldScreen.getName() + " / meta, jira.fieldscreen.id : " + fieldScreen.getId());
					ActionDescriptor actionDescriptor = jiraWorkflow.getDescriptor().getAction(Integer.parseInt(e.getKey()));
					actionDescriptor.setView(fieldScreen.getName());
					actionDescriptor.getMetaAttributes().put("jira.fieldscreen.id", fieldScreen.getId());
				}
			}

			log.info("newIssueType - setting conditions");

			for (ConditionDefinition condition : conditions) {

				for (Integer id : condition.getTransitions()) {
					log.info("newIssueType - setting conditions / key : " + Arrays.toString(condition.getTransitions()) + " / action id : " + id);

					try {
						ActionDescriptor actionDescriptor = jiraWorkflow.getDescriptor().getAction(id);
						RestrictionDescriptor restrictionDescriptor = actionDescriptor.getRestriction();
						if (restrictionDescriptor == null) {
							restrictionDescriptor = new RestrictionDescriptor();
							actionDescriptor.setRestriction(restrictionDescriptor);
						}

						ConditionsDescriptor conditionsDescriptor = restrictionDescriptor.getConditionsDescriptor();
						if (conditionsDescriptor == null) {
							conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();
							restrictionDescriptor.setConditionsDescriptor(conditionsDescriptor);
						}
						conditionsDescriptor.setType("AND");

						ConditionDescriptor cd = DescriptorFactory.getFactory().createConditionDescriptor();
						cd.setType("class");
						cd.getArgs().put("class.name", condition.getType());

						if (condition.getParams() != null) {
							for (Entry<String, String> e : condition.getParams().entrySet()) {
								cd.getArgs().put(e.getKey(), e.getValue());
							}
						}
						
						List<ConditionDescriptor> listLonditions = conditionsDescriptor.getConditions();
						listLonditions.add(cd);	

					} catch (Exception e) {
						log.error("newIssueType - setting conditions / " + e.getClass().toString() + " - can't find action action ID " + id);
					}
				}
			}

			ComponentAccessor.getWorkflowManager().updateWorkflow(user, jiraWorkflow);

			//TODO: Exception handling!!!

			FieldScreenScheme fieldScreenScheme = null;
			try {
				fieldScreenScheme = createNewFieldScreenScheme(fieldScreenCreate, fieldScreenView, fieldScreenEdit, issueTypeName);
			} catch (Exception e1) {
				log.error("");
				e1.printStackTrace();
				throw e1;
			}

			//set default permission scheme 
			Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey);

			//check if issue type screen scheme already exists
			IssueTypeScreenScheme issueTypeScreenScheme = ComponentAccessor.getIssueTypeScreenSchemeManager().getIssueTypeScreenScheme(project);

			if (issueTypeScreenScheme == null) {
				//set default issue type screen scheme
				ComponentAccessor.getIssueTypeScreenSchemeManager().associateWithDefaultScheme(project);
				issueTypeScreenScheme = ComponentAccessor.getIssueTypeScreenSchemeManager().getIssueTypeScreenScheme(project);
			}

			//compose
			IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(ComponentAccessor.getIssueTypeScreenSchemeManager(), (GenericValue) null, JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getConstantsManager());
			issueTypeScreenSchemeEntity.setIssueTypeId(issueType.getId());
			issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenScheme);
			issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);

			FieldConfigScheme issueTypeScheme = ComponentAccessor.getIssueTypeSchemeManager().getConfigScheme(project);
			log.info("newIssueType - get ConfigScheme/IssueTypeScheme / project id : " + project.getId());

			if (!issueTypeScheme.getName().equals(projectKey + Appendix.ISSUETYPESCHEME.getName())) {
				log.info("newIssueType - create ConfigScheme/IssueTypeScheme / project id : " + project.getId());
				issueTypeScheme = ComponentAccessor.getIssueTypeSchemeManager().create(projectKey + Appendix.ISSUETYPESCHEME.getName(), "", null);
			}

			LinkedList<String> issueTypesIds = new LinkedList<String>();
			for (IssueType it : ComponentAccessor.getIssueTypeSchemeManager().getIssueTypesForProject(project)) {
				if (it.getPropertySet().getBoolean("PubFlow") == true) {
					log.info("newIssueType - add IssueTypes to scheme / issuetype id : " + it.getId());
					issueTypesIds.add(it.getId());					
				}
			}

			log.info("newIssueType - add IssueType to scheme / issuetype id : " + issueType.getId());
			issueTypesIds.add(issueType.getId());

			ComponentAccessor.getIssueTypeSchemeManager().update(issueTypeScheme, issueTypesIds);
			ComponentAccessor.getFieldConfigSchemeManager().updateFieldConfigScheme(issueTypeScheme, contexts, ComponentAccessor.getFieldManager().getConfigurableField(IssueFieldConstants.ISSUE_TYPE));

			//add workflow scheme
			AssignableWorkflowScheme workflowScheme = ComponentAccessor.getWorkflowSchemeManager().getWorkflowSchemeObj("PubFlow" + Appendix.WORKFLOWSCHEME.getName());

			if (workflowScheme == null) {
				Builder builder = ComponentAccessor.getWorkflowSchemeManager().assignableBuilder();
				builder.setName("PubFlow" + Appendix.WORKFLOWSCHEME.getName());
				builder.setDescription("");
				workflowScheme = builder.build();
				ComponentAccessor.getWorkflowSchemeManager().createScheme(workflowScheme);
			}

			//TODO Should be fixed when Atlassian offers addWorkflowToScheme for workflow objects (approx. in 1000 years) 
			GenericValue workflowSchemeGeneric = ComponentAccessor.getWorkflowSchemeManager().getScheme("PubFlow" + Appendix.WORKFLOWSCHEME.getName());

			ComponentAccessor.getWorkflowSchemeManager().addWorkflowToScheme(workflowSchemeGeneric, issueTypeName + Appendix.WORKFLOW.getName(), issueType.getId());

			ComponentAccessor.getWorkflowSchemeManager().addWorkflowToScheme(workflowSchemeGeneric, "jira", issueType.getId());
			ComponentAccessor.getWorkflowSchemeManager().addSchemeToProject(project.getGenericValue(), workflowSchemeGeneric);	

		}



		log.info("newIssueType - return issueType " + issueType.getName());
		return issueType.getId();
	}

	private static FieldScreen createHumbleFieldScreen(String name) {
		log.info("generateHumbleFieldScreen - name : " + name);

		FieldScreen fieldScreen = new FieldScreenImpl(ComponentAccessor.getFieldScreenManager());
		fieldScreen.setName(name);
		FieldScreenTab fieldScreenTab = fieldScreen.addTab("Job");
		fieldScreenTab.setPosition(0);
		fieldScreenTab.addFieldScreenLayoutItem(ComponentAccessor.getFieldManager().getField(IssueFieldConstants.REPORTER).getId());
		fieldScreenTab.addFieldScreenLayoutItem(ComponentAccessor.getFieldManager().getField(IssueFieldConstants.SUMMARY).getId());

		return fieldScreen;
	}

	private static FieldScreenScheme createNewFieldScreenScheme(FieldScreen fieldScreenCreate, FieldScreen fieldScreenEdit,FieldScreen fieldScreenView, String fieldScreenSchemeName) throws Exception {
		log.info("generateNewFieldScreenScheme - fieldScreenSchemeName : " + fieldScreenSchemeName);

		if (fieldScreenCreate == null || fieldScreenEdit == null || fieldScreenView == null) {
			throw new Exception("One or more field screens are null");
		}

		FieldScreenSchemeItem fieldScreenSchemeItemCreate = new FieldScreenSchemeItemImpl(JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemCreate.setIssueOperation(IssueOperations.CREATE_ISSUE_OPERATION);
		fieldScreenSchemeItemCreate.setFieldScreen(fieldScreenCreate);

		FieldScreenSchemeItem fieldScreenSchemeItemEdit = new FieldScreenSchemeItemImpl(JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemEdit.setIssueOperation(IssueOperations.EDIT_ISSUE_OPERATION);
		fieldScreenSchemeItemEdit.setFieldScreen(fieldScreenEdit);

		FieldScreenSchemeItem fieldScreenSchemeItemView = new FieldScreenSchemeItemImpl(JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemView.setIssueOperation(IssueOperations.VIEW_ISSUE_OPERATION);
		fieldScreenSchemeItemView.setFieldScreen(fieldScreenView);

		FieldScreenScheme fieldScreenScheme = new FieldScreenSchemeImpl(JiraManagerPlugin.fieldScreenSchemeManager, null);
		fieldScreenScheme.setName(fieldScreenSchemeName + Appendix.FIELDSCREENSCHEME.getName());
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemView);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemEdit);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemCreate);

		return fieldScreenScheme;
	}

	private static MutableIssue createNewMutableIssue(String projectKey, String summary, String description, String reporter, ApplicationUser user, String issueTypeName) throws Exception {
		if (user == null) {
			log.error("generateNewMutableIssue - user null");
			throw new Exception("User is null");
		}
		
		log.info("generateNewMutableIssue - projectKey : " + projectKey + " / issueTypeName : " + issueTypeName + " / user : " + user.getName() + " / summary : " + summary +  " / description : " + description + " / reporter : " + reporter);

		MutableIssue newIssue = ComponentAccessor.getIssueFactory().getIssue();
		newIssue.setProjectObject(ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey));
		newIssue.setIssueTypeObject(JiraObjectGetter.findIssueTypeByName(issueTypeName + Appendix.ISSUETYPE.getName()));
		newIssue.setSummary(issueTypeName + " / " + summary);
		newIssue.setDescription(description);
		
		//TODO Set reporter
		//newIssue.setReporter(JiraManaPlugin.userManager.getUserByName(username));
		//newIssue.setReporter(JiraManaPlugin.userManager.getUserByName(reporter));
		return newIssue;
	}

	/**
	 * @param userName
	 * @param password
	 * @return
	 * @throws PermissionException
	 * @throws CreateException
	 * @throws AddException
	 */
	public static ApplicationUser createUser(String userName, String password) throws PermissionException, CreateException, AddException {
		ApplicationUser pubflowUser = ComponentAccessor.getUserUtil().getUserByName(userName);
		if (pubflowUser == null) {
			pubflowUser = ApplicationUsers.from(ComponentAccessor.getUserUtil().createUserNoNotification(userName, password, "", ""));	
		}
		return pubflowUser;
	}

	/**
	 * @param name
	 * @return
	 * @throws OperationNotPermittedException
	 * @throws InvalidGroupException
	 */
	public static Group createGroup(String name) throws OperationNotPermittedException, InvalidGroupException {
		Group group = ComponentAccessor.getGroupManager().getGroup(name);
		if (group == null) {
			group = ComponentAccessor.getGroupManager().createGroup(name);
		}

		return group;
	}

}
