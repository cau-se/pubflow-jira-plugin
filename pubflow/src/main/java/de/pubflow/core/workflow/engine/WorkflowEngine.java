package de.pubflow.core.workflow.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.PubFlowSystem;
import de.pubflow.common.entity.workflow.PubFlow;
import de.pubflow.common.entity.workflow.WFParamList;
import de.pubflow.common.entity.workflow.WFParameter;
import de.pubflow.common.enumerartion.WFType;
import de.pubflow.common.exception.WFException;
import de.pubflow.core.communication.message.MessageToolbox;
import de.pubflow.core.communication.message.email.Email;

public abstract class WorkflowEngine implements Runnable{
	
	private static Logger myLogger;
	static
	{
		myLogger = LoggerFactory.getLogger(WorkflowEngine.class);
	}
	
	/**
	 * Method to deploy a new Publication Workflow in a Workflow Engine
	 * 
	 * @param wf (PubFlow) : The workflow to deploy
	 * @throws WFException
	 */
	public abstract void deployWF(PubFlow wf) throws WFException;
	
	/**
	 * Starts the pubflow with the given ID
	 * @param wfID (long) : the ID of the workflow to start
	 * @param params (WFParameter ...) : The list of the parameters needed by the Workflow
	 * @throws WFException
	 */
	public abstract void setParams(WFParamList params) throws WFException;
	
	/**
	 * Method to undeploy a deployed pubflow
	 * 
	 * @param wfID (long) : the ID of the workflow
	 * @throws WFException
	 */
	public abstract void undeployWF(long wfID) throws WFException;
	
	/**
	 * Method to stop a running Pubflow
	 * 
	 * @param wfID (long) : the ID of the PubFlow
	 * @throws WFException
	 */
	public abstract void stopWF(long wfID) throws WFException;
	
	public abstract List<WFType> getCompatibleWFTypes();
	
	protected void sendEmailMsg(Email pMsg) {
		myLogger.info("Sending Msg ...");
		ProducerTemplate producer;
		CamelContext context = PubFlowSystem.getInstance().getContext();
		producer = context.createProducerTemplate();
		producer.sendBody("mail-jms:mailqueue:in.queue",MessageToolbox.transformToString(pMsg));
		myLogger.info("Msg sent!");
	}

}
