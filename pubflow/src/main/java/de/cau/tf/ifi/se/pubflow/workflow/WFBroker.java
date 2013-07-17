package de.cau.tf.ifi.se.pubflow.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import de.cau.tf.ifi.se.pubflow.PubFlowCore;
import de.cau.tf.ifi.se.pubflow.common.entity.workflow.PubFlow;
import de.cau.tf.ifi.se.pubflow.common.enumerartion.WFType;
import de.cau.tf.ifi.se.pubflow.workflow.engine.IWorkflowEngine;
import de.cau.tf.ifi.se.pubflow.workflow.engine.jbpm.JBPMEngine;

public class WFBroker {

	private static final String WFID = "WFID";
	private static final String PARAMS = "WFPARAMS";
	
	private static volatile WFBroker instance;
	private static final String WFCallChannel = "test-jms:WFBroker:out.queue";
	private static final String WFResponseChannel = "WFBroker:from:msg.queue";
	
	private Logger myLogger;
	
	private Hashtable<WFType, ArrayList<IWorkflowEngine>> registry;
	private Hashtable<Long, PubFlow> workflows;
	
	private WFBroker()
	{
		myLogger = LoggerFactory.getLogger(this.getClass());	
		myLogger.info("Starting WFBroker");
		registry = new Hashtable<WFType,ArrayList<IWorkflowEngine>>();
		
		ArrayList<IWorkflowEngine> bpmn2Engines = new ArrayList<IWorkflowEngine>(); 
		bpmn2Engines.add(JBPMEngine.getInstance());
		
		registry.put(WFType.BPMN2, bpmn2Engines);
		
		workflows = new Hashtable<Long, PubFlow>();
		loadWorkflows();
	}
	
	private synchronized void loadWorkflows()
	{
		
		//TODO
		/*
		 * Load all existing Workflows from the DB and add them to the workflows hashtable.
		 * 
		 * The PubFlowID (long) of the WF is the key
		 * 
		 * Extension (to be done later):
		 * Maybe start a Quartz job to update WF list in a predefined intervall?!?
		 */
	}
	
	public static synchronized WFBroker getInstance()
	{
		if(instance == null)
		{
			instance = new WFBroker();
		}
		return instance;
	}
	
	private PubFlow getWFByID(long pubflowID)
	{
		PubFlow result = null;
		
		result = workflows.get(pubflowID);
		
		return result;	
	}
	
	@Consume(uri = "test-jms:queue:out2.queue")
	public void reciveWFCall(String msg)
	{
		myLogger.info("RECIVED >> " + msg);
		sendWFResponse("Recived!!!");
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
