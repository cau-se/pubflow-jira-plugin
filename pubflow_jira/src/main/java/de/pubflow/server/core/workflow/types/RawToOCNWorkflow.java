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
 * Contains all information for the Workflow, which transfers raw data to the
 * OCN/CVOO database.
 * 
 * @author Marc Adolf
 *
 */
public class RawToOCNWorkflow extends AbstractWorkflow {

	public RawToOCNWorkflow() {
		super("Publish Raw Cruise Data", "de.pubflow.RawToOCN", "/RAWTOCVOO-WORKFLOW.xml", "", "");
	}

	@Override
	public List<String> getScreenNames() {
		String issueTypeRawToOCNName = this.getWorkflowName();

		List<String> screenNamesRawToOCN = new ArrayList<String>();
		screenNamesRawToOCN.add(issueTypeRawToOCNName + Appendix.FIELDSCREEN + "ActionCreate");
		screenNamesRawToOCN.add(issueTypeRawToOCNName + Appendix.FIELDSCREEN + "ActionEdit");
		screenNamesRawToOCN.add(issueTypeRawToOCNName + Appendix.FIELDSCREEN + "ActionView");

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

		return customFieldsRawToOCN;

	}
}
