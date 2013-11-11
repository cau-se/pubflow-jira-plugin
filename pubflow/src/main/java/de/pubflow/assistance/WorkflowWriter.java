package de.pubflow.assistance;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
		
		wf.setPubFlowWFID(1l);
		wf.setType(WFType.BPMN2);
		wf.setWFID("de.pubflow.OCN");
		try {
			wf.setgBpmn(new File("OCN.bpmn"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WorkflowProvider pro = WorkflowProvider.getInstance(); 
		pro.setEntry(wf);
		
		List<WorkflowEntity> wfList = pro.getAllEntries();
		System.out.println(wfList.size());
		for (WorkflowEntity workflowEntity : wfList) {
			System.out.println(workflowEntity.getWFID());
		}
	}
}
