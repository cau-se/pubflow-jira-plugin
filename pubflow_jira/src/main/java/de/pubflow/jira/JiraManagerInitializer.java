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
package de.pubflow.jira;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.project.ProjectCreationData;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.opensymphony.workflow.loader.ActionDescriptor;

import de.pubflow.common.PropLoader;
import de.pubflow.jira.accessors.JiraObjectCreator;
import de.pubflow.jira.accessors.JiraObjectGetter;
import de.pubflow.jira.accessors.JiraObjectManipulator;
import de.pubflow.jira.accessors.JiraObjectRemover;
import de.pubflow.jira.misc.Appendix;
import de.pubflow.jira.misc.CustomFieldDefinition;
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

public class JiraManagerInitializer {

	public static List<CustomField> customFieldsCache = new LinkedList<CustomField>();
	private static Logger log = Logger.getLogger(JiraManagerInitializer.class);
	private static final ProjectManager projectManager = ComponentAccessor.getProjectManager();

	/**
	 * Creates a new Jira project
	 * 
	 * @param projectName
	 *          : the name of the new project
	 * @param projectKey
	 *          : the project's key
	 * @param user : the ApplicationUser that holds the project (lead of the project)
	 * 
	 * @return returns the created project object
	 * @throws Exception
	 */

	public static Project initProject(String projectName, String projectKey, ApplicationUser user,
			boolean kill) throws Exception {

		log.debug("initProject - projectName : " + projectName + " / projectKey : " + projectKey + " / kill : " + kill);
		final PermissionSchemeManager permissionSchemeManager = ComponentAccessor.getPermissionSchemeManager();

		if (user != null){
			log.debug("initProject - user : " + user.getUsername());
		}else{
			log.error("initProject - user null");
			throw new Exception("User is null");
		}  

		if (projectKey.length() > 4) {
			final String errorMsg = "initProject: error: project key length > 4 ! ";
			log.error(errorMsg);
			throw new Exception(errorMsg);
		}

		Project project = projectManager.getProjectObjByKey(projectKey);


		if (project == null) {
			int avatarId = 10100;
			ProjectCreationData projectData = new ProjectCreationData.Builder().withName(projectName)
					.withLead(user).withKey(projectKey).withDescription("Geht so").withType("business")
					.withAvatarId(new Long(avatarId)).build();
			project = projectManager.createProject(user, projectData);
			permissionSchemeManager.addDefaultSchemeToProject(project);
			log.info("initProject: created a new project with projectKey "+projectKey);
		} else {
			log.debug("initProject: project with projectKey " +projectKey+ " already exists");

		}

		return project;
	}


	/**
	 * Initializes the issue types, issue type scheme and maps them to a project
	 *
	 * @author abar
	 * @param projectKey : the project's key we add the issue type and scheme to
	 * @param issueTypeName : the name of the issue type we want to create
	 */
	public static void initIssueManagement(String projectKey, String issueTypeName, String workflowID)
			throws CreateException {
		final Project project = projectManager.getProjectObjByKey(projectKey);
		JiraObjectCreator.createIssueType(project, issueTypeName, workflowID);
		final FieldConfigScheme issueTypeScheme = JiraObjectCreator.createIssueTypeScheme(project);

		JiraObjectManipulator.addIssueTypeSchemeToProject(issueTypeScheme, project);
	}

