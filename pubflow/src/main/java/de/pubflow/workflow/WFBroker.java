package de.pubflow.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.common.entity.repository.WorkflowEntity;
import de.pubflow.common.entity.workflow.JBPMPubflow;
import de.pubflow.common.entity.workflow.PubFlow;
import de.pubflow.common.entity.workflow.WFParamList;
import de.pubflow.common.enumerartion.WFType;
import de.pubflow.common.exception.WFException;
import de.pubflow.common.repository.workflow.WorkflowProvider;
import de.pubflow.communication.message.MessageToolbox;
import de.pubflow.communication.message.workflow.WorkflowMessage;
import de.pubflow.workflow.engine.IWorkflowEngine;
import de.pubflow.workflow.engine.jbpm.JBPMEngine;

public class WFBroker {

	private static final String WFID = "WFID";
	private static final String PARAMS = "WFPARAMS";
	
	private static volatile WFBroker instance;
	private static final String WFCallChannel = "test-jms:WFBroker:out.queue";
	private static final String WFResponseChannel = "WFBroker:from:msg.queue";
	
	private Logger myLogger;
	
	private Hashtable<WFType, ArrayList<IWorkflowEngine>> registry;

	
	private WFBroker()
	{
		myLogger = LoggerFactory.getLogger(this.getClass());	
		myLogger.info("Starting WFBroker");
		registry = new Hashtable<WFType,ArrayList<IWorkflowEngine>>();
		
		ArrayList<IWorkflowEngine> bpmn2Engines = new ArrayList<IWorkflowEngine>(); 
		bpmn2Engines.add(JBPMEngine.getInstance());
		
		registry.put(WFType.BPMN2, bpmn2Engines);
		
	}

	
	public static synchronized WFBroker getInstance()
	{
		if(instance == null)
		{
			instance = new WFBroker();
		}
		return instance;
	}

	
	@Consume(uri = "test-jms:wfbroker:in.queue")
	public void reciveWFCall(String msg)
	{
		//TODO implement rest
		myLogger.info("recived WF-Msg: " + msg);
		WorkflowMessage wm = MessageToolbox.loadFromString(msg, WorkflowMessage.class);
		myLogger.info("Loading WF with ID ("+wm.getWorkflowID()+") from WFRepo");
		WorkflowProvider provider = WorkflowProvider.getInstance();
		WorkflowEntity wfEntity = provider.getByWFID(wm.getWorkflowID());
		
		WFType type = wfEntity.getType();
		IWorkflowEngine engine = null;
		if(type!=null){
		ArrayList<IWorkflowEngine> engineList = registry.get(type);
		engine = engineList.get(0);
		}
		else{
			myLogger.error("Workflow NOT deployed >> Msg was malformed / No type provided");
			return;
		}
		PubFlow myWF = null;
		if(type.equals(WFType.BPMN2))
		{
			myLogger.info("BPMN2.0 Workflow detected");
			myWF = new JBPMPubflow();
			myWF.setWFID(wfEntity.getWFID());
			myWF.setWfDef(wfEntity.getgBpmn());
			myLogger.info("Set WFDef: "+wfEntity.getgBpmn().toString());
			//TODO fill var
		}
		else if (type.equals(WFType.BPEL)) {
			myLogger.info("BPEL Workflow detected");
			//TODO
		}
		else
		{
			myLogger.error("Workflow NOT deployed >> Type could not be resolved");
			return;
		}
		try {
			long wfRef = engine.deployWF(myWF);
			WFParamList params = wm.getWfparams();
			if (params!=null){
				myLogger.info("Parameter found ...");
			engine.startWF(wfRef, wm.getWfparams());
			}
			else
			{
				myLogger.info("No Parameter found!");
				engine.startWF(wfRef, null);
			}
		} catch (WFException e) {
			e.printStackTrace();
		}
	}
	

	
	private void sendWFResponse(String msg)
	{
//		myLogger.info("Sending Msg ...");
//		ProducerTemplate producer;
//		CamelContext context = PubFlowCore.getInstance().getContext();
//		producer = context.createProducerTemplate();
//		producer.sendBody("test-jms:queue:testOut.queue",msg);
//		myLogger.info("Msg sent!");
	}
}
