package de.pubflow.server.assistance;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import de.pubflow.server.common.entity.WorkflowEntity;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.repository.WorkflowProvider;

public class WorkflowWriterPi4d {

	public static void main(String[] args) {		

		System.out.println("STARTING");
		WorkflowEntity wfOCN = new WorkflowEntity();
		wfOCN.addEntryToParameterMap("issueKey", String.class);
		wfOCN.addEntryToParameterMap("Leg ID_OCN", String.class);
		wfOCN.addEntryToParameterMap("PID_OCN", String.class);
		wfOCN.addEntryToParameterMap("Login_OCN", String.class);
		wfOCN.addEntryToParameterMap("Source_OCN", String.class);
		wfOCN.addEntryToParameterMap("Author_OCN", String.class);
		wfOCN.addEntryToParameterMap("Project_OCN", String.class);
		wfOCN.addEntryToParameterMap("Topology_OCN", String.class);
		wfOCN.addEntryToParameterMap("Status_OCN", String.class);
		wfOCN.addEntryToParameterMap("Zielpfad_OCN", String.class);
		wfOCN.addEntryToParameterMap("Reference_OCN", String.class);
		wfOCN.addEntryToParameterMap("File name_OCN", String.class);
		wfOCN.addEntryToParameterMap("Leg comment_OCN", String.class);
		wfOCN.addEntryToParameterMap("Start Time (QUARTZ)_OCN", Date.class);
		wfOCN.setType(WFType.BPMN2);
		wfOCN.setWorkflowId("de.pubflow.OCN");
		wfOCN.setWorkflowName("OCN");

		try {
			wfOCN.setgBpmn(new File("OCN.bpmn"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WorkflowProvider pro = WorkflowProvider.getInstance(); 
		pro.addEntry(wfOCN);

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