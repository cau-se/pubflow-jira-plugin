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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.service.services.mail.MailServersValuesGenerator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.atlassian.mail.server.managers.XMLMailServerManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;

import de.pubflow.jira.accessors.JiraObjectCreator;
import de.pubflow.jira.accessors.JiraObjectGetter;
import de.pubflow.jira.accessors.JiraObjectManipulator;
import de.pubflow.jira.misc.ConditionDefinition;
import de.pubflow.jira.misc.ConditionDefinition.ConditionDefinitionType;
import de.pubflow.jira.misc.CustomFieldDefinition;
import de.pubflow.jira.misc.CustomFieldDefinition.CustomFieldType;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.properties.PropLoader;
import de.pubflow.server.common.repository.ScheduledWorkflowProvider;
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

public class JiraManagerInitializer implements InitializingBean, DisposableBean{

	public static List<CustomField> customFieldsCache = new LinkedList<CustomField>();
	private static Logger log = Logger.getLogger(JiraManagerInitializer.class);
	private static boolean inited = false;
	private static EventPublisher eventPublisher;
	
	public JiraManagerInitializer(EventPublisher eventPublisher) {
		JiraManagerInitializer.eventPublisher = eventPublisher;
	}
	
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
		log.info("Init");
	
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_BASEURL, 
				PropLoader.getInstance().getProperty( "JIRA_BASEURL", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_MODE, 
				PropLoader.getInstance().getProperty("JIRA_MODE", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_TITLE, 
				PropLoader.getInstance().getProperty("JIRA_TITLE", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TOP_BGCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TOP_BGCOLOUR", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TOP_HIGHLIGHTCOLOR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TOP_HIGHLIGHTCOLOR", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TOP_SEPARATOR_BGCOLOR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TOP_SEPARATOR_BGCOLOR", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TOP_TEXTCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TOP_TEXTCOLOUR", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TOP_TEXTHIGHLIGHTCOLOR", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_MENU_BGCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_MENU_BGCOLOUR", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_MENU_SEPARATOR, 
				PropLoader.getInstance().getProperty("JIRA_LF_MENU_SEPARATOR", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_MENU_TEXTCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_MENU_TEXTCOLOUR", JiraManagerInitializer.class));	
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_HERO_BUTTON_BASEBGCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_HERO_BUTTON_BASEBGCOLOUR", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_HERO_BUTTON_TEXTCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_HERO_BUTTON_TEXTCOLOUR", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TEXT_ACTIVE_LINKCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TEXT_ACTIVE_LINKCOLOUR", JiraManagerInitializer.class));
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TEXT_HEADINGCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TEXT_HEADINGCOLOUR", JiraManagerInitializer.class));		
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_TEXT_LINKCOLOUR, 
				PropLoader.getInstance().getProperty("JIRA_LF_TEXT_LINKCOLOUR", JiraManagerInitializer.class));		
		ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_LF_LOGO_URL, 
				PropLoader.getInstance().getProperty("JIRA_LF_LOGO_URL", JiraManagerInitializer.class));		


		//TODO: Set mail settings automatically

		try {
			if (ComponentAccessor.getProjectManager().getProjectObjByName("PubFlow") == null) {

				SMTPMailServerImpl smtp = new SMTPMailServerImpl();
				smtp.setName("Mail Server");
				smtp.setDescription("");
				smtp.setDefaultFrom("pubflow@bough.de");
				smtp.setPrefix("[pubflow]");
				smtp.setPort("587");
				smtp.setMailProtocol(MailProtocol.SMTP);
				smtp.setHostname("mail.bough.de");
				smtp.setUsername("wp10598327-null");
				smtp.setPassword("kidoD3l77");
				smtp.setTlsRequired(true);
				ComponentAccessor.getMailServerManager().create(smtp);
				
				Group groupDataManagers = JiraObjectCreator.createGroup("datamanagers");
				Group groupScientists = JiraObjectCreator.createGroup("scientists");
				Group groupSuperUsers = JiraObjectCreator.createGroup("superusers");
				
				ApplicationUser userPubFlow = JiraObjectCreator.createUser("PubFlow", "$Boogie3", "", "PubFlow");
				JiraObjectManipulator.addUserToGroup(userPubFlow, "jira-administrators");
				JiraObjectManipulator.addUserToGroup(userPubFlow, "jira-developers");
				JiraObjectManipulator.addUserToGroup(userPubFlow, "jira-users");
				JiraObjectManipulator.addUserToGroup(userPubFlow, groupScientists);
				JiraObjectManipulator.addUserToGroup(userPubFlow, groupDataManagers);

				JiraManagerPlugin.user = userPubFlow;

				//TODO fix deprecation when admin is application user by default
				User userAdmin = ComponentAccessor.getUserManager().getUserObject("admin");
				ComponentAccessor.getCrowdService().removeUser(userAdmin);

				//Admins
				
//				ApplicationUser userRoot = JiraObjectCreator.createUser("root", "$Boogie3", "plumhoff@email.uni-kiel.de", "Root");
//				JiraObjectManipulator.addUserToGroup(userRoot, groupDataManagers);
//				JiraObjectManipulator.addUserToGroup(userRoot, groupScientists);
//				JiraObjectManipulator.addUserToGroup(userRoot, groupSuperUsers);
//				JiraObjectManipulator.addUserToGroup(userRoot, "jira-administrators");
//				JiraObjectManipulator.addUserToGroup(userRoot, "jira-developers");
//				JiraObjectManipulator.addUserToGroup(userRoot, "jira-users");
				
				//Datamanagers
				
				ApplicationUser userDataManager0 = JiraObjectCreator.createUser("SampleDataManager", "test1234", "plumhoff@email.uni-kiel.de", "");
				JiraObjectManipulator.addUserToGroup(userDataManager0, groupDataManagers);
				JiraObjectManipulator.addUserToGroup(userDataManager0, groupScientists);
				JiraObjectManipulator.addUserToGroup(userDataManager0, "jira-developers");
				JiraObjectManipulator.addUserToGroup(userDataManager0, "jira-users");

				ApplicationUser userDataManager1 = JiraObjectCreator.createUser("HMehrtens", "NFwjHo8ie7", "", "Hela Mehrtens");
				JiraObjectManipulator.addUserToGroup(userDataManager1, groupDataManagers);
				JiraObjectManipulator.addUserToGroup(userDataManager1, groupScientists);
				JiraObjectManipulator.addUserToGroup(userDataManager1, "jira-developers");
				JiraObjectManipulator.addUserToGroup(userDataManager1, "jira-users");

				
				//Scientists
				
				ApplicationUser userScientist0 = JiraObjectCreator.createUser("SampleScientist", "test1234", "plumhoff@email.uni-kiel.de", "Mr. Sample");
				JiraObjectManipulator.addUserToGroup(userScientist0, groupScientists);			
				JiraObjectManipulator.addUserToGroup(userScientist0, "jira-users");

//				ApplicationUser userScientist1 = JiraObjectCreator.createUser("BFiedler", "jhACbmfUPv", "", "Björn Fiedler");
//				JiraObjectManipulator.addUserToGroup(userScientist1, groupScientists);			
//				JiraObjectManipulator.addUserToGroup(userScientist1, "jira-users");
				
				ApplicationUser userScientist2 = JiraObjectCreator.createUser("SMilinski", "jhACbmfUPv", "", "Sebastian Milinski");
				JiraObjectManipulator.addUserToGroup(userScientist2, groupScientists);			
				JiraObjectManipulator.addUserToGroup(userScientist2, "jira-users");
				
				JiraObjectCreator.createProject("PubFlow", "PUB", userPubFlow, false);

				List<ConditionDefinition> conditionsRawToOCN = new LinkedList<ConditionDefinition>();
				List<ConditionDefinition> conditionsOCNTo4D = new LinkedList<ConditionDefinition>();

				Map<String, String> mapParamsDatamanager = new HashMap<String, String>();
				mapParamsDatamanager.put("group", "datamanagers");
				
				Map<String, String> mapParamsPubFlow = new HashMap<String, String>();
				mapParamsPubFlow.put("group", "superusers");
			
				Map<String, String> mapParamsScientists = new HashMap<String, String>();
				mapParamsScientists.put("group", "scientists");
				
				conditionsRawToOCN.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsDatamanager, new int[]{21, 81, 161, 171, 71, 91}));	
				conditionsRawToOCN.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsScientists, new int[]{11}));
				conditionsRawToOCN.add(new ConditionDefinition(ConditionDefinitionType.ATTACHMENT, null, new int[]{11}));
				
