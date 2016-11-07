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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;
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
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.lifecycle.LifecycleAware;

import de.pubflow.jira.accessors.JiraObjectGetter;
import de.pubflow.jira.accessors.JiraObjectManipulator;
import de.pubflow.jira.misc.InternalConverterMsg;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.core.rest.messages.ServiceCallData;
import de.pubflow.server.core.workflow.WorkflowBroker;

/**
 * Simple JIRA listener using the atlassian-event library and demonstrating
 * plugin lifecycle integration.
 */
public class JiraManagerPlugin implements LifecycleAware, InitializingBean, DisposableBean {
	private static final Logger log = LoggerFactory.getLogger(JiraManagerPlugin.class);

	public static WorkflowSchemeManager workflowSchemeManager;
	public static IssueTypeManager issueTypeManager;
	public static EventPublisher eventPublisher;
	public static FieldScreenSchemeManager fieldScreenSchemeManager;
	public static StatusManager statusManager;
	public static ApplicationUser user = JiraObjectGetter.getUserByName("root");
	public static final SecureRandom secureRandom = new SecureRandom();
	private final JiraManagerPluginJob jiraManagerPluginJob;

	@GuardedBy("this")
	private final Set<LifecycleEvent> lifecycleEvents = EnumSet.noneOf(LifecycleEvent.class);

