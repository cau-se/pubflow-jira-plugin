package de.pubflow.jira;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean;
import com.atlassian.jira.issue.comments.Comment;
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
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.action.util.workflow.WorkflowEditorTransitionConditionUtil;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.AssignableWorkflowScheme.Builder;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import de.pubflow.jira.misc.ConditionDefinition;
import de.pubflow.jira.misc.CustomFieldDefinition;
import de.pubflow.jira.misc.ConditionDefinition.ConditionDefinitionType;
import de.pubflow.jira.misc.CustomFieldDefinition.CustomFieldType;

/**
 * 
 *	@author arl
 *
 *	Jira Manager Core
 *	
 *	Still some work to do:
 *	- add some more features
 *	- proper exception handling
 *
 *	- prevent and fix usage of generic types in newIssueType(), initProject(...)! (deprecated)
 *	- loads of other things.... 
 *
 *
 *	Things to mind:
 *	Jira objects are updated automatically when setXy(...) is used
 *
 */
public class JiraManagerCore {

	public static List<CustomField> customFieldsCache = new LinkedList<CustomField>();
	//public static List<IssueType> issueTypes = new LinkedList<IssueType>();

	private static final String ISSUETYPE_APPENDIX = "";
	private static final String FIELDSCREEN_APPENDIX = "_FieldScreen";
	private static final String WORKFLOWSCHEME_APPENDIX = "_WorkflowScheme";
	private static final String WORKFLOW_APPENDIX = "_Workflow";
	private static final String FIELDSCREENSCHEME_APPENDIX = "_FieldScreenScheme";
	private static final String ISSUETYPENSCHEME_APPENDIX = "_IssueTypeScheme";
	private static Logger log = Logger.getLogger(JiraManagerCore.class.getName());

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
	 * Creates a new IssueType in Jira
	 * 
	 * @param projectKey : the projects key
	 * @param issueTypeName : 
	 * @param mapCondition 
	 * @param parameters : list of custom fields (name : default value)
	 * @return returns the issue type id
	 * @throws Exception 
	 */

