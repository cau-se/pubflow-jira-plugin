package de.pubflow.server.assistance;

import java.util.LinkedList;
import java.util.List;

import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.repository.ScheduledWorkflowProvider;
import de.pubflow.server.core.workflow.WorkflowMessage;

public class ScheduledWorkflowWriter {

	public static void main(String[] args) throws Exception {		

		WorkflowMessage eprintsWfMsg = new WorkflowMessage();
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

		
		List<WorkflowMessage> wfList = pro.getAllEntries();

		System.out.println("File count : " + wfList.size());
		for (WorkflowMessage workflowEntity : wfList) {
			
			System.out.println("-----------------------------------------------");
			System.out.println("Instance Id : " + workflowEntity.getInstanceId());
			System.out.println();
			for (WFParameter e : workflowEntity.getParameters()){
				System.out.println(e.getKey() + " : " + e.getValue());
			}
		}
		System.out.println("-----------------------------------------------");

	}

}