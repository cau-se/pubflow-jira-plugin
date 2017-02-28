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
	public BufferedReader getInreader();
	public void setInreader(final BufferedReader inreader);
	public ApplicationUser getUser();
}
