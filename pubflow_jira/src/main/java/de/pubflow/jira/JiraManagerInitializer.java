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
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.opensymphony.workflow.loader.ActionDescriptor;

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
	    final PermissionSchemeManager permissionSchemeManager = ComponentAccessor.getPermissionSchemeManager();
	    if (projectKey.length() > 4) {
	      throw new Exception("error: project key length > 4 ! ");
	    }

	    Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey);

	    if (project == null) {
	      int avatarId = 10100;
	      ProjectCreationData projectData = new ProjectCreationData.Builder().withName(projectName)
	          .withLead(user).withKey(projectKey).withDescription("Geht so").withType("business")
	          .withAvatarId(new Long(avatarId)).build();
	      project = ComponentAccessor.getProjectManager().createProject(user, projectData);
	      permissionSchemeManager.addDefaultSchemeToProject(project);
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
	public static void initIssueManagement(String projectKey, String issueTypeName)
			throws CreateException {
		final Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey);
		JiraObjectCreator.createIssueType(project, issueTypeName);
		final FieldConfigScheme issueTypeScheme = JiraObjectCreator.createIssueTypeScheme(projectKey, issueTypeName);
		JiraObjectManipulator.addIssueTypeSchemeToProject(issueTypeScheme, project);
	}


	public static FieldScreenScheme initHumbleScreens(List<String> names, List<CustomFieldDefinition> customFields, String issueTypeName, List<Long> customFieldIdsTest, Project project) throws Exception {
		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow("PUB" + Appendix.WORKFLOW);
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
				log.debug("newIssueType - transition screen grouping loops / id : " + id + " / name : "
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
				log.debug("newIssueType - transition screen id loops / c.getName() : " + c.getName() + "_" + issueTypeName);
				String l = customFieldManager.getCustomFieldObjectByName(c.getName()
						+ "_" + issueTypeName).getId();

				if(l != null){
					customFieldIds.add(l);
				}else{
					log.error("newIssueType - custom field is null / c.getName() : " +
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
	public static void initWorkflow(String projectKey, String workflowXML, ApplicationUser user) {
		JiraWorkflow jiraWorkflow = JiraObjectManipulator.addWorkflow(projectKey, workflowXML, user);    
		WorkflowScheme workflowScheme = JiraObjectCreator.createWorkflowScheme(projectKey, user, jiraWorkflow);

		JiraObjectManipulator.addWorkflowToProject(workflowScheme, ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey));

		Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey);
		IssueType ocnIssueType = JiraObjectGetter.getIssueTypeByName("OCN" + Appendix.ISSUETYPE);
		try {
			ComponentAccessor.getWorkflowSchemeManager().addWorkflowToScheme(ComponentAccessor.getWorkflowSchemeManager().getWorkflowScheme(project), jiraWorkflow.getName(), ocnIssueType.getId());
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initialized the whole PubFlow project.
	 * @author arl, abar
	 * 
	 */
	public static void initPubFlowProject()
			throws GenericEntityException, KeyManagementException, UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		final ApplicationProperties applicationPropertiesManager = ComponentAccessor.getApplicationProperties();
		applicationPropertiesManager.setString(APKeys.JIRA_TITLE, "PubFlow Jira");
		applicationPropertiesManager.setString(APKeys.JIRA_MODE, "Private");
		applicationPropertiesManager.setString(APKeys.JIRA_BASEURL, "http://maui.informatik.uni-kiel.de:2990/jira/");
		final String issueTypeName = "OCN";
		final String projectKey = "PUB";
		Project project = ComponentAccessor.getProjectManager().getProjectObjByName("PubFlow");
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
				project = initProject("PubFlow", projectKey, userRoot, false);
			}

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

			initIssueManagement(projectKey, issueTypeName);
			JiraObjectManipulator.addStatuses(projectKey, statuses);
			initWorkflow(projectKey, JiraManagerPlugin.getTextResource("/PubFlow.xml"), ComponentAccessor.getUserManager().getUserByName("PubFlow"));

			List<String> screenNames = new ArrayList<String>();
			screenNames.add(issueTypeName + Appendix.FIELDSCREEN + "ActionCreate");
			screenNames.add(issueTypeName + Appendix.FIELDSCREEN + "ActionEdit");
			screenNames.add(issueTypeName + Appendix.FIELDSCREEN + "ActionView");


			LinkedList<CustomFieldDefinition> customFields = new
					LinkedList<CustomFieldDefinition>();
			customFields.add(new CustomFieldDefinition("Leg ID",
					CustomFieldType.TEXT, true, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("PID",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("Login",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("Source",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("Author",
					CustomFieldType.TEXT, false, new String[]{"11", "141",
			"111"}));
			customFields.add(new CustomFieldDefinition("Project",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("Topology",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("Status",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("Zielpfad",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("Reference",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("File name",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("Leg comment",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("Quartz Cron",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("DOI",
					CustomFieldType.TEXT, false, new String[]{"141", "111"}));
			customFields.add(new CustomFieldDefinition("Author name",
					CustomFieldType.TEXT, false, new String[]{"11"}));
			customFields.add(new CustomFieldDefinition("Title",
					CustomFieldType.TEXT, false, new String[]{"11"}));
			customFields.add(new CustomFieldDefinition("Cruise",
					CustomFieldType.TEXT, false, new String[]{"11"}));
			customFields.add(new CustomFieldDefinition("Start Time (QUARTZ)", CustomFieldType.DATETIME, false, new
					String[]{"141", "111"}));

			List<Long> customFieldIds = JiraObjectCreator.createCustomFields(customFields, project);
			FieldScreenScheme fieldScreenScheme = initHumbleScreens(screenNames, customFields, issueTypeName, customFieldIds, project);
			JiraObjectManipulator.addIssueTypeScreenSchemeToProject(project, fieldScreenScheme, JiraObjectGetter.getIssueTypeByName(issueTypeName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}