package de.pubflow.jira.misc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.DateTimeCFType;
import com.atlassian.jira.issue.fields.CustomField;

/**
 * @author arl
 *	This class extracts relevant information from an issueEvent
 */

public class InternalConverterMsg {

	private long eventType;
	private Date date;
	private String issueTypeName;
	private Map<String, String> values = new HashMap<String, String>();

	/**
	 * @param issueEvent
	 */
	public InternalConverterMsg(IssueEvent issueEvent) {

		eventType = issueEvent.getEventTypeId();
		date = issueEvent.getTime();

		Issue issue = issueEvent.getIssue();
		issueTypeName = issue.getIssueTypeObject().getName();

		values.put("reporter", issue.getReporterId());
		values.put("assignee", issue.getAssigneeId());
		values.put("workflowName", issueTypeName);
		values.put("issueKey", issue.getKey() + "");
		values.put("eventType", eventType + "");
		values.put("date", date.getTime() + "");
		values.put("status", issue.getStatusObject().getName());

		List<CustomField> customFields = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects(issue);

		for (CustomField customField : customFields) {
			if (customField != null && customField.getName() != null && issue.getCustomFieldValue(customField) != null) {

				String customFieldName = customField.getName();

				if (customField.getCustomFieldType() instanceof DateTimeCFType) {
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					Date date;
					try {
						date = formatter.parse(issue.getCustomFieldValue(customField).toString());
						values.put("quartzMillis", date.getTime() + "");
					} catch (ParseException e) {
						e.printStackTrace();
						values.put(customFieldName, null);
					}

				} else {
					values.put(customFieldName, issue.getCustomFieldValue(customField).toString());
				}
			}
		}
	}

	public long getEventType() {
		return eventType;
	}

	public Date getDate() {
		return date;
	}

	public String getIssueTypeName() {
		return issueTypeName;
	}

	public Map<String, String> getValues() {
		return values;
	}
}
