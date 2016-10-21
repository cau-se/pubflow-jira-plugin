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
package de.pubflow.server.core.workflow.types;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.pubflow.jira.misc.Appendix;
import de.pubflow.jira.misc.CustomFieldDefinition;
import de.pubflow.jira.misc.CustomFieldDefinition.CustomFieldType;

/**
 * This class represents the old OCN Workflow and is used for testing purposes
 * @author mad
 *
 */
public class OldOCNWorkflow extends AbstractWorkflow {

	public OldOCNWorkflow() {
		super("Old OCN Workflow ", "de.pubflow.oldOCN", "/PubFlow.xml", "/workflow/TestWorkflow");
	}

	@Override
	public List<String> getScreenNames() {
		String issueTypeName = this.getWorkflowName();

		List<String> screenNames = new ArrayList<String>();
		screenNames.add(issueTypeName + Appendix.FIELDSCREEN + "ActionCreate");
		screenNames.add(issueTypeName + Appendix.FIELDSCREEN + "ActionEdit");
		screenNames.add(issueTypeName + Appendix.FIELDSCREEN + "ActionView");

		return screenNames;
	}

	@Override
	public List<CustomFieldDefinition> getCustomFields() {

		LinkedList<CustomFieldDefinition> customFields = new LinkedList<CustomFieldDefinition>();
		customFields
				.add(new CustomFieldDefinition("Leg ID", CustomFieldType.TEXT, true, new String[] { "141", "111" }));
		customFields.add(new CustomFieldDefinition("PID", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields
				.add(new CustomFieldDefinition("Login", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields
				.add(new CustomFieldDefinition("Source", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields.add(
				new CustomFieldDefinition("Author", CustomFieldType.TEXT, false, new String[] { "11", "141", "111" }));
		customFields
				.add(new CustomFieldDefinition("Project", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields
				.add(new CustomFieldDefinition("Topology", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields
				.add(new CustomFieldDefinition("Status", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields
				.add(new CustomFieldDefinition("Zielpfad", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields.add(
				new CustomFieldDefinition("Reference", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields.add(
				new CustomFieldDefinition("File name", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields.add(
				new CustomFieldDefinition("Leg comment", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields.add(
				new CustomFieldDefinition("Quartz Cron", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields.add(new CustomFieldDefinition("DOI", CustomFieldType.TEXT, false, new String[] { "141", "111" }));
		customFields.add(new CustomFieldDefinition("Author name", CustomFieldType.TEXT, false, new String[] { "11" }));
		customFields.add(new CustomFieldDefinition("Title", CustomFieldType.TEXT, false, new String[] { "11" }));
		customFields.add(new CustomFieldDefinition("Cruise", CustomFieldType.TEXT, false, new String[] { "11" }));
		customFields.add(new CustomFieldDefinition("Start Time (QUARTZ)", CustomFieldType.DATETIME, false,
				new String[] { "141", "111" }));

		return customFields;
	}

}