	public static FieldScreenScheme initHumbleScreens(List<String> names, List<CustomFieldDefinition> customFields, String issueTypeName, List<Long> customFieldIdsTest, Project project) throws Exception {
		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(project.getKey() + Appendix.WORKFLOW);
		Map<String,LinkedList<CustomFieldDefinition>>availableActionFieldScreens=new HashMap<String,LinkedList<CustomFieldDefinition>>();

		final CustomFieldManager customFieldManager =ComponentAccessor.getCustomFieldManager();

		for(CustomFieldDefinition customFieldDefinition : customFields)
		{
			for (String id : customFieldDefinition.getScreens()) {
				if (availableActionFieldScreens.get(id) == null) {
					LinkedList<CustomFieldDefinition> sameKeyDefs = new LinkedList<CustomFieldDefinition>();
					sameKeyDefs.add(customFieldDefinition);
					availableActionFieldScreens.put(id, sameKeyDefs);
				} else {
					availableActionFieldScreens.get(id).add(customFieldDefinition);
				}
				log.debug("initHumbleScreens: transition screen grouping loops / id : " + id + " / name : "
						+ customFieldDefinition.getName());
			}
		}    

		FieldScreen fieldScreenCreate = JiraObjectCreator.createActionScreen(issueTypeName +
				Appendix.FIELDSCREEN + "ActionCreate");
		FieldScreen fieldScreenView = JiraObjectCreator.createActionScreen(issueTypeName +
				Appendix.FIELDSCREEN + "ActionView");
		FieldScreen fieldScreenEdit = JiraObjectCreator.createActionScreen(issueTypeName +
				Appendix.FIELDSCREEN + "ActionEdit");

		for(Entry<String, LinkedList<CustomFieldDefinition>> e : availableActionFieldScreens.entrySet()){
			List<String> customFieldIds = new LinkedList<String>();

			for(CustomFieldDefinition c : e.getValue()){
				log.debug("initHumbleScreens: transition screen id loops / c.getName() : " + c.getName() + "_" + issueTypeName);
				String l = customFieldManager.getCustomFieldObjectByName(c.getName() + "_" + issueTypeName).getId();

				if(l != null){
					customFieldIds.add(l);
				}else{
					log.error("initHumbleScreens: custom field is null / c.getName() : " +
							c.getName() + "_" + issueTypeName);
				}
			}

			FieldScreen fieldScreen = JiraObjectCreator.createActionScreen(issueTypeName + Appendix.FIELDSCREEN + "Action" + e.getKey());
			List<FieldScreenTab> fieldScreenTabs = fieldScreen.getTabs();
			FieldScreenTab jobTab = null;
			for(FieldScreenTab fieldScreenTab : fieldScreenTabs) {
				if(fieldScreenTab.getName().equals("Job")) {
					jobTab = fieldScreenTab;
				}
			}

			if(jobTab == null) {
				jobTab = fieldScreen.addTab("Job");
			}

			FieldScreenTab fieldScreenTab = jobTab;

			for(String s : customFieldIds){
				if(fieldScreenTab.getFieldScreenLayoutItem(s) == null) {
					fieldScreenTab.addFieldScreenLayoutItem(s);
				}
			}

			if(e.getKey().equals("Create")){
				fieldScreenCreate = fieldScreen;
			}else if(e.getKey().equals("Edit")){
				fieldScreenEdit = fieldScreen;
			}else if(e.getKey().equals("View")){
				fieldScreenView = fieldScreen;
			}else{
				Collection<ActionDescriptor> allActions = jiraWorkflow.getAllActions();
				Map<String, String> metaAttributes=new HashMap<String, String>();
				metaAttributes.put("jira.fieldscreen.id", Long.toString(fieldScreen.getId()));
				for(ActionDescriptor aActions : allActions){
					if(aActions.getId() == Integer.parseInt(e.getKey())){
						aActions.setView(fieldScreen.getName());
						aActions.setMetaAttributes(metaAttributes);
					}
				}      

			}
		}


		FieldScreenScheme fieldScreenScheme = JiraObjectCreator.generateNewFieldScreenScheme(fieldScreenCreate, fieldScreenView, fieldScreenEdit, issueTypeName);

		return fieldScreenScheme;
	}


