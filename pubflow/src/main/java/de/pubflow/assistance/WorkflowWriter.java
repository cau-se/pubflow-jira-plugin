package de.pubflow.assistance;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import de.pubflow.common.entity.repository.WorkflowEntity;
import de.pubflow.common.enumerartion.WFType;
import de.pubflow.common.repository.workflow.WorkflowProvider;

public class WorkflowWriter {
	
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
		WorkflowEntity wf = new WorkflowEntity();
		wf.addEntryToParameterMap("Leg ID_OCN", String.class);
		wf.addEntryToParameterMap("PID_OCN", String.class);
		wf.addEntryToParameterMap("Login_OCN", String.class);
		wf.addEntryToParameterMap("Source_OCN", String.class);
		wf.addEntryToParameterMap("Author_OCN", String.class);
		wf.addEntryToParameterMap("Project_OCN", String.class);
		wf.addEntryToParameterMap("Topology_OCN", String.class);
		wf.addEntryToParameterMap("Status_OCN", String.class);
		wf.addEntryToParameterMap("Zielpfad_OCN", String.class);
		wf.addEntryToParameterMap("Reference_OCN", String.class);
		wf.addEntryToParameterMap("File name_OCN", String.class);
		wf.addEntryToParameterMap("Leg comment_OCN", String.class);
		wf.addEntryToParameterMap("Start Time (QUARTZ)_OCN", Date.class);
		wf.setPubFlowWFID(1l);
		wf.setType(WFType.BPMN2);
		wf.setWFID("de.pubflow.OCN");
		wf.setWorkflowName("OCN");
		
		try {
			wf.setgBpmn(new File("OCN.bpmn"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		WorkflowProvider pro = WorkflowProvider.getInstance(); 
		pro.setEntry(wf);
		
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