package de.pubflow.core.workflow.engine;

import java.util.ArrayList;
import java.util.List;

import de.pubflow.common.entity.workflow.PubFlow;
import de.pubflow.common.entity.workflow.WFParamList;
import de.pubflow.common.entity.workflow.WFParameter;
import de.pubflow.common.enumerartion.WFType;
import de.pubflow.common.exception.WFException;

public interface IWorkflowEngine {
	
	/**
	 * Method to deploy a new Publication Workflow in a Workflow Engine
	 * 
	 * @param wf (PubFlow) : The workflow to deploy
	 * @return (long) : the ID assigned to the workflow
	 * @throws WFException
	 */
	long deployWF(PubFlow wf) throws WFException;
	
	/**
	 * Starts the pubflow with the given ID
	 * @param wfID (long) : the ID of the workflow to start
	 * @param params (WFParameter ...) : The list of the parameters needed by the Workflow
	 * @throws WFException
	 */
	void startWF(long wfID, WFParamList params) throws WFException;
	
	/**
	 * Method to undeploy a deployed pubflow
	 * 
	 * @param wfID (long) : the ID of the workflow
	 * @throws WFException
	 */
	void undeployWF(long wfID) throws WFException;
	
	/**
	 * Method to stop a running Pubflow
	 * 
	 * @param wfID (long) : the ID of the PubFlow
	 * @throws WFException
	 */
	void stopWF(long wfID) throws WFException;
	
	List<WFType> getCompatibleWFTypes();

}
