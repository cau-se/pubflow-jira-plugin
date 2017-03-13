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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.pubflow.jira.misc.Appendix;
import de.pubflow.jira.misc.CustomFieldDefinition;
import de.pubflow.jira.misc.CustomFieldDefinition.CustomFieldType;

/**
 * Contains all necessary information to provide the CVOO to Pangaea Workflow.
 * (Which takes data from the CVOO-DB and creates a 4D file to publish the Data
 * by Pangaea.)
 * 
 * @author Marc Adolf
 *
 */
public class CVOOTo4DIDWorkflow extends AbstractWorkflow {

	public CVOOTo4DIDWorkflow() {
		// TODO OCN and CVOO use currently the same XML file for Jira
		super("Export Data (CVOO) to PANGAEA", "de.pubflow.CVOO", "/OCNTO4D-ID-WORKFLOW.xml", "/workflow/CVOOWorkflow");
	}

	@Override
	public Map<String, String> getScreenNames() {

		final String issueTypeCVOOTo4DName = this.getWorkflowName();

		final Map<String, String> screenNamesCVOOTo4D = new HashMap<String, String>();
		screenNamesCVOOTo4D.put("create", issueTypeCVOOTo4DName + Appendix.FIELDSCREEN + "ActionCreate");
		screenNamesCVOOTo4D.put("edit", issueTypeCVOOTo4DName + Appendix.FIELDSCREEN + "ActionEdit");
		screenNamesCVOOTo4D.put("view", issueTypeCVOOTo4DName + Appendix.FIELDSCREEN + "ActionView");
		screenNamesCVOOTo4D.put("getauthors", issueTypeCVOOTo4DName + Appendix.FIELDSCREEN + "Get Authors");

		return screenNamesCVOOTo4D;

	}

	@Override
	public List<CustomFieldDefinition> getCustomFields() {

		final List<CustomFieldDefinition> customFieldsCVOOTo4D = new LinkedList<CustomFieldDefinition>();
		customFieldsCVOOTo4D.add(new CustomFieldDefinition("Notification Groups", CustomFieldType.MULTIGROUPPICKER,
				true, new String[] { "111", "191" }));
		customFieldsCVOOTo4D.add(
				new CustomFieldDefinition("Leg ID", CustomFieldType.TEXT, true, new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(
				new CustomFieldDefinition("PID", CustomFieldType.TEXT, false, new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(
				new CustomFieldDefinition("Login", CustomFieldType.TEXT, false, new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(
				new CustomFieldDefinition("Source", CustomFieldType.TEXT, false, new String[] { "111", "131", "191" }));
		// customFieldsCVOOTo4D
		// .add(new CustomFieldDefinition("Author", CustomFieldType.TEXT, false,
		// new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(new CustomFieldDefinition("Project", CustomFieldType.TEXT, false,
				new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(new CustomFieldDefinition("Topology", CustomFieldType.TEXT, false,
				new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(
				new CustomFieldDefinition("Status", CustomFieldType.TEXT, false, new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(new CustomFieldDefinition("Target Path", CustomFieldType.TEXT, false,
				new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(new CustomFieldDefinition("Reference", CustomFieldType.TEXT, false,
				new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(new CustomFieldDefinition("File Name", CustomFieldType.TEXT, false,
				new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(new CustomFieldDefinition("Leg Comment", CustomFieldType.TEXT, false,
				new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(new CustomFieldDefinition("Quartz Cron", CustomFieldType.TEXT, false,
				new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(
				new CustomFieldDefinition("DOI", CustomFieldType.TEXT, false, new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D.add(new CustomFieldDefinition("Start Time (QUARTZ)", CustomFieldType.DATETIME, false,
				new String[] { "111", "131", "191" }));
		customFieldsCVOOTo4D
				.add(new CustomFieldDefinition("Author List", CustomFieldType.TEXTAREA, false, new String[] { "211" }));

		return customFieldsCVOOTo4D;
	}

}
