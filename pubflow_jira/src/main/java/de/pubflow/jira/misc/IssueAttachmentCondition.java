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

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

public class IssueAttachmentCondition extends com.atlassian.jira.workflow.condition.AbstractJiraCondition{

	@Override
	public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

		if (transientVars.get("issue") != null) {
			if (transientVars.get("issue") instanceof Issue) {
				Issue issue = (Issue) transientVars.get("issue");
				if (issue != null) {
					try {
						if (issue.getAttachments().size() >= 1) {
							return true;
						} else {
							return false;
						}

					} catch (NullPointerException e) {
						return false;
					}
				}
			}
		}
		return true;
	}

}