	/**
	 * Constructor.
	 * 
	 * @param eventPublisher
	 *            injected {@code EventPublisher} implementation.
	 */
	public JiraManagerPlugin(EventPublisher eventPublisher, IssueTypeManager issueTypeManager,
			FieldScreenSchemeManager fieldScreenSchemeManager, StatusManager statusManager,
			JiraManagerPluginJob jiraManagerPluginJob) {
		log.debug("Plugin started");

		JiraManagerPlugin.issueTypeManager = issueTypeManager;
		JiraManagerPlugin.fieldScreenSchemeManager = fieldScreenSchemeManager;
		JiraManagerPlugin.statusManager = statusManager;
		JiraManagerPlugin.eventPublisher = eventPublisher;
		this.jiraManagerPluginJob = jiraManagerPluginJob;
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
	 * Receives any {@code IssueEvent}s sent by JIRA. Extracts data to an
	 * instance of the {@link ServiceCallData} to work with a general class
	 * 
	 * @param issueEvent
	 *            the IssueEvent passed to us
	 */

	@EventListener
	public void onIssueEvent(IssueEvent issueEvent) {
		InternalConverterMsg msg = new InternalConverterMsg(issueEvent);

		Issue issue = issueEvent.getIssue();
		if (
				// (issueEvent.getEventTypeId().equals( EventType.ISSUE_CREATED_ID) &&
				// ComponentAccessor.getWorkflowManager().getWorkflow(issueEvent.getIssue()).getName()
				// != "jira") ||
				issue.getStatus().getName().equals("Data Processing by PubFlow")) {


			try {
				ServiceCallData callData = new ServiceCallData();
				List<WFParameter> wfpm = new LinkedList<WFParameter>();

				for (Entry<String, String> e : msg.getValues().entrySet()) {
					WFParameter wfp = new WFParameter(e.getKey(), e.getValue());
					wfpm.add(wfp);
				}
				// to enable mapping to the jira ticket
				callData.setJiraKey(issue.getKey());
				callData.setParameters(wfpm);

				callData.setWorkflowID(JiraObjectGetter.getIssueTypeNamebyJiraKey(callData.getJiraKey()));
				WorkflowBroker wfBroker = WorkflowBroker.getInstance();
				wfBroker.receiveWFCall(callData);

			} catch (Exception e) {
				log.error(e.getLocalizedMessage() + " " + e.getCause());
				JiraObjectManipulator.addIssueComment(issueEvent.getIssue().getKey(),
						"Error: " + e.getMessage(), user);
			}

		} else if (issueEvent.getEventTypeId().equals(EventType.ISSUE_UPDATED_ID)) {
			// TODO

		} else if (issueEvent.getIssue().getStatus().getName().equals("Open")
				&& !issueEvent.getEventTypeId().equals(EventType.ISSUE_DELETED_ID)) {
			if (ComponentAccessor.getCommentManager().getComments(issueEvent.getIssue()).size() == 0) {

				String txtmsg = "Dear " + issueEvent.getUser().getName() + " (" + issueEvent.getUser().getName()
						+ "),\n please append your raw data as an file attachment to this issue and provide the following information "
						+ "about your data as a comment:\nTitle, Authors, Cruise\n\nAfter that you can start the processing by pressing the "
						+ "\"Send to Data Management\" button. \nFor demonstration purposes an attachment has been added automatically."
						+ "\nThank you!";
				ComponentAccessor.getCommentManager().create(issueEvent.getIssue(), user, txtmsg, false);
			} else {
				MutableIssue mutableIssue = ComponentAccessor.getIssueManager()
						.getIssueByCurrentKey(issueEvent.getIssue().getKey());
				mutableIssue.setAssignee(issueEvent.getIssue().getReporter());
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
					default:
						break;
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " " + e.getCause());
			e.printStackTrace();
		}
		return steps;
	}

	/**
	 * Called when the plugin has been enabled.
	 * 
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() {
		registerListener();
		onLifecycleEvent(LifecycleEvent.AFTER_PROPERTIES_SET);
	}

	/**
	 * This is received from SAL after the system is really up and running from
	 * its perspective. This includes things like the database being set up and
	 * other tricky things like that. This needs to happen before we try to
	 * schedule anything, or the scheduler's tables may not be in a good state
	 * on a clean install.
	 */
	@Override
	public void onStart() {
		onLifecycleEvent(LifecycleEvent.LIFECYCLE_AWARE_ON_START);
	}

	/**
	 * This is received from the plugin system after the plugin is fully
	 * initialized. It is not safe to use Active Objects before this event is
	 * received.
	 */
	@EventListener
	public void onPluginEnabled(PluginEnabledEvent event) {
		log.info("I AM HERE");
		onLifecycleEvent(LifecycleEvent.PLUGIN_ENABLED);

	}

	/**
	 * Called when the plugin is being disabled or removed.
	 * 
	 * @throws Exception
	 */
	@Override
	public void destroy() throws Exception {
		unregisterListener();
		jiraManagerPluginJob.destroy();
	}

	/**
	 * The latch which ensures all of the plugin/application lifecycle progress
	 * is completed before we call {@code launch()}.
	 */
	private void onLifecycleEvent(LifecycleEvent event) {
		log.info("onLifecycleEvent: " + event);
		if (isLifecycleReady(event)) {
			log.info("Got the last lifecycle event... Time to get started!");
			unregisterListener();

			try {
				launch();
			} catch (Exception ex) {
				log.error("Unexpected error during launch", ex);
			}
		}
	}

	synchronized private boolean isLifecycleReady(LifecycleEvent event) {
		return lifecycleEvents.add(event) && lifecycleEvents.size() == LifecycleEvent.values().length;
	}

	/**
	 * Do all the things we can't do before the system is fully up.
	 */
	private void launch() throws Exception {
		log.info("LAUNCH!");
		jiraManagerPluginJob.init();
		log.info("launched successfully");
	}

	private void registerListener() {
		log.info("registerListeners");
		eventPublisher.register(this);
	}

	private void unregisterListener() {
		log.info("unregisterListeners");
		eventPublisher.unregister(this);
	}

	/**
	 * Used to keep track of everything that needs to happen before we are sure
	 * that it is safe to talk to all of the components we need to use,
	 * particularly the {@code SchedulerService} and Active Objects. We will not
	 * try to initialize until all of them have happened.
	 */
	static enum LifecycleEvent {
		AFTER_PROPERTIES_SET, PLUGIN_ENABLED, LIFECYCLE_AWARE_ON_START
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub

	}
}