	@SuppressWarnings("unchecked")
	public static String newIssueType(String projectKey, String issueTypeName, ApplicationUser user, String workflowXML, List<String> statuses, LinkedList<CustomFieldDefinition> customFields, List<ConditionDefinition> conditions) throws Exception{
		//Add Workflow
		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(projectKey + WORKFLOW_APPENDIX);

		if(jiraWorkflow == null){
			log.debug("newIssueType - jiraWorkflow search : " + projectKey + WORKFLOW_APPENDIX + " null, creating");
			jiraWorkflow = addWorkflow(projectKey, workflowXML, user);
		}

		log.debug("newIssueType - jiraWorkflow : " + jiraWorkflow.getName());

		//Add statuses
		Map<String, String> statusMap = new HashMap<String, String>();

		for(String status : statuses){
			Status s = getStatusByName(projectKey, status);

			if(s == null){
				log.debug("newIssueType - status search : " + projectKey + " / " + status + " null, creating");
				s = JiraManagerPlugin.statusManager.createStatus(status , "", "/images/icons/statuses/generic.png");
			}

			log.debug("newIssueType - status : " + s.getId() + " / " + s.getName());

			statusMap.put(status, s.getId());

		}		

		//Map Steps to Statuses
		for(StepDescriptor step : (List<StepDescriptor>)jiraWorkflow.getDescriptor().getSteps()){

			log.debug("newIssueType - step : " + step.getId() + " / " + step.getName());

			String stepName = step.getName();
			String id = "";

			if(statuses.contains(stepName)){
				id = statusMap.get(stepName);
				log.debug("newIssueType - step : workflow contains step " + stepName);

			}

			for(Entry<String, String> entry : (Set<Entry<String, String>>) step.getMetaAttributes().entrySet()){
				if(entry.getKey().equals("jira.status.id")){
					step.getMetaAttributes().put("jira.status.id", id);
					log.debug("newIssueType - setting jira.status.id : " + id + " in step");

				}
			}

			//ComponentAccessor.getWorkflowManager().updateWorkflow(projectKey + WORKFLOW_APPENDIX, jiraWorkflow);
			log.debug("newIssueType - updating workflow " + jiraWorkflow.getName() + " / user : " + user.getName());
			ComponentAccessor.getWorkflowManager().updateWorkflow(user, jiraWorkflow);

		}

		//check if issue type already exists
		IssueType issueType = findIssueTypeByName(issueTypeName + ISSUETYPE_APPENDIX);

		if(issueType == null){
			log.debug("newIssueType - issueType search : " + issueTypeName + ISSUETYPE_APPENDIX + " null, creating");

			//create new issue type
			issueType = JiraManagerPlugin.issueTypeManager.createIssueType(issueTypeName + ISSUETYPE_APPENDIX, "", "/images/icons/ico_epic.png");
			//issueTypes.add(issueType);

			//prepare list of project's issue types
			List<GenericValue> issueTypesGenericValue = new ArrayList<GenericValue>();
			issueTypesGenericValue.add(issueType.getGenericValue());

			//create a list of project contexts for which the custom field needs to be available
			List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
			contexts.add(GlobalIssueContext.getInstance());

			int i = 0;
			String[] parameterCustomFieldIds = new String[customFields.size() + 2];

			//generate custom fields
			for(CustomFieldDefinition e : customFields){

				//check if custom field already exists
				CustomField customFieldObject = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(e.getName() + "_" + issueTypeName);

				if(customFieldObject == null){
					log.debug("newIssueType - customField search : " + e.getName() + "_" + issueTypeName + " null, creating");

					//create custom field
					customFieldObject = ComponentAccessor.getCustomFieldManager().createCustomField(e.getName() + "_" + issueTypeName, e.getName() + "-CustomField for " + issueTypeName, 
							ComponentAccessor.getCustomFieldManager().getCustomFieldType(e.getType()),
							null, 
							contexts, issueTypesGenericValue);

					//add custom field ids to the temporary list of the issue type's custom field ids
					customFieldsCache.add(customFieldObject);
					parameterCustomFieldIds[i++] = customFieldObject.getId();
				}
				log.debug("newIssueType - customField : " + e.getName() + "_" + issueTypeName + " / type : " + e.getType());
			}


			//Map Screens to Transitions

			Map<String, LinkedList<CustomFieldDefinition>> availableActionFieldScreens = new HashMap<String, LinkedList<CustomFieldDefinition>>();

			for(CustomFieldDefinition customFieldDefinition : customFields){
				for(String id : customFieldDefinition.getScreens()){
					if(availableActionFieldScreens.get(id) == null){
						LinkedList<CustomFieldDefinition> sameKeyDefs = new LinkedList<CustomFieldDefinition>();
						sameKeyDefs.add(customFieldDefinition);
						availableActionFieldScreens.put(id, sameKeyDefs);
					}else{
						availableActionFieldScreens.get(id).add(customFieldDefinition);
					}
					log.debug("newIssueType - transition screen grouping loops /  id : " + id + " / name : " + customFieldDefinition.getName());
				}
			}

			FieldScreen fieldScreenCreate = null;
			FieldScreen fieldScreenView = null;
			FieldScreen fieldScreenEdit = null;

			if(!availableActionFieldScreens.containsKey("Create")){
				log.debug("newIssueType - transition screen /  name : " + issueTypeName + FIELDSCREEN_APPENDIX + "ActionCreate creating");
				fieldScreenCreate = generateHumbleFieldScreen(issueTypeName + FIELDSCREEN_APPENDIX + "ActionCreate");
			}

			if(!availableActionFieldScreens.containsKey("View")){
				log.debug("newIssueType - transition screen / name : " + issueTypeName + FIELDSCREEN_APPENDIX + "ActionView creating");
				fieldScreenView = generateHumbleFieldScreen(issueTypeName + FIELDSCREEN_APPENDIX + "ActionView");
			}

			if(!availableActionFieldScreens.containsKey("Edit")){
				log.debug("newIssueType - transition screen / name : " + issueTypeName + FIELDSCREEN_APPENDIX + "ActionEdit creating");
				fieldScreenEdit = generateHumbleFieldScreen(issueTypeName + FIELDSCREEN_APPENDIX + "ActionEdit");
			}

			for(Entry<String, LinkedList<CustomFieldDefinition>> e : availableActionFieldScreens.entrySet()){
				List<String> customFieldIds = new LinkedList<String>(); 

				for(CustomFieldDefinition c : e.getValue()){
					log.debug("newIssueType - transition screen id loops / c.getName() : " + c.getName() + "_" + issueTypeName);
					String l = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(c.getName() + "_" + issueTypeName).getId();

					if(l != null){
						customFieldIds.add(l);
					}else{
						log.error("newIssueType - custom field is null / c.getName() : " + c.getName() + "_" + issueTypeName);
					}
				}

				log.debug("newIssueType - transition screen id loops / fieldscreen.name : " + issueTypeName + FIELDSCREEN_APPENDIX + "Action" + e.getKey() + " creating");
				FieldScreen fieldScreen = new FieldScreenImpl(ComponentAccessor.getFieldScreenManager());
				fieldScreen.setName(issueTypeName + FIELDSCREEN_APPENDIX + "Action" + e.getKey());
				FieldScreenTab fieldScreenTab = fieldScreen.addTab("Job");
				fieldScreenTab.setPosition(0);

				for(String s : customFieldIds){
					fieldScreenTab.addFieldScreenLayoutItem(s);
				}

				fieldScreenTab.addFieldScreenLayoutItem(ComponentAccessor.getFieldManager().getField(IssueFieldConstants.REPORTER).getId());
				fieldScreenTab.addFieldScreenLayoutItem(ComponentAccessor.getFieldManager().getField(IssueFieldConstants.SUMMARY).getId());

				if(e.getKey().equals("Create")){
					fieldScreenCreate = fieldScreen;
				}else if(e.getKey().equals("Edit")){
					fieldScreenEdit = fieldScreen;
				}else if(e.getKey().equals("View")){
					fieldScreenView = fieldScreen;
				}else{
					log.debug("newIssueType - transition screen id loops, manipulating action descriptor / id : " + e.getKey() + " / view: " + fieldScreen.getName() + " / meta, jira.fieldscreen.id : " + fieldScreen.getId());
					ActionDescriptor actionDescriptor = jiraWorkflow.getDescriptor().getAction(Integer.parseInt(e.getKey()));
					actionDescriptor.setView(fieldScreen.getName());
					actionDescriptor.getMetaAttributes().put("jira.fieldscreen.id", fieldScreen.getId());
				}
			}

			log.debug("newIssueType - setting conditions");

			for(ConditionDefinition condition : conditions){

				for(Integer id : condition.getTransitions()){
					log.debug("newIssueType - setting conditions / key : " + Arrays.toString(condition.getTransitions()) + " / action id : " + id);

					try{
						ActionDescriptor actionDescriptor = jiraWorkflow.getDescriptor().getAction(id);
						RestrictionDescriptor restrictionDescriptor = actionDescriptor.getRestriction();
						if(restrictionDescriptor == null){
							restrictionDescriptor = new RestrictionDescriptor();
							actionDescriptor.setRestriction(restrictionDescriptor);
						}

						ConditionsDescriptor conditionsDescriptor = restrictionDescriptor.getConditionsDescriptor();
						if(conditionsDescriptor == null){
							conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor();
							restrictionDescriptor.setConditionsDescriptor(conditionsDescriptor);
						}
						conditionsDescriptor.setType("AND");
						List listLonditions = conditionsDescriptor.getConditions();

						WorkflowEditorTransitionConditionUtil transUtil = new WorkflowEditorTransitionConditionUtil();

						ConditionDescriptor cd = DescriptorFactory.getFactory().createConditionDescriptor();
						cd.setType("class");

						cd.getArgs().put("class.name", condition.getType());

						if(condition.getParams() != null){
							for(Entry<String, String> e : condition.getParams().entrySet()){
								cd.getArgs().put(e.getKey(), e.getValue());
							}
						}

						listLonditions.add(cd);	

					}catch(Exception e){
						log.error("newIssueType - setting conditions / " + e.getClass().toString() + " - can't find action action ID " + id);
					}
				}
			}

			ComponentAccessor.getWorkflowManager().updateWorkflow(user, jiraWorkflow);

			//TODO: Exception handling!!!

			FieldScreenScheme fieldScreenScheme = null;
			try {
				fieldScreenScheme = generateNewFieldScreenScheme(fieldScreenCreate, fieldScreenView, fieldScreenEdit, issueTypeName);
			} catch (Exception e1) {
				log.error("");
				e1.printStackTrace();
				throw e1;
			}

			//set default permission scheme 
			Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey);

			//check if issue type screen scheme already exists
			IssueTypeScreenScheme issueTypeScreenScheme = ComponentAccessor.getIssueTypeScreenSchemeManager().getIssueTypeScreenScheme(project);

			if(issueTypeScreenScheme == null){
				//set default issue type screen scheme
				ComponentAccessor.getIssueTypeScreenSchemeManager().associateWithDefaultScheme(project);
				issueTypeScreenScheme = ComponentAccessor.getIssueTypeScreenSchemeManager().getIssueTypeScreenScheme(project);
			}

			//compose
			IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(ComponentAccessor.getIssueTypeScreenSchemeManager(), (GenericValue) null, JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getConstantsManager());
			issueTypeScreenSchemeEntity.setIssueTypeId(issueType.getId());
			issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenScheme);
			issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);

			FieldConfigScheme issueTypeScheme = ComponentAccessor.getIssueTypeSchemeManager().create(projectKey + ISSUETYPENSCHEME_APPENDIX, "", null);
			LinkedList<String> issueTypesStrings = new LinkedList<String>();
			issueTypesStrings.add(issueType.getId());
			ComponentAccessor.getIssueTypeSchemeManager().update(issueTypeScheme, issueTypesStrings);

			ComponentAccessor.getFieldConfigSchemeManager().updateFieldConfigScheme(issueTypeScheme, contexts, ComponentAccessor.getFieldManager().getConfigurableField(IssueFieldConstants.ISSUE_TYPE));

			//add workflow scheme
			AssignableWorkflowScheme workflowScheme = ComponentAccessor.getWorkflowSchemeManager().getWorkflowSchemeObj(projectKey + WORKFLOWSCHEME_APPENDIX);

			if(workflowScheme == null){
				Builder builder = ComponentAccessor.getWorkflowSchemeManager().assignableBuilder();
				builder.setName(projectKey + WORKFLOWSCHEME_APPENDIX);
				builder.setDescription("");
				workflowScheme = builder.build();
				ComponentAccessor.getWorkflowSchemeManager().createScheme(workflowScheme);
			}

			//TODO Should be fixed when Atlassian offers addWorkflowToScheme for workflow objects (approx. in 1000 years) 
			GenericValue workflowSchemeGeneric = ComponentAccessor.getWorkflowSchemeManager().getScheme(projectKey + WORKFLOWSCHEME_APPENDIX);

			ComponentAccessor.getWorkflowSchemeManager().addWorkflowToScheme(workflowSchemeGeneric, projectKey + WORKFLOW_APPENDIX, issueType.getId());
			ComponentAccessor.getWorkflowSchemeManager().addWorkflowToScheme(workflowSchemeGeneric, "jira", issueType.getId());
			ComponentAccessor.getWorkflowSchemeManager().addSchemeToProject(project.getGenericValue(), workflowSchemeGeneric);	

		}



		log.debug("newIssueType - return issueType " + issueType.getName());
		return issueType.getId();
	}

	private static FieldScreen generateHumbleFieldScreen(String name){
		log.debug("generateHumbleFieldScreen - name : " + name);

		FieldScreen fieldScreen = new FieldScreenImpl(ComponentAccessor.getFieldScreenManager());
		fieldScreen.setName(name);
		FieldScreenTab fieldScreenTab = fieldScreen.addTab("Job");
		fieldScreenTab.setPosition(0);
		fieldScreenTab.addFieldScreenLayoutItem(ComponentAccessor.getFieldManager().getField(IssueFieldConstants.REPORTER).getId());
		fieldScreenTab.addFieldScreenLayoutItem(ComponentAccessor.getFieldManager().getField(IssueFieldConstants.SUMMARY).getId());

		return fieldScreen;
	}

	private static FieldScreenScheme generateNewFieldScreenScheme(FieldScreen fieldScreenCreate, FieldScreen fieldScreenEdit,FieldScreen fieldScreenView, String fieldScreenSchemeName) throws Exception{
		log.debug("generateNewFieldScreenScheme - fieldScreenSchemeName : " + fieldScreenSchemeName);

		if(fieldScreenCreate == null || fieldScreenEdit == null || fieldScreenView == null){
			throw new Exception("One or more field screens are null");
		}

		FieldScreenSchemeItem fieldScreenSchemeItemCreate = new FieldScreenSchemeItemImpl(JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemCreate.setIssueOperation(IssueOperations.CREATE_ISSUE_OPERATION);
		fieldScreenSchemeItemCreate.setFieldScreen(fieldScreenCreate);

		FieldScreenSchemeItem fieldScreenSchemeItemEdit= new FieldScreenSchemeItemImpl(JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemEdit.setIssueOperation(IssueOperations.EDIT_ISSUE_OPERATION);
		fieldScreenSchemeItemEdit.setFieldScreen(fieldScreenEdit);

		FieldScreenSchemeItem fieldScreenSchemeItemView = new FieldScreenSchemeItemImpl(JiraManagerPlugin.fieldScreenSchemeManager, ComponentAccessor.getFieldScreenManager());
		fieldScreenSchemeItemView.setIssueOperation(IssueOperations.VIEW_ISSUE_OPERATION);
		fieldScreenSchemeItemView.setFieldScreen(fieldScreenView);

		FieldScreenScheme fieldScreenScheme = new FieldScreenSchemeImpl(JiraManagerPlugin.fieldScreenSchemeManager, null);
		fieldScreenScheme.setName(fieldScreenSchemeName + FIELDSCREENSCHEME_APPENDIX);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemView);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemEdit);
		fieldScreenScheme.addFieldScreenSchemeItem(fieldScreenSchemeItemCreate);

		return fieldScreenScheme;
	}

	private static MutableIssue generateNewMutableIssue(String projectKey, ApplicationUser user, String issueTypeName, String summary, String reporter) throws Exception{
		log.debug("generateNewMutableIssue - projectKey : " + projectKey + " / issueTypeName : " + issueTypeName + " / summary : " + summary + " / reporter : " + reporter);

		if (user != null){
			log.debug("generateNewMutableIssue - user : " + user.getUsername());
		}else{
			log.error("generateNewMutableIssue - user null");
			throw new Exception("User is null");
		}

		MutableIssue newIssue = ComponentAccessor.getIssueFactory().getIssue();

		newIssue.setProjectObject(ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey));
		newIssue.setIssueTypeObject(findIssueTypeByName(issueTypeName + ISSUETYPE_APPENDIX));
		newIssue.setSummary(summary);

		//TODO ?
		//		newIssue.setReporter(JiraManaPlugin.userManager.getUserByName(username));
		//		newIssue.setReporter(JiraManaPlugin.userManager.getUserByName(reporter));

		return newIssue;
	}

	/**
	 * Creates a new Jira project
	 * 
	 * @param projectName : the name of the new project
	 * @param projectKey : the project's key
	 * @param workflowXML : the Jira workflow, can be null
	 * @param statuses : list of statuses (steps) provided by the assigned workflow
	 * 
	 * @return returns true if project has been created successfully
	 * @throws Exception 
	 */

	public static void initProject(String projectName, String projectKey, ApplicationUser user, boolean kill) throws Exception{
		log.debug("initProject - projectName : " + projectName + " / projectKey : " + projectKey + " / kill : " + kill);

		if (user != null){
			log.debug("initProject - user : " + user.getUsername());
		}else{
			log.error("initProject - user null");
			throw new Exception("User is null");
		}

		if(projectKey.length() > 4){
			throw new Exception("error: project key length > 4 ! ");
		}

		// if kill is set ALL issue types will be deleted
		if(kill){
			for(IssueType it :JiraManagerPlugin.issueTypeManager.getIssueTypes()){
				try{
					JiraManagerPlugin.issueTypeManager.removeIssueType(it.getId(), null);
				}catch(Exception e){
					System.out.println("Unable to delete IssueType " + it.getName());
				}
			}	
		}

		//create a list of project contexts for which the custom field needs to be available
		List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
		contexts.add(GlobalIssueContext.getInstance());

		Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey);

		if(project == null){

			project = ComponentAccessor.getProjectManager().createProject(projectName, projectKey, "", user.getUsername(), "", 0l);
			ComponentAccessor.getPermissionSchemeManager().addDefaultSchemeToProject(project);

			//			ArrayList<IssueType> issueTypes = new ArrayList<IssueType>(project.getIssueTypes());	
			//
			//			GenericValue defaultWorkflowScheme = ComponentAccessor.getWorkflowSchemeManager().createDefaultScheme();
			//
			//			for(IssueType issueType : issueTypes){
			//				ComponentAccessor.getWorkflowSchemeManager().addWorkflowToScheme(defaultWorkflowScheme, "jira", issueType.getId());
			//			}
			//
			//			ComponentAccessor.getWorkflowSchemeManager().addSchemeToProject(project.getGenericValue(), defaultWorkflowScheme);

		}
	}

	/**
	 * Creates a new Issue in Jira
	 * 
	 * @param projectKey : the projects key
	 * @param issueTypeName : determines which issue type should be used as issue scheme  
	 * @param comment : value for default field 'comment'
	 * @param parameters : map of custom field values (name : value)
	 * @return returns the issue id 
	 * 
	 **/
	public static String newIssue(String projectKey, String workflowName, String summary, ApplicationUser user, String description, Map<String, String> parameters, String reporter) throws Exception{
		log.debug("newIssue - projectKey : " + projectKey + " / workflowName : " + workflowName + " / summary : " + summary + " / description : " + description + " / reporter : " + reporter);

		if (user != null){
			log.debug("newIssue - user : " + user.getUsername());
		}else{
			log.error("newIssue - user null");
			throw new Exception("User is null");
		}

		MutableIssue mutableIssue = generateNewMutableIssue(projectKey, user, workflowName, parameters.get("summary"), reporter);

		for(Entry<String, String> entry : parameters.entrySet()){
			CustomField customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(entry.getKey() + "_" + workflowName);

			if(customField != null){
				if(entry.getValue() != null){
					mutableIssue.setCustomFieldValue(customField, entry.getValue());
				}else{
					mutableIssue.setCustomFieldValue(customField, "");
				}
				log.debug("newIssue - custom field loop / entry.key : " + entry.getKey() + " / value : " + entry.getValue());
			}else{
				log.debug("newIssue - custom field loop / customField search: entry.key : " + entry.getKey() + " null");
			}
		}

		//mutableIssue.setAssignee(JiraManaPlugin.userManager.getUserByName(reporter));

		mutableIssue.setSummary(workflowName + " / " + reporter);

		if(!description.equals("")){
			mutableIssue.setDescription(description);
		}

		//TODO should be fixed when there is createIssueObject for ApplicationUser 
		Issue issue = ComponentAccessor.getIssueManager().createIssueObject(ApplicationUsers.toDirectoryUser(user), mutableIssue);
		return issue.getKey();
	}

	public static boolean deleteIssue(String issueKey){
		try {
			Issue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
			ComponentAccessor.getIssueManager().deleteIssueNoEvent(issue);
			return true;

		} catch (RemoveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
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

		Status nextStatus = getStatusByName(issue.getProjectObject().getKey(), statusName);

		if(issue == null || jiraWorkflow == null || nextStatus == null){
			return false;
		}else{
			ComponentAccessor.getWorkflowManager().migrateIssueToWorkflow(issue, jiraWorkflow, nextStatus);
			return true;
		}
	}


	/**
	 * Get available status names
	 * 
	 * @param projectKey : the projects key
	 * @return returns a string array of all available status names
	 */

	public static List<String> getStatusNames(String projectKey){
		List<Status> status = ComponentAccessor.getWorkflowManager().getWorkflow(projectKey + WORKFLOW_APPENDIX).getLinkedStatusObjects();
		List<String> result = new ArrayList<String>();

		for(Status statusItem : status){
			result.add(statusItem.getName());
		}

		return result;
	}


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
	 * removes an attachment
	 * 
	 * @param attachmentId
	 * @return
	 */
	public static boolean removeAttachment(long attachmentId){
		try{
			Attachment attachment = ComponentAccessor.getAttachmentManager().getAttachment(attachmentId);
			ComponentAccessor.getAttachmentManager().deleteAttachment(attachment);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @param projectKey
	 * @param workflowXML
	 */
	public static JiraWorkflow addWorkflow(String projectKey, String workflowXML, ApplicationUser user){

		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(projectKey + WORKFLOW_APPENDIX);

		if(jiraWorkflow == null && workflowXML != null){
			try {
				jiraWorkflow = new ConfigurableJiraWorkflow(projectKey + WORKFLOW_APPENDIX, WorkflowUtil.convertXMLtoWorkflowDescriptor(workflowXML), ComponentAccessor.getWorkflowManager());
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

	public static ApplicationUser createUser(String userName, String password) throws PermissionException, CreateException, AddException{
		ApplicationUser pubflowUser = ComponentAccessor.getUserUtil().getUserByName(userName);
		if(pubflowUser == null){
			pubflowUser = ApplicationUsers.from(ComponentAccessor.getUserUtil().createUserNoNotification(userName, password, "", ""));	
		}
		return pubflowUser;
	}

	public static ApplicationUser getUserByName(String userName){
		return ComponentAccessor.getUserUtil().getUserByName(userName);
	}

	public static Group createGroup(String name) throws OperationNotPermittedException, InvalidGroupException{
		Group group = ComponentAccessor.getGroupManager().getGroup(name);
		if(group == null){
			group = ComponentAccessor.getGroupManager().createGroup(name);
		}

		return group;
	}


	public static void initPubFlowProject() throws GenericEntityException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException{
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_TITLE, "PubFlow Jira");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_MODE, "Private");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_BASEURL, "http://maui.se.informatik.uni-kiel.de:38080/jira/");

		//TODO have to change this, using classes of the web front end is really dirty 
		//		SetupMailNotifications mail = new SetupMailNotifications(ComponentAccessor.getUserUtil(), new FileSystemFileFactory(JiraSystemProperties.getInstance()));
		//		mail.setUsername("wp10598327-arndlange");
		//		mail.setMailservertype("SMTP");
		//		mail.setName("Mail");
		//		mail.setTlsRequired(true);
		//		mail.setServiceProvider("Custom");
		//		mail.setFrom("info@bough.de");
		//		mail.setPrefix("Mail");
		//		mail.setName("mail.bough.de");
		//		mail.setPort("143");
		//		mail.setPassword("");
		//		try {
		//			mail.returnComplete();
		//			mail.execute();
		//			mail.setupAlready();
		//		} catch (Exception e1) {
		//			// TODO Auto-generated catch block
		//			e1.printStackTrace();
		//		}

		try {
			if(ComponentAccessor.getProjectManager().getProjectObjByName("PubFlow") == null){

				Group groupDataManager = createGroup("datamanager");
				Group groupScientists = createGroup("scientists");

				ApplicationUser userPubFlow = createUser("PubFlow", new BigInteger(130, JiraManagerPlugin.secureRandom).toString(32));
				addUserToGroup(userPubFlow, "jira-administrators");
				addUserToGroup(userPubFlow, "jira-developers");
				addUserToGroup(userPubFlow, "jira-users");
				addUserToGroup(userPubFlow, groupScientists);
				addUserToGroup(userPubFlow, groupDataManager);

				//TODO fix deprecation when admin is application user by default
				User userAdmin = ComponentAccessor.getUserManager().getUserObject("admin");
				ApplicationUser userRoot = createUser("root", "$Boogie3");
				addUserToGroup(userRoot, groupDataManager);
				addUserToGroup(userRoot, groupScientists);
				addUserToGroup(userRoot, "jira-administrators");
				addUserToGroup(userRoot, "jira-developers");
				addUserToGroup(userRoot, "jira-users");
				ComponentAccessor.getCrowdService().removeUser(userAdmin);

				ApplicationUser userDataManager = createUser("SampleDataManager", "ilovedata");
				addUserToGroup(userDataManager, groupDataManager);
				addUserToGroup(userDataManager, groupScientists);
				addUserToGroup(userDataManager, "jira-developers");
				addUserToGroup(userDataManager, "jira-users");

				ApplicationUser userScientist = createUser("SampleScientist", "sciencerulez");
				addUserToGroup(userScientist, groupScientists);			
				addUserToGroup(userScientist, "jira-users");

				List<String> statuses = new LinkedList<String>();
				statuses.add("Open");
				statuses.add("Ready for Convertion by Data Management");
				statuses.add("Ready for OCN-Import");
				statuses.add("Prepare for PubFlow");
				statuses.add("Data Processing by PubFlow");
				statuses.add("Ready for Pangaea-Import");
				statuses.add("Data Needs Correction");
				//statuses.add("Waiting for DOI");
				statuses.add("Closed");
				statuses.add("Done");
				statuses.add("Rejected");
				initProject("PubFlow", "PUB", userPubFlow, false);

		

				List<ConditionDefinition> conditionMap = new LinkedList<ConditionDefinition>();

				Map <String, String> mapParamsDatamanager = new HashMap<String, String>();
				mapParamsDatamanager.put("group", "datamanager");
				conditionMap.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsDatamanager, new int[]{21, 81, 141, 121, 61, 71, 91, 111, 151, 131, 161}));

				Map <String, String> mapParamsPubFlow = new HashMap<String, String>();
				mapParamsPubFlow.put("group", "jira-administrators");
				conditionMap.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsPubFlow, new int[]{41,101}));

				Map <String, String> mapParamsScientists = new HashMap<String, String>();
				mapParamsScientists.put("group", "datamanager");
				conditionMap.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsScientists, new int[]{1, 11}));

				conditionMap.add(new ConditionDefinition(ConditionDefinitionType.ATTACHMENT, null, new int[]{11}));


				LinkedList<CustomFieldDefinition> customFields = new LinkedList<CustomFieldDefinition>();
				customFields.add(new CustomFieldDefinition("Leg ID", CustomFieldType.TEXT, true, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("PID", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Login", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Source", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Author", CustomFieldType.TEXT, false, new String[]{"11", "141", "111"}));
				customFields.add(new CustomFieldDefinition("Project", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Topology", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Status", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Zielpfad", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Reference", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("File name", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Leg comment", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Quartz Cron", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("DOI", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Author name", CustomFieldType.TEXT, false, new String[]{"11"}));
				customFields.add(new CustomFieldDefinition("Title", CustomFieldType.TEXT, false, new String[]{"11"}));
				customFields.add(new CustomFieldDefinition("Cruise", CustomFieldType.TEXT, false, new String[]{"11"}));

				customFields.add(new CustomFieldDefinition("Start Time (QUARTZ)", CustomFieldType.DATETIME, false, new String[]{"141", "111"}));

				newIssueType("PUB", "OCN", userPubFlow, JiraManagerPlugin.getTextResource("/PubFlow.xml"), statuses, customFields, conditionMap);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}