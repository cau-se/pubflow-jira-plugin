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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

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
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import de.pubflow.common.PropLoader;
import de.pubflow.jira.accessors.JiraObjectCreator;
import de.pubflow.jira.accessors.JiraObjectGetter;
import de.pubflow.jira.accessors.JiraObjectManipulator;
import de.pubflow.jira.misc.CustomFieldDefinition;
import de.pubflow.server.core.workflow.WorkflowBroker;
import de.pubflow.server.core.workflow.types.AbstractWorkflow;
import de.pubflow.server.core.workflow.types.CVOOTo4DIDWorkflow;
import de.pubflow.server.core.workflow.types.EPrintsWorkflow;
import de.pubflow.server.core.workflow.types.RawToOCNWorkflow;

/**
 * 
 * @author arl
 *
 *         Jira Manager Core
 * 
 *         Still some work to do: - add some more features - proper exception
 *         handling.
 *
 *         - prevent and fix usage of generic types in newIssueType(),
 *         initProject(...)! (deprecated) - loads of other things....
 *
 *
 *         Things to mind: Jira objects are updated automatically when
 *         setXy(...) is used.
 *
 */

public class JiraManagerInitializer {

	public static List<CustomField> customFieldsCache = new LinkedList<CustomField>();
	private static Logger log = Logger.getLogger(JiraManagerInitializer.class);
	private static final ProjectManager projectManager = ComponentAccessor.getProjectManager();

	/**
	 * Creates a new Jira project.
	 * 
	 * @param projectName
	 *            : the name of the new project
	 * @param projectKey
	 *            : the project's key
	 * @param user
	 *            : the ApplicationUser that holds the project (lead of the
	 *            project)
	 * 
	 * @return returns the created project object.
	 * @throws Exception
	 */

	public static Project initProject(String projectName, String projectKey, ApplicationUser user, boolean kill)
			throws Exception {

		log.debug("initProject - projectName : " + projectName + " / projectKey : " + projectKey + " / kill : " + kill);
		final PermissionSchemeManager permissionSchemeManager = ComponentAccessor.getPermissionSchemeManager();

		if (user != null) {
			log.debug("initProject - user : " + user.getUsername());
		} else {
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
			ProjectCreationData projectData = new ProjectCreationData.Builder().withName(projectName).withLead(user)
					.withKey(projectKey).withDescription("Data Pulication Workflows").withType("business")
					.withAvatarId(new Long(avatarId)).build();
			project = projectManager.createProject(user, projectData);
			permissionSchemeManager.addDefaultSchemeToProject(project);
			ComponentAccessor.getNotificationSchemeManager().addDefaultSchemeToProject(project);
			Scheme notificationScheme = ComponentAccessor.getNotificationSchemeManager().getSchemeFor(project);
			
	
			log.info("initProject: created a new project with projectKey " + projectKey);
		} else {
			log.debug("initProject: project with projectKey " + projectKey + " already exists");

		}

		return project;
	}

	/**
	 * Initializes the issue types, issue type scheme and maps them to a project.
	 *
	 * @author abar
	 * @param projectKey
	 *            : the project's key we add the issue type and scheme to
	 * @param issueTypeName
	 *            : the name of the issue type we want to create
	 */
	public static void initIssueManagement(String projectKey, String issueTypeName, String workflowID)
			throws CreateException {
		final Project project = projectManager.getProjectObjByKey(projectKey);
		JiraObjectCreator.createIssueType(project, issueTypeName, workflowID);
		final FieldConfigScheme issueTypeScheme = JiraObjectCreator.createIssueTypeScheme(project);

		JiraObjectManipulator.addIssueTypeSchemeToProject(issueTypeScheme, project);
	}

