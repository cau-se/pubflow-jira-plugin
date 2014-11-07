/***************************************************************************
 * Copyright 2012 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
/**
 * @author arl
 *
 */

package de.pubflow.server.common.repository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.entity.WorkflowEntity;
import de.pubflow.server.common.repository.abstractRepository.BasicProvider;
import de.pubflow.server.common.repository.abstractRepository.adapters.FSStorageAdapter;
import de.pubflow.server.common.repository.abstractRepository.misc.ERepositoryName;


public class WorkflowProvider extends BasicProvider<WorkflowEntity>{

	Logger myLogger;

	{
		myLogger = LoggerFactory.getLogger(WorkflowProvider.class);
	}

	private static WorkflowProvider wfp;

	private WorkflowProvider() {
		super(ERepositoryName.WORKFLOW, new FSStorageAdapter());

		// Register shutdownhook
		Thread t = new Thread(new ShutdownActions());
		Runtime.getRuntime().addShutdownHook(t);
	}

	public static WorkflowProvider getInstance(){
		if(wfp == null){
			wfp = new WorkflowProvider();
		}
		return wfp;
	}

	@Override
	public long addEntry(WorkflowEntity o) {
		myLogger.info("Registering WF >>"+o.getPubFlowWFID());
		long intWfRef = super.br.add(o);
		myLogger.info(" WF Mapping added: "+o.getPubFlowWFID()+" >> "+intWfRef);
		return intWfRef;
	}

	public WorkflowEntity getByWFID(long pID)
	{
		for(WorkflowEntity we : super.getAllEntries()){
			if(we.getPubFlowWFID() == pID){
				return we;
			}
		}
		return null;
	}

	public long getIDByWFName(String pName)
	{
		List<WorkflowEntity> entries = getAllEntries();
		for (WorkflowEntity workflowEntity : entries) {
			if(workflowEntity.getWFID().equalsIgnoreCase(pName))
			{
				return workflowEntity.getPubFlowWFID();
			}
		}

		return 0;
	}


	// ----------------------------------------------------------------------------------------
	// Inner Classes
	// ----------------------------------------------------------------------------------------

	/**
	 * 
	 * @author pcb
	 * 
	 */
	class ShutdownActions implements Runnable {


		// Register all shutdown actions here
		public void run() {
			Thread.currentThread().setName("WFRepo Shutdownhook");
			Logger shutdownLogger = LoggerFactory.getLogger(this.getClass());
			shutdownLogger.info("<< Shutting down Repo >>");
			shutdownLogger.info("<< BYE >>");
		}

	}
}