	/**
	 * Initializes the workflow, workflow scheme and maps them to a project
	 *
	 * @author abar
	 * @param projectKey : the project's key we add the issue type and scheme to
	 * @param workflowXML : the path to a XML that defines the workflow
	 * @param user : the ApplicationUser that can create a workflow (administrator in general)
	 */
	public static void initWorkflow(String projectKey, String workflowXML, ApplicationUser user, String issueTypeName) {
		final WorkflowSchemeManager workflowSchemeManager = ComponentAccessor.getWorkflowSchemeManager(); 

		if (user != null){
			log.debug("initWorkflow: user : " + user.getUsername());
		} else {
			log.error("initWorkflow: user is null");
		}

		JiraWorkflow jiraWorkflow = JiraObjectCreator.addWorkflow(projectKey, workflowXML, user);    
		WorkflowScheme workflowScheme = JiraObjectCreator.createWorkflowScheme(projectKey, user, jiraWorkflow, issueTypeName+Appendix.ISSUETYPE);
		JiraObjectManipulator.addWorkflowToProject(workflowScheme, projectManager.getProjectObjByKey(projectKey));
		Project project = projectManager.getProjectObjByKey(projectKey);
		IssueType ocnIssueType = JiraObjectGetter.getIssueTypeByName(issueTypeName + Appendix.ISSUETYPE);

		try {
			workflowSchemeManager.addWorkflowToScheme(workflowSchemeManager.getWorkflowScheme(project), jiraWorkflow.getName(), ocnIssueType.getId());
			log.info("initWorkflow: add the workflow: "+jiraWorkflow.getName()+" to the workflowscheme of the project: "+project.getName());
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			log.error("initWorkflow: Couldn't  add the workflow: "+jiraWorkflow.getName()+" to the workflowscheme of the project: "+project.getName());
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the Look&Feel
	 */
	public static void initJiraSettings(){
		final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
		applicationProperties.setString(APKeys.JIRA_BASEURL, PropLoader.getInstance().getProperty( "JIRA_BASEURL", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_MODE, 
				PropLoader.getInstance().getProperty("JIRA_MODE", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_TITLE, 
				PropLoader.getInstance().getProperty("JIRA_TITLE", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_LF_TOP_BGCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TOP_BGCOLOUR", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TOP_HIGHLIGHTCOLOR", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TOP_SEPARATOR_BGCOLOR", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_LF_TOP_TEXTCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TOP_TEXTCOLOUR", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_LF_MENU_BGCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_MENU_BGCOLOUR", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_LF_MENU_SEPARATOR, 
				PropLoader.getInstance().getProperty("JIRA_LF_MENU_SEPARATOR", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_LF_MENU_TEXTCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_MENU_TEXTCOLOUR", JiraManagerInitializer.class));	
		applicationProperties.setString(APKeys.JIRA_LF_HERO_BUTTON_BASEBGCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_HERO_BUTTON_BASEBGCOLOUR", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_LF_HERO_BUTTON_TEXTCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_HERO_BUTTON_TEXTCOLOUR", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TEXT_ACTIVE_LINKCOLOUR", JiraManagerInitializer.class));
		applicationProperties.setString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TEXT_HEADINGCOLOUR", JiraManagerInitializer.class));		
		applicationProperties.setString(APKeys.JIRA_LF_TEXT_LINKCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TEXT_LINKCOLOUR", JiraManagerInitializer.class));		
		applicationProperties.setString(APKeys.JIRA_LF_LOGO_URL, 
				PropLoader.getInstance().getProperty("JIRA_LF_LOGO_URL", JiraManagerInitializer.class));	

		SMTPMailServerImpl smtp = new SMTPMailServerImpl();
		smtp.setName("Mail Server");
		smtp.setDescription("");
		smtp.setDefaultFrom("pubflow@bough.de");
		smtp.setPrefix("[pubflow]");
		smtp.setPort("587");
		smtp.setMailProtocol(MailProtocol.SMTP);
		smtp.setHostname("mail.bough.de");
		smtp.setUsername("wp10598327-pubflow");
		smtp.setPassword("kidoD3l77");
		smtp.setTlsRequired(true);
		try {
			ComponentAccessor.getMailServerManager().create(smtp);
		} catch (MailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the whole PubFlow project.
	 * 
	 * Set application properties
	 * 		v
	 * "PubFlow" project will be initialized.
	 * 		v
	 * user groups "datamanager" and "scientists" are created
	 * 		v
	 * users "PubFlow" and "root" will created and added to all user groups.
	 * 		v
	 * Statuses will be created
	 * 		v
	 * IssueTypes and Schemes will be created (need statuses)
	 * 		v
	 * Create Workflow and Scheme (needs statuses, project, and issuetypes)
	 * 		v
	 * Create CustomField
	 * 		v
	 * Create all Screens (needs screennames, issuetypes, customfields, and a project)
	 * 		v
	 * Map sceenschemes to a given project (needs the project, the issuetype, and the screen for the issuetype)
	 * 
	 * @author arl, abar
	 * 
	 */
	public static void initPubFlowProject()
			throws GenericEntityException, KeyManagementException, UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		final ApplicationProperties applicationPropertiesManager = ComponentAccessor.getApplicationProperties();
		final UserManager userManager = ComponentAccessor.getUserManager();
		applicationPropertiesManager.setString(APKeys.JIRA_TITLE, "PubFlow Jira");
		applicationPropertiesManager.setString(APKeys.JIRA_MODE, "Private");
		applicationPropertiesManager.setString(APKeys.JIRA_BASEURL, "http://maui.informatik.uni-kiel.de:2990/jira/");
		
		initJiraSettings();
		
		final String projectKey = "PUB";
		Project project = projectManager.getProjectObjByName("PubFlow");

		try {
			if (project == null) {
				//
				Group groupDataManager = JiraObjectCreator.createGroup("datamanager");
				Group groupScientists = JiraObjectCreator.createGroup("scientists");
				//
				ApplicationUser userPubFlow = JiraObjectCreator.createUser("PubFlow",
						new BigInteger(130, JiraManagerPlugin.secureRandom).toString(32));
				JiraObjectManipulator.addUserToGroup(userPubFlow, "jira-administrators");
				JiraObjectManipulator.addUserToGroup(userPubFlow, "jira-core-users");
				JiraObjectManipulator.addUserToGroup(userPubFlow, "jira-software-users");
				JiraObjectManipulator.addUserToGroup(userPubFlow, groupScientists);
				JiraObjectManipulator.addUserToGroup(userPubFlow, groupDataManager);
				//
				// //TODO fix deprecation when admin is application user by
				// default
				ApplicationUser userRoot = JiraObjectCreator.createUser("root", "$Boogie3");
				JiraObjectManipulator.addUserToGroup(userRoot, groupDataManager);
				JiraObjectManipulator.addUserToGroup(userRoot, groupScientists);
				JiraObjectManipulator.addUserToGroup(userRoot, "jira-administrators");

				// The Group "jira-developers" does not exist in Jira 7.x.x. Use
				// "jira-core-users"
				// The Group "jira-users" does not exist in Jira 7.x.x. Use
				// "jira-software-users"
				JiraObjectManipulator.addUserToGroup(userRoot, "jira-core-users");
				JiraObjectManipulator.addUserToGroup(userRoot, "jira-software-users");
				JiraObjectRemover.deleteUser(userRoot, "admin");
				//
				ApplicationUser userDataManager = JiraObjectCreator.createUser("SampleDataManager", "ilovedata");
				JiraObjectManipulator.addUserToGroup(userDataManager, groupDataManager);
				JiraObjectManipulator.addUserToGroup(userDataManager, groupScientists);
				JiraObjectManipulator.addUserToGroup(userDataManager, "jira-core-users");
				JiraObjectManipulator.addUserToGroup(userDataManager, "jira-software-users");

				ApplicationUser userScientist = JiraObjectCreator.createUser("SampleScientist", "sciencerulez");
				JiraObjectManipulator.addUserToGroup(userScientist, groupScientists);
				JiraObjectManipulator.addUserToGroup(userScientist, "jira-software-users");

				log.debug("initPubfowProject: created users and usergroups for PubFlow");

				project = initProject("PubFlow", projectKey, userRoot, false);
			}

			//RAWTOPUBFLOW
			//			List<ConditionDefinition> conditionsRawToOCN = new LinkedList<ConditionDefinition>();
			//			List<ConditionDefinition> conditionsOCNTo4D = new LinkedList<ConditionDefinition>();

			//			conditionsRawToOCN.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsDatamanager, new int[]{21, 81, 161, 171, 71, 91}));	
			//			conditionsRawToOCN.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsScientists, new int[]{11}));
			//			conditionsRawToOCN.add(new ConditionDefinition(ConditionDefinitionType.ATTACHMENT, null, new int[]{11}));

			//			conditionsOCNTo4D.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsDatamanager, new int[]{171, 71, 91, 111, 151, 131, 191}));
			//			conditionsOCNTo4D.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsPubFlow, new int[]{41,101}));

			List<String> statuses = new LinkedList<String>();
			statuses.add("Open");
			statuses.add("Ready for Convertion by Data Management");
			statuses.add("Ready for OCN-Import");
			statuses.add("Prepare for PubFlow");
			statuses.add("Data Processing by PubFlow");
			statuses.add("Ready for Pangaea-Import");
			statuses.add("Data Needs Correction");
			statuses.add("Waiting for DOI");
			statuses.add("Closed");
			statuses.add("Done");
			statuses.add("Rejected");
			JiraObjectCreator.addStatuses(projectKey, statuses);
			
			final String issueTypeCVOOTo4DName = "Export Data (CVOO) to PANGAEA";
			final String issueTypeOCNTo4DName = "Export Data (OCN) to PANGAEA";
			final String issueTypeEprintsName = "EPRINTS";
			final String issueTypeRawToOCNName = "Publish Raw Cruise Data";

			initIssueManagement(projectKey, issueTypeRawToOCNName, "");
			initIssueManagement(projectKey, issueTypeEprintsName, "de.pubflow.EPRINTS");
			initIssueManagement(projectKey, issueTypeCVOOTo4DName, "de.pubflow.CVOO");
			initIssueManagement(projectKey, issueTypeOCNTo4DName, "de.pubflow.OCN");
			
			initWorkflow(projectKey, JiraManagerPlugin.getTextResource("/OCNTO4D-WORKFLOW.xml"), userManager.getUserByName("PubFlow"), issueTypeCVOOTo4DName);
			initWorkflow(projectKey, JiraManagerPlugin.getTextResource("/OCNTO4D-WORKFLOW.xml"), userManager.getUserByName("PubFlow"), issueTypeOCNTo4DName);
			initWorkflow(projectKey, JiraManagerPlugin.getTextResource("/EPRINTS.xml"), userManager.getUserByName("PubFlow"), issueTypeEprintsName);
			initWorkflow(projectKey, JiraManagerPlugin.getTextResource("/RAWTOCVOO-WORKFLOW.xml"), userManager.getUserByName("PubFlow"), issueTypeRawToOCNName);

			List<String> screenNamesCVOOTo4D = new ArrayList<String>();
			screenNamesCVOOTo4D.add(issueTypeCVOOTo4DName + Appendix.FIELDSCREEN + "ActionCreate");
			screenNamesCVOOTo4D.add(issueTypeCVOOTo4DName + Appendix.FIELDSCREEN + "ActionEdit");
			screenNamesCVOOTo4D.add(issueTypeCVOOTo4DName + Appendix.FIELDSCREEN + "ActionView");

			List<String> screenNamesOCNTo4D = new ArrayList<String>();
			screenNamesOCNTo4D.add(issueTypeOCNTo4DName + Appendix.FIELDSCREEN + "ActionCreate");
			screenNamesOCNTo4D.add(issueTypeOCNTo4DName + Appendix.FIELDSCREEN + "ActionEdit");
			screenNamesOCNTo4D.add(issueTypeOCNTo4DName + Appendix.FIELDSCREEN + "ActionView");

			List<String> screenNamesEprints = new ArrayList<String>();
			screenNamesEprints.add(issueTypeEprintsName + Appendix.FIELDSCREEN + "ActionCreate");
			screenNamesEprints.add(issueTypeEprintsName + Appendix.FIELDSCREEN + "ActionEdit");
			screenNamesEprints.add(issueTypeEprintsName + Appendix.FIELDSCREEN + "ActionView");

			List<String> screenNamesRawToOCN = new ArrayList<String>();
			screenNamesRawToOCN.add(issueTypeRawToOCNName + Appendix.FIELDSCREEN + "ActionCreate");
			screenNamesRawToOCN.add(issueTypeRawToOCNName + Appendix.FIELDSCREEN + "ActionEdit");
			screenNamesRawToOCN.add(issueTypeRawToOCNName + Appendix.FIELDSCREEN + "ActionView");

			LinkedList<CustomFieldDefinition> customFieldsOCNTo4D = new LinkedList<CustomFieldDefinition>();
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Leg ID", CustomFieldType.TEXT, true, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("PID", CustomFieldType.TEXT, false, new String[]{ "111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Login", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Source", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Author", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Project", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Topology", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Status", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Target Path", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Reference", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("File Name", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Leg Comment", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Quartz Cron", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("DOI", CustomFieldType.TEXT, false, new String[]{"111", "191"}));
			customFieldsOCNTo4D.add(new CustomFieldDefinition("Start Time (QUARTZ)", CustomFieldType.DATETIME, false, new String[]{"111", "191"}));

			LinkedList<CustomFieldDefinition> customFieldsRawToOCN = new LinkedList<CustomFieldDefinition>();
			customFieldsRawToOCN.add(new CustomFieldDefinition("Author", CustomFieldType.TEXT, false, new String[]{"11"}));
			customFieldsRawToOCN.add(new CustomFieldDefinition("Author Name", CustomFieldType.TEXT, false, new String[]{"11"}));
			customFieldsRawToOCN.add(new CustomFieldDefinition("Title", CustomFieldType.TEXT, false, new String[]{"11"}));
			customFieldsRawToOCN.add(new CustomFieldDefinition("Cruise", CustomFieldType.TEXT, false, new String[]{"11"}));

			List<Long> customFieldIdsOCNTo4D = JiraObjectCreator.createCustomFields(customFieldsOCNTo4D, project, issueTypeOCNTo4DName);
			List<Long> customFieldIdsCVOOTo4D = JiraObjectCreator.createCustomFields(customFieldsOCNTo4D, project, issueTypeCVOOTo4DName);
			List<Long> customFieldIdsRawToOCN = JiraObjectCreator.createCustomFields(customFieldsRawToOCN, project, issueTypeRawToOCNName);			
			
			FieldScreenScheme fieldScreenSchemeOCNTo4D = initHumbleScreens(screenNamesOCNTo4D, customFieldsOCNTo4D, issueTypeOCNTo4DName, customFieldIdsOCNTo4D, project);
			FieldScreenScheme fieldScreenSchemeCVOOTo4D = initHumbleScreens(screenNamesCVOOTo4D, customFieldsOCNTo4D, issueTypeCVOOTo4DName, customFieldIdsCVOOTo4D, project);
			FieldScreenScheme fieldScreenSchemeEprints = initHumbleScreens(screenNamesEprints, new LinkedList<CustomFieldDefinition>(), issueTypeEprintsName, new ArrayList<Long>(), project);
			FieldScreenScheme fieldScreenSchemeRawToOCN = initHumbleScreens(screenNamesRawToOCN, customFieldsRawToOCN, issueTypeRawToOCNName, customFieldIdsRawToOCN, project);
			
			JiraObjectManipulator.addIssueTypeScreenSchemeToProject(project, fieldScreenSchemeOCNTo4D, JiraObjectGetter.getIssueTypeByName(issueTypeOCNTo4DName + Appendix.ISSUETYPE));
			JiraObjectManipulator.addIssueTypeScreenSchemeToProject(project, fieldScreenSchemeCVOOTo4D, JiraObjectGetter.getIssueTypeByName(issueTypeCVOOTo4DName + Appendix.ISSUETYPE));
			JiraObjectManipulator.addIssueTypeScreenSchemeToProject(project, fieldScreenSchemeEprints, JiraObjectGetter.getIssueTypeByName(issueTypeEprintsName + Appendix.ISSUETYPE));
			JiraObjectManipulator.addIssueTypeScreenSchemeToProject(project, fieldScreenSchemeRawToOCN, JiraObjectGetter.getIssueTypeByName(issueTypeRawToOCNName + Appendix.ISSUETYPE));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}