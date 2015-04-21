package de.pubflow.jira;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;

import de.pubflow.jira.accessors.JiraObjectCreator;
import de.pubflow.jira.accessors.JiraObjectManipulator;
import de.pubflow.jira.misc.ConditionDefinition;
import de.pubflow.jira.misc.ConditionDefinition.ConditionDefinitionType;
import de.pubflow.jira.misc.CustomFieldDefinition;
import de.pubflow.jira.misc.CustomFieldDefinition.CustomFieldType;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.entity.workflow.WFParameterList;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.core.jira.JiraConnector;
import de.pubflow.server.core.workflow.WorkflowMessage;

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


	private static Logger log = Logger.getLogger(JiraManagerCore.class.getName());


	/**
	 * @throws GenericEntityException
	 * @throws KeyManagementException
	 * @throws UnrecoverableKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static void initPubFlowProject() throws GenericEntityException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException{
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_TITLE, "PubFlow Jira");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_MODE, "Private");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_BASEURL, "http://maui.se.informatik.uni-kiel.de:38080/jira/");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TOP_BGCOLOUR, "#ffffff");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR, "#dedede");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, "#03030d");		
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TOP_TEXTCOLOUR, "#292929");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR, "#ffffff");		
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_MENU_BGCOLOUR, "#00015e");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_MENU_SEPARATOR, "#ffffff");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_MENU_TEXTCOLOUR, "#ffffff");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_HERO_BUTTON_BASEBGCOLOUR, "#00007f");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_HERO_BUTTON_TEXTCOLOUR, "#ffffff");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, "#3b73af");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR, "#292929");
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TEXT_LINKCOLOUR, "#3b73af");

		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_LOGO_URL, "http://www.pubflow.uni-kiel.de/en/de/logo-pubflow/@@images/image/mini");
		
		//TODO: Set mail settings automatically

		try {
			if (ComponentAccessor.getProjectManager().getProjectObjByName("PubFlow") == null) {

				Group groupDataManager = JiraObjectCreator.createGroup("datamanager");
				Group groupScientists = JiraObjectCreator.createGroup("scientists");

				ApplicationUser userPubFlow = JiraObjectCreator.createUser("PubFlow", new BigInteger(130, JiraManagerPlugin.secureRandom).toString(32));
				JiraObjectManipulator.addUserToGroup(userPubFlow, "jira-administrators");
				JiraObjectManipulator.addUserToGroup(userPubFlow, "jira-developers");
				JiraObjectManipulator.addUserToGroup(userPubFlow, "jira-users");
				JiraObjectManipulator.addUserToGroup(userPubFlow, groupScientists);
				JiraObjectManipulator.addUserToGroup(userPubFlow, groupDataManager);

				JiraManagerPlugin.user = userPubFlow;


				//TODO fix deprecation when admin is application user by default
				User userAdmin = ComponentAccessor.getUserManager().getUserObject("admin");
				ComponentAccessor.getCrowdService().removeUser(userAdmin);

				ApplicationUser userRoot = JiraObjectCreator.createUser("root", "$Boogie3");
				JiraObjectManipulator.addUserToGroup(userRoot, groupDataManager);
				JiraObjectManipulator.addUserToGroup(userRoot, groupScientists);
				JiraObjectManipulator.addUserToGroup(userRoot, "jira-administrators");
				JiraObjectManipulator.addUserToGroup(userRoot, "jira-developers");
				JiraObjectManipulator.addUserToGroup(userRoot, "jira-users");

				ApplicationUser userDataManager = JiraObjectCreator.createUser("SampleDataManager", "ilovedata");
				JiraObjectManipulator.addUserToGroup(userDataManager, groupDataManager);
				JiraObjectManipulator.addUserToGroup(userDataManager, groupScientists);
				JiraObjectManipulator.addUserToGroup(userDataManager, "jira-developers");
				JiraObjectManipulator.addUserToGroup(userDataManager, "jira-users");

				ApplicationUser userScientist = JiraObjectCreator.createUser("SampleScientist", "sciencerulez");
				JiraObjectManipulator.addUserToGroup(userScientist, groupScientists);			
				JiraObjectManipulator.addUserToGroup(userScientist, "jira-users");

				JiraObjectCreator.createProject("PubFlow", "PUB", userPubFlow, false);

				List<ConditionDefinition> conditions = new LinkedList<ConditionDefinition>();

				Map<String, String> mapParamsDatamanager = new HashMap<String, String>();
				mapParamsDatamanager.put("group", "datamanager");
				conditions.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsDatamanager, new int[]{21, 81, 141, /*121, 61, */ 71, 91, 111, 151, 131, 161}));

				Map<String, String> mapParamsPubFlow = new HashMap<String, String>();
				mapParamsPubFlow.put("group", "jira-administrators");
				conditions.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsPubFlow, new int[]{41,101}));

				Map<String, String> mapParamsScientists = new HashMap<String, String>();
				mapParamsScientists.put("group", "datamanager");
				conditions.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsScientists, new int[]{1, 11}));

				conditions.add(new ConditionDefinition(ConditionDefinitionType.ATTACHMENT, null, new int[]{11}));

				LinkedList<CustomFieldDefinition> customFields = new LinkedList<CustomFieldDefinition>();
				customFields.add(new CustomFieldDefinition("Leg ID", CustomFieldType.TEXT, true, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("PID", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Login", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Source", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Author", CustomFieldType.TEXT, false, new String[]{"11", "141", "111"}));
				customFields.add(new CustomFieldDefinition("Project", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Topology", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Status", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Target Path", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Reference", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("File Name", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Leg Comment", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Quartz Cron", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("DOI", CustomFieldType.TEXT, false, new String[]{"141", "111"}));
				customFields.add(new CustomFieldDefinition("Author Name", CustomFieldType.TEXT, false, new String[]{"11"}));
				customFields.add(new CustomFieldDefinition("Title", CustomFieldType.TEXT, false, new String[]{"11"}));
				customFields.add(new CustomFieldDefinition("Cruise", CustomFieldType.TEXT, false, new String[]{"11"}));
				customFields.add(new CustomFieldDefinition("Start Time (QUARTZ)", CustomFieldType.DATETIME, false, new String[]{"141", "111"}));

				JiraObjectCreator.createIssueType("PUB", "OCN", userPubFlow, JiraManagerPlugin.getTextResource("/PubFlow.xml"), customFields, conditions);
				JiraObjectCreator.createIssueType("PUB", "EPRINTS", userPubFlow, JiraManagerPlugin.getTextResource("/EPRINTS.xml"), new LinkedList<CustomFieldDefinition>(), new LinkedList<ConditionDefinition>());
				JiraObjectCreator.createIssueType("PUB", "CVOO", userPubFlow, JiraManagerPlugin.getTextResource("/PubFlow.xml"), customFields, conditions);


				WorkflowMessage wm = new WorkflowMessage();
				wm.setWorkflowID("de.pubflow.EPRINTS");
				WFParameterList wpl = new WFParameterList();
				WFParameter wp1 = new WFParameter("workflowName", "EPRINTS");		
				WFParameter wp2 = new WFParameter("quartzCron", "*/30 * * * *");		
				wpl.add(wp1);
				wpl.add(wp2);
				wm.setParameters(wpl);
				wm.setType(WFType.BPMN2);
				JiraConnector.getInstance().compute(wm);
			}
			
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " " + e.getCause());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}