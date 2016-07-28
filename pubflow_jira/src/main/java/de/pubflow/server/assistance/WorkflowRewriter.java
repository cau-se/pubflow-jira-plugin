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
package de.pubflow.server.assistance;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import de.pubflow.server.common.entity.WorkflowEntity;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.repository.WorkflowProvider;

public class WorkflowRewriter {

	public static void main (String[]arghs) throws Exception{
		rewriteWorkflows();
	}
	
	public static void rewriteWorkflows() throws Exception {		
		WorkflowProvider pro = WorkflowProvider.getInstance(); 

		pro.clear();
		System.out.println("STARTING");
		WorkflowEntity wfOCN = new WorkflowEntity();
		wfOCN.setType(WFType.BPMN2);
		wfOCN.setWorkflowId("de.pubflow.OCN");
		wfOCN.setWorkflowName("OCN");

		try {
			wfOCN.setgBpmn(new File("OCN.bpmn"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pro.addEntry(wfOCN);

		WorkflowEntity wfEPRINTS = new WorkflowEntity();
		wfEPRINTS.setType(WFType.BPMN2);
		wfEPRINTS.setWorkflowId("de.pubflow.EPRINTS");
		wfEPRINTS.setWorkflowName("EPRINTS");

		try {
			wfEPRINTS.setgBpmn(new File("EPRINTS.bpmn"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pro.addEntry(wfEPRINTS);

		WorkflowEntity wfCVOO = new WorkflowEntity();
		wfCVOO.setType(WFType.BPMN2);
		wfCVOO.setWorkflowId("de.pubflow.CVOO");
		wfCVOO.setWorkflowName("CVOO");

		try {
			wfCVOO.setgBpmn(new File("CVOO.bpmn"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pro.addEntry(wfCVOO);

		List<WorkflowEntity> wfList = pro.getAllEntries();

		System.out.println("File count : " + wfList.size());
		for (WorkflowEntity workflowEntity : wfList) {

			System.out.println("-----------------------------------------------");
			System.out.println("Workflow Id : " + workflowEntity.getWorkflowId());
			System.out.println("Workflow Type : " + workflowEntity.getType().toString());
			System.out.println("Workflow Name : " + workflowEntity.getWorkflowName());
			System.out.println();
			for (Entry e : workflowEntity.getParameterMap().entrySet()){
				System.out.println(e.getKey() + " : " + e.getValue());
			}
		}
		System.out.println("-----------------------------------------------");

	}
}