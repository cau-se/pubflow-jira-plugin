package de.pubflow.server.core.workflow;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.entity.WorkflowEntity;
import de.pubflow.server.common.entity.workflow.ParameterType;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.exceptions.WFException;
import de.pubflow.server.common.repository.WorkflowProvider;
import de.pubflow.server.core.communication.WorkflowCall;
import de.pubflow.server.core.restConnection.WorkflowReceiver;
import de.pubflow.server.core.restConnection.WorkflowSender;
import de.pubflow.server.core.workflow.engines.JBPMEngine;

public class WorkflowBroker {

	private static volatile WorkflowBroker instance;

	private Logger myLogger;

	private Hashtable<WFType, ArrayList<Class<? extends WorkflowEngine>>> registry;

	private WorkflowBroker() {
		myLogger = LoggerFactory.getLogger(this.getClass());
		myLogger.info("Starting WorkflowBroker");
		registry = new Hashtable<WFType, ArrayList<Class<? extends WorkflowEngine>>>();

		ArrayList<Class<? extends WorkflowEngine>> bpmn2Engines = new ArrayList<Class<? extends WorkflowEngine>>();
		bpmn2Engines.add(JBPMEngine.class);

		registry.put(WFType.BPMN2, bpmn2Engines);
	}

	public static synchronized WorkflowBroker getInstance() {
		if (instance == null) {
			instance = new WorkflowBroker();
		}
		return instance;
	}

	public void receiveWFCall(ServiceCallData wm) throws WFException {

		// TODO: meaning of the compute()-method in JiraConnector
//		TODO register started Workflows and react to messages to them

		if (!wm.isValid()) {
			myLogger.error("Workflow NOT deployed >> Msg is not valid ");
			return;
		}
		myLogger.info("Creating new Instance of the '" + wm.getWorkflowID() + "' Workflow");
		WorkflowCall wfRestCall = new WorkflowCall();
		//add id
		wfRestCall.setId(UUID.randomUUID());

		WorkflowProvider provider = WorkflowProvider.getInstance();
		WorkflowEntity wfEntity = provider.getByWFID(wm.getWorkflowID());
		//add type
		wfRestCall.setType(wfEntity.getType().toString());
		//add Workflow as byte array
		wfRestCall.setWf(wfEntity.getgBpmn());
		//add Callback address
			try {
				wfRestCall.setCallbackAddress(WorkflowReceiver.getCallbackAddress());
			} catch (MalformedURLException | UnknownHostException e) {
				throw new WFException("Couldn't set callback address");
			}
		//TODO process Jira specific data in the Jira related classes and convert them to a general entity 
		//add the parameters of the Workflow
		wfRestCall.setWorkflowParameters(computeParameter(wm));
		
		WorkflowSender.getInstance().initWorkflow(wfRestCall);


	}
	
	public List<WFParameter> computeParameter(ServiceCallData data){

		List<WFParameter> parameters = data.getParameters();
		List<WFParameter> filteredParameters = new LinkedList<WFParameter>();

		for(WFParameter parameter : parameters){ 
			myLogger.info(parameter.getKey() + " : " + parameter.getValue());
			String key = parameter.getKey();

			if(parameter.getPayloadClazz().equals(ParameterType.STRING)){

				switch (key) {
				case "quartzCron":
					break;

				case "status":
					data.setState(null);
					break;

				case "assignee":
					break;

				case "eventType":
					break;

				case "date":
					break;
					
				case "issueKey":
					filteredParameters.add(parameter);
					break;
					
				case "reporter":
					break;
					
				case "workflowName":
					break;
					
				default:
					try{
						//if(msg.getWorkflowID().substring(msg.getWorkflowID().lastIndexOf(".") + 1).equals(key.substring(key.lastIndexOf("_") + 1))){
						//	parameter.setKey(key.substring(0, key.lastIndexOf("_")));
						parameter.setKey(key);
						filteredParameters.add(parameter);
						//}
					}catch(Exception e){
						myLogger.error(e.getCause().toString() + " : " + e.getMessage());
					}

				}
			}
		}		
//		TODO this case is not considered and may be needed in the future
//		msg.setParameters(filteredParameters);
//
//		if(!quartzCron.equals("")){		
//			myLogger.info("Scheduling new job");			
//			final ServiceCallData schedulerMsg = msg;
//			Scheduler s = new Scheduler();
//
//			s.schedule(quartzCron, new Runnable() {					
//				public void run() {
//					PubFlowJob.execute(schedulerMsg);
//				}
//			});
//			s.start();
//
//		}

		return filteredParameters;
	}
}