				conditionsOCNTo4D.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsDatamanager, new int[]{171, 71, 91, 111, 151, 131, 191}));
				conditionsOCNTo4D.add(new ConditionDefinition(ConditionDefinitionType.USERINGROUP, mapParamsPubFlow, new int[]{41,101}));
				
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
								
				JiraObjectCreator.createIssueType("PUB", "EPRINTS", userPubFlow, JiraManagerPlugin.getTextResource("/EPRINTS.xml"), new LinkedList<CustomFieldDefinition>(), new LinkedList<ConditionDefinition>(), "de.pubflow.EPRINTS");
				JiraObjectCreator.createIssueType("PUB", "Publish Raw Cruise Data", userPubFlow, JiraManagerPlugin.getTextResource("/RAWTOCVOO-WORKFLOW.xml"), customFieldsRawToOCN, conditionsRawToOCN, "");
				JiraObjectCreator.createIssueType("PUB", "Export Data (CVOO) to PANGAEA", userPubFlow, JiraManagerPlugin.getTextResource("/OCNTO4D-WORKFLOW.xml"), customFieldsOCNTo4D, conditionsOCNTo4D, "de.pubflow.CVOO");
				JiraObjectCreator.createIssueType("PUB", "Export Data (OCN) to PANGAEA", userPubFlow, JiraManagerPlugin.getTextResource("/OCNTO4D-WORKFLOW.xml"), customFieldsOCNTo4D, conditionsOCNTo4D, "de.pubflow.OCN");

				WorkflowMessage eprintsWfMsg = new WorkflowMessage();
				eprintsWfMsg.setWorkflowID("de.pubflow.EPRINTS");
				List<WFParameter> wpl = new LinkedList<WFParameter>();
				WFParameter wp1 = new WFParameter("workflowName", "EPRINTS");		
				WFParameter wp2 = new WFParameter("quartzCron", "*/60 * * * *");		
				wpl.add(wp1);
				wpl.add(wp2);
				eprintsWfMsg.setParameters(wpl);
				eprintsWfMsg.setType(WFType.BPMN2);
				
				ScheduledWorkflowProvider.getInstance().clear();
				ScheduledWorkflowProvider.getInstance().addEntry(eprintsWfMsg);
			}

		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " " + e.getCause());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@EventListener
	public void init(PluginEnabledEvent event) {
		if(inited == false){
			JiraManagerPlugin.user = JiraObjectGetter.getUserByName("PubFlow");

			if(event.getPlugin().getKey().equals("de.pubflow.jira")){
				if(PropLoader.getInstance().getProperty("INITED", this.getClass()).equals("false")){
					try {
						JiraManagerInitializer.initPubFlowProject();
						inited = true;
					} catch (KeyManagementException | UnrecoverableKeyException
							| GenericEntityException | NoSuchAlgorithmException
							| KeyStoreException | CertificateException | IOException e1) {
						log.error(e1.getLocalizedMessage() + " " + e1.getCause());
						e1.printStackTrace();
					}
					PropLoader.getInstance().setProperty("INITED", this.getClass(), "true");
				}

				List<WorkflowMessage> scheduledWfs = ScheduledWorkflowProvider.getInstance().getAllScheduledWorkflows();
				for(WorkflowMessage wm : scheduledWfs){
					JiraConnector.getInstance().compute(wm);
				}
			}
		}
	}
	
	/**
	 * Called when the plugin has been enabled.
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// register ourselves with the EventPublisher
		eventPublisher.register(this);
	}

	/**
	 * Called when the plugin is being disabled or removed.
	 * @throws Exception
	 */
	@Override
	public void destroy() throws Exception {
		// unregister ourselves with the EventPublisher
		eventPublisher.unregister(this);
	}


}