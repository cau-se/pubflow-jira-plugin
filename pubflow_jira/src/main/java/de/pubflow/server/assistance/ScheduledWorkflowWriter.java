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

import java.util.LinkedList;
import java.util.List;

import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.repository.ScheduledWorkflowProvider;
import de.pubflow.server.core.workflow.ServiceCallData;

public class ScheduledWorkflowWriter {

	public static void main(String[] args) throws Exception {		
		RepoWiper.wipeRepos();
		
		ServiceCallData eprintsWfMsg = new ServiceCallData();
		eprintsWfMsg.setWorkflowID("de.pubflow.EPRINTS");
		List<WFParameter> wpl = new LinkedList<WFParameter>();
		WFParameter wp1 = new WFParameter("workflowName", "EPRINTS");		
		WFParameter wp2 = new WFParameter("quartzCron", "* * * * *");		
		wpl.add(wp1);
		wpl.add(wp2);
		eprintsWfMsg.setParameters(wpl);
		eprintsWfMsg.setType(WFType.BPMN2);

		ScheduledWorkflowProvider pro = ScheduledWorkflowProvider.getInstance(); 
		pro.addEntry(eprintsWfMsg);

		
		List<ServiceCallData> wfList = pro.getAllEntries();

		System.out.println("File count : " + wfList.size());
		for (ServiceCallData workflowEntity : wfList) {	
			System.out.println("-----------------------------------------------");
			System.out.println("Instance Id : " + workflowEntity.getWorkflowInstanceId());
			System.out.println("Workflow Id : " + workflowEntity.getWorkflowID());
			System.out.println("Workflow Parameter : " + workflowEntity.getParameters());
		}
		
		System.out.println("-----------------------------------------------");

	}

}