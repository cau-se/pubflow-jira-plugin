package de.pubflow.jira;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

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

import de.pubflow.jira.accessors.JiraObjectGetter;
import de.pubflow.jira.accessors.JiraObjectManipulator;
import de.pubflow.jira.misc.InternalConverterMsg;
import de.pubflow.server.PubFlowSystem;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.core.jira.JiraConnector;
import de.pubflow.server.core.workflow.WorkflowMessage;

/**
 * Simple JIRA listener using the atlassian-event library and demonstrating
 * plugin lifecycle integration.
 */
public class JiraManagerPlugin implements InitializingBean, DisposableBean  {
	private static final Logger log = LoggerFactory.getLogger(PubFlowSystem.class.getClass());

	public static IssueTypeManager issueTypeManager;
	public static EventPublisher eventPublisher;
	public static FieldScreenSchemeManager fieldScreenSchemeManager;
	public static StatusManager statusManager;
	public static ApplicationUser user = JiraObjectGetter.getUserByName("PubFlow");
	public static final SecureRandom secureRandom = new SecureRandom();

	/**
	 * Constructor.
	 * @param eventPublisher injected {@code EventPublisher} implementation.
	 */
	public JiraManagerPlugin(EventPublisher eventPublisher, IssueTypeManager issueTypeManager, FieldScreenSchemeManager fieldScreenSchemeManager, StatusManager statusManager) {	
		log.debug("Plugin started");
		PubFlowSystem.getInstance();

		JiraManagerPlugin.issueTypeManager = issueTypeManager;
		JiraManagerPlugin.fieldScreenSchemeManager = fieldScreenSchemeManager;
		JiraManagerPlugin.statusManager = statusManager;
		JiraManagerPlugin.eventPublisher = eventPublisher;
	}


	/**
	 * @param resourceName
	 * @return
	 */
	public static String getTextResource(String resourceName) { 
		StringBuffer content = new StringBuffer();

		try {
			String line;
			InputStream rs = JiraManagerPlugin.class.getResourceAsStream(resourceName);
			BufferedReader in = new BufferedReader(new InputStreamReader(rs, "UTF-8"));
			while ((line = in.readLine()) != null) {
				content.append(line).append("\n");
			}
			in.close();

		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " " + e.getCause());
			e.printStackTrace();
		}
		return content.toString();
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

		if (
				//(issueEvent.getEventTypeId().equals( EventType.ISSUE_CREATED_ID) && ComponentAccessor.getWorkflowManager().getWorkflow(issueEvent.getIssue()).getName() != "jira") ||
				issueEvent.getIssue().getStatusObject().getName().equals("Data Processing by PubFlow")) {

			try {
				WorkflowMessage wm = new WorkflowMessage();
				List<WFParameter> wfpm = new LinkedList<WFParameter>();				

				for (Entry<String, String> e : msg.getValues().entrySet()) {
					WFParameter wfp = new WFParameter(e.getKey(), e.getValue());
					wfpm.add(wfp);
				}

				wm.setParameters(wfpm);
				wm.setWorkflowID("de.pubflow.OCN");
				JiraConnector jpmp = JiraConnector.getInstance();
				jpmp.compute(wm);

			} catch (Exception e) {
				log.error(e.getLocalizedMessage() + " " + e.getCause());
				e.printStackTrace();
				JiraObjectManipulator.addIssueComment(issueEvent.getIssue().getKey(), e.getClass().getSimpleName() + e.getMessage(), user);
			}

		} else if (issueEvent.getEventTypeId().equals( EventType.ISSUE_UPDATED_ID)) {
			//TODO

		} else if (issueEvent.getIssue().getStatusObject().getName().equals("Open")) {
			if (ComponentAccessor.getCommentManager().getComments(issueEvent.getIssue()).size() == 0) {

				String txtmsg = "Dear " + issueEvent.getUser().getName() + " (" + issueEvent.getUser().getName()
						+ "),\n please append your raw data as an file attachment to this issue and provide the following information "
						+ "about your data as a comment:\nTitle, Authors, Cruise\n\nAfter that you can start the processing by pressing the "
						+ "\"Send to Data Management\" button. \nFor demonstration purposes an attachment has been added automatically."
						+ "\nThank you!";

				ComponentAccessor.getCommentManager().create(issueEvent.getIssue(), user, txtmsg, false);
				JiraObjectManipulator.addAttachment(issueEvent.getIssue().getKey(), new byte[]{0}, "rawdata", "txt", user);
			} else {
				MutableIssue issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueEvent.getIssue().getKey());
				issue.setAssignee(issueEvent.getIssue().getReporter());
			}
		}
	}

	/**
	 * @param workflowXMLString
	 * @return
	 */
	public static LinkedList<String> getSteps(String workflowXMLString) {
		StringReader sw = new StringReader(workflowXMLString);

		LinkedList<String> steps = new LinkedList<String>();

		try {
			XMLInputFactory xmlif = XMLInputFactory.newInstance();
			xmlif.setProperty(XMLInputFactory.SUPPORT_DTD, false);

			XMLEventReader xmler = xmlif.createXMLEventReader(sw);

			while (xmler.hasNext()) {
				XMLEvent event = xmler.nextEvent();
				if (event.isStartElement()) {
					String localPart = event.asStartElement().getName().getLocalPart();
					switch (localPart) {
					case "step": 
						try {
							steps.add(event.asStartElement().getAttributeByName(new QName("name")).getValue());
						} catch (NullPointerException e1) {
							log.error(e1.getLocalizedMessage() + " " + e1.getCause());
							e1.printStackTrace();
						}
						break;
					default: break;
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " " + e.getCause());
			e.printStackTrace();
		}
		return steps;
	}
}
