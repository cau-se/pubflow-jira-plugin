/**
 * Copyright (C) 2016 Marc Adolf, Arnd Plumhoff (http://www.pubflow.uni-kiel.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.pubflow.server.core.workflow;

import java.util.List;

import de.pubflow.common.exceptions.WFException;
import de.pubflow.server.common.entity.workflow.PubFlow;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.enumeration.WFType;

public abstract class WorkflowEngine implements Runnable{
	
	
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
	public abstract void setParams(List<WFParameter> params) throws WFException;
	
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
}
