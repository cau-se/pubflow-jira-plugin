package de.pubflow.jira;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map.Entry;

import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;

import de.pubflow.jira.misc.InternalConverterMsg;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.entity.workflow.WFParameterList;
import de.pubflow.server.core.communication.WorkflowMessage;
import de.pubflow.server.core.jira.JiraConnector;


/**
 * Simple JIRA listener using the atlassian-event library and demonstrating
 * plugin lifecycle integration.
 */
public class JiraManagerPlugin implements InitializingBean, DisposableBean  {
	private static final Logger log = LoggerFactory.getLogger(JiraManagerPlugin.class);

	protected static IssueTypeManager issueTypeManager;
	protected static EventPublisher eventPublisher;
	protected static FieldScreenSchemeManager fieldScreenSchemeManager;
	protected static StatusManager statusManager;
	public static ApplicationUser user = JiraManagerCore.getUserByName("PubFlow");

	protected static final SecureRandom secureRandom = new SecureRandom();


	/**
	 * Constructor.
	 * @param eventPublisher injected {@code EventPublisher} implementation.
	 */
	public JiraManagerPlugin(EventPublisher eventPublisher, IssueTypeManager issueTypeManager, FieldScreenSchemeManager fieldScreenSchemeManager, StatusManager statusManager) {	
		log.debug("Plugin started");
		JiraManagerPlugin.issueTypeManager = issueTypeManager;
		JiraManagerPlugin.fieldScreenSchemeManager = fieldScreenSchemeManager;
		JiraManagerPlugin.statusManager = statusManager;

		JiraManagerPlugin.eventPublisher = eventPublisher;
	}

	static String getTextResource(String resourceName){ 
		StringBuffer content = new StringBuffer();

		try{
			String line;
			InputStream rs = JiraManagerPlugin.class.getResourceAsStream(resourceName);
			BufferedReader in = new BufferedReader(new InputStreamReader(rs, "UTF-8"));
			while((line = in.readLine()) != null) content.append(line).append("\n");
			in.close();

		}catch(Exception e){
			e.printStackTrace();
		}
		return content.toString();
	}

	@EventListener
	public void init(PluginModuleEnabledEvent e){
		try {
			JiraManagerCore.initPubFlowProject();
		} catch (KeyManagementException | UnrecoverableKeyException
				| GenericEntityException | NoSuchAlgorithmException
				| KeyStoreException | CertificateException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		user = JiraManagerCore.getUserByName("PubFlow");

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


	/**
	 * Receives any {@code IssueEvent}s sent by JIRA.
	 * @param issueEvent the IssueEvent passed to us
	 * @throws Exception 
	 */

	@EventListener
	public void onIssueEvent(IssueEvent issueEvent) {
		InternalConverterMsg msg = new InternalConverterMsg(issueEvent);

		if(
				//(issueEvent.getEventTypeId().equals( EventType.ISSUE_CREATED_ID) && ComponentAccessor.getWorkflowManager().getWorkflow(issueEvent.getIssue()).getName() != "jira") ||
				issueEvent.getIssue().getStatusObject().getName().equals("Data Processing by PubFlow")){

			try {
				WorkflowMessage wm = new WorkflowMessage();
				WFParameterList wfpm = new WFParameterList();				
				
				for(Entry<String, String> e : msg.getValues().entrySet()) {
					WFParameter wfp = new WFParameter();
					wfp.setKey(e.getKey());
					wfp.setStringValue(e.getValue());
					wfpm.add(wfp);
				}
				wm.setParameters(wfpm);
				wm.setWorkflowID(1);
				JiraConnector jpmp = JiraConnector.getInstance();
				jpmp.compute(wm);
				
			} catch (Exception e){
				JiraManagerCore.addIssueComment(issueEvent.getIssue().getKey(), e.getClass().getSimpleName() + e.getMessage(), user);
			}
		}else if(issueEvent.getEventTypeId().equals( EventType.ISSUE_UPDATED_ID)){
			//TODO
		}else if(issueEvent.getIssue().getStatusObject().getName().equals("Open")){
			if(ComponentAccessor.getCommentManager().getComments(issueEvent.getIssue()).size() == 0){
				ComponentAccessor.getCommentManager().create(issueEvent.getIssue(), user, "Dear " + issueEvent.getUser().getName() +" (" + issueEvent.getUser().getName() +  "),\n please append your raw data as an file attachment to this issue and provide the following information about your data as a comment:\nTitle, Authors, Cruise\n\nAfter that you can start the processing by pressing the \"Send to Data Management\" button. \nFor demonstration purposes an attachment has been added automatically.\nThank you!", false);
				JiraManagerCore.addAttachment(issueEvent.getIssue().getKey(), new byte[]{0}, "rawdata", "txt", user);
			}else{
				MutableIssue issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueEvent.getIssue().getKey());
				issue.setAssignee(issueEvent.getIssue().getReporter());
			}
		}

	}
}
