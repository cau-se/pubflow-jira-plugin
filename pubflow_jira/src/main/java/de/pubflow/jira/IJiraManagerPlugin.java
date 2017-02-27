package de.pubflow.jira;

import java.io.BufferedReader;
import java.security.SecureRandom;
import java.util.List;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.user.ApplicationUser;

public interface IJiraManagerPlugin {
	public String getTextResource(final String resourceName);
	public List<String> getSteps(final String workflowXMLString);
	public IssueTypeManager getIssueTypeManager();
	public FieldScreenSchemeManager getFieldScreenSchemeManager();
	public StatusManager getStatusManager();
	public SecureRandom getSecureRandom();
	public BufferedReader getIn();
	public void setIn(final BufferedReader in);
	public ApplicationUser getUser();
}
