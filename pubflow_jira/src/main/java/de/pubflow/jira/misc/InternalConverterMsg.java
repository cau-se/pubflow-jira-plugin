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
package de.pubflow.jira.misc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.DateTimeCFType;
import com.atlassian.jira.issue.fields.CustomField;

/**
 * @author arl This class extracts relevant information from an issueEvent
 */

public class InternalConverterMsg {

	/**
	 * Placeholder for an empty string
	 */
	private static final String EMPTY_STRING = "";
	
	/**
	 * The type of the entity
	 */
	private final long eventType;
	
	/**
	 * The date it was created.
	 */
	private final Date date;
	
	/**
	 * the corresponding issuetype name
	 */
	private final String issueTypeName;
	
	/**
	 * 
	 */
	private final Map<String, String> values = new HashMap<String, String>();

	/**
	 * @param issueEvent
	 */
	public InternalConverterMsg(final IssueEvent issueEvent) {

		this.eventType = issueEvent.getEventTypeId();
		this.date = issueEvent.getTime();

		final Issue issue = issueEvent.getIssue();
		this.issueTypeName = issue.getIssueType().getName();

		getValues().put("reporter", issue.getReporterId());
		getValues().put("assignee", issue.getAssigneeId());
		getValues().put("workflowName", getIssueTypeName());
		getValues().put("issueKey", issue.getKey() + EMPTY_STRING);
		getValues().put("eventType", getEventType() + EMPTY_STRING);
		getValues().put("date", getDate().getTime() + EMPTY_STRING);
		getValues().put("status", issue.getStatus().getName());

		final List<CustomField> customFields = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects(issue);

		for (final CustomField customField : customFields) {
			if (customField != null && customField.getName() != null
					&& issue.getCustomFieldValue(customField) != null) {

				final String customFieldName = customField.getName();

				if (customField.getCustomFieldType() instanceof DateTimeCFType) {
					final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
					Date date;
					try {
						date = formatter.parse(issue.getCustomFieldValue(customField).toString());
						getValues().put("quartzMillis", date.getTime() + EMPTY_STRING);
					} catch (ParseException e) {
						e.printStackTrace();
						getValues().put(customFieldName, null);
					}

				} else {
					getValues().put(customFieldName, issue.getCustomFieldValue(customField).toString());
				}
			}
		}
	}

	/**
	 * 
	 * @return long the type of the event.
	 */
	public long getEventType() {
		return eventType;
	}

	/**
	 * 
	 * @return Date the date the entity were created.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * 
	 * @return String the issuetype the event is corresponding to
	 */
	public String getIssueTypeName() {
		return issueTypeName;
	}

	/**
	 * 
	 * @return Map<String, String> values that are mapped to the entity.
	 */
	public Map<String, String> getValues() {
		return values;
	}
}
