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
/**
 * @author arl
 *
 */

package de.pubflow.server.common.repository;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.repository.abstractRepository.BasicProvider;
import de.pubflow.server.common.repository.abstractRepository.adapters.FSStorageAdapter;
import de.pubflow.server.common.repository.abstractRepository.misc.ERepositoryName;
import de.pubflow.server.core.workflow.ServiceCallData;

public class ScheduledWorkflowProvider extends BasicProvider<ServiceCallData> {

	Logger myLogger;

	{
		myLogger = LoggerFactory.getLogger(ScheduledWorkflowProvider.class);
	}

	private static ScheduledWorkflowProvider swfp;

	private ScheduledWorkflowProvider() {
		super(ERepositoryName.SCHEDULEDWF, new FSStorageAdapter());

		// Register shutdownhook
		Thread t = new Thread(new ShutdownActions());
		Runtime.getRuntime().addShutdownHook(t);
	}

	public static ScheduledWorkflowProvider getInstance() {
		if (swfp == null) {
			swfp = new ScheduledWorkflowProvider();
		}
		return swfp;
	}

	public List<ServiceCallData> getAllScheduledWorkflows() {
		return super.getAllEntries();
	}

	public ServiceCallData getScheduledWorkflow(UUID instanceId) {
		for (ServiceCallData wm : super.getAllEntries()) {
			if (wm.getWorkflowInstanceId() == instanceId) {
				return wm;
			}
		}
		return null;
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