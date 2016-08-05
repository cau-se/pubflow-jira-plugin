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
package de.pubflow.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.common.PropLoader;
import de.pubflow.server.core.jira.JiraConnector;

public class PubFlowSystem {

	private static PubFlowSystem instance = null;
	private Logger myLogger;

	private PubFlowSystem() {
		myLogger = LoggerFactory.getLogger(this.getClass());
		myLogger.info("Starting PubFlow System");

		try {
			JiraConnector.getInstance().start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Register shutdownhook
		Thread t = new Thread(new ShutdownActions());
		Runtime.getRuntime().addShutdownHook(t);
		myLogger.info("Running");
	}

	public static synchronized PubFlowSystem getInstance(){

		if (instance == null) {
			instance = new PubFlowSystem();
		}
		return instance;
	}


	class ShutdownActions implements Runnable {
		// Register all shutdown actions here
		public void run() {
			Thread.currentThread().setName("PubFlow Shutdownhook");
			Logger shutdownLogger = LoggerFactory.getLogger(this.getClass());
			shutdownLogger.info("<< Shutting down PubFlow >>");
			PubFlowSystem core = PubFlowSystem.getInstance();
			shutdownLogger.info("Stopping Quartz");
			//TODO
			// Write props to file
			shutdownLogger.debug("Saving Properties to File");
			PropLoader.getInstance().saveProperties();
			shutdownLogger.info("<< BYE >>");
		}
	}
}