	/**
	 * 
	 * @param customFields
	 * @param issueTypeName
	 * @param customFieldIdsTest
	 * @param project
	 * @return
	 * @throws Exception
	 */
	public static void initHumbleScreens(List<CustomFieldDefinition> customFields, String issueTypeName,
			List<Long> customFieldIdsTest, Project project) throws Exception {
		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issueTypeName);
		Map<String, LinkedList<CustomFieldDefinition>> availableActionFieldScreens = new HashMap<String, LinkedList<CustomFieldDefinition>>();

		final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();

		for (CustomFieldDefinition customFieldDefinition : customFields) {
			for (String id : customFieldDefinition.getScreens()) {
				// 'init' new screens, the id in the map represents the Screen
				// (id)
				if (availableActionFieldScreens.get(id) == null) {
					LinkedList<CustomFieldDefinition> sameKeyDefs = new LinkedList<CustomFieldDefinition>();
					sameKeyDefs.add(customFieldDefinition);
					availableActionFieldScreens.put(id, sameKeyDefs);
					// add to existing screens
				} else {
					availableActionFieldScreens.get(id).add(customFieldDefinition);
				}
				log.debug("initHumbleScreens: transition screen grouping loops / id : " + id + " / name : "
						+ customFieldDefinition.getName());
			}
		}

