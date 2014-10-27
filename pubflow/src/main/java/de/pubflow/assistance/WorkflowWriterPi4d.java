package de.pubflow.assistance;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import de.pubflow.common.entity.WorkflowEntity;
import de.pubflow.common.enumeration.WFType;
import de.pubflow.common.repository.WorkflowProvider;

public class WorkflowWriterPi4d {

	public static void main(String[] args) {		
		File wfs = new File("./etc/workflow.list");

		if(wfs.exists()){
			wfs.delete();
		}

		File storage = new File("./etc/FSStorageAdapter");

		if(storage.exists()){
			for (File f : storage.listFiles()){
				f.delete();
			}
		}

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
		wfOCN.setPubFlowWFID(1l);
		wfOCN.setType(WFType.BPMN2);
		wfOCN.setWFID("de.pubflow.OCN");
		wfOCN.setWorkflowName("OCN");

		try {
			wfOCN.setgBpmn(new File("OCN.bpmn"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WorkflowProvider pro = WorkflowProvider.getInstance(); 
		pro.addEntry(wfOCN);

//		WorkflowEntity wfPi = new WorkflowEntity();
//		wfPi.addEntryToParameterMap("tropfenzahl_Pi", String.class);
//
//		wfPi.setPubFlowWFID(2l);
//		wfPi.setType(WFType.BPMN2);
//		wfPi.setWFID("de.pubflow.Pi");
//		wfPi.setWorkflowName("Pi");
//
//		try {
//			wfPi.setgBpmn(new File("pi.bpmn"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		pro.addEntry(wfPi);

		List<WorkflowEntity> wfList = pro.getAllEntries();

		System.out.println("File count : " + wfList.size());
		for (WorkflowEntity workflowEntity : wfList) {
			System.out.println("-----------------------------------------------");
			System.out.println("WFID: " + workflowEntity.getWFID());
			for (Entry e : workflowEntity.getParameterMap().entrySet()){
				System.out.println(e.getKey() + " : " + e.getValue());
			}
		}
		System.out.println("-----------------------------------------------");

	}

}