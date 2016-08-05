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
package de.pubflow.server.core.jira;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiraConnector {

	private static Logger myLogger = LoggerFactory.getLogger(JiraConnector.class.getSimpleName());
	private static JiraConnector jiraConnector;

	static {
		myLogger = LoggerFactory.getLogger(JiraConnector.class);
	}

	public JiraConnector(){
	}

	public static JiraConnector getInstance(){
		if (jiraConnector == null) {
			jiraConnector = new JiraConnector();
		}
		return jiraConnector;			
	}

	public void start() throws Exception {		
		//Scheduler.getInstance().start();
	}

	public void stop(){
		//Scheduler.getInstance().shutdown();
	}

	
}
