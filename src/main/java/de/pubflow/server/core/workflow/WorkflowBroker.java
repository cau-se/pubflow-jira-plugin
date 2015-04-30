package de.pubflow.server.core.workflow;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.entity.WorkflowEntity;
import de.pubflow.server.common.entity.workflow.JBPMPubflow;
import de.pubflow.server.common.entity.workflow.PubFlow;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.exceptions.WFException;
import de.pubflow.server.common.repository.WorkflowProvider;
import de.pubflow.server.core.workflow.engines.JBPMEngine;

public class WorkflowBroker {
	
	private static volatile WorkflowBroker instance;

	private Logger myLogger;

	private Hashtable<WFType, ArrayList<Class<? extends WorkflowEngine> >> registry;

	private WorkflowBroker(){
		myLogger = LoggerFactory.getLogger(this.getClass());	
		myLogger.info("Starting WorkflowBroker");
		registry = new Hashtable<WFType,ArrayList<Class<? extends WorkflowEngine> >>();

		ArrayList<Class<? extends WorkflowEngine> > bpmn2Engines = new ArrayList<Class<? extends WorkflowEngine> >(); 
		bpmn2Engines.add(JBPMEngine.class);

		registry.put(WFType.BPMN2, bpmn2Engines);
	}


	public static synchronized WorkflowBroker getInstance(){
		if(instance == null){
			instance = new WorkflowBroker();
		}
		return instance;
	}

	public void receiveWFCall(WorkflowMessage wm){

		if(!wm.isValid()){
			myLogger.error("Workflow NOT deployed >> Msg is not valid ");
			return;
		}
		myLogger.info("Loading WF with ID (" + wm.getWorkflowID() + ")");
		WorkflowProvider provider = WorkflowProvider.getInstance();
		WorkflowEntity wfEntity = provider.getByWFID(wm.getWorkflowID());
		WFType type = wfEntity.getType();

		PubFlow myWF = null;

		if(type.equals(WFType.BPMN2)){
			myLogger.info("BPMN2.0 workflow detected");
			myWF = new JBPMPubflow();
			myWF.setWFID(wfEntity.getWorkflowId());
			myWF.setWfDef(wfEntity.getgBpmn());
			myLogger.info("Name : "+wfEntity.getWorkflowName());
			//TODO fill var

		}else if (type.equals(WFType.BPEL)) {
			myLogger.info("BPEL workflow detected");
			//TODO

		}else{
			myLogger.error("Workflow NOT deployed >> Type could not be resolved");
			return;
		}

		WorkflowEngine engine = null;
		if(type!=null){

			ArrayList<Class<? extends WorkflowEngine> > engineList = registry.get(type);
			Class<? extends WorkflowEngine> clazz = engineList.get(0);

			try {
				engine = clazz.newInstance();

			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			engine.deployWF(myWF);
			List<WFParameter> params = wm.getParameters();

			if (params!=null){
				engine.setParams(wm.getParameters());
			}
			Thread wfEngineThread = new Thread(engine);
			wfEngineThread.start();

		} catch (WFException e) {
			e.printStackTrace();
		}
	}
}
