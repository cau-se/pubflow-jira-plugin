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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.entity.WorkflowEntity;
import de.pubflow.server.common.repository.abstractRepository.BasicProvider;
import de.pubflow.server.common.repository.abstractRepository.adapters.FSStorageAdapter;
import de.pubflow.server.common.repository.abstractRepository.misc.ERepositoryName;

public class WorkflowProvider extends BasicProvider<WorkflowEntity> {

	Logger myLogger;

	{
		myLogger = LoggerFactory.getLogger(WorkflowProvider.class);
	}

	private static WorkflowProvider wfp;

	private WorkflowProvider() {
		super(ERepositoryName.WORKFLOW, new FSStorageAdapter());
	}

	public static WorkflowProvider getInstance() {
		if (wfp == null) {
			wfp = new WorkflowProvider();
		}
		return wfp;
	}

	@Override
	public long addEntry(WorkflowEntity o) {
		myLogger.info("Registering WF >>" + o.getWorkflowId());
		long intWfRef = super.br.add(o);
		myLogger.info(" WF Mapping added: " + o.getWorkflowId() + " >> " + intWfRef);
		return intWfRef;
	}

	public WorkflowEntity getByWFID(String id) {
		for (WorkflowEntity we : super.getAllEntries()) {
			if (we.getWorkflowId().equals(id)) {
				return we;
			}
		}
		return null;
	}

}