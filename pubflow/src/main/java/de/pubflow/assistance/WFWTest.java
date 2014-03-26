package de.pubflow.assistance;

import java.util.List;
import java.util.Map.Entry;

import de.pubflow.common.entity.repository.WorkflowEntity;
import de.pubflow.common.repository.workflow.WorkflowProvider;

public class WFWTest {
	public static void main(String[] args){
		WorkflowProvider pro = WorkflowProvider.getInstance(); 
		
		List<WorkflowEntity> wfList = pro.getAllEntries();
		System.out.println("File count : " + wfList.size());
		for (WorkflowEntity workflowEntity : wfList) {
			System.out.println("-----------------------------------------------");
			System.out.println("WFID: " + workflowEntity.getWFID());
			for (Entry e : workflowEntity.getParameterMap().entrySet()){
				System.out.println(e.getKey() + " : " + e.getValue());
			}
			
		}
	}
}
