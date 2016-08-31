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
package de.pubflow.server.common.repository;

public class WorkflowLocationInformation {
	
	private String bpmnFile;
	private String workingDir;
	private String baseDir;
	private String deploymentName;
	
	public WorkflowLocationInformation(String bpmnFile, String workingDir, String baseDir, String deploymentName){
		this.bpmnFile = bpmnFile;
		this.workingDir = workingDir;
		this.baseDir = baseDir;
		this.deploymentName = deploymentName;
	}

	public String getBpmnFile() {
		return bpmnFile;
	}

	public void setBpmnFile(String bpmnFile) {
		this.bpmnFile = bpmnFile;
	}

	public String getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getDeploymentName() {
		return deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}
	
}
