package de.pubflow.repository.workflowRepository;

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