		for (Entry<String, LinkedList<CustomFieldDefinition>> e : availableActionFieldScreens.entrySet()) {
			List<String> customFieldIds = new LinkedList<String>();

			for (CustomFieldDefinition c : e.getValue()) {
				log.debug("initHumbleScreens: transition screen id loops / c.getName() : " + c.getName() + "_"
						+ issueTypeName);
				Collection<CustomField> l = customFieldManager.getCustomFieldObjectsByName(c.getName());

				// should be created beforehand
				if (l != null) {
					customFieldIds.add(l.iterator().next().getId());
				} else {
					log.error("initHumbleScreens: custom field is null / c.getName() : " + c.getName() + "_"
							+ issueTypeName);
				}
			}

			FieldScreen fieldScreen = JiraObjectCreator.createActionScreen(issueTypeName + "Action" + e.getKey());
			List<FieldScreenTab> fieldScreenTabs = fieldScreen.getTabs();
			FieldScreenTab jobTab = null;
			for (FieldScreenTab fieldScreenTab : fieldScreenTabs) {
				if (fieldScreenTab.getName().equals("Job")) {
					jobTab = fieldScreenTab;
				}
			}

			if (jobTab == null) {
				jobTab = fieldScreen.addTab("Job");
			}

			for (String s : customFieldIds) {
				if (jobTab.getFieldScreenLayoutItem(s) == null) {
					jobTab.addFieldScreenLayoutItem(s);
				}
			}

				Collection<ActionDescriptor> allActions = jiraWorkflow.getAllActions();
				Map<String, String> metaAttributes = new HashMap<String, String>();
				metaAttributes.put("jira.fieldscreen.id", Long.toString(fieldScreen.getId()));
				for (ActionDescriptor aActions : allActions) {
					if (aActions.getId() == Integer.parseInt(e.getKey())) {
						aActions.setView(fieldScreen.getName());
						aActions.setMetaAttributes(metaAttributes);
					}
				}

			}
		}

	

	/**
	 * Initializes the workflow, workflow scheme and maps them to a project.
	 *
	 * @author abar
	 * @param projectKey
	 *            : the project's key we add the issue type and scheme to
	 * @param workflowXML
	 *            : the path to a XML that defines the workflow
	 * @param user
	 *            : the ApplicationUser that can create a workflow
	 *            (administrator in general)
	 */
	private static JiraWorkflow initWorkflow(String projectKey, String workflowXML, ApplicationUser user,
			String issueTypeName) {
		if (user != null) {
			log.debug("initWorkflow: user : " + user.getUsername());
		} else {
			log.error("initWorkflow: user is null");
		}

		JiraWorkflow jiraWorkflow = JiraObjectCreator.addWorkflow(projectKey, workflowXML, user, issueTypeName);
		WorkflowScheme workflowScheme = JiraObjectCreator.createWorkflowScheme(projectKey, user, jiraWorkflow,
				issueTypeName);
		JiraObjectManipulator.addWorkflowToProject(workflowScheme, projectManager.getProjectObjByKey(projectKey));

		return jiraWorkflow;

	}

	/**
	 * Initializes the Look&Feel
	 */
	public static void initJiraSettings() {
		final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
		applicationProperties.setString(APKeys.JIRA_BASEURL,
				PropLoader.getInstance().getProperty("JIRA_BASEURL", JiraManagerInitializer.class));
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

		// SMTPMailServerImpl smtp = new SMTPMailServerImpl();
		// smtp.setName("Mail Server");
		// smtp.setDescription("");
		// smtp.setDefaultFrom("pubflow@bough.de");
		// smtp.setPrefix("[pubflow]");
		// smtp.setPort("587");
		// smtp.setMailProtocol(MailProtocol.SMTP);
		// smtp.setHostname("mail.bough.de");
		// smtp.setUsername("wp10598327-pubflow");
		// smtp.setPassword("kidoD3l77");
		// smtp.setTlsRequired(true);
		// try {
		// ComponentAccessor.getMailServerManager().create(smtp);
		// } catch (MailException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	/**
	 * Initializes the whole PubFlow project.
	 * 
	 * Set application properties v "PubFlow" project will be initialized. v
	 * User groups "datamanager" and "scientists" are created v Users "PubFlow"
	 * and "root" will created and added to all user groups. v Statuses will be
	 * created v IssueTypes and Schemes will be created (need statuses) v Create
	 * Workflow and Scheme (needs statuses, project, and issue types) v Create
	 * CustomField v Create all Screens (needs screen names, issue types, custom
	 * fields, and a project) v Map screen schemes to a given project (needs the
	 * project, the issue type, and the screen for the issue type).
	 * 
	 * @author arl, abar
	 * 
	 */
	public static void initPubFlowProject() throws GenericEntityException, KeyManagementException,
			UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
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

				JiraDefaultUser defaultUserCreator = new JiraDefaultUser(project, projectKey);
				// Add users and return the project owner
				ApplicationUser owningUser = defaultUserCreator.addDefaultUser();

				log.debug("initPubfowProject: created users and usergroups for PubFlow");

				project = initProject("PubFlow", projectKey, owningUser, false);
			}

			List<String> statuses = new LinkedList<String>();

			// the order of the statuses is important for the id
			// the id has to be the same as in the xml for jira
			// this is the way how jira does things

			// should already exist in Jira with ID=1
			statuses.add("Open");

			// Ready for Convertion by Data Management ID: 10000
			// quickfix: 10100
			statuses.add("Ready for Convertion by Data Management");
			// Ready for OCN-Import already ID: 10001
			// quickfix: 10101
			statuses.add("CVOO-Import");
			// Prepare for PubFlow ID: 10002
			// quickfix: 10102
			statuses.add("Prepared for PubFlow");
			// Data Processing by PubFlow ID: 10003
			// quickfix: 10103
			statuses.add("Data Processing by PubFlow");
			// Ready for Pangaea-Import ID: 10004
			// quickfix: 10104
			statuses.add("Ready for Pangaea-Import");
			// Data Needs Correction ID: 10005
			// quickfix: 10105
			statuses.add("Data Needs Correction");
			// Waiting for DOI ID: 10006
			// quickfix: 10106
			statuses.add("Waiting for DOI");
			// should already exist in Jira with ID=6
			statuses.add("Closed");
			// Done ID: 10007
			// quickfix:10001
			statuses.add("Done");
			// Rejected ID: 10008
			// quickfix: 10107
			statuses.add("Rejected");
			// Pangaea Data Upload ID:10009
			// quickfix: 10108
			statuses.add("Pangaea Data Upload");
			// should be (test it):
			// Pangaea Data Upload ID:10010
			// quickfix: 10109
			statuses.add("Aquire ORCIDs");
			

			// add new statuses at the end
			// TODO is there a more generic solution?

			JiraObjectCreator.addStatuses(projectKey, statuses);

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		ApplicationUser user = userManager.getUserByName("PubFlow");

		// add workflows

		List<AbstractWorkflow> workflowsToAdd = new LinkedList<>();
		workflowsToAdd.add(new EPrintsWorkflow());
		// workflowsToAdd.add(new OCNTo4DWorkflow());
		// workflowsToAdd.add(new CVOOTo4DWorkflow());
		workflowsToAdd.add(new CVOOTo4DIDWorkflow());
		workflowsToAdd.add(new RawToOCNWorkflow());

		// for testing purposes
		// workflowsToAdd.add(new OldOCNWorkflow());

		// add the workflows one after another
		for (AbstractWorkflow workflow : workflowsToAdd) {
				try {
					addNewWorkflow(projectKey, workflow, project, user);
				} catch (Exception e) {
					log.info("Could not add Workflow: " + workflow.getWorkflowName());
					log.debug("", e);
				}
		}

	}

	private static void addNewWorkflow(String projectKey, AbstractWorkflow workflow, Project project,
			ApplicationUser user) throws Exception {
		log.info("Adding Worklow: " + workflow.getWorkflowName() + "to project key: " + projectKey);
		log.info("Using XML file located at: " + workflow.getJiraWorkflowXMLPath());

		if (project == null) {
			project = projectManager.getProjectObjByKey(projectKey);
		}

		String workflowName = workflow.getWorkflowName();
		final WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
		JiraWorkflow jiraWorkflow = workflowManager.getWorkflow(workflowName);

		String workflowXMLString = JiraManagerPlugin.getTextResource(workflow.getJiraWorkflowXMLPath());
		initIssueManagement(projectKey, workflowName, workflow.getWorkflowID());

		// TODO verify if this step does the right thing
		jiraWorkflow = createWorkflowAndMapIssueTypeIDs(projectKey, workflow, user, workflowXMLString);

		IssueType issueType = JiraObjectGetter.getIssueTypeByName(workflowName);
		WorkflowSchemeManager workflowSchemeManager = ComponentAccessor.getWorkflowSchemeManager();

		try {
			workflowSchemeManager.addWorkflowToScheme(workflowSchemeManager.getWorkflowScheme(project),
					jiraWorkflow.getName(), issueType.getId());
			log.info("initWorkflow: added the workflow: " + jiraWorkflow.getName()
					+ " to the workflowscheme of the project: " + project.getName());
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			log.error("initWorkflow: Couldn't  add the workflow: " + jiraWorkflow.getName()
					+ " to the workflowscheme of the project: " + project.getName());
			e.printStackTrace();
		}
		
		List<CustomFieldDefinition> customFields = workflow.getCustomFields();
		List<Long> customFieldIds = JiraObjectCreator.createCustomFields(customFields, project, workflowName);
		// TODO use screenNames in initHumbleScreens
		// List<String> screenNames = workflow.getScreenNames();
		FieldScreenScheme fieldScreenScheme = createBasicFieldScreens(workflow);
//		FieldScreenScheme fieldScreenScheme = initHumbleScreens(customFields, workflowName, customFieldIds, project);
		initHumbleScreens(customFields, workflowName, customFieldIds, project);

		JiraObjectManipulator.addIssueTypeScreenSchemeToProject(project, fieldScreenScheme, issueType);

		// register Workflow with WorkflowBroker
		// this should happen even if the Workflow is already saved by jira
		WorkflowBroker.addWorkflow(workflow);

	}

	/**
	 * Gathers the steps defined in the Workflow XML files and maps the id's of
	 * the status accordingly
	 * 
	 * @param projectKey
	 * @param workflow
	 * @param user
	 * @param workflowXMLString
	 */
	@SuppressWarnings("unchecked")
	private static JiraWorkflow createWorkflowAndMapIssueTypeIDs(String projectKey, AbstractWorkflow workflow,
			ApplicationUser user, String workflowXMLString) {

		String issueTypeName = workflow.getWorkflowName();

		LinkedList<String> statuses = JiraManagerPlugin.getSteps(workflowXMLString);

		JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(issueTypeName);

		// only create workflow, if it doesn't exist
		if (jiraWorkflow != null) {
			log.info("Issuetype ID mapping: " + issueTypeName + " expected, but doesnt exist");
			return jiraWorkflow;

		}

		// jiraWorkflow = initWorkflow(projectKey, workflowXMLString, user,
		// issueTypeName);
		jiraWorkflow = JiraObjectCreator.addWorkflow(projectKey, workflowXMLString, user, issueTypeName);

		log.debug("Issuetype ID mapping:  " + issueTypeName);

		Map<String, String> statusMap = JiraObjectCreator.addStatuses(projectKey, statuses);

		// Map Steps to Statuses
		// for each defined step lookup the meta attributes and update the
		// jira.status.id accordingly
		for (StepDescriptor step : (List<StepDescriptor>) jiraWorkflow.getDescriptor().getSteps()) {
			log.info("newIssueType - step : " + step.getId() + " / " + step.getName());
			String stepName = step.getName();
			String id = "";

			if (statuses.contains(stepName)) {
				id = statusMap.get(stepName);
				log.info("newIssueType - step : workflow contains step " + stepName);
			}
			// TODO can harm be done if some status exists here and no id is
			// there?
			for (Entry<String, String> entry : (Set<Entry<String, String>>) step.getMetaAttributes().entrySet()) {
				if (entry.getKey().equals("jira.status.id")) {
					step.getMetaAttributes().put("jira.status.id", id);
					log.info("newIssueType - setting jira.status.id : " + id + " in step");
				}
			}

			// ComponentAccessor.getWorkflowManager().updateWorkflow(issueTypeName
			// + WORKFLOW_APPENDIX, jiraWorkflow);
			log.info("newIssueType - updating workflow " + jiraWorkflow.getName() + " / user : " + user.getName());
			ComponentAccessor.getWorkflowManager().updateWorkflow(user, jiraWorkflow);
			mapScreensAndActions(step);
		}

		WorkflowScheme workflowScheme = JiraObjectCreator.createWorkflowScheme(projectKey, user, jiraWorkflow,
				issueTypeName);
		JiraObjectManipulator.addWorkflowToProject(workflowScheme, projectManager.getProjectObjByKey(projectKey));

		return jiraWorkflow;
	}
	
	private static FieldScreenScheme createBasicFieldScreens(AbstractWorkflow workflow) throws Exception {
		FieldScreen fieldScreenCreate = JiraObjectCreator.createActionScreen(workflow.getScreenNames().get("create"));
		FieldScreen fieldScreenView = JiraObjectCreator.createActionScreen(workflow.getScreenNames().get("view"));
		FieldScreen fieldScreenEdit = JiraObjectCreator.createActionScreen(workflow.getScreenNames().get("edit"));
	
		FieldScreenScheme fieldScreenScheme = JiraObjectCreator.generateNewFieldScreenScheme(fieldScreenCreate,
				fieldScreenView, fieldScreenEdit, workflow.getWorkflowName());

		return fieldScreenScheme;
		
	}
	
	@SuppressWarnings("unchecked")
	private static void mapScreensAndActions(StepDescriptor stepDescriptor) {
		
		List<ActionDescriptor> oActions = stepDescriptor.getActions();
		
		
		for(ActionDescriptor oAction : oActions)
		{
		    if(oAction.getView().equals("fieldscreen"))
		    {
		       log.info("vllt");
		    	// do things with oAction.GetId()...
		    }
		}
		// ActionDescriptor actionDescriptor =
		// jiraWorkflow.getDescriptor().getAction(Integer.parseInt(e.getKey()));
		// actionDescriptor.setView(fieldScreen.getName());
		// actionDescriptor.getMetaAttributes().put("jira.fieldscreen.id",
		// fieldScreen.getId());
		// }
	}
	
}