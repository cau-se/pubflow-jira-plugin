package de.pubflow.server.core.workflow;

import java.util.ArrayList;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.entity.WorkflowEntity;
import de.pubflow.server.common.entity.workflow.JBPMPubflow;
import de.pubflow.server.common.entity.workflow.PubFlow;
import de.pubflow.server.common.entity.workflow.WFParameterList;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.exceptions.WFException;
import de.pubflow.server.common.repository.WorkflowProvider;
import de.pubflow.server.core.communication.WorkflowMessage;
import de.pubflow.server.core.workflow.engines.JBPMEngine;

public class WFBroker {
	
	private static volatile WFBroker instance;

	private Logger myLogger;

	private Hashtable<WFType, ArrayList<Class<? extends WorkflowEngine> >> registry;

	private WFBroker(){
		myLogger = LoggerFactory.getLogger(this.getClass());	
		myLogger.info("Starting WFBroker");
		registry = new Hashtable<WFType,ArrayList<Class<? extends WorkflowEngine> >>();

		ArrayList<Class<? extends WorkflowEngine> > bpmn2Engines = new ArrayList<Class<? extends WorkflowEngine> >(); 
		bpmn2Engines.add(JBPMEngine.class);

		registry.put(WFType.BPMN2, bpmn2Engines);

	}


	public static synchronized WFBroker getInstance(){
		if(instance == null){
			instance = new WFBroker();
		}
		return instance;
	}

	public void receiveWFCall(WorkflowMessage wm){
		myLogger.info("Received WF-Msg");

		if(!wm.isValid()){
			myLogger.error("Workflow NOT deployed >> Msg is not valid ");
			return;
		}
		myLogger.info("Loading WF with ID (" + wm.getWorkflowID() + ") from WFRepo");
		WorkflowProvider provider = WorkflowProvider.getInstance();
		WorkflowEntity wfEntity = provider.getByWFID(wm.getWorkflowID());
		WFType type = wfEntity.getType();

		PubFlow myWF = null;

		if(type.equals(WFType.BPMN2)){
			myLogger.info("BPMN2.0 Workflow detected");
			myWF = new JBPMPubflow();
			myWF.setWFID(wfEntity.getWorkflowId());
			myWF.setWfDef(wfEntity.getgBpmn());
			myLogger.info("Name : "+wfEntity.getWorkflowName());
			//TODO fill var

		}else if (type.equals(WFType.BPEL)) {
			myLogger.info("BPEL Workflow detected");
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
				myLogger.info("Creating new "+clazz.getCanonicalName());
				engine = clazz.newInstance();
				myLogger.info("Instance created! ");

			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			myLogger.info("Deploying WF");
			engine.deployWF(myWF);
			WFParameterList params = wm.getParameters();

			if (params!=null){
				myLogger.info("Parameter found ...");
				engine.setParams(wm.getParameters());

			}else{
				myLogger.info("No Parameter found!");

			}
			myLogger.info("Starting WF ...");
			Thread wfEngineThread = new Thread(engine);
			wfEngineThread.start();
			myLogger.info("... engine up and running");

		} catch (WFException e) {
			e.printStackTrace();
		}
	}
}
