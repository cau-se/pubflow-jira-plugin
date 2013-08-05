package de.pubflow.assistance;

import java.util.List;

import de.pubflow.common.entity.repository.WorkflowEntity;
import de.pubflow.common.repository.workflow.WorkflowProvider;

public class WorkflowWriter {
	
	public static void main(String[] args) {
		System.out.println("STARTING");
		WorkflowEntity wf = new WorkflowEntity();
		
		wf.setWFID("123321");
		WorkflowProvider pro = WorkflowProvider.getInstance(); 
		pro.setEntry(wf);
		
		List<WorkflowEntity> wfList = pro.getAllEntries();
		System.out.println(wfList.size());
		for (WorkflowEntity workflowEntity : wfList) {
			System.out.println(workflowEntity.getPubFlowWFID());
		}
	
	}

}
