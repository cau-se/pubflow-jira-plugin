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
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

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
import com.atlassian.jira.issue.watchers.WatcherManager;
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
	/**
	 * 
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JiraManagerPlugin.class);

	/**
	 * 
	 */
	public static WorkflowSchemeManager workflowSchemeManager;
	
	/**
	 * 
	 */
	public static IssueTypeManager issueTypeManager;
	
	/**
	 * 
	 */
	public static EventPublisher eventPublisher;
	
	/**
	 * 
	 */
	public static FieldScreenSchemeManager fieldScreenSchemeManager;
	
	/**
	 * 
	 */
	public static StatusManager statusManager;
	
	/**
	 * 
	 */
	public static ApplicationUser user = JiraObjectGetter.getUserByName("pubflow");
	
	/**
	 * 
	 */
	public static final SecureRandom SECURERANDOM = new SecureRandom();
	
	/**
	 * 
	 */
	private final JiraManagerPluginJob jiraManagerPluginJob;

	/**
	 * 
	 */
	@GuardedBy("this")
	private final Set<LifecycleEvent> lifecycleEvents = EnumSet.noneOf(LifecycleEvent.class);

	/**
	 * Constructor.
	 * 
	 * @param eventPublisher
	 *            injected {@code EventPublisher} implementation.
	 */
	@SuppressWarnings("PMD.LongVariable")
	public JiraManagerPlugin(final EventPublisher eventPublisher, final IssueTypeManager issueTypeManager,
			final FieldScreenSchemeManager fieldScreenSchemeManager, final StatusManager statusManager,
			final JiraManagerPluginJob jiraManagerPluginJob) {
		LOGGER.debug("Plugin started");
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
	@SuppressWarnings("PMD.AssignmentInOperand")
	public static String getTextResource(final String resourceName) {
		final StringBuffer content = new StringBuffer();
		try {
			String line;
			final InputStream resourceStream = JiraManagerPlugin.class.getResourceAsStream(resourceName);
			final BufferedReader inputReader = new BufferedReader(new InputStreamReader(resourceStream, "UTF-8"));
			while ((line = inputReader.readLine()) != null) {
				content.append(line).append("\n");
			}
			inputReader.close();
		} catch (final Exception e) {
			LOGGER.error(e.getLocalizedMessage() + " " + e.getCause());
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
	public void onIssueEvent(final IssueEvent issueEvent) {
		final InternalConverterMsg msg = new InternalConverterMsg(issueEvent);

		final Issue issue = issueEvent.getIssue();
		final String issueStatus = issue.getStatus().getName();
		if (issueStatus.equals("Data Processing by PubFlow")) {
			this.callWf(issueEvent);
		} else if (issueEvent.getEventTypeId().equals(EventType.ISSUE_UPDATED_ID)) {
			// TODO

		} else if (issueEvent.getIssue().getStatus().getName().equals("Open")
				&& !issueEvent.getEventTypeId().equals(EventType.ISSUE_DELETED_ID)) {

			// add users from groups to watchlist
			final WatcherManager watcherManager = ComponentAccessor.getWatcherManager();
			final Collection<String> watchingGroups = new HashSet<>();
			watchingGroups.add("librarians");
			watchingGroups.add("datamanagers");

			final SortedSet<ApplicationUser> watchingUsers = ComponentAccessor.getUserUtil()
					.getAllUsersInGroupNames(watchingGroups);
			for (final ApplicationUser user : watchingUsers) {
				watcherManager.startWatching(user, issue);
			}

			if (ComponentAccessor.getCommentManager().getComments(issueEvent.getIssue()).size() == 0) {

				final String txtmsg = "Dear " + issueEvent.getUser().getName() + " (" + issueEvent.getUser().getName()
						+ "),\n please provide the following information "
						+ "about your data:\n  Title, Authors, Cruise" + "\n Thank you!";
				ComponentAccessor.getCommentManager().create(issueEvent.getIssue(), user, txtmsg, false);
			} else {
				final MutableIssue mutableIssue = ComponentAccessor.getIssueManager()
						.getIssueByCurrentKey(issueEvent.getIssue().getKey());
				mutableIssue.setAssignee(issueEvent.getIssue().getReporter());
			}
		} else if (issueStatus.equals("Aquire ORCIDs")
				&& issueEvent.getEventTypeId().equals(EventType.ISSUE_GENERICEVENT_ID)) {
			String commentText = "For the following authors identification is needed: " + "\n";
			commentText += this.getParamterFieldAsComment(issue, "Author", msg.getValues());
			ComponentAccessor.getCommentManager().create(issueEvent.getIssue(), user, commentText, false);
			// JiraObjectManipulator.changeStatus(issue.getKey(), "Aquire
			// ORCIDs");

		} else if (issueStatus.equals("Prepared for PubFlow")
				&& issueEvent.getEventTypeId().equals(EventType.ISSUE_GENERICEVENT_ID)) {
			String commentText = "Results of the Identification " + "\n";
			commentText += this.getParamterFieldAsComment(issue, "Author", msg.getValues());
			ComponentAccessor.getCommentManager().create(issueEvent.getIssue(), user, commentText, false);
			// JiraObjectManipulator.changeStatus(issue.getKey(), "Aquire
			// ORCIDs");

		} else if (issueStatus.equals("CVOO-Import")
				&& issueEvent.getEventTypeId().equals(EventType.ISSUE_GENERICEVENT_ID)) {
			final String commentText = "Sending data for import to the CVOO database.";
			ComponentAccessor.getCommentManager().create(issueEvent.getIssue(), user, commentText, false);
			
			this.callWf(issueEvent);

		}

	}

	/**
	 * Writes the given values in the {@link Issue} as a String for comments.
	 * 
	 * @param issue
	 */
	private String getParamterFieldAsComment(final Issue issue, final String paramterKey, final Map<String, String> parameters) {
		String commentText = "";
		for (final Entry<String, String> e : parameters.entrySet()) {
			if (e.getKey().contains(paramterKey)) {
				commentText += e.getValue() + ": \n";
			}
		}

		return commentText;
	}

	/**
	 * Returns the names of single 'steps' in a given XML.
	 * 
	 * @param workflowXMLString
	 *            the path to the XML file
	 * @return
	 */
	public static LinkedList<String> getSteps(final String workflowXMLString) {
		final StringReader sw = new StringReader(workflowXMLString);

		final LinkedList<String> steps = new LinkedList<String>();

		try {
			final XMLInputFactory xmlif = XMLInputFactory.newInstance();
			xmlif.setProperty(XMLInputFactory.SUPPORT_DTD, false);

			final XMLEventReader xmler = xmlif.createXMLEventReader(sw);

			while (xmler.hasNext()) {
				final XMLEvent event = xmler.nextEvent();
				if (event.isStartElement()) {
					final String localPart = event.asStartElement().getName().getLocalPart();
					switch (localPart) {
					case "step":
						try {
							steps.add(event.asStartElement().getAttributeByName(new QName("name")).getValue());
						} catch (final NullPointerException e1) {
							LOGGER.error(e1.getLocalizedMessage() + " " + e1.getCause());
							e1.printStackTrace();
						}
						break;
					default:
						break;
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.error(e.getLocalizedMessage() + " " + e.getCause());
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
	public void onPluginEnabled(final PluginEnabledEvent event) {
		LOGGER.info("I AM HERE");
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
	private void onLifecycleEvent(final LifecycleEvent event) {
		LOGGER.info("onLifecycleEvent: " + event);
		if (isLifecycleReady(event)) {
			LOGGER.info("Got the last lifecycle event... Time to get started!");
			unregisterListener();

			try {
				launch();
			} catch (final Exception ex) {
				LOGGER.error("Unexpected error during launch", ex);
			}
		}
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	synchronized private boolean isLifecycleReady(final LifecycleEvent event) {
		return lifecycleEvents.add(event) && lifecycleEvents.size() == LifecycleEvent.values().length;
	}

	/**
	 * Do all the things we can't do before the system is fully up.
	 */
	private void launch() throws Exception {
		LOGGER.info("LAUNCH!");
		jiraManagerPluginJob.init();
		LOGGER.info("launched successfully");
	}

	/**
	 * 
	 */
	private void registerListener() {
		LOGGER.info("registerListeners");
		eventPublisher.register(this);
	}

	/**
	 * 
	 */
	private void unregisterListener() {
		LOGGER.info("unregisterListeners");
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

	/**
	 * 
	 */
	@Override
	public void onStop() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * 
	 * @param issueEvent
	 */
	private void callWf(final IssueEvent issueEvent) {
		final InternalConverterMsg msg = new InternalConverterMsg(issueEvent);
		final Issue issue = issueEvent.getIssue();
		try {
			final ServiceCallData callData = new ServiceCallData();
			final List<WFParameter> wfpm = new LinkedList<WFParameter>();
			for (final Entry<String, String> e : msg.getValues().entrySet()) {
				final WFParameter wfp = new WFParameter(e.getKey(), e.getValue());
				wfpm.add(wfp);
			}
			// to enable mapping to the jira ticket
			callData.setJiraKey(issue.getKey());
			callData.setParameters(wfpm);
			callData.setWorkflowID(JiraObjectGetter.getIssueTypeNamebyJiraKey(callData.getJiraKey()));
			final WorkflowBroker wfBroker = WorkflowBroker.getInstance();
			wfBroker.receiveWFCall(callData);
		} catch (final Exception e) {
			LOGGER.error(e.getLocalizedMessage() + " " + e.getCause());
			JiraObjectManipulator.addIssueComment(issueEvent.getIssue().getKey(), "Error: " + e.getMessage(), user);
		}
	}
}
