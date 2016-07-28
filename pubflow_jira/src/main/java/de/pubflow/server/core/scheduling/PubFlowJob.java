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
package de.pubflow.server.core.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.core.workflow.WorkflowBroker;
import de.pubflow.server.common.exceptions.WFException;
import de.pubflow.server.core.workflow.ServiceCallData;

public class PubFlowJob {
	private static Logger myLogger;

	static {
		myLogger = LoggerFactory.getLogger(PubFlowJob.class);
	}

	public static void execute(ServiceCallData data) {
		myLogger.info("Starting scheduled job");
		try {
			WorkflowBroker.getInstance().receiveWFCall(data);
		} catch (WFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
