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

import de.pubflow.jira.misc.CustomFieldDefinition;
import de.pubflow.jira.misc.CustomFieldDefinition.CustomFieldType;

/**
 * Contains all information for the Workflow, which transfers raw data to the
 * OCN/CVOO database.
 * 
 * @author Marc Adolf
 *
 */
public class RawToOCNWorkflow extends AbstractWorkflow {

	public RawToOCNWorkflow() {
		super("Publish Raw Cruise Data", "de.pubflow.RawToOCN", "/RAWTOCVOO-WORKFLOW.xml", "/workflow/DataToCVOOWorkflow");
	}

	@Override
	public List<String> getScreenNames() {
		String issueTypeRawToOCNName = this.getWorkflowName();

		List<String> screenNamesRawToOCN = new ArrayList<String>();
		screenNamesRawToOCN.add(issueTypeRawToOCNName + "ActionCreate");
		screenNamesRawToOCN.add(issueTypeRawToOCNName + "ActionEdit");
		screenNamesRawToOCN.add(issueTypeRawToOCNName + "ActionView");

		return screenNamesRawToOCN;
	}

	@Override
	public List<CustomFieldDefinition> getCustomFields() {

		LinkedList<CustomFieldDefinition> customFieldsRawToOCN = new LinkedList<CustomFieldDefinition>();
		customFieldsRawToOCN
				.add(new CustomFieldDefinition("Author", CustomFieldType.TEXT, false, new String[] { "11" }));
		customFieldsRawToOCN
				.add(new CustomFieldDefinition("Author Name", CustomFieldType.TEXT, false, new String[] { "11" }));
		customFieldsRawToOCN
				.add(new CustomFieldDefinition("Title", CustomFieldType.TEXT, false, new String[] { "11" }));
		customFieldsRawToOCN
				.add(new CustomFieldDefinition("Cruise", CustomFieldType.TEXT, false, new String[] { "11" }));
		customFieldsRawToOCN
				.add(new CustomFieldDefinition("contact_email", CustomFieldType.TEXT, true, new String[] { "21" }));
		customFieldsRawToOCN
				.add(new CustomFieldDefinition("reference", CustomFieldType.TEXT, true, new String[] { "21" }));
		customFieldsRawToOCN
				.add(new CustomFieldDefinition("metadata_block", CustomFieldType.TEXT, true, new String[] { "21" }));
		customFieldsRawToOCN
				.add(new CustomFieldDefinition("data_block", CustomFieldType.TEXT, true, new String[] { "21" }));

		return customFieldsRawToOCN;

	}
}
