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

import de.pubflow.jira.misc.CustomFieldDefinition;
import de.pubflow.jira.misc.CustomFieldDefinition.CustomFieldType;

/**
 * Contains all necessary information to provide the OCN to Pangaea Workflow.
 * (Which takes data from the OCN-DB and creates a 4D file to publish the Data
 * by Pangaea.)
 * 
 * @author Marc Adolf
 *
 */
public class OCNTo4DWorkflow extends AbstractWorkflow {

	public OCNTo4DWorkflow() {
		// TODO OCN and CVOO use currently the same XML file for Jira
		super("Export Data (OCN) to PANGAEA", "de.pubflow.OCN", "/OCNTO4D-WORKFLOW.xml", "/workflow/OCNWorkflow");
	}

	@Override
	public List<CustomFieldDefinition> getCustomFields() {

		LinkedList<CustomFieldDefinition> customFieldsOCNTo4D = new LinkedList<CustomFieldDefinition>();
		customFieldsOCNTo4D
				.add(new CustomFieldDefinition("Leg ID", CustomFieldType.TEXT, true, new String[] { "111", "191" }));
		customFieldsOCNTo4D
				.add(new CustomFieldDefinition("PID", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D
				.add(new CustomFieldDefinition("Login", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D
				.add(new CustomFieldDefinition("Source", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D
				.add(new CustomFieldDefinition("Author", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D
				.add(new CustomFieldDefinition("Project", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D
				.add(new CustomFieldDefinition("Topology", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D
				.add(new CustomFieldDefinition("Status", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D.add(
				new CustomFieldDefinition("Target Path", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D.add(
				new CustomFieldDefinition("Reference", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D.add(
				new CustomFieldDefinition("File Name", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D.add(
				new CustomFieldDefinition("Leg Comment", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D.add(
				new CustomFieldDefinition("Quartz Cron", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D
				.add(new CustomFieldDefinition("DOI", CustomFieldType.TEXT, false, new String[] { "111", "191" }));
		customFieldsOCNTo4D.add(new CustomFieldDefinition("Start Time (QUARTZ)", CustomFieldType.DATETIME, false,
				new String[] { "111", "191" }));

		return customFieldsOCNTo4D;
	}

}
