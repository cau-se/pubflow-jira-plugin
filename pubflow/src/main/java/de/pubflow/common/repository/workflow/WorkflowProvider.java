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

package de.pubflow.common.repository.workflow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.common.entity.repository.WorkflowEntity;
import de.pubflow.common.repository.abstractRepository.interaction.BasicProvider;
import de.pubflow.common.repository.abstractRepository.repository.ERepositoryName;
import de.pubflow.common.repository.abstractRepository.storageAdapter.FSStorageAdapter;


public class WorkflowProvider extends BasicProvider<WorkflowEntity>{

	volatile Properties workflowMap;
	private static final String CONF_FILE = "etc/workflow.list";
	Logger myLogger;
	
	{
		myLogger = LoggerFactory.getLogger(WorkflowProvider.class);
	}
	
	private static WorkflowProvider wfp;

	private WorkflowProvider() {
		super(ERepositoryName.WORKFLOW, new FSStorageAdapter());
		workflowMap = new Properties();
		
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(CONF_FILE);
		} catch (Exception e) {
			myLogger.error("Could not find Properties File");

			// e.printStackTrace();
		}

		try {
			workflowMap.loadFromXML(fi);
		} catch (Exception e) {
			myLogger.error("Could not load Properties File");
			e.printStackTrace();
		}
		workflowMap.list(System.out);
		
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
	public long setEntry(WorkflowEntity o) {
		myLogger.info("Registering WF >>"+o.getWFID());
		long intWfRef = super.br.add(o);
		myLogger.info(" WF Mapping added: "+o.getWFID()+" >> "+intWfRef);
		workflowMap.put(o.getWFID()+"",intWfRef+"");
		return intWfRef;
	}
	
	public WorkflowEntity getByWFID(long pID)
	{
		String temp = workflowMap.getProperty(pID+"");
		long internalID = Long.parseLong(temp);
		return super.getEntry(internalID);
	}
	
	public void saveProps()
	{
		myLogger.info("Persisting Properties");
		FileOutputStream fs = null;
		try {
			fs = new FileOutputStream(CONF_FILE);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			workflowMap.storeToXML(fs, "Workflow list (last updated "
					+ Calendar.getInstance().getTime().toLocaleString() + ")");
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			WorkflowProvider.getInstance().saveProps();
			shutdownLogger.info("<< BYE >>");
		}

	}
}