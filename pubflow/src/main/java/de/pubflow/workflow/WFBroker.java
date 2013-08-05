package de.pubflow.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import de.pubflow.PubFlowCore;
import de.pubflow.common.entity.repository.WorkflowEntity;
import de.pubflow.common.entity.workflow.PubFlow;
import de.pubflow.common.enumerartion.WFType;
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
		myLogger.info("recived WF-Msg: " + msg);
		WorkflowMessage wm = MessageToolbox.loadFromString(msg, WorkflowMessage.class);
		myLogger.info("Loading WF with ID ("+wm.getWorkflowID()+") from WFRepo");
		WorkflowProvider provider = WorkflowProvider.getInstance();
		WorkflowEntity wfEntity = provider.getEntry(wm.getWorkflowID());
		
		WFType type = wfEntity.getType();
		ArrayList<IWorkflowEngine> engineList = registry.get(type);
		IWorkflowEngine engine = engineList.get(0);
		//engine.deployWF(wfEntity.)
		
//		PubFlowMessage nachricht = PubFlowMessage.initFromString(msg);
//		if(nachricht.getAction().equalsIgnoreCase("START_WF"))
//		{
//			Set<Entry<String, String>> content = nachricht.getMessage().entrySet();
//			PubFlow wfToStart = null;  
//			for (Entry<String, String> entry : content) {
//				if(entry.getKey().equalsIgnoreCase(WFID))
//				{
//					long id = Long.parseLong(entry.getValue());
//					wfToStart = getWFByID(id);
//				}
//			}
//			HashMap<String, String> params = PubFlowMessage.getMap(PARAMS, nachricht.getMessage());
//			if(wfToStart!=null)
//			{
//				runWF(wfToStart, params);
//			}
//			else
//			{
//				
//				// TODO
//				// It would be nice to throw an exception
//			}
//		}
	
	}
	
	private void runWF(PubFlow wfToStart,HashMap<String, String> params)
	{
//		if(wfToStart==null)
//		{ 
//			// TODO Exception
//		}
//		WFType type = wfToStart.getType();
//		ArrayList<IWorkflowEngine> engines = registry.get(type);
//		if(engines==null)
//		{
//			//TODO Exception
//		}
//		//TODO better engine selection
//		IWorkflowEngine currentEngine = engines.get(0);
//		try {
//			long pointer = currentEngine.deployWF(wfToStart);
//			ArrayList<WFParameter> currentParameters = WFParameter.loadFromHashmap(params); 
//			currentEngine.startWF(pointer, currentParameters);
//		} catch (WFException e) {
//			e.printStackTrace();
//		}
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
