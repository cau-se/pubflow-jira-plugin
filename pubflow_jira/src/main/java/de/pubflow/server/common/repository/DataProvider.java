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

import de.pubflow.server.common.repository.abstractRepository.BasicProvider;
import de.pubflow.server.common.repository.abstractRepository.adapters.FSStorageAdapter;
import de.pubflow.server.common.repository.abstractRepository.misc.ERepositoryName;
import de.pubflow.server.core.workflow.ServiceCallData;


public class DataProvider extends BasicProvider<ServiceCallData>{

	Logger myLogger;

	{
		myLogger = LoggerFactory.getLogger(DataProvider.class);
	}

	private static DataProvider dp;

	private DataProvider() {
		super(ERepositoryName.DATA, new FSStorageAdapter());

		// Register shutdownhook
		Thread t = new Thread(new ShutdownActions());
		Runtime.getRuntime().addShutdownHook(t);
	}

	public static DataProvider getInstance(){
		if(dp == null){
			dp = new DataProvider();
		}
		return dp;
	}

	public List<ServiceCallData> getAllDataEntries(){
		return super.getAllEntries();			
	}

	public ServiceCallData getDataEntry(long instanceId){
		for(ServiceCallData wm : super.getAllEntries()){			
			if(wm.getInstanceId() == instanceId){
				return wm;
			}
		}
		return null;			
	}

	// ----------------------------------------------------------------------------------------
	// Inner Classes
	// ----------------------------------------------------------------------------------------

